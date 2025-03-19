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

package de.gematik.test.core.expectations.verifier;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.core.expectations.requirements.RequirementsSet;
import de.gematik.test.erezept.fhir.r4.erp.ErxConsentBundle;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class ConsentBundleVerifier {
  private ConsentBundleVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  public static VerificationStep<ErxConsentBundle> containsKvnr(
      KVNR expected, RequirementsSet req) {
    Predicate<ErxConsentBundle> predicate =
        bundle -> {
          val value = bundle.getConsent().orElseThrow().getPatient().getIdentifier().getValue();
          log.debug("KVNR form Consent: {} vs. given KVNR: {}", value, expected.getValue());
          return value.equals(expected.getValue());
        };
    val step =
        new VerificationStep.StepBuilder<ErxConsentBundle>(
            req.getRequirement(),
            format(
                "Die KVNR im Consent entspricht nicht der übergebenen KVNR: {0}",
                expected.getValue()));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxConsentBundle> hasConsent(RequirementsSet req) {
    Predicate<ErxConsentBundle> predicate = ErxConsentBundle::hasConsent;
    val step =
        new VerificationStep.StepBuilder<ErxConsentBundle>(
            req.getRequirement(), "Es ist ein Consent hinterlegt");
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxConsentBundle> hasNoConsent(RequirementsSet req) {
    Predicate<ErxConsentBundle> predicate = bundle -> !bundle.hasConsent();
    val step =
        new VerificationStep.StepBuilder<ErxConsentBundle>(
            req.getRequirement(), "Es ist kein Consent hinterlegt");
    return step.predicate(predicate).accept();
  }
}
