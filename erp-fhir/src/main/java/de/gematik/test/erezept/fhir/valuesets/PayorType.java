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

package de.gematik.test.erezept.fhir.valuesets;

import de.gematik.bbriccs.fhir.coding.FromValueSet;
import de.gematik.bbriccs.fhir.coding.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvCodeSystem;
import java.util.Arrays;
import lombok.Getter;
import org.hl7.fhir.r4.model.Coding;

@Getter
public enum PayorType implements FromValueSet {
  SKT("SKT", "Sonstige Kostenträger"),
  UK("UK", "Unfallkassen"),
  ;

  public static final KbvCodeSystem CODE_SYSTEM = KbvCodeSystem.PAYOR_TYPE;
  public static final String VERSION = "1.0.3";
  public static final String DESCRIPTION = "Zulässige Kostenträgerarten";
  public static final String PUBLISHER = "Kassenärztliche Bundesvereinigung";

  private final String code;
  private final String display;
  private final String definition;

  PayorType(String code, String display) {
    this.code = code;
    this.display = display;
    this.definition = "N/A definition in profile";
  }

  @Override
  public KbvCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static PayorType fromCode(Coding coding) {
    return fromCode(coding.getCode());
  }

  public static PayorType fromCode(String code) {
    return Arrays.stream(PayorType.values())
        .filter(pt -> pt.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(PayorType.class, code));
  }

  public static PayorType fromDisplay(String displayValue) {
    return Arrays.stream(PayorType.values())
        .filter(pt -> pt.display.toLowerCase().contains(displayValue.toLowerCase()))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(PayorType.class, displayValue));
  }
}
