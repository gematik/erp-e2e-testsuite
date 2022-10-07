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
import de.gematik.test.erezept.fhir.parser.profiles.ErpCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import java.util.Arrays;
import lombok.Getter;
import lombok.NonNull;
import org.hl7.fhir.r4.model.Extension;

/** https://applications.kbv.de/S_KBV_PERSONENGRUPPE.xhtml */
@Getter
public enum PersonGroup implements IValueSet {
  NOT_SET("00", "Nicht gesetzt"),
  SOZ("04", "SOZ"),
  BVG("06", "BVG"),
  SVA_1("07", "SVA1"),
  SVA_2("08", "SVA2"),
  ASY("09", "ASY");

  public static final ErpCodeSystem CODE_SYSTEM = ErpCodeSystem.PERSON_GROUP;
  public static final String VERSION = "1.02";
  public static final String DESCRIPTION =
      "Personengruppe: kennzeichnet, zu welcher Personengruppe der Versicherte gehört (§ 264 SGB V). Die Angabe ist auf der EGK vorhanden und auf der KVK Teil des Feldes: Statusergänzung";
  public static final String PUBLISHER = "Kassenärztliche Bundesvereinigung";

  private final String code;
  private final String display;
  private final String definition = "N/A definition in profile";

  PersonGroup(String code, String display) {
    this.code = code;
    this.display = display;
  }

  @Override
  public ErpCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public Extension asExtension() {
    return new Extension(
        ErpStructureDefinition.GKV_PERSON_GROUP.getCanonicalUrl(), this.asCoding());
  }

  public static PersonGroup fromCode(@NonNull String coding) {
    return Arrays.stream(PersonGroup.values())
        .filter(mc -> mc.code.equals(coding))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(PersonGroup.class, coding));
  }
}
