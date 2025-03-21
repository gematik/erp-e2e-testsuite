/*
 * Copyright 2025 gematik GmbH
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

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.test.erezept.client.usecases.TaskActivateCommand;
import de.gematik.test.erezept.client.usecases.TaskCreateCommand;
import de.gematik.test.erezept.fhir.builder.kbv.KbvEvdgaBundleBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvHealthAppRequestFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.kbv.KbvEvdgaBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.fhirdump.FhirDumper;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.util.DataMatrixCodeGenerator;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.nio.file.Path;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
public class IssueDiGAPrescription implements Task {

  private final Actor patient;

  private IssueDiGAPrescription(Actor patient) {
    this.patient = patient;
  }

  public static IssueDiGAPrescription forPatient(Actor patient) {
    return new IssueDiGAPrescription(patient);
  }

  private KbvPatient getPatientBaseData() {
    val baseData = SafeAbility.getAbility(patient, ProvidePatientBaseData.class);
    log.info(
        "Issue ePrescription to patient {} with insurance type {}",
        baseData.getFullName(),
        baseData.getPatientInsuranceType());
    return baseData.getPatient();
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
    val insurance =
        SafeAbility.getAbility(patient, ProvidePatientBaseData.class).getInsuranceCoverage();
    val kbvPatient = getPatientBaseData();

    val flowType = PrescriptionFlowType.FLOW_TYPE_162;
    val createCmd = new TaskCreateCommand(flowType);
    val createResponse = erpClientAbility.request(createCmd);
    val draftTask = createResponse.getExpectedResource();
    val prescriptionId = draftTask.getPrescriptionId();

    val appRequest =
        KbvHealthAppRequestFaker.forPatient(kbvPatient)
            .withInsurance(insurance)
            .withRequester(kbvPractitioner)
            .fake();

    val kbvEvdgaBundle =
        KbvEvdgaBundleBuilder.forPrescription(prescriptionId)
            .practitioner(kbvPractitioner)
            .medicalOrganization(kbvOrganization)
            .patient(kbvPatient)
            .insurance(insurance)
            .healthAppRequest(appRequest)
            .build();

    val activeTask =
        this.activateTask(erpClientAbility, konnektorAbility, draftTask, kbvEvdgaBundle);

    // store the issued prescription
    managePrescriptions.append(activeTask);

    // handover DMC if a concrete actor is available
    this.handoverDmc(activeTask);
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
      KbvEvdgaBundle kbvEvdgaBundle) {
    // create a draft Task
    val taskId = draftTask.getTaskId();
    val accessCode = draftTask.getOptionalAccessCode().orElseThrow();

    val kbvXml = clientAbility.encode(kbvEvdgaBundle, EncodingType.XML);
    val signedKbv = konnektorAbility.signDocumentWithHba(kbvXml).getPayload();

    // activate the task
    val activate = new TaskActivateCommand(taskId, accessCode, signedKbv);
    return clientAbility.request(activate).getExpectedResource();
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
    // if we have the not only the KVRN but also the actor, give him also a DMC Prescription
    log.info("Doctor hands over DMC for {} to {}", dmc.getTaskId(), patient.getName());
    SafeAbility.getAbility(patient, ManageDataMatrixCodes.class).appendDmc(dmc);
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
}
