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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * corresponding to WOP FHIR-ValueSet {@link de.gematik.test.erezept.fhir.valuesets.MedicationType}
 */
@Getter
@RequiredArgsConstructor
public enum MedicationTypeDto {
  PZN("PZN"),
  INGREDIENT("Wirkstoff"),
  FREETEXT("Freitext"),
  COMPOUNDING("Rezeptur");

  @JsonValue private final String display;

  @JsonCreator
  public static MedicationTypeDto fromCode(@NonNull String code) {
    return Arrays.stream(MedicationTypeDto.values())
        .filter(it -> it.display.equalsIgnoreCase(code) || it.name().equalsIgnoreCase(code))
        .findFirst()
        .orElseThrow(() -> new InvalidCodeValueException(MedicationTypeDto.class, code));
  }
}
