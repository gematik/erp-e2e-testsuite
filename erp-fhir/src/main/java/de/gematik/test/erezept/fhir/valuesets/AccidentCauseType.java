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

import de.gematik.test.erezept.fhir.parser.profiles.ICodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvCodeSystem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccidentCauseType implements IValueSet {
  ACCIDENT("1", "Unfall"),
  ACCIDENT_AT_WORK("2", "Arbeitsunfall (Berufsgenossenschaft/Unfallkasse)"),
  //   SUPPLY_PROBLEMS("3", "Versorgungsleiden"), // not yet allowed by the profiles
  OCCUPATIONAL_DISEASE("4", "Berufskrankheit (Berufsgenossenschaft/Unfallkasse)");

  public static final KbvCodeSystem CODE_SYSTEM = KbvCodeSystem.URSACHE_TYPE;
  public static final String VERSION = "1.01";
  public static final String DESCRIPTION = "Ausprägung der möglichen Unfallursache";
  public static final String PUBLISHER = "Kassenärztliche Bundesvereinigung";

  private final String code;
  private final String display;
  private final String definition = "N/A";

  @Override
  public ICodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }
}
