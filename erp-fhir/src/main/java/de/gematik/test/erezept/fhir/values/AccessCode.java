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

package de.gematik.test.erezept.fhir.values;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import org.hl7.fhir.r4.model.Identifier;

public class AccessCode extends Value<String> {

  public AccessCode(final String accessCode) {
    super(ErpWorkflowNamingSystem.ACCESS_CODE, accessCode);
  }

  public boolean isValid() {
    // Note: the entropy of the actual access-code is not calculated for A_19021 here
    return this.getValue().matches("[0-9a-f]{64}");
  }

  public static boolean isAccessCode(Identifier identifier) {
    return isAccessCode(identifier.getSystem());
  }

  public static boolean isAccessCode(String system) {
    return system.equals(ErpWorkflowNamingSystem.ACCESS_CODE.getCanonicalUrl())
        || system.equals(ErpWorkflowNamingSystem.ACCESS_CODE_121.getCanonicalUrl());
  }

  public static AccessCode fromString(String accessCode) {
    return new AccessCode(accessCode);
  }

  public static AccessCode random() {
    return GemFaker.fakerAccessCode();
  }
}
