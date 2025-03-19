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

package de.gematik.test.erezept.integration.task;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeContainsInDetailText;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.FhirRequirements;
import de.gematik.test.core.expectations.requirements.RequirementsSet;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationRequestFaker;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Invalide IKNR in der Verordnung")
public class ActivateInvalidCoverageIknr extends ErpTest {

  @Actor(name = "Gündüla Gunther")
  private DoctorActor doctorActor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  private static Stream<Arguments> baseIKNRComposer() {
    return ArgumentComposer.composeWith()
        .arguments("123456789", "die Prüfziffer 0 statt 9")
        .arguments("111111111", "die Prüfziffer 9 statt 1")
        .arguments("123456", "die IKNR zu kurz")
        .arguments("12119124", "die IKNR ohne Prüfziffer")
        .arguments("1234567891", "die IKNR zu lang")
        .arguments("12C45678", "ein Buchstabe enthalten")
        .multiply(List.of(InsuranceTypeDe.BG, InsuranceTypeDe.GKV, InsuranceTypeDe.PKV))
        .multiply(1, PrescriptionAssignmentKind.class)
        .create();
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_COVERAGE_IKNR_01")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt stellt ein E-Rezept mit invalider Prüfnummer: {2} für den"
              + " Kostenträger {0} aus, da {3} ist!")
  @DisplayName("Es muss geprüft werden, dass der Fachdienst die IKNR  korrekt validiert")
  @MethodSource("baseIKNRComposer")
  void activatePrescriptionWithInvalidIknr(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind prescriptionAssignmentKind,
      String iknr,
      String reason) {
    RequirementsSet requirementsSet = ErpAfos.A_23888;
    String detailedText =
        "Ungültiges Institutionskennzeichen (IKNR): Das übergebene Institutionskennzeichen im"
            + " Versicherungsstatus entspricht nicht den Prüfziffer-Validierungsregeln.";
    AccidentExtension accident = null;
    Consumer<KbvErpBundle> kbvErpBundleConsumer;
    if (insuranceType.equals(InsuranceTypeDe.BG)) {
      detailedText =
          "Ungültiges Institutionskennzeichen (IKNR): Das übergebene Institutionskennzeichen des"
              + " Kostenträgers entspricht nicht den Prüfziffer-Validierungsregeln.";
      requirementsSet = ErpAfos.A_24030;
      sina.changeCoverageInsuranceType(InsuranceTypeDe.BG);
      accident = AccidentExtension.accidentAtWork().atWorkplace();

      kbvErpBundleConsumer =
          kbvBundle ->
              kbvBundle.getCoverage().getPayorFirstRep().getIdentifier().getExtension().stream()
                  .filter(ext -> KbvItaForStructDef.ALTERNATIVE_IK.matches(ext.getUrl()))
                  .forEach(ext -> ext.getValue().castToIdentifier(ext.getValue()).setValue(iknr));

    } else {
      kbvErpBundleConsumer =
          kbvBundle -> kbvBundle.getCoverage().getPayorFirstRep().getIdentifier().setValue(iknr);
    }
    if (iknr.length() != 9) {
      detailedText = "FHIR-Validation error";
      requirementsSet = FhirRequirements.FHIR_VALIDATION_ERROR;
    }
    val medication = KbvErpMedicationPZNFaker.builder().fake();
    val medicationRequest = getMedicationRequest(accident, medication);
    val kbvBundleBuilder = getBundleBuilder(medication, medicationRequest);
    val issuePrescription =
        getIssuePrescription(prescriptionAssignmentKind, kbvErpBundleConsumer, kbvBundleBuilder);
    val activation = doctorActor.performs(issuePrescription);

    doctorActor.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome(requirementsSet)
            .hasResponseWith(returnCode(400, requirementsSet))
            .and(operationOutcomeContainsInDetailText(detailedText, requirementsSet))
            .isCorrect());
  }

  private IssuePrescription getIssuePrescription(
      PrescriptionAssignmentKind prescriptionAssignmentKind,
      Consumer<KbvErpBundle> kbvErpBundleConsumer,
      KbvErpBundleBuilder kbvBundleBuilder) {
    return IssuePrescription.forPatient(sina)
        .ofAssignmentKind(prescriptionAssignmentKind)
        .withResourceManipulator(kbvErpBundleConsumer)
        .withKbvBundleFrom(kbvBundleBuilder);
  }

  private KbvErpMedicationRequest getMedicationRequest(
      AccidentExtension accident, KbvErpMedication medication) {
    return KbvErpMedicationRequestFaker.builder()
        .withPatient(sina.getPatientData())
        .withInsurance(sina.getInsuranceCoverage())
        .withRequester(doctorActor.getPractitioner())
        .withAccident(accident)
        .withMedication(medication)
        .fake();
  }

  private KbvErpBundleBuilder getBundleBuilder(
      KbvErpMedication medication, KbvErpMedicationRequest medicationRequest) {
    return KbvErpBundleFaker.builder()
        .withKvnr(sina.getKvnr())
        .withPractitioner(doctorActor.getPractitioner())
        .withMedication(medication)
        .toBuilder()
        .medicationRequest(medicationRequest);
  }
}
