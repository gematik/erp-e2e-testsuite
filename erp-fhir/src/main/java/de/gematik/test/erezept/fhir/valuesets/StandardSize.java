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

package de.gematik.test.erezept.fhir.valuesets;

import de.gematik.test.erezept.fhir.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.DeBasisStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisCodeSystem;
import java.util.Arrays;
import lombok.Getter;
import lombok.NonNull;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Extension;

/** https://applications.kbv.de/S_KBV_NORMGROESSE_V1.00.xhtml */
@Getter
public enum StandardSize implements IValueSet {
  KA("KA", "Kein Angabe"),
  KTP("KTP", "Keine therapiegerechte Packungsgröße"),
  N1("N1", "Normgröße 1"),
  N2("N2", "Normgröße 2"),
  N3("N3", "Normgröße 3"),
  NB("NB", "Nicht betroffen"),
  SONSTIGES("Sonstiges", "Sonstiges");

  public static final DeBasisCodeSystem CODE_SYSTEM = DeBasisCodeSystem.NORMGROESSE;
  public static final String VERSION = "1.00";
  public static final String DESCRIPTION =
      "Bildet die zulässigen Normgrößen im Rahmen der elektronischen Arzneimittelverordnung ab.";
  public static final String PUBLISHER = "Kassenärztliche Bundesvereinigung";

  private final String code;
  private final String display;
  private final String definition = "N/A definition in profile";

  StandardSize(String code, String display) {
    this.code = code;
    this.display = display;
  }

  @Override
  public DeBasisCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public Extension asExtension() {
    return new Extension(
        DeBasisStructDef.NORMGROESSE.getCanonicalUrl(), new CodeType(this.getCode()));
  }

  public static StandardSize fromCode(@NonNull String coding) {
    return Arrays.stream(StandardSize.values())
        .filter(mc -> mc.code.equals(coding))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(StandardSize.class, coding));
  }
}
