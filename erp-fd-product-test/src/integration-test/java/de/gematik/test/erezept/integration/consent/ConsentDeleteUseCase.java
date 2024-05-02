/*
 *  Copyright 2023 gematik GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package de.gematik.test.erezept.integration.consent;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIsBetween;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeContainsInDetailText;

import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.RejectConsent;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.ErpActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.usecases.search.ConsentDeleteBuilder;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Consent Loeschen")
public class ConsentDeleteUseCase extends ErpTest {
  @Actor(name = "Fridolin Straßer")
  private PatientActor fridolin;

  @Actor(name = "Am Flughafen")
  private PharmacyActor flughafenApo;

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  private static Stream<Arguments> actorComposer() {
    return ArgumentComposer.composeWith()
        .arguments(
            "Doctor",
            (Function<ErpTest, ErpActor>) (erpTest) -> erpTest.getDoctorNamed("Adelheid Ulmenwald"))
        .arguments(
            "Apotheke",
            (Function<ErpTest, ErpActor>) (erpTest) -> erpTest.getPharmacyNamed("Am Flughafen"))
        .create();
  }

  @TestcaseId("ERP_CONSENT_DELETE_01")
  @ParameterizedTest(
      name = "[{index}] -> Prüfe, dass DELETE_CONSENT nicht als {0} gesetzt werden kann")
  @DisplayName("Prüfe, dass DELETE_CONSENT nicht durch eine Apotheke möglich ist")
  @MethodSource("actorComposer")
  void deleteConsentAsLEShouldFail(String actorType, Function<ErpTest, ErpActor> actorProvider) {
    val actor = actorProvider.apply(this);
    actor.attemptsTo(
        Verify.that(actor.performs(RejectConsent.forOneSelf().withoutEnsureIsPresent()))
            .withOperationOutcome(ErpAfos.A_22155)
            .and(
                operationOutcomeContainsInDetailText(
                    "endpoint is forbidden for professionOID", ErpAfos.A_22155))
            .hasResponseWith(returnCodeIsBetween(400, 403))
            .isCorrect());
  }

  @TestcaseId("ERP_CONSENT_DELETE_02")
  @Test
  @DisplayName("Prüfe, dass DELETE_CONSENT ohne ?category abgelehnt wird")
  void deleteConsentWithoutCategoryIsForbidden() {
    val rejection =
        fridolin.performs(
            RejectConsent.forOneSelf()
                .withCustomCommand(ConsentDeleteBuilder.withCustomQuerySet().build(), false));
    fridolin.attemptsTo(
        Verify.that(rejection)
            .withOperationOutcome(ErpAfos.A_22154)
            .hasResponseWith(returnCode(405))
            .isCorrect());
  }

  @TestcaseId("ERP_CONSENT_DELETE_03")
  @Test
  @DisplayName("Prüfe, dass DELETE_CONSENT mit falschem Code in ?category abgelehnt wird")
  void deleteConsentWithWrongCodeIsForbidden() {
    val rejection =
        fridolin.performs(
            RejectConsent.forOneSelf()
                .withCustomCommand(ConsentDeleteBuilder.withCustomCategory("TEST"), true));
    fridolin.attemptsTo(
        Verify.that(rejection)
            .withOperationOutcome(ErpAfos.A_22154)
            .and(
                operationOutcomeContainsInDetailText(
                    "Category must be 'CHARGCONS'", ErpAfos.A_22874))
            .hasResponseWith(returnCode(400))
            .isCorrect());
  }

  @TestcaseId("ERP_CONSENT_DELETE_04")
  @Test
  @DisplayName("Prüfe, dass bei DELETE_CONSENT der Fachdienst das Consent löscht")
  void deleteConsent() {
    fridolin.performs(RejectConsent.forOneSelf().buildValid());
    val rejectionTwo = fridolin.performs(RejectConsent.forOneSelf().withoutEnsureIsPresent());
    fridolin.attemptsTo(
        Verify.that(rejectionTwo)
            .withOperationOutcome(ErpAfos.A_22158)
            .responseWith(returnCodeIsBetween(400, 405))
            .and(
                operationOutcomeContainsInDetailText(
                    "Could not find any consent for given KVNR", ErpAfos.A_22158))
            .isCorrect());
  }
}
