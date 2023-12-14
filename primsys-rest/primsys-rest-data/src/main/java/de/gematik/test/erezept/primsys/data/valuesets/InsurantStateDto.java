/*
 * Copyright 2023 gematik GmbH
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

/**
 * corresponding to WOP FHIR-ValueSet {@link
 * de.gematik.test.erezept.fhir.valuesets.VersichertenStatus}
 */
@Getter
@RequiredArgsConstructor
public enum InsurantStateDto {
  MEMBERS("1", "Mitglieder"),
  FAMILY_MEMBERS("3", "Familienangehoerige"),
  PENSIONER("5", "Rentner");

  private final String code;
  @JsonValue
  private final String display;

  @JsonCreator
  public static InsurantStateDto fromCode(String code) {
    return Arrays.stream(InsurantStateDto.values())
        .filter(
            it ->
                it.getCode().equals(code)
                    || it.getDisplay().equalsIgnoreCase(code)
                    || it.name().equalsIgnoreCase(code)
                    || it.name().replace("_", " ").equalsIgnoreCase(code))
        .findFirst()
        .orElseThrow(() -> new InvalidCodeValueException(InsurantStateDto.class, code));
  }
}
