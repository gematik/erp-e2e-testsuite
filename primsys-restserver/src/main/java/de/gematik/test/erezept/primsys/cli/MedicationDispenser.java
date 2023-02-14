/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.primsys.cli;

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.client.exceptions.UnexpectedResponseResourceError;
import de.gematik.test.erezept.client.usecases.DispenseMedicationCommand;
import de.gematik.test.erezept.client.usecases.TaskAcceptCommand;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationBuilder;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import de.gematik.test.erezept.lei.cfg.TestsuiteConfiguration;
import de.gematik.test.erezept.primsys.model.ActorContext;
import de.gematik.test.erezept.primsys.model.actor.Pharmacy;
import de.gematik.test.erezept.primsys.rest.data.AcceptData;
import de.gematik.test.erezept.primsys.rest.data.DispensedData;
import de.gematik.test.erezept.primsys.rest.data.MedicationData;
import de.gematik.test.erezept.primsys.rest.data.PrescriptionData;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

@Slf4j
public class MedicationDispenser implements Callable<Integer> {

  @CommandLine.Option(
      names = "--conf",
      paramLabel = "CONFIG",
      type = Path.class,
      required = true,
      description = "Path to a Configuration File")
  private Path config;

  @CommandLine.Option(
      names = "--input",
      type = Path.class,
      required = true,
      description = "Path of summary log file from which contains prescription information")
  private Path input;

  @CommandLine.Option(
      names = "--output",
      type = Path.class,
      description = "Directory to store the output-file (default=current working directory)")
  private Path outdir = Path.of(System.getProperty("user.dir"));

  @CommandLine.ArgGroup(exclusive = false)
  private DispenseParameters dispenseParameters = new DispenseParameters();

  private static class DispenseParameters {
    @CommandLine.Option(
        names = "--substitute",
        type = Boolean.class,
        description =
            "Decide if the originally prescribed medication shall be substituted (default=false)")
    private boolean substitute = false;

    @CommandLine.Option(
        names = "--subnum",
        type = Integer.class,
        description =
            "If --substitute is activated, how many alternative medications shall be dispensed")
    private int substituteMedications = 3;
  }

  @Override
  public Integer call() throws Exception {
    int returnCode = 0;

    val mapper =
        new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    val prescriptionData =
        mapper.readValue(input.toFile(), new TypeReference<List<PrescriptionData>>() {});

    val configFile = config.toFile();
    log.info("Initialize Erp-Client with Config from " + configFile.getAbsolutePath());
    TestsuiteConfiguration.getInstance(configFile);

    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(0);

    try {
      prescriptionData.forEach(
          pd -> {
            val taskId = pd.getTaskId();
            val accessCode = pd.getAccessCode();
            val acceptData = acceptPrescription(pharmacy, taskId, accessCode);
            if (!dispenseParameters.substitute) {
              dispenseOriginalPrescriptions(pharmacy, pd, acceptData);
            } else {
              dispenseReplacements(pharmacy, pd, acceptData);
            }
          });
    } catch (Exception | UnexpectedResponseResourceError e) {
      if (e.getClass().equals(UnexpectedResponseResourceError.class)) {
        log.error("Received an unexpected Response from FD", e);
      } else {
        log.error("Something went wrong!", e);
      }
      // make sure accepted Tasks are written to file!
      writeAcceptedSummary("accepted", ctx.getAcceptedPrescriptions());
      throw e; // rethrow to stop execution
    }

    writeAcceptedSummary("accepted", ctx.getAcceptedPrescriptions());
    writeAcceptedSummary("dispensed", ctx.getDispensedMedications());
    return returnCode;
  }

  private void dispenseOriginalPrescriptions(
      Pharmacy pharmacy, PrescriptionData prescriptionData, AcceptData acceptData) {
    val medicationData = prescriptionData.getMedication();
    val patientData = prescriptionData.getPatient();

    log.info(
        format(
            "Prepare the originally prescribed Medication {0} with PZN {1} to {2}",
            medicationData.getName(), medicationData.getPzn(), patientData.getKvnr()));

    val supplyForm =
        medicationData.getEnumDarreichungsForm() != null
            ? medicationData.getEnumDarreichungsForm()
            : GemFaker.fakerValueSet(Darreichungsform.class);

    val category =
        medicationData.getEnumCategory() != null
            ? medicationData.getEnumCategory()
            : GemFaker.fakerValueSet(MedicationCategory.class);

    val size =
        medicationData.getEnumStandardSize() != null
            ? medicationData.getEnumStandardSize()
            : GemFaker.fakerValueSet(StandardSize.class);

    val medication =
        KbvErpMedicationBuilder.builder()
            .pzn(medicationData.getPzn(), medicationData.getName())
            .normgroesse(size)
            .category(category)
            .amount(medicationData.getAmount())
            .darreichungsform(supplyForm)
            .build();

    val medicationDispense =
        ErxMedicationDispenseBuilder.forKvid(patientData.getKvnr())
            .medication(medication)
            .prescriptionId(prescriptionData.getPrescriptionId())
            .performerId(pharmacy.getSmcb().getTelematikId())
            .batch(GemFaker.fakerLotNumber(), GemFaker.fakerFutureExpirationDate())
            .build();

    dispense(pharmacy, acceptData, List.of(medicationDispense));
  }

  private void dispenseReplacements(
      Pharmacy pharmacy, PrescriptionData prescriptionData, AcceptData acceptData) {
    val patientData = prescriptionData.getPatient();

    log.info(
        format(
            "Prepare {0} replacement Medications to {1}",
            dispenseParameters.substituteMedications, patientData.getKvnr()));

    val medicationDispenses = new ArrayList<ErxMedicationDispense>();
    IntStream.range(0, dispenseParameters.substituteMedications)
        .forEach(
            idx -> {
              val medication = KbvErpMedicationBuilder.faker().build();
              val medicationDispense =
                  ErxMedicationDispenseBuilder.forKvid(patientData.getKvnr())
                      .medication(medication)
                      .prescriptionId(prescriptionData.getPrescriptionId())
                      .performerId(pharmacy.getSmcb().getTelematikId())
                      .batch(GemFaker.fakerLotNumber(), GemFaker.fakerFutureExpirationDate())
                      .build();
              medicationDispenses.add(medicationDispense);
              log.info(
                  format(
                      "Prepare {0} with PZN {1}",
                      medication.getMedicationName(), medication.getPzn().get(0)));
            });

    dispense(pharmacy, acceptData, medicationDispenses);
  }

  private AcceptData acceptPrescription(Pharmacy actor, String taskId, String accessCode) {
    val acceptCommand = new TaskAcceptCommand(taskId, new AccessCode(accessCode));
    val acceptResponse = actor.erpRequest(acceptCommand);
    val acceptedTaskOpt = acceptResponse.getResourceOptional(acceptCommand.expectedResponseBody());

    val atomicAcceptedTask = new AtomicReference<ErxTask>();
    acceptedTaskOpt.ifPresentOrElse(
        t -> atomicAcceptedTask.set(t.getTask()),
        () -> atomicAcceptedTask.set(readTaskAfterFailedAccept(actor, taskId, accessCode)));

    val task = atomicAcceptedTask.get();
    val acceptData = new AcceptData();
    acceptData.setTaskId(task.getUnqualifiedId());
    acceptData.setAccessCode(task.getAccessCode().getValue());
    acceptData.setSecret(task.getSecret().orElseThrow().getValue());
    ActorContext.getInstance().addAcceptedPrescription(acceptData);

    return acceptData;
  }

  /**
   * In case the pharmacy is not allowed to accept the task: maybe we've already accepted? Just try
   * to read the task
   *
   * @param actor dispensing pharmacy
   * @param taskId of the Prescription
   * @param accessCode to the Prescription
   * @return ErxTask if possible and throw Exception otherwise
   */
  private ErxTask readTaskAfterFailedAccept(Pharmacy actor, String taskId, String accessCode) {
    log.info(format("Could not accept Task {0}, try to read", taskId));
    val cmd = new TaskGetByIdCommand(taskId, new AccessCode(accessCode));
    val response = actor.erpRequest(cmd);
    val bundle = response.getResource(cmd.expectedResponseBody());
    // make sure the Task has a Secret, which would indicate this is an already accepted task
    log.info(format("Check if the Task has a secret: {0}", bundle.getTask().hasSecret()));
    assert bundle.getTask().hasSecret();
    return bundle.getTask();
  }

  private void dispense(
      Pharmacy pharmacy, AcceptData acceptData, List<ErxMedicationDispense> medicationDispenses) {
    log.info(
        format(
            "Dispense Prescription with TaskID {0} and Secret {1}",
            acceptData.getTaskId(), acceptData.getSecret()));
    val secret = new Secret(acceptData.getSecret());
    val cmd = new DispenseMedicationCommand(acceptData.getTaskId(), secret, medicationDispenses);
    val response = pharmacy.erpRequest(cmd);
    val receipt = response.getResource(cmd.expectedResponseBody());

    val dd = new DispensedData();
    dd.setAcceptData(acceptData);
    dd.setDispensedDate(new Date());
    dd.setMedications(
        medicationDispenses.stream()
            .map(md -> MedicationData.fromMedication(md.getErpMedicationFirstRep()))
            .collect(Collectors.toList()));

    val ctx = ActorContext.getInstance();
    ctx.addDispensedMedications(dd);
    ctx.getAcceptedPrescriptions().remove(acceptData);
  }

  @SneakyThrows
  private void writeAcceptedSummary(String fileNamePrefix, List<?> data) {
    if (data.size() > 0) { // avoid writing empty lists
      val mapper = new ObjectMapper();
      val timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
      val fileName = format("{0}_{1}.json", fileNamePrefix, timestamp);
      val out = Path.of(outdir.toAbsolutePath().toString(), fileName).toFile();
      mapper.writerWithDefaultPrettyPrinter().writeValue(out, data);
    }
  }
}
