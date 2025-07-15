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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/** corresponding to FHIR-ValueSet {@link de.gematik.test.erezept.fhir.valuesets.StandardSize} */
@Getter
@RequiredArgsConstructor
public enum StandardSizeDto {
  KA("KA", "Kein Angabe"),
  KTP("KTP", "Keine therapiegerechte Packungsgröße"),
  N1("N1", "Normgröße 1"),
  N2("N2", "Normgröße 2"),
  N3("N3", "Normgröße 3"),
  NB("NB", "Nicht betroffen"),
  SONSTIGES("Sonstiges", "Sonstiges");

  @JsonValue private final String code;
  private final String display;

  @JsonCreator
  public static StandardSizeDto fromCode(@NonNull String code) {
    return Arrays.stream(StandardSizeDto.values())
        .filter(
            it ->
                it.getCode().equals(code)
                    || it.name().equalsIgnoreCase(code)
                    || it.getDisplay().equalsIgnoreCase(code))
        .findFirst()
        .orElseThrow(() -> new InvalidCodeValueException(StandardSizeDto.class, code));
  }
}
