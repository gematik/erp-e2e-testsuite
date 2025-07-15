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

package de.gematik.test.erezept.fhir.values;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.coding.SemanticValue;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import lombok.SneakyThrows;
import lombok.val;
import org.hl7.fhir.r4.model.Extension;

public class AsvTeamNumber extends SemanticValue<String, DeBasisProfilNamingSystem> {

  private AsvTeamNumber(String value) {
    super(DeBasisProfilNamingSystem.ASV_TEAMNUMMER, value);
  }

  public static AsvTeamNumber from(String value) {
    return new AsvTeamNumber(value);
  }

  /**
   * Generates a random ASV team number in the format "00XXXXXXX", where X is a digit. The first two
   * digits are always "00". in normal case the 9.th digit is a checksum, but here we ignore it <a
   * href="https://www.kbv.de/html/8160.php">...</a> @ Abs. ASV-Teamnummer angeben
   *
   * @return A new instance of {@link AsvTeamNumber} with a random value.
   */
  @SneakyThrows
  public static AsvTeamNumber random() {
    val num = GemFaker.getFaker().regexify("[0-9]{7}");
    return new AsvTeamNumber(format("00{0}", num));
  }

  public Extension asExtension() {
    return new Extension(
        DeBasisProfilNamingSystem.ASV_TEAMNUMMER.getCanonicalUrl(), this.asCoding());
  }
}
