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

package de.gematik.test.fuzzing.fhirfuzz.impl.stringtypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StringFuzzImplTest {
  static FuzzConfig fuzzConfig;
  static FuzzerContext fuzzerContext;
  static StringFuzzImpl stringFuzzer;

  @BeforeAll
  static void setup() {
    fuzzConfig = new FuzzConfig();
    fuzzerContext = new FuzzerContext(fuzzConfig);
    stringFuzzer = new StringFuzzImpl(fuzzerContext);
  }

  @Test
  void getContext() {
    assertNotNull(stringFuzzer.getContext());
  }

  @ParameterizedTest
  @CsvSource({
    "'http://aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 40 ",
    "'Http://www.bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb,cccccccccccccccccccccccccccccccccccccccccccccccc,mlknweoiflnkyxlknasdpo90ß32msc.,mpo0.,ascd',"
        + " 15",
    "'https://abcde123456ABCDE', 80",
    "'Https://1234566789', 70",
    "'https://abraCadabraundRumpelPumpel', 60",
  })
  void shouldFuzz(String s, float percent) {
    fuzzConfig.setPercentOfEach(percent);
    var org = s;
    var fuzzedStr = stringFuzzer.fuzz(s);
    assertNotEquals(org, fuzzedStr);
  }

  @ParameterizedTest
  @CsvSource({
    "'http://aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 0 ",
    "'Http://www.bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb,cccccccccccccccccccccccccccccccccccccccccccccccc,mlknweoiflnkyxlknasdpo90ß32msc.,mpo0.,ascd',"
        + " 0",
    "'https://abcde123456ABCDE', 0",
    "'Https://1234566789', 0",
    "'https://abraCadabraundRumpelPumpel', 0",
  })
  void shouldNotFuzz(String s, float percent) {
    fuzzConfig.setPercentOfEach(percent);
    var org = s;
    var fuzzedStr = stringFuzzer.fuzz(s);
    assertEquals(org, fuzzedStr);
  }

  @Test
  void shouldNotFuzz() {
    assertNull(stringFuzzer.fuzz(null));
  }

  @Test
  void shouldNotThrowExceptionAtNull() {
    var fuzzConf = new FuzzConfig();
    fuzzConf.setPercentOfEach(15.0f);
    var resp = stringFuzzer.fuzz(null);
    assertNull(resp);
  }

  @Test
  void shouldGenerateRandom() {
    assertNotNull(stringFuzzer.generateRandom(150));
  }
}
