/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.fhir.valuesets.dav;

import de.gematik.test.erezept.fhir.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.parser.profiles.systems.AbdaCodeSystem;
import de.gematik.test.erezept.fhir.valuesets.IValueSet;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public enum MedicationDispenseType implements IValueSet {
  ABGABE("Abgabeinformationen", "MedicationDispense repräsentiert Abgabeinformationen"),
  ZUSATZ("ZusatzdatenHerstellung", "MedicationDispense repräsentiert ZusatzdatenHerstellung");

  public static final AbdaCodeSystem CODE_SYSTEM = AbdaCodeSystem.MEDICATIONDISPENSE_TYPE;
  public static final String VERSION = "1.2";
  public static final String DESCRIPTION = "Type of the dispensed medication";
  public static final String PUBLISHER = "DAV";

  private final String code;
  private final String display;

  @Override
  public String getDefinition() {
    return DESCRIPTION;
  }

  @Override
  public AbdaCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static MedicationDispenseType fromCode(@NonNull String code) {
    return Arrays.stream(MedicationDispenseType.values())
        .filter(mdt -> mdt.code.equalsIgnoreCase(code))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(MedicationDispenseType.class, code));
  }
}
