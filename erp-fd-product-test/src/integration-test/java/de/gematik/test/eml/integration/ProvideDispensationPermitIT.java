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
import de.gematik.test.eml.tasks.CheckEpaOpProvideDispensation;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.AcceptPrescription;
import de.gematik.test.erezept.actions.ClosePrescriptionWithoutDispensation;
import de.gematik.test.erezept.actions.DispensePrescription;
import de.gematik.test.erezept.actions.DownloadAuditEvent;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.GemaTestActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.arguments.WorkflowAndMedicationComposer;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SearchPrefix;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBuilder;
import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationFaker;
import de.gematik.test.erezept.fhir.builder.erp.GemOperationInputParameterBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationCompoundingFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationFreeTextBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationIngredientFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.erp.GemDispenseOperationParameters;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Provide Dispensation with Consent Test")
@Tag("ProvideEmlDispensationPermit")
@Tag("EpaEml")
public class ProvideDispensationPermitIT extends ErpTest {

  private final List<IQueryParameter> searchParams =
      IQueryParameter.search()
          .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.EQ)
          .sortedBy("date", SortOrder.DESCENDING)
          .createParameter();

  @Actor(name = "Günther Angermänn")
  private PatientActor patient;

  @Actor(name = "Gündüla Gunther")
  private DoctorActor doc;

  @Actor(name = "Am Waldesrand")
  private PharmacyActor pharmacy;

  private ErxAcceptBundle acceptance;

  public static Stream<Arguments> workflowAndMedicationComposer() {
    return WorkflowAndMedicationComposer.workflowAndMedicationComposer().create();
  }

  @TestcaseId("EML_PROVIDE_PRESCRIPTION_WITH_CONSENT_DECISION_APPLY_01")
  @ParameterizedTest(
      name =
          "[{index}] -> für einen Flow Type {2} sollen die zum Epa-Aktensystem gesendete"
              + " Prescription mit {3} überprüft werden")
  @DisplayName(
      "Es wird geprüft, dass die Werte der übermittelten EmlMedicationDispense an das Epa"
          + " Aktensystem den Werten der Dispensation entspricht")
  @MethodSource("workflowAndMedicationComposer")
  void checkSubmittedPrescriptionInformation(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      PrescriptionFlowType expectedFlowTypeForDescription,
      String medicationType) {

    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    this.config.equipWithEpaMockClient(epaFhirChecker);

    patient.changePatientInsuranceType(insuranceType);

    val task =
        doc.performs(
                IssuePrescription.forPatient(patient)
                    .ofAssignmentKind(assignmentKind)
                    .withKbvBundleFrom(
                        KbvErpBundleFaker.builder()
                            .withMedication(getMedication(medicationType))
                            .toBuilder()))
            .getExpectedResponse();

    acceptance = pharmacy.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();

    val dispenseAction =
        DispensePrescription.withCredentials(task.getTaskId(), acceptance.getSecret())
            .withParameters(getDispenseParameters(task));

    val dispensationInterAction = pharmacy.performs(dispenseAction);

    pharmacy.attemptsTo(Verify.that(dispensationInterAction).withExpectedType().isCorrect());
    pharmacy.performs(
        ClosePrescriptionWithoutDispensation.forTheTask(task, acceptance.getSecret()));

    val mdBundle = dispensationInterAction.getExpectedResponse();
    epaFhirChecker.attemptsTo(
        CheckEpaOpProvideDispensation.forDispensation(
            mdBundle, pharmacy.getTelematikId(), task.getPrescriptionId()));

    val auditEvents = patient.performs(DownloadAuditEvent.withQueryParams(searchParams));
    patient.attemptsTo(
        Verify.that(auditEvents)
            .withExpectedType()
            .and(
                bundleContainsLogFor(
                    task.getPrescriptionId(),
                    "Die Medikamentenabgabe wurde in die Patientenakte übertragen"))
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

  private List<ErxMedicationDispense> getMedDisp(ErxTask task) {
    val medication1 =
        KbvErpMedicationPZNFaker.builder()
            .withAmount(666)
            .withPznMedication(PZN.from("17377588"), "Comirnaty von BioNTech/Pfizer")
            .fake();

    val md =
        ErxMedicationDispenseBuilder.forKvnr(patient.getKvnr())
            .dosageInstruction("1-2-3-4")
            .whenHandedOver(new Date())
            .wasSubstituted(false)
            .status(MedicationDispense.MedicationDispenseStatus.COMPLETED)
            .medication(medication1)
            .performerId(pharmacy.getTelematikId().getValue())
            .prescriptionId(task.getPrescriptionId())
            .build();

    return List.of(md);
  }

  private GemDispenseOperationParameters getDispenseParameters(ErxTask task) {

    val medication =
        GemErpMedicationFaker.forPznMedication()
            .withAmount(666)
            .withPzn(PZN.from("17377588"), "Comirnaty von BioNTech/Pfizer")
            .fake();

    val md =
        ErxMedicationDispenseBuilder.forKvnr(patient.getKvnr())
            .dosageInstruction("1-2-3-4")
            .whenHandedOver(new Date())
            .wasSubstituted(false)
            .status(MedicationDispense.MedicationDispenseStatus.COMPLETED)
            .medication(medication)
            .performerId(pharmacy.getTelematikId().getValue())
            .prescriptionId(task.getPrescriptionId())
            .build();

    return GemOperationInputParameterBuilder.forDispensingPharmaceuticals()
        .with(md, medication)
        .build();
  }
}
