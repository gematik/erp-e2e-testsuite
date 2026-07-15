/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.eml.integration;

import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvideDispensationVerifier.medicationInProvDispensationContainsAtcCodingWithVersion;
import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvideDispensationVerifier.provDispensationContainsDosageInstruction;
import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvidePrescriptionVerifier.emlMedRequestDosageHasText;

import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.eml.tasks.CheckEpaOpProvideDispensation;
import de.gematik.test.eml.tasks.CheckEpaOpProvidePrescriptionWithTask;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.GemaTestActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SearchPrefix;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseFaker;
import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationFaker;
import de.gematik.test.erezept.fhir.builder.erp.GemOperationInputParameterBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationCompoundingFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Map DosageInstruction In Deprecated Version")
@Tag("Map_DosageInstruction")
@Tag("EpaEml")
class MapDosageInstructionTemporaryIT extends ErpTest {
  private static final KbvItaErpVersion itaErpVersion = KbvItaErpVersion.V1_3_0;
  private static final KbvItaForVersion itaForVersion = KbvItaForVersion.V1_2_0;
  private static final ErpWorkflowVersion erpWfVersion = ErpWorkflowVersion.V1_5;

  @Actor(name = "Günther Angermänn")
  private PatientActor patient;

  @Actor(name = "Gündüla Gunther")
  private DoctorActor doc;

  @Actor(name = "Am Waldesrand")
  private PharmacyActor pharmacy;

  private ErxAcceptBundle acceptance;

  private final List<IQueryParameter> searchParams =
      IQueryParameter.search()
          .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.EQ)
          .sortedBy("date", SortOrder.DESCENDING)
          .createParameter();

  @TestcaseId("EML_MAP_DOSAGE_INSTRUCTION_IN_PRESCRIPTION_01")
  @Test()
  @DisplayName(
      "Es muss geprüft werden, dass das übermittelte DosageInstruction Textfeld der"
          + " ProvidePrescription an das Epa Aktensystem den Werten der ausgestellten Prescription"
          + " entspricht")
  void submitDosageInstructionToEpaMock() {
    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    this.config.equipWithEpaMockClient(epaFhirChecker);

    val dosageInstruction = "Dosieranweisung im Textfeld";

    doc.setVersion(itaForVersion);
    patient.setVersion(itaForVersion);

    val medication =
        KbvErpMedicationPZNFaker.builder(itaErpVersion)
            .withVaccine(false)
            .withPznMedication(PZN.from("19201712"), "Pomalidomid Accord 1 mg 21 x 1 Hartkapseln")
            .fake();
    val activation =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder(itaErpVersion, itaForVersion)
                        .withMedication(medication)
                        .withDosageInstruction(dosageInstruction)
                        .toBuilder()));

    // verifies correct activated prescription and get KbvErpBundle for validation step
    val task = activation.getExpectedResponse();

    val prescr =
        patient.performs(
            GetPrescriptionById.withTaskId(task.getTaskId()).withAccessCode(task.getAccessCode()));

    // performs the resource-content validation
    val kbvBundle = prescr.getExpectedResponse().getKbvBundle().orElseThrow();
    epaFhirChecker.attemptsTo(
        CheckEpaOpProvidePrescriptionWithTask.forPrescription(
            kbvBundle,
            doc.getSmcbTelematikId(),
            doc.getHbaTelematikId(),
            emlMedRequestDosageHasText(dosageInstruction)));
  }

  @TestcaseId("EML_MAP_PATIENT_INSTRUCTION_IN_PRESCRIPTION_02")
  @Test()
  @DisplayName(
      "Es muss geprüft werden, dass das übermittelte DosageInstruction PatientInstruction der"
          + " ProvidePrescription an das Epa Aktensystem den Werten der ausgestellten Prescription"
          + " entspricht")
  void submitPatientInstructionInstructionToEpaMock() {
    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    this.config.equipWithEpaMockClient(epaFhirChecker);

    val dosageInstruction = "Dosieranweisung im Textfeld";

    doc.setVersion(itaForVersion);
    patient.setVersion(itaForVersion);

    val medication =
        KbvErpMedicationCompoundingFaker.builder(itaErpVersion).withVaccine(false).fake();
    val activation =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder(itaErpVersion, itaForVersion)
                        .withMedication(medication)
                        .withDosageInstruction(dosageInstruction)
                        .toBuilder()));

    // verifies correct activated prescription and get KbvErpBundle for validation step
    val task = activation.getExpectedResponse();

    val prescr =
        patient.performs(
            GetPrescriptionById.withTaskId(task.getTaskId()).withAccessCode(task.getAccessCode()));

    // performs the resource-content validation
    val kbvBundle = prescr.getExpectedResponse().getKbvBundle().orElseThrow();
    epaFhirChecker.attemptsTo(
        CheckEpaOpProvidePrescriptionWithTask.forPrescription(
            kbvBundle,
            doc.getSmcbTelematikId(),
            doc.getHbaTelematikId(),
            emlMedRequestDosageHasText(dosageInstruction)));
  }

  @TestcaseId("EML_MAP_DOSAGE_AND_PATIENT_INSTRUCTION_IN_DISPENSATION_03")
  @Test()
  @DisplayName(
      "Es muss geprüft werden, dass das übermittelte DosageInstruction Textfeld und"
          + " PatientInstruction der ProvideDispensation an das Epa Aktensystem den Werten der"
          + " ausgestellten Dispensation entspricht")
  void submitDosageAndPatientInstructionByDispensationToEpaMock() {
    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    this.config.equipWithEpaMockClient(epaFhirChecker);

    val dosageInstruction = "Dosieranweisung im Textfeld";
    val patientInstruction = "Dosieranweisung im PatientInstruction-field";

    doc.setVersion(itaForVersion);
    patient.setVersion(itaForVersion);

    val medication =
        KbvErpMedicationCompoundingFaker.builder(itaErpVersion).withVaccine(false).fake();
    val activation =
        doc.performs(
                IssuePrescription.forPatient(patient)
                    .withKbvBundleFrom(
                        KbvErpBundleFaker.builder(itaErpVersion, itaForVersion)
                            .withMedication(medication)
                            .toBuilder()))
            .getExpectedResponse();

    acceptance = pharmacy.performs(AcceptPrescription.forTheTask(activation)).getExpectedResponse();
    val task = acceptance.getTask();

    // todo map original KBV Medication to GemErpMedication
    val gemMedicationCompounding =
        GemErpMedicationFaker.forMedicationCompounding(erpWfVersion).fake();
    gemMedicationCompounding.getCode().getCoding().forEach(c -> c.setVersion("2026"));
    val medDisp =
        ErxMedicationDispenseFaker.builder(erpWfVersion)
            .withKvnr(patient.getKvnr())
            .withPrescriptionId(task.getPrescriptionId())
            .withMedication(gemMedicationCompounding)
            .withPerformer(pharmacy.getTelematikId().getValue())
            .withDosageInstruction(dosageInstruction)
            .withPatientInstruction(patientInstruction)
            .fake();

    val paramsBuilder =
        GemOperationInputParameterBuilder.forDispensingPharmaceuticals()
            .version(erpWfVersion)
            .with(medDisp, gemMedicationCompounding);
    val params = paramsBuilder.build();

    pharmacy
        .performs(
            DispensePrescriptionNew.withCredentials(acceptance.getTaskId(), acceptance.getSecret())
                .withParameters(params))
        .getExpectedResponse();

    pharmacy.performs(
        ClosePrescriptionWithoutDispensation.forTheTask(task, task.getSecret().orElseThrow()));

    val dispensation =
        patient.performs(GetMedicationDispense.withQueryParams(searchParams)).getExpectedResponse();

    epaFhirChecker.attemptsTo(
        CheckEpaOpProvideDispensation.forDispensationWithAdditionalVerifier(
            dispensation,
            pharmacy.getTelematikId(),
            task.getPrescriptionId(),
            List.of(
                provDispensationContainsDosageInstruction(
                    dosageInstruction + "; " + patientInstruction))));
  }

  @TestcaseId("EML_MAP_PATIENT_INSTRUCTION_IN_DISPENSATION_04")
  @Test()
  @DisplayName(
      "Es muss geprüft werden, dass das übermittelte PatientInstruction der"
          + " ProvideDispensation an das Epa Aktensystem den Werten der ausgestellten Dispensation"
          + " entspricht")
  void submitPatientInstructionByDispensationToEpaMock() {
    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    this.config.equipWithEpaMockClient(epaFhirChecker);

    val patientInstruction = "Dosieranweisung im PatientInstruction-field";

    doc.setVersion(itaForVersion);
    patient.setVersion(itaForVersion);

    val medication =
        KbvErpMedicationCompoundingFaker.builder(itaErpVersion).withVaccine(false).fake();
    val activation =
        doc.performs(
                IssuePrescription.forPatient(patient)
                    .withKbvBundleFrom(
                        KbvErpBundleFaker.builder(itaErpVersion, itaForVersion)
                            .withMedication(medication)
                            .toBuilder()))
            .getExpectedResponse();

    acceptance = pharmacy.performs(AcceptPrescription.forTheTask(activation)).getExpectedResponse();
    val task = acceptance.getTask();

    // todo map original KBV Medication to GemErpMedication
    val gemMedicationCompounding =
        GemErpMedicationFaker.forMedicationCompounding(erpWfVersion).fake();
    gemMedicationCompounding.getCode().getCoding().forEach(c -> c.setVersion("2026"));
    val medDisp =
        ErxMedicationDispenseFaker.builder(erpWfVersion)
            .withKvnr(patient.getKvnr())
            .withPrescriptionId(task.getPrescriptionId())
            .withMedication(gemMedicationCompounding)
            .withPerformer(pharmacy.getTelematikId().getValue())
            .withPatientInstruction(patientInstruction)
            .fake();

    val paramsBuilder =
        GemOperationInputParameterBuilder.forDispensingPharmaceuticals()
            .version(erpWfVersion)
            .with(medDisp, gemMedicationCompounding);
    val params = paramsBuilder.build();

    pharmacy
        .performs(
            DispensePrescriptionNew.withCredentials(acceptance.getTaskId(), acceptance.getSecret())
                .withParameters(params))
        .getExpectedResponse();
    pharmacy.performs(
        ClosePrescriptionWithoutDispensation.forTheTask(task, task.getSecret().orElseThrow()));

    val dispensation =
        patient.performs(GetMedicationDispense.withQueryParams(searchParams)).getExpectedResponse();

    epaFhirChecker.attemptsTo(
        CheckEpaOpProvideDispensation.forDispensationWithAdditionalVerifier(
            dispensation,
            pharmacy.getTelematikId(),
            task.getPrescriptionId(),
            List.of(provDispensationContainsDosageInstruction(patientInstruction))));
  }

  @TestcaseId("EML_ADD_VERSION_TO_ATC_CODING_05")
  @Test()
  @DisplayName(
      "Es muss geprüft werden, dass der Fachdienst in der Medication die Codings mit einer Version"
          + " ergänzt")
  void submitPatientInstructionByDispensationToEpaMockWithAtcWithoutVersion() {
    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    this.config.equipWithEpaMockClient(epaFhirChecker);
    val patientInstruction = "Dosieranweisung im PatientInstruction-field";

    doc.setVersion(itaForVersion);
    patient.setVersion(itaForVersion);

    val medication =
        KbvErpMedicationCompoundingFaker.builder(itaErpVersion).withVaccine(false).fake();
    val activation =
        doc.performs(
                IssuePrescription.forPatient(patient)
                    .withKbvBundleFrom(
                        KbvErpBundleFaker.builder(itaErpVersion, itaForVersion)
                            .withMedication(medication)
                            .toBuilder()))
            .getExpectedResponse();

    acceptance = pharmacy.performs(AcceptPrescription.forTheTask(activation)).getExpectedResponse();
    val task = acceptance.getTask();

    // todo map original KBV Medication to GemErpMedication
    val gemMedicationCompounding =
        GemErpMedicationFaker.forMedicationCompounding(erpWfVersion)
            .withAtc(ATC.from("123456"))
            .fake();

    val medDisp =
        ErxMedicationDispenseFaker.builder(erpWfVersion)
            .withKvnr(patient.getKvnr())
            .withPrescriptionId(task.getPrescriptionId())
            .withMedication(gemMedicationCompounding)
            .withPerformer(pharmacy.getTelematikId().getValue())
            .withPatientInstruction(patientInstruction)
            .fake();

    val paramsBuilder =
        GemOperationInputParameterBuilder.forDispensingPharmaceuticals()
            .version(erpWfVersion)
            .with(medDisp, gemMedicationCompounding);
    val params = paramsBuilder.build();

    pharmacy
        .performs(
            DispensePrescriptionNew.withCredentials(acceptance.getTaskId(), acceptance.getSecret())
                .withParameters(params))
        .getExpectedResponse();
    pharmacy.performs(
        ClosePrescriptionWithoutDispensation.forTheTask(task, task.getSecret().orElseThrow()));

    val dispensation =
        patient.performs(GetMedicationDispense.withQueryParams(searchParams)).getExpectedResponse();

    epaFhirChecker.attemptsTo(
        CheckEpaOpProvideDispensation.forDispensationWithAdditionalVerifier(
            dispensation,
            pharmacy.getTelematikId(),
            task.getPrescriptionId(),
            List.of(medicationInProvDispensationContainsAtcCodingWithVersion())));
  }
}
