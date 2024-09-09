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

package de.gematik.test.core.expectations.verifier.rawhttpverifier;

import de.gematik.test.core.expectations.requirements.PKIRequirements;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.erezept.actions.rawhttpactions.pki.TslListWrapper;
import java.util.function.Predicate;
import lombok.val;

public class TSLVerifier {

  private TSLVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  public static VerificationStep<TslListWrapper> containsX509Certs() {
    Predicate<TslListWrapper> predicate =
        lt -> !lt.getFilteredForFDSicAndEncX509Certificates().isEmpty();
    val step =
        new VerificationStep.StepBuilder<TslListWrapper>(
            PKIRequirements.COMPONENT_CA.getRequirement(),
            "Es ist mindestens ein X509-Zertifikat vorhanden");
    return step.predicate(predicate).accept();
  }
}
