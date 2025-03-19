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

package de.gematik.test.eml.integration;

import static de.gematik.test.core.expectations.verifier.AuditEventVerifier.bundleContainsLogFor;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.eml.tasks.CheckEpaOpCancelPrescriptionWithTask;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.AcceptPrescription;
import de.gematik.test.erezept.actions.DownloadAuditEvent;
import de.gematik.test.erezept.actions.GetPrescriptionById;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.TaskAbort;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.GemaTestActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.arguments.WorkflowAndMedicationComposer;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SearchPrefix;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationCompoundingFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationFreeTextBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationIngredientFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.time.LocalDate;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Cancel Prescription with Consent Decision")
@Tag("CancelEmlPrescription")
@Tag("EpaEml")
public class CancelPrescriptionWithConsentIT extends ErpTest {

  private static final String MEDICATION_PZN = "Medication PZN";
  private static final String MEDICATION_INGREDIENT = "Medication Ingredient";
  private static final String MEDICATION_COMPOUNDING = "Medication Compounding";
  private static final String MEDICATION_FREITEXT = "Freitextverordnung";

  @Actor(name = "Günther Angermänn")
  private PatientActor patient;

  @Actor(name = "Gündüla Gunther")
  private DoctorActor doc;

  @Actor(name = "Stadtapotheke")
  private PharmacyActor pharmacy;

  static Stream<Arguments> prescriptionTypesProvider() {
    return WorkflowAndMedicationComposer.workflowAndMedicationComposer().create();
  }

  @TestcaseId("EML_CANCEL_PRESCRIPTION_WITH_CONSENT_DECISION_APPLY_AS_PATIENT_01")
  @ParameterizedTest(
      name =
          "[{index}] -> Für einen Flow Type {2} soll die stornierte Prescription mit {3} im"
              + " Epa-Aktensystem überprüft werden")
  @DisplayName(
      "Es muss geprüft werden, dass eine vom Patient stornierte Prescription korrekt im Epa-"
          + " Aktensystem erfasst wird")
  @MethodSource(
      "de.gematik.test.erezept.arguments.WorkflowAndMedicationComposer#workflowPharmacyOnlyAndMedicationComposer")
  void checkCanceledPrescriptionInformationPatient(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      PrescriptionFlowType expectedFlowTypeForDescription,
      String medicationType) {

    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    this.config.equipWithEpaMockClient(epaFhirChecker);

    patient.changePatientInsuranceType(insuranceType);

    val activation =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(assignmentKind)
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder()
                        .withMedication(getMedication(medicationType))
                        .toBuilder()));

    // Retrieve and validate the activated prescription task
    val task = activation.getExpectedResponse();

    val prescription =
        patient
            .performs(
                GetPrescriptionById.withTaskId(task.getTaskId())
                    .withAccessCode(task.getAccessCode()))
            .getExpectedResponse();

    // Perform cancellation
    patient.performs(TaskAbort.asPatient(task));

    // performs the resource-content validation
    epaFhirChecker.attemptsTo(
        CheckEpaOpCancelPrescriptionWithTask.forCancelPrescription(
            prescription.getKbvBundle().orElseThrow()));

    // Prepare search parameters for audit events
    val searchParams =
        IQueryParameter.search()
            .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.EQ)
            .sortedBy("date", SortOrder.DESCENDING)
            .createParameter();

    val auditEvents = patient.performs(DownloadAuditEvent.withQueryParams(searchParams));

    patient.attemptsTo(
        Verify.that(auditEvents)
            .withExpectedType()
            .and(
                bundleContainsLogFor(
                    prescription.getTask().getPrescriptionId(),
                    "Die Löschinformation zum E-Rezept wurde in die Patientenakte übermittelt"))
            .isCorrect());
  }

  @TestcaseId("EML_CANCEL_PRESCRIPTION_WITH_CONSENT_DECISION_APPLY_AS_DOCTOR_02")
  @ParameterizedTest(
      name =
          "[{index}] -> Für einen Flow Type {2} soll die stornierte Prescription mit {3} im"
              + " Epa-Aktensystem überprüft werden")
  @DisplayName(
      "Es muss geprüft werden, dass eine vom Arzt stornierte Prescription korrekt im Epa-"
          + " Aktensystem erfasst wird")
  @MethodSource("prescriptionTypesProvider")
  void checkCanceledPrescriptionInformationDoctor(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      PrescriptionFlowType expectedFlowTypeForDescription,
      String medicationType) {

    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    this.config.equipWithEpaMockClient(epaFhirChecker);

    patient.changePatientInsuranceType(insuranceType);

    val activation =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(assignmentKind)
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder()
                        .withMedication(getMedication(medicationType))
                        .toBuilder()));

    // Verifies correct activated prescription and get KbvErpBundle for validation step
    val task = activation.getExpectedResponse();

    val prescription =
        patient.performs(
            GetPrescriptionById.withTaskId(task.getTaskId()).withAccessCode(task.getAccessCode()));

    // Performs cancellation
    doc.performs(TaskAbort.asLeistungserbringer(task));

    // performs the resource-content validation
    epaFhirChecker.attemptsTo(
        CheckEpaOpCancelPrescriptionWithTask.forCancelPrescription(
            prescription.getExpectedResponse().getKbvBundle().orElseThrow()));

    // Validates cancellation logs
    val searchParams =
        IQueryParameter.search()
            .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.EQ)
            .sortedBy("date", SortOrder.DESCENDING)
            .createParameter();

    val auditEvents = patient.performs(DownloadAuditEvent.withQueryParams(searchParams));

    patient.attemptsTo(
        Verify.that(auditEvents)
            .withExpectedType()
            .and(
                bundleContainsLogFor(
                    prescription.getExpectedResponse().getTask().getPrescriptionId(),
                    "Die Löschinformation zum E-Rezept wurde in die Patientenakte übermittelt"))
            .isCorrect());
  }

  @TestcaseId("EML_CANCEL_PRESCRIPTION_WITH_CONSENT_DECISION_APPLY_AS_PHARMACY_03")
  @ParameterizedTest(
      name =
          "[{index}] -> Für einen Flow Type {2} soll die stornierte Prescription mit {3} im"
              + " Epa-Aktensystem überprüft werden")
  @DisplayName(
      "Es muss geprüft werden, dass eine von der Apotheke stornierte Prescription korrekt im Epa-"
          + " Aktensystem erfasst wird")
  @MethodSource("prescriptionTypesProvider")
  void checkCanceledPrescriptionInformationPharmacy(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      PrescriptionFlowType expectedFlowTypeForDescription,
      String medicationType) {

    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    this.config.equipWithEpaMockClient(epaFhirChecker);

    patient.changePatientInsuranceType(insuranceType);

    val activation =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(assignmentKind)
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder()
                        .withMedication(getMedication(medicationType))
                        .toBuilder()));

    // Verifies correct activated prescription and get KbvErpBundle for validation step
    val task = activation.getExpectedResponse();

    val prescription =
        patient.performs(
            GetPrescriptionById.withTaskId(task.getTaskId()).withAccessCode(task.getAccessCode()));

    // Performs cancellation
    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();
    val abortRespInteraction = pharmacy.performs(TaskAbort.asPharmacy(acceptation));
    pharmacy.attemptsTo(
        Verify.that(abortRespInteraction)
            .withoutBody()
            .hasResponseWith(returnCode(204))
            .isCorrect());

    epaFhirChecker.attemptsTo(
        CheckEpaOpCancelPrescriptionWithTask.forCancelPrescription(
            prescription.getExpectedResponse().getKbvBundle().orElseThrow()));

    // Validates cancellation logs
    val searchParams =
        IQueryParameter.search()
            .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.EQ)
            .sortedBy("date", SortOrder.DESCENDING)
            .createParameter();

    val auditEvents = patient.performs(DownloadAuditEvent.withQueryParams(searchParams));

    patient.attemptsTo(
        Verify.that(auditEvents)
            .withExpectedType()
            .and(
                bundleContainsLogFor(
                    prescription.getExpectedResponse().getTask().getPrescriptionId(),
                    "Die Löschinformation zum E-Rezept wurde in die Patientenakte übermittelt"))
            .isCorrect());
  }

  private KbvErpMedication getMedication(String medicationType) {
    return switch (medicationType) {
      case MEDICATION_PZN -> KbvErpMedicationPZNFaker.builder().fake();
      case MEDICATION_INGREDIENT -> KbvErpMedicationIngredientFaker.builder().fake();
      case MEDICATION_COMPOUNDING -> KbvErpMedicationCompoundingFaker.builder().fake();
      case MEDICATION_FREITEXT -> KbvErpMedicationFreeTextBuilder.builder()
          .freeText("Erp-To-Epa E2E Test")
          .build();
      default -> throw new IllegalArgumentException("Unknown medication type: " + medicationType);
    };
  }
}
