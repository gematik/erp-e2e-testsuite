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

package de.gematik.test.eml.integration;

import static de.gematik.test.core.expectations.verifier.AuditEventVerifier.bundleContainsLogFor;
import static de.gematik.test.erezept.arguments.WorkflowAndMedicationComposer.*;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.eml.tasks.CheckEpaOpProvideDispensation;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.GemaTestActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.arguments.WorkflowAndMedicationComposer;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SearchPrefix;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.values.TelematikID;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
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
@DisplayName("Provide Dispensation with Consent Test")
@Tag("ProvideEmlDispensationPermit")
@Tag("EpaEml")
public class ProvideDispensationPermitIT extends ErpTest {

  @Actor(name = "Günther Angermänn")
  private PatientActor patient;

  @Actor(name = "Gündüla Gunther")
  private DoctorActor doc;

  @Actor(name = "Am Waldesrand")
  private PharmacyActor pharmacy;

  private final List<IQueryParameter> searchParams =
      IQueryParameter.search()
          .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.EQ)
          .sortedBy("date", SortOrder.DESCENDING)
          .createParameter();

  public static Stream<Arguments> workflowAndMedicationComposer() {
    return WorkflowAndMedicationComposer.workflowAndMedicationComposer().create();
  }

  @TestcaseId("EML_Provide_Prescription_with_Consent_01")
  @ParameterizedTest(
      name =
          "[{index}] -> für einen Flow Type {2} sollen die zum Epa-Aktensystem gesendete"
              + " Prescription mit {3} überprüft werden")
  @DisplayName(
      "Es wird geprüft, dass die Werte der übermittelten EmlMedicationDispense an das Epa"
          + " Aktensystem den Werten der Dispensation entspricht")
  @MethodSource("workflowAndMedicationComposer")
  void checkSubmittedPrescriptionInformation(
      VersicherungsArtDeBasis insuranceType,
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

    val dispensation =
        pharmacy.performs(
            DispensePrescription.forPrescription(acceptance.getTaskId(), acceptance.getSecret())
                .withMedDsp(List.of(getMedDisp(task))));

    pharmacy.attemptsTo(Verify.that(dispensation).withExpectedType().isCorrect());
    pharmacy.performs(
        ClosePrescriptionWithoutDispensation.forTheTask(task, acceptance.getSecret()));

    epaFhirChecker.attemptsTo(
        CheckEpaOpProvideDispensation.forDispensation(
            dispensation.getExpectedResponse().getMedicationDispenses().get(0),
            TelematikID.from(SafeAbility.getAbility(pharmacy, UseSMCB.class).getTelematikID())));

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

  private ErxMedicationDispense getMedDisp(ErxTask task) {
    val medication1 =
        KbvErpMedicationPZNFaker.builder()
            .withAmount(666)
            .withPznMedication(PZN.from("17377588"), "Comirnaty von BioNTech/Pfizer")
            .fake();

    return ErxMedicationDispenseBuilder.forKvnr(patient.getKvnr())
        .dosageInstruction("1-2-3-4")
        .whenHandedOver(new Date())
        .wasSubstituted(false)
        .status(MedicationDispense.MedicationDispenseStatus.COMPLETED)
        .medication(medication1)
        .performerId(pharmacy.getTelematikId().getValue())
        .prescriptionId(task.getPrescriptionId())
        .build();
  }
}
