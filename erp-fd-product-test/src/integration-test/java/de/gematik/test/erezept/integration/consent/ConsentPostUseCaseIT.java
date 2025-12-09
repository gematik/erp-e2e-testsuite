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

package de.gematik.test.erezept.integration.consent;

import static de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe.*;
import static de.gematik.test.core.expectations.verifier.AcceptBundleVerifier.consentIsPresent;
import static de.gematik.test.core.expectations.verifier.AcceptBundleVerifier.isInProgressStatus;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIsBetween;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeContainsInDetailText;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.isInReadyStatus;
import static de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind.DIRECT_ASSIGNMENT;
import static de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind.PHARMACY_ONLY;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.verifier.ConsentBundleVerifier;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.AcceptPrescription;
import de.gematik.test.erezept.actions.ClosePrescription;
import de.gematik.test.erezept.actions.GrantConsent;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.ReadConsent;
import de.gematik.test.erezept.actions.RejectConsent;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.builder.erp.ErxConsentBuilder;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.erezept.tasks.EnsureConsent;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Consent Setzen")
public class ConsentPostUseCaseIT extends ErpTest {
  @Actor(name = "Fridolin Straßer")
  private PatientActor fridolin;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Am Flughafen")
  private PharmacyActor flughafenApo;

  private static Stream<Arguments> prescriptionConsent() {
    return ArgumentComposer.composeWith()
        .arguments(GKV, PHARMACY_ONLY, false)
        .arguments(GKV, DIRECT_ASSIGNMENT, false)
        .arguments(PKV, PHARMACY_ONLY, false)
        .arguments(PKV, PHARMACY_ONLY, true)
        .arguments(PKV, DIRECT_ASSIGNMENT, false)
        .arguments(PKV, DIRECT_ASSIGNMENT, true)
        .create();
  }

  @TestcaseId("ERP_CONSENT_POST_01")
  @ParameterizedTest(
      name = "[{index}] -> Prüfe als {0}-Versicherter ob beim akzeptieren der Consent {2} ist")
  @DisplayName("Prüfe den Consent beim Accept als abgebende Apotheke")
  @MethodSource("prescriptionConsent")
  void checkConsentOnAccept(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      boolean shouldHaveConsent) {

    sina.changePatientInsuranceType(insuranceType);
    sina.attemptsTo(EnsureConsent.shouldBeSet(shouldHaveConsent));

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(assignmentKind)
                .withRandomKbvBundle());
    doctor.attemptsTo(
        Verify.that(activation)
            .withExpectedType(ErpAfos.A_19022)
            .hasResponseWith(returnCode(200))
            .and(isInReadyStatus())
            .isCorrect());

    val acceptance =
        flughafenApo.performs(AcceptPrescription.forTheTask(activation.getExpectedResponse()));

    flughafenApo.attemptsTo(
        Verify.that(acceptance)
            .withExpectedType(ErpAfos.A_19166)
            .hasResponseWith(returnCode(200))
            .and(isInProgressStatus())
            .and(consentIsPresent(shouldHaveConsent))
            .isCorrect());

    // cleanup
    flughafenApo.performs(ClosePrescription.acceptedWith(acceptance));
  }

  @TestcaseId("ERP_CONSENT_POST_02")
  @ParameterizedTest(
      name =
          "Prüfe, dass ein Consent ohne DateTime akzeptiert wird, aber keine 500 zurück liefert,"
              + " wenn vorher ein Consent gesetzt war: {0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("Prüfe, Consent ohne DateTime")
  void setConsentWithoutDateTime(boolean ensureConsentIsUnset) {
    val consent = ErxConsentBuilder.forKvnr(fridolin.getKvnr()).build();
    consent.setDateTime(null);
    val consentDeposit =
        fridolin.performs(
            GrantConsent.forOneSelf()
                .ensureConsentIsUnset(ensureConsentIsUnset)
                .withConsent(consent));
    fridolin.attemptsTo(
        Verify.that(consentDeposit)
            .withoutBody() // body wird bewusst nicht validiert, da er nicht FHIR konform ist
            .hasResponseWith( // returncode kann bei gesetztem Consent eine 409 sein, wenn schon ein
                // Consent gesetzt war
                returnCodeIsBetween(200, 499, ErpAfos.A_22351))
            .isCorrect());
  }

  @TestcaseId("ERP_CONSENT_POST_03")
  @Test
  @DisplayName(
      "Prüfe, dass die KVNR im Authorization-Header mit der in Consent.patient.identifier"
          + " übereinstimmt")
  void setConsentWithAlternativIdentifierKvnr() {
    val rejection = fridolin.performs(RejectConsent.forOneSelf().buildValid());
    fridolin.attemptsTo(Verify.that(rejection).withoutBody().isCorrect());
    val consent = ErxConsentBuilder.forKvnr(sina.getKvnr()).build();
    val consentDeposit =
        fridolin.performs(
            GrantConsent.forOneSelf().ensureConsentIsUnset(true).withConsent(consent));

    fridolin.attemptsTo(
        Verify.that(consentDeposit)
            .withOperationOutcome(ErpAfos.A_22289)
            .hasResponseWith(returnCode(403))
            .isCorrect());
  }

  @TestcaseId("ERP_CONSENT_POST_04")
  @Test
  @DisplayName("Rollenprüfung beim hinterlegen eines Consent als Apotheke")
  void setConsentAsPharmacy() {
    flughafenApo.attemptsTo(
        Verify.that(
                flughafenApo.performs(
                    GrantConsent.withKvnrAndDecideIsUnset(KVNR.random(), false)
                        .withDefaultConsent()))
            .withOperationOutcome(ErpAfos.A_22161)
            .and(
                operationOutcomeContainsInDetailText(
                    "endpoint is forbidden for professionOID", ErpAfos.A_22161))
            .isCorrect());
  }

  @TestcaseId("ERP_CONSENT_POST_05")
  @Test
  @DisplayName("Rollenprüfung beim hinterlegen eines Consent als Arzt:in")
  void setConsentAsDoctor() {
    doctor.attemptsTo(
        Verify.that(
                doctor.performs(
                    GrantConsent.withKvnrAndDecideIsUnset(KVNR.random(), false)
                        .withDefaultConsent()))
            .withOperationOutcome(ErpAfos.A_22161)
            .and(
                operationOutcomeContainsInDetailText(
                    "endpoint is forbidden for professionOID", ErpAfos.A_22161))
            .isCorrect());
  }

  @TestcaseId("ERP_CONSENT_POST_06")
  @Test
  @DisplayName("Prüfe, das das Consent je Versichertem nur einmal hinterlegt werden kann")
  void shouldNotSetConsentTwice() {
    fridolin.attemptsTo(EnsureConsent.shouldBeSet(true));
    val secondDeposit = fridolin.performs(GrantConsent.forOneSelf().withDefaultConsent());
    fridolin.attemptsTo(
        Verify.that(secondDeposit)
            .withOperationOutcome(ErpAfos.A_22162)
            .andResponse(returnCode(409))
            .and(
                operationOutcomeContainsInDetailText(
                    "CHARGCONS consent already exists for this kvnr", ErpAfos.A_22162))
            .isCorrect());
  }

  @TestcaseId("ERP_CONSENT_POST_07")
  @Test
  @DisplayName("Prüfe, das das Consent des Versicherten persistiert wird")
  void shouldPersistConsentDecision() {
    fridolin.performs(GrantConsent.forOneSelf().withDefaultConsent());
    val consentRequest = fridolin.performs(ReadConsent.forOneSelf());
    fridolin.attemptsTo(
        Verify.that(consentRequest)
            .withExpectedType()
            .and(ConsentBundleVerifier.hasConsent(ErpAfos.A_22350))
            .responseWith(returnCode(200))
            .isCorrect());
    fridolin.performs(RejectConsent.forOneSelf().withoutEnsureIsPresent());
    val consentRequest2 = fridolin.performs(ReadConsent.forOneSelf());

    fridolin.attemptsTo(
        Verify.that(consentRequest2)
            .withExpectedType()
            .and(ConsentBundleVerifier.hasNoConsent(ErpAfos.A_22158))
            .responseWith(returnCode(200))
            .isCorrect());
  }
}
