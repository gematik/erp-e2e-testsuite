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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/** corresponding to WOP FHIR-ValueSet {@link de.gematik.test.erezept.fhir.valuesets.Wop} */
@Getter
@RequiredArgsConstructor
public enum WopDto {
  DUMMY("00", "Dummy bei eGK"),
  SCHLESWIG_HOLSTEIN("01", "Schleswig-Holstein"),
  HAMBURG("02", "Hamburg"),
  BREMEN("03", "Bremen"),
  NIEDERSACHSEN("17", "Niedersachsen"),
  WESTFALEN_LIPPE("20", "Westfalen-Lippe"),
  NORDRHEIN("38", "Nordrhein"),
  HESSEN("46", "Hessen"),
  KOBLENZ("47", "Koblenz"),
  RHEINHESSEN("48", "Rheinhessen"),
  PFALZ("49", "Pfalz"),
  TRIER("50", "Trier"),
  RHEINLAND_PFALZ("51", "Rheinland-Pfalz"),
  BADEN_WUERTTEMBERG("52", "Baden-Württemberg"),
  NORD_BADEN("55", "Nordbaden"),
  SUED_BADEN("60", "Südbaden"),
  NORD_WUERTTEMBERG("61", "Nordwürttemberg"),
  SUED_WUERTTEMBERG("62", "Südwürttemberg"),
  BAYERN("71", "Bayern"),
  BERLIN("72", "Berlin"),
  SAARLAND("73", "Saarland"),
  MECKLENBURG_VORPOMMERN("78", "Mecklenburg-Vorpommern"),
  BRANDENBURG("83", "Brandenburg"),
  SACHSEN_ANHALT("88", "Sachsen-Anhalt"),
  THUERINGEN("93", "Thüringen"),
  SACHSEN("98", "Sachsen");

  private final String code;
  private final String display;

  @JsonValue
  public String getDisplay() {
    return display;
  }

  @JsonCreator
  public static WopDto fromCode(@NonNull String code) {
    return Arrays.stream(WopDto.values())
        .filter(
            it ->
                it.getCode().equals(code)
                    || it.name().equalsIgnoreCase(code)
                    || it.getDisplay().equalsIgnoreCase(code))
        .findFirst()
        .orElseThrow(() -> new InvalidCodeValueException(WopDto.class, code));
  }
}
