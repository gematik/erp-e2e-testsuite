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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.eml.integration;

import static de.gematik.test.core.expectations.verifier.AuditEventVerifier.bundleContainsLogFor;
import static de.gematik.test.erezept.arguments.WorkflowAndMedicationComposer.MEDICATION_COMPOUNDING;
import static de.gematik.test.erezept.arguments.WorkflowAndMedicationComposer.MEDICATION_FREITEXT;
import static de.gematik.test.erezept.arguments.WorkflowAndMedicationComposer.MEDICATION_INGREDIENT;
import static de.gematik.test.erezept.arguments.WorkflowAndMedicationComposer.MEDICATION_PZN;

import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.eml.tasks.CheckEpaOpProvidePrescriptionWithTask;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.DownloadAuditEvent;
import de.gematik.test.erezept.actions.GetPrescriptionById;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.TaskAbort;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.GemaTestActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.arguments.WorkflowAndMedicationComposer;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SearchPrefix;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationCompoundingFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationFreeTextFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationIngredientFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
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
@DisplayName("Provide Prescription with Consent Test")
@Tag("ProvideEmlPrescription")
@Tag("EpaEml")
public class ProvidePrescriptionWithConsentIT extends ErpTest {

  public static Stream<Arguments> workflowAndMedicationComposer() {
    return WorkflowAndMedicationComposer.workflowAndMedicationComposer().create();
  }

  @Actor(name = "Günther Angermänn")
  private PatientActor patient;

  @Actor(name = "Gündüla Gunther")
  private DoctorActor doc;

  @TestcaseId("EML_PROVIDE_PRESCRIPTION_WITH_CONSENT_DECISION_APPLY_01")
  @ParameterizedTest(
      name =
          "[{index}] -> für einen Flow Type {2} sollen die zum Epa-Aktensystem gesendete"
              + " Prescription mit {3} überprüft werden")
  @DisplayName(
      "Es muss geprüft werden, dass die übermittelte Prescription an das Epa Aktensystem den Werten"
          + " der Prescription entspricht")
  @MethodSource("workflowAndMedicationComposer")
  void checkSubmittedPrescriptionInformation(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      PrescriptionFlowType expectedFlowTypeForDescription,
      String medicationType) {

    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    this.config.equipWithEpaMockClient(epaFhirChecker);

    patient.changePatientInsuranceType(insuranceType);
    // todo activate if PUT is activated
    // patient.attemptsTo(EmlProvidePrescriptionApply.forKvnr(patient.getKvnr()));

    val activation =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(assignmentKind)
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder()
                        .withMedication(getMedication(medicationType))
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
            kbvBundle, doc.getSmcbTelematikId(), doc.getHbaTelematikId()));

    // patient checks auditEvent content
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
                    prescr.getExpectedResponse().getTask().getPrescriptionId(),
                    "Die Verordnung wurde in die Patientenakte übertragen"))
            .isCorrect());

    // cleanUp
    patient.performs(TaskAbort.asPatient(task));
  }

  private KbvErpMedication getMedication(String medicationType) {
    return switch (medicationType) {
      case MEDICATION_PZN -> KbvErpMedicationPZNFaker.builder()
          .withPznMedication(PZN.random(), "IBU-ratiopharm 400mg akut Schmerztabletten")
          .withAmount(50, "Tablet")
          .withStandardSize(StandardSize.N1)
          .fake();
      case MEDICATION_INGREDIENT -> KbvErpMedicationIngredientFaker.builder()
          .withCategory(MedicationCategory.C_00)
          .withAmount("1", 1, "halt sowas")
          .withDrugName("Grippostad C® Fruchtgummi")
          .withStandardSize(StandardSize.N1)
          .withIngredientComponent(2, 1, "wölkchen")
          .fake();
      case MEDICATION_COMPOUNDING -> KbvErpMedicationCompoundingFaker.builder()
          .withCategory(MedicationCategory.C_00)
          .withMedicationIngredient(PZN.random(), "Vertigoheel® 20 mg", "freitextPzn")
          .withAmount(5, 1, "Stk")
          .withDosageForm("Zäpfchen, viel Spaß")
          .fake();
      case MEDICATION_FREITEXT -> KbvErpMedicationFreeTextFaker.builder()
          .withCategory(MedicationCategory.C_00)
          .withDosageForm("Zäpfchen, viel Spaß")
          .withVaccine(false)
          .withFreeText("Hier ist der mentale Meilenstein des LE")
          .fake();
      default -> throw new IllegalArgumentException("Unknown medication type: " + medicationType);
    };
  }
}
