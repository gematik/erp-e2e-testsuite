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

package de.gematik.test.erezept.primsys.data.valuesets;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import de.gematik.test.erezept.primsys.exceptions.InvalidCodeValueException;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** corresponding to FHIR-ValueSet {@link de.gematik.test.erezept.fhir.valuesets.PayorType} */
@Getter
@RequiredArgsConstructor
public enum PayorTypeDto {
  SKT("Sonstige Kostenträger"),
  UK("Unfallkassen");

  @JsonValue private final String display;

  @JsonCreator
  public static PayorTypeDto fromCode(String code) {
    return Arrays.stream(PayorTypeDto.values())
        .filter(it -> it.name().equalsIgnoreCase(code) || it.getDisplay().equalsIgnoreCase(code))
        .findFirst()
        .orElseThrow(() -> new InvalidCodeValueException(PayorTypeDto.class, code));
  }
}
