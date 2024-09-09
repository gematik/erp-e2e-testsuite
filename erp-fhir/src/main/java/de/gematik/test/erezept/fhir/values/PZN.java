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

package de.gematik.test.erezept.fhir.values;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisCodeSystem;
import java.util.regex.Pattern;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

/** <a href="https://de.wikipedia.org/wiki/Pharmazentralnummer">Pharmazentralnummer</a> */
public class PZN extends Value<String> implements WithChecksum {
  private static final Pattern PZN_PATTERN = Pattern.compile("^\\d{8}$");

  private PZN(String value) {
    super(DeBasisCodeSystem.PZN, value);
  }

  public Coding asCoding() {
    val coding = new Coding();
    coding.setSystem(this.getSystemAsString()).setCode(this.getValue());
    return coding;
  }

  /**
   * With this method the PZN is wrapped up as a CodeableConcept with the given drugName
   *
   * @param drugName corresponding to the PZN
   * @return CodeableConcept with PZN and drug drugName
   */
  public CodeableConcept asNamedCodeable(String drugName) {
    val codeable = new CodeableConcept(asCoding());
    codeable.setText(drugName);
    return codeable;
  }

  /**
   * With this method the PZN is wrapped up as a CodeableConcept with a randomly generated name
   *
   * @return CodeableConcept with PZN and a random drug name
   */
  public CodeableConcept asNamedCodeable() {
    return asNamedCodeable(GemFaker.fakerDrugName());
  }

  public static PZN from(@NonNull String value) {
    return new PZN(value);
  }

  public static PZN random() {
    val faker = GemFaker.getFaker();
    String numbers;
    int checkNum;
    do {
      numbers = faker.regexify("[0-9]{7}");
      checkNum = calcChecksum(numbers);
    } while (checkNum == 10);
    val value = format("{0}{1}", numbers, checkNum);
    return from(value);
  }

  @Override
  public boolean isValid() {
    val pzn = getValue();
    if (pzn == null) return false;
    val matcher = PZN_PATTERN.matcher(pzn);
    if (!matcher.matches()) return false;

    val calcChecksum = calcChecksum(pzn);
    if (calcChecksum == 10) return false;
    return getChecksum() == calcChecksum;
  }

  private int getChecksum() {
    val value = getValue();
    return Character.getNumericValue(value.charAt(value.length() - 1));
  }

  protected static int calcChecksum(String pzn) {
    int sum = 0;
    for (int i = 0; i <= 6; i++) {
      val value = Character.getNumericValue(pzn.charAt(i)) * (i + 1);
      sum += value;
    }
    return sum % 11;
  }
}
