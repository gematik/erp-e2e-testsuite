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

import static de.gematik.test.core.expectations.verifier.AuditEventVerifier.bundleDoesNotContainLogFor;
import static de.gematik.test.erezept.arguments.WorkflowAndMedicationComposer.MEDICATION_COMPOUNDING;
import static de.gematik.test.erezept.arguments.WorkflowAndMedicationComposer.MEDICATION_FREITEXT;
import static de.gematik.test.erezept.arguments.WorkflowAndMedicationComposer.MEDICATION_INGREDIENT;
import static de.gematik.test.erezept.arguments.WorkflowAndMedicationComposer.MEDICATION_PZN;

import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.eml.tasks.CheckErpDoesNotProvideDispensationToEpa;
import de.gematik.test.erezept.ErpInteraction;
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
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBuilder;
import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationFaker;
import de.gematik.test.erezept.fhir.builder.erp.GemOperationInputParameterBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationCompoundingFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationFreeTextBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationIngredientFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.hl7.fhir.r4.model.MedicationDispense;
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
@DisplayName("Provide Dispensation without Consent Test")
@Tag("ProvideEmlDispensationDeny")
@Tag("EpaEml")
public class ProvideDispensationWithoutConsentIT extends ErpTest {

  private final List<IQueryParameter> searchParams =
      IQueryParameter.search()
          .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.EQ)
          .sortedBy("date", SortOrder.DESCENDING)
          .createParameter();

  @Actor(name = "Hanna Bäcker")
  private PatientActor patient;

  @Actor(name = "Gündüla Gunther")
  private DoctorActor doc;

  @Actor(name = "Am Waldesrand")
  private PharmacyActor pharmacy;

  public static Stream<Arguments> workflowAndMedicationComposer() {
    return WorkflowAndMedicationComposer.workflowAndMedicationComposer().create();
  }

  @TestcaseId("EML_PROVIDE_DISPENSATION_WITH_CONSENT_DECISION_DENY_01")
  @ParameterizedTest(
      name =
          "[{index}] -> für einen Flow Type {2} sollen die Information zur Provide-Dispensation"
              + " nicht zum Epa-Aktensystem gesendete werden")
  @DisplayName(
      "Es wird geprüft, dass die Werte der übermittelten EmlMedicationDispense nicht in dem Epa"
          + " Aktensystem gefunden werden")
  @MethodSource("workflowAndMedicationComposer")
  void checkSubmittedDispensationInformation(
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
    val acceptance = pharmacy.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();

    val dispensation = getDispenseBundleErpInteraction(task, acceptance);

    pharmacy.attemptsTo(Verify.that(dispensation).withExpectedType().isCorrect());
    pharmacy.performs(
        ClosePrescriptionWithoutDispensation.forTheTask(task, acceptance.getSecret()));

    epaFhirChecker.attemptsTo(
        CheckErpDoesNotProvideDispensationToEpa.forDispensation(dispensation));

    val auditEvents = patient.performs(DownloadAuditEvent.withQueryParams(searchParams));

    patient.attemptsTo(
        Verify.that(auditEvents)
            .withExpectedType()
            .and(
                bundleDoesNotContainLogFor(
                    task.getPrescriptionId(),
                    "Die Medikamentenabgabe wurde in die Patientenakte übertragen."))
            .and(
                bundleDoesNotContainLogFor(
                    task.getPrescriptionId(),
                    "Die Medikamentenabgabe konnte nicht in die Patientenakte übertragen werden."))
            .isCorrect());
  }

  private ErpInteraction<ErxMedicationDispenseBundle> getDispenseBundleErpInteraction(
      ErxTask task, ErxAcceptBundle acceptance) {
    ErpInteraction<ErxMedicationDispenseBundle> dispensation;

    val medDsp = getMedDspBuilder(task);
    val lotNr = GemFaker.fakerLotNumber();
    val expDate = GemFaker.fakerFutureExpirationDate();
    val gemMedication = GemErpMedicationFaker.forPznMedication().fake();

    val gemMedDsp = medDsp.batch(lotNr, expDate).medication(gemMedication).build();

    dispensation =
        pharmacy.performs(
            DispensePrescription.withCredentials(acceptance.getTaskId(), acceptance.getSecret())
                .withParameters(
                    GemOperationInputParameterBuilder.forDispensingPharmaceuticals()
                        .with(gemMedDsp, gemMedication)
                        .build()));

    return dispensation;
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

  private KbvErpMedication getMedication(ErxTask task) {
    return KbvErpMedicationPZNFaker.builder()
        .withAmount(666)
        .withPznMedication(PZN.from("17377588"), "Comirnaty von BioNTech/Pfizer")
        .fake();
  }

  private ErxMedicationDispenseBuilder getMedDspBuilder(ErxTask task) {
    return ErxMedicationDispenseBuilder.forKvnr(patient.getKvnr())
        .dosageInstruction("1-2-3-4")
        .whenHandedOver(new Date())
        .wasSubstituted(false)
        .status(MedicationDispense.MedicationDispenseStatus.COMPLETED)
        .performerId(pharmacy.getTelematikId().getValue())
        .prescriptionId(task.getPrescriptionId());
  }
}
