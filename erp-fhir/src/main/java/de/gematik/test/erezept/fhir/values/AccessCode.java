/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.fhir.values;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.parser.profiles.ErpNamingSystem;

public class AccessCode extends Value<String> {

  public AccessCode(final String accessCode) {
    super(ErpNamingSystem.ACCESS_CODE, accessCode);
  }

  public static AccessCode random() {
    return GemFaker.fakerAccessCode();
  }

  public boolean isValid() {
    // kal TODO: do we need to calculate the entropy of the actual access-code to ensure if it's
    // valid for A_19021?
    return this.getValue().matches("[0-9a-f]{64}");
  }
}
