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

package de.gematik.test.erezept.primsys.data.valuesets;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import de.gematik.test.erezept.primsys.exceptions.InvalidCodeValueException;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PatientInsuranceTypeDto {
  GKV("gesetzliche Krankenversicherung"),
  PKV("private Krankenversicherung");

  private final String display;

  @JsonValue
  public String getCode() {
    return this.name();
  }

  @JsonCreator
  public static PatientInsuranceTypeDto fromCode(String code) {
    return Arrays.stream(PatientInsuranceTypeDto.values())
        .filter(it -> it.getCode().equalsIgnoreCase(code))
        .findFirst()
        .orElseThrow(() -> new InvalidCodeValueException(InsuranceTypeDto.class, code));
  }
}
