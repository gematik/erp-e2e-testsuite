/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.core.expectations.verifier;

import de.gematik.test.core.expectations.requirements.FhirRequirements;
import java.util.function.Predicate;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;

public class FhirVerifier {

  private FhirVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  public static <T extends Resource> VerificationStep<T> hasIdentifier() {
    Predicate<T> predicate = Resource::hasId;
    val step =
        new VerificationStep.StepBuilder<T>(
            FhirRequirements.LOGICAL_ID, "Jede FHIR Resource MUSS eine ID haben");
    return step.predicate(predicate).accept();
  }
}
