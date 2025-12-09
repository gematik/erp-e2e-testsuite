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
import de.gematik.test.erezept.fhir.profiles.systems.GemErpEuNamingSystem;
import java.util.Optional;
import org.hl7.fhir.r4.model.Identifier;

public class EuAccessCode extends SemanticValue<String, GemErpEuNamingSystem> {

  private static final String BUILDING_PATTERN = "[a-zA-Z0-9]{6}";

  protected EuAccessCode(String euAccessCode) {
    super(GemErpEuNamingSystem.ACCESS_CODE, euAccessCode);
  }

  public boolean isValid() {
    return this.getValue().matches(BUILDING_PATTERN);
  }

  public static boolean isAccessCode(Identifier identifier) {
    return isAccessCode(identifier.getSystem());
  }

  public static boolean isAccessCode(String system) {
    return GemErpEuNamingSystem.ACCESS_CODE.matches(system);
  }

  public static EuAccessCode from(Identifier identifier) {
    return Optional.of(identifier)
        .filter(GemErpEuNamingSystem.ACCESS_CODE::matches)
        .map(id -> EuAccessCode.from(id.getValue()))
        .orElseThrow(
            () ->
                new BuilderException(
                    format("Cannot extract AccessCode from {0}", identifier.getSystem())));
  }

  public static EuAccessCode from(String accessCode) {
    return new EuAccessCode(accessCode);
  }

  public static EuAccessCode random() {
    return from(GemFaker.getFaker().regexify(BUILDING_PATTERN));
  }
}
