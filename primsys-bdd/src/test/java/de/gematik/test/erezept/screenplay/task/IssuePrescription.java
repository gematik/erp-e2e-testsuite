/*
 * Copyright 2024 gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.screenplay.task;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.exceptions.UnexpectedResponseResourceError;
import de.gematik.test.erezept.client.usecases.TaskActivateCommand;
import de.gematik.test.erezept.client.usecases.TaskCreateCommand;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhirdump.FhirDumper;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.ManageDoctorsPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ProvideDoctorBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.erezept.screenplay.strategy.prescription.*;
import de.gematik.test.erezept.screenplay.util.DataMatrixCodeGenerator;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
public class IssuePrescription implements Task {

  private final Actor pharmacy;
  private final PrescriptionDataMapper prescriptionDataMapper;

  private final KVNR patientKvnr;

  private IssuePrescription(
      @Nullable KVNR kvnr,
      @Nullable Actor pharmacy,
      PrescriptionDataMapper prescriptionDataMapper) {
    this.patientKvnr = kvnr;
    this.pharmacy = pharmacy;
    this.prescriptionDataMapper = prescriptionDataMapper;
  }

  public static IssuePrescriptionBuilder forKvnr(String kvnr) {
    return new IssuePrescriptionBuilder(KVNR.from(kvnr));
  }

  public static IssuePrescriptionBuilder forPatient(Actor patient) {
    return new IssuePrescriptionBuilder(patient);
  }

  @Override
  @Step("{0} issues ePrescription with PZN #pzn to #patientKvnr")
  public <T extends Actor> void performAs(final T actor) {
    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val konnektorAbility = SafeAbility.getAbility(actor, UseTheKonnektor.class);
    val baseDataAbility = SafeAbility.getAbility(actor, ProvideDoctorBaseData.class);
    val managePrescriptions = SafeAbility.getAbility(actor, ManageDoctorsPrescriptions.class);

    // Practitioner base data (Stammdaten)
    val kbvPractitioner = baseDataAbility.getPractitioner();
    val kbvOrganization = baseDataAbility.getMedicalOrganization();

    val type = prescriptionDataMapper.getType();
    prescriptionDataMapper
        .createKbvBundles(kbvPractitioner, kbvOrganization)
        .forEach(
            builderFlowtypePair -> {
              val builder = builderFlowtypePair.getLeft();
              val flowType = builderFlowtypePair.getRight();

              log.info(
                  format(
                      "Create Task for {0} with WorkflowType {1} ({2})",
                      type, flowType.getCode(), flowType.getDisplay()));

              val createCmd = new TaskCreateCommand(flowType);
              val createResponse = erpClientAbility.request(createCmd);
              val draftTask = createResponse.getExpectedResource();
              val prescriptionId = draftTask.getPrescriptionId();

              val kbvBundle = builder.prescriptionId(prescriptionId).build();

              val activeTask =
                  this.activateTask(erpClientAbility, konnektorAbility, draftTask, kbvBundle);

              // store the issued prescription
              managePrescriptions.append(activeTask);

              // handover DMC if a concrete actor is available
              this.handoverDmc(activeTask);
            });
  }

  /**
   * Create a draft task and activate with the values from given Map
   *
   * @param clientAbility is the ability to communication with the eRP-FD
   * @param konnektorAbility is the ability to sign the KBV Bundle
   * @return an activated Task
   */
  private ErxTask activateTask(
      UseTheErpClient clientAbility,
      UseTheKonnektor konnektorAbility,
      ErxTask draftTask,
      KbvErpBundle kbvBundle) {
    // create a draft Task
    val taskId = draftTask.getTaskId();
    val accessCode = draftTask.getOptionalAccessCode().orElseThrow();

    val kbvXml = clientAbility.encode(kbvBundle, EncodingType.XML);
    val signedKbv = konnektorAbility.signDocumentWithHba(kbvXml).getPayload();

    // activate the task
    val activate = new TaskActivateCommand(taskId, accessCode, signedKbv);
    val activateResponse = clientAbility.request(activate);
    return activateResponse
        .getResourceOptional()
        .orElseThrow(
            () ->
                new AssertionError(
                    new UnexpectedResponseResourceError(
                        activate.expectedResponseBody(), activateResponse.getAsBaseResource())));
  }

  /**
   * If this Task was created with a concrete Patient and not only with a KVNR, the activated Task
   * will be transformed to a DmcPrescription (Data Matrix Code) and handed over to this patient
   *
   * <p>If the Task was create with PrescriptionAssignmentKind.DIRECT_ASSIGNMENT and a pharmacy was
   * given handover the DMC to the pharmacy (Direktzuweisung)
   *
   * @param activatedTask is the Task which represents the E-Rezept
   */
  private void handoverDmc(ErxTask activatedTask) {
    val dmc = DmcPrescription.ownerDmc(activatedTask.getTaskId(), activatedTask.getAccessCode());
    writeDmcToReport(dmc);
    val patient = this.prescriptionDataMapper.getPatient();
    // if we have the not only the KVRN but also the actor, give him also a DMC Prescription
    if (patient != null) {
      log.info(format("Doctor hands over DMC for {0} to {1}", dmc.getTaskId(), patient.getName()));
      SafeAbility.getAbility(patient, ManageDataMatrixCodes.class).appendDmc(dmc);
    } else {
      log.info(format("No concrete Actor was given to handover the DMC for {0}", dmc.getTaskId()));
    }
    val type = this.prescriptionDataMapper.getType();
    if (pharmacy != null) {
      if (type.equals(PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)) {
        SafeAbility.getAbility(pharmacy, ManagePharmacyPrescriptions.class)
            .appendAssignedPrescription(dmc);
      } else {
        log.error(
            format(
                "The pharmacy {0} was given for direct assignment but assignment is of type {1}",
                pharmacy.getName(), type));
      }
    }
  }

  @SneakyThrows
  private void writeDmcToReport(DmcPrescription dmc) {
    // write the DMC to file and append to the Serenity Report
    val dmcPath =
        Path.of("target", "site", "serenity", "dmcs", format("dmc_{0}.png", dmc.getTaskId()));
    val bitMatrix =
        DataMatrixCodeGenerator.generateDmc(dmc.getTaskId().getValue(), dmc.getAccessCode());
    DataMatrixCodeGenerator.writeToFile(bitMatrix, dmcPath.toFile());

    Serenity.recordReportData()
        .withTitle("Data Matrix Code for " + dmc.getTaskId())
        .downloadable()
        .fromFile(dmcPath);

    FhirDumper.getInstance()
        .writeDump(
            format(
                "DMC for {0} with AccessCode {1}", dmc.getTaskId(), dmc.getAccessCode().getValue()),
            format("dmc_{0}.png", dmc.getTaskId()),
            file -> DataMatrixCodeGenerator.writeToFile(bitMatrix, file));
  }

  public static class IssuePrescriptionBuilder {
    private KVNR kvnr;
    private Actor patient;
    private Actor pharmacy;
    private PrescriptionAssignmentKind type =
        PrescriptionAssignmentKind.PHARMACY_ONLY; // as default

    private IssuePrescriptionBuilder(KVNR kvnr) {
      this.kvnr = kvnr;
    }

    private IssuePrescriptionBuilder(Actor patient) {
      this.patient = patient;
    }

    public IssuePrescriptionBuilder as(PrescriptionAssignmentKind type) {
      this.type = type;
      return this;
    }

    public IssuePrescriptionBuilder to(Actor pharmacy) {
      this.pharmacy = pharmacy;
      return this;
    }

    public IssuePrescription forPznPrescription(List<Map<String, String>> medications) {

      val mapper = new PrescriptionDataMapperPZN(patient, type, medications);
      return new IssuePrescription(kvnr, pharmacy, mapper);
    }

    public IssuePrescription forFreitextVerordnung(List<Map<String, String>> freitextVerordnungen) {
      val mapper = new PrescriptionDataMapperFreitext(patient, type, freitextVerordnungen);
      return new IssuePrescription(kvnr, pharmacy, mapper);
    }

    public IssuePrescription forRezepturVerordnung(List<Map<String, String>> rezepturVerordnungen) {
      val mapper = new PrescriptionDataMapperCompounding(patient, type, rezepturVerordnungen);
      return new IssuePrescription(kvnr, pharmacy, mapper);
    }

    public IssuePrescription forWirkstoffVerordnung(
        List<Map<String, String>> wirkstoffVerordnungen) {
      val mapper = new PrescriptionDataMapperIngredient(patient, type, wirkstoffVerordnungen);
      return new IssuePrescription(kvnr, pharmacy, mapper);
    }

    public IssuePrescription randomPrescription() {
      // concrete content of does not matter; we just simply need a single entry to issue a single
      // random prescription
      val medications = List.of(Map.of("prescription", "1"));
      return forPznPrescription(medications);
    }
  }
}
