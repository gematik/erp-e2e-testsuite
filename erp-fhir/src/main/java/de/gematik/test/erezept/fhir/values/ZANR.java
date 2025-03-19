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
 */

package de.gematik.test.erezept.fhir.values;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import java.util.Arrays;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;

/** <a href="https://de.wikipedia.org/wiki/Lebenslange_Arztnummer">Lebenslange Zahnarztnummer</a> */
public class ZANR extends BaseANR {

  @RequiredArgsConstructor
  private enum ADDITIONAL_VALID_NUMBERS {
    PSEUDO_NUMBER_REHAB(Pattern.compile("^4{7}\\d{2}$")),
    AMBULANCES_HOSPITAL(Pattern.compile("^9{7}00$")),
    PRESCRIPTIONS_SUPPLY(Pattern.compile("^5{7}\\d{2}$")),
    EXCEPTION_PRESCRIPTIONS_SUPPLY(Pattern.compile("^0{9}$")),
    DENTISTS(Pattern.compile("^9{7}91$")),
    ;

    private final Pattern pattern;

    static boolean isValid(String value) {
      return Arrays.stream(values()).anyMatch(it -> it.pattern.matcher(value).matches());
    }
  }

  public ZANR(String value) {
    super(ANRType.ZANR, value);
  }

  public static ZANR random() {
    return new ZANR(GemFaker.fakerZanr());
  }

  @Override
  public boolean isValid() {
    return ADDITIONAL_VALID_NUMBERS.isValid(getValue()) || super.isValid();
  }
}
