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

package de.gematik.test.erezept.fhir.values;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.coding.SemanticValue;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.profiles.systems.ErpWorkflowNamingSystem;
import java.util.Optional;
import org.hl7.fhir.r4.model.Identifier;

public class AccessCode extends SemanticValue<String, ErpWorkflowNamingSystem> {

  private AccessCode(String accessCode) {
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
    return ErpWorkflowNamingSystem.ACCESS_CODE.matches(system);
  }

  public static AccessCode from(Identifier identifier) {
    return Optional.of(identifier)
        .filter(ErpWorkflowNamingSystem.ACCESS_CODE::matches)
        .map(id -> AccessCode.from(id.getValue()))
        .orElseThrow(
            () ->
                new BuilderException(
                    format("Cannot extract AccessCode from {0}", identifier.getSystem())));
  }

  public static AccessCode from(String accessCode) {
    return new AccessCode(accessCode);
  }

  public static AccessCode random() {
    return from(GemFaker.fakerAccessCode());
  }
}
