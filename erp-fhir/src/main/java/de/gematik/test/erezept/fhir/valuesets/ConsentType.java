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

package de.gematik.test.erezept.fhir.valuesets;

import de.gematik.test.erezept.fhir.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowCodeSystem;
import java.util.Arrays;
import lombok.Getter;
import lombok.NonNull;

@Getter
public enum ConsentType implements IValueSet {
  CHARGCONS("CHARGCONS", "Consent for saving electronic charge item", "N/A");

  public static final ErpWorkflowCodeSystem CODE_SYSTEM = ErpWorkflowCodeSystem.CONSENT_TYPE;
  public static final String VERSION = "1.1.0";
  public static final String DESCRIPTION = "Type of Consents for the ePrescription";
  public static final String PUBLISHER = "gematik GmbH";

  private final String code;
  private final String display;
  private final String definition;

  ConsentType(String code, String display, String definition) {
    this.code = code;
    this.display = display;
    this.definition = definition;
  }

  @Override
  public ErpWorkflowCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static ConsentType fromCode(@NonNull String code) {
    return Arrays.stream(ConsentType.values())
        .filter(pt -> pt.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(ConsentType.class, code));
  }
}
