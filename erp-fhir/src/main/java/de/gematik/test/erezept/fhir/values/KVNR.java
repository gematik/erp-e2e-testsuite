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

package de.gematik.test.erezept.fhir.values;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import lombok.val;

/**
 * <a href="https://de.wikipedia.org/wiki/Krankenversichertennummer">Krankenversichertennummer</a>
 */
public class KVNR extends Value<String> {

  private static final Pattern KVNR_PATTERN = Pattern.compile("^([A-Z])(\\d{8})(\\d)$");

  private KVNR(String value) {
    super(DeBasisNamingSystem.KVID, value);
  }

  public boolean check() {
    return checkId(this.getValue());
  }
  
  public static KVNR random() {
    val faker = GemFaker.getFaker();
    val capLetter = faker.regexify("[A-Z]{1}").charAt(0);
    val numbers = GemFaker.getFaker().regexify("[0-9]{8}");
    val checkNum = getCalculateCheckNumber(capLetter, numbers);
    val value = format("{0}{1}{2}", capLetter, numbers, checkNum);
    return from(value);
  }

  public static KVNR from(String value) {
    return new KVNR(value);
  }

  public static boolean checkId(KVNR kvnr) {
    return checkId(kvnr.getValue());
  }

  public static boolean checkId(String kvnr) {
    if (kvnr == null) return false;

    val matcher = KVNR_PATTERN.matcher(kvnr);
    if (!matcher.matches()) return false;

    val checkNumber = Integer.parseInt(matcher.group(3));
    val calculated = getCalculateCheckNumber(matcher.group(0).charAt(0), matcher.group(2));

    return calculated == checkNumber;
  }

  /**
   * get the chunked KVNR without the check number and calculate the check number 
   * @param capLetter is the leading capital letter [A-Z]
   * @param numbers 8 random digits 
   * @return the calculated check number 
   */
  private static int getCalculateCheckNumber(char capLetter, String numbers) {
    // convert the leading letter to a two-digit number
    val letterValue = String.format("%02d", capLetter - 64);
    val rawNumber = format("{0}{1}", letterValue, numbers);

    val idx = new AtomicInteger();
    var sum = new AtomicInteger();
    rawNumber
            .chars()
            .map(asciiValue -> asciiValue - 48)
            .forEach(
                    value -> {
                      if (idx.getAndIncrement() % 2 == 1) value *= 2;
                      if (value > 9) value -= 9;
                      sum.addAndGet(value);
                    });
    return sum.get() % 10;
  }
}
