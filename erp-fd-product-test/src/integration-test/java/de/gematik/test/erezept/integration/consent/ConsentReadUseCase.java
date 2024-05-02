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

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIsBetween;

import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.verifier.ConsentBundleVerifier;
import de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.ReadConsent;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.ErpActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.tasks.EnsureConsent;
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
@DisplayName("Consent lesen")
public class ConsentReadUseCase extends ErpTest {

  @Actor(name = "Fridolin Straßer")
  private PatientActor fridolin;

  @Actor(name = "Am Flughafen")
  private PharmacyActor flughafenApo;

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  private static Stream<Arguments> actorComposer() {
    return ArgumentComposer.composeWith()
        .arguments(
            "Doctor:in",
            (Function<ErpTest, ErpActor>) (erpTest) -> erpTest.getDoctorNamed("Adelheid Ulmenwald"))
        .arguments(
            "Apotheker:in",
            (Function<ErpTest, ErpActor>) (erpTest) -> erpTest.getPharmacyNamed("Am Flughafen"))
        .create();
  }

  @TestcaseId("ERP_GET_CONSENT_01")
  @ParameterizedTest(
      name = "[{index}] -> Prüfe, dass GET_CONSENT nicht als {0} gelesen werden kann")
  @DisplayName("Prüfe, dass GET_CONSENT nicht durch eine Apotheke möglich ist")
  @MethodSource("actorComposer")
  void readConsentAsPharmaIsForbidden(String actorType, Function<ErpTest, ErpActor> actorProvider) {
    val actor = actorProvider.apply(this);
    val getConsentInteraction = actor.performs(ReadConsent.forOneSelf());
    actor.attemptsTo(
        Verify.that(getConsentInteraction)
            .withOperationOutcome(ErpAfos.A_22159)
            .hasResponseWith(returnCodeIsBetween(400, 409))
            .and(
                OperationOutcomeVerifier.operationOutcomeContainsInDetailText(
                    "endpoint is forbidden for professionOID", ErpAfos.A_22159))
            .isCorrect());
  }

  @TestcaseId("ERP_GET_CONSENT_02")
  @Test
  @DisplayName("Prüfe, dass GET_CONSENT das Consent des Aufrufers zurück liefert")
  void verifyConsentKvnr() {
    fridolin.attemptsTo(EnsureConsent.shouldBePresent());
    val consentResponse = fridolin.performs(ReadConsent.forOneSelf());
    fridolin.attemptsTo(
        Verify.that(consentResponse)
            .withExpectedType(ErpAfos.A_22160)
            .and(ConsentBundleVerifier.containsKvnr(fridolin.getKvnr(), ErpAfos.A_22160))
            .isCorrect());
  }
}
