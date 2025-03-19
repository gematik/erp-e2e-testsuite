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

package de.gematik.test.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.hl7.fhir.r4.model.Coding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HelperTest {

  @Test
  void shouldCompareCodingCorrect() {
    var coding1 = new Coding();
    coding1.setCode("code1");
    coding1.setSystem("system1");

    var coding2 = new Coding();
    coding2.setCode("code1");
    coding2.setSystem("system1");

    var result = Helper.compareCodings(coding1, coding2);

    assertTrue(result);
  }

  @ParameterizedTest
  @CsvSource({
    "'code1' ,'system1', 'code2',  'system2'",
    "'code1' ,'system2', 'code2',  'system2'",
    "'code2' ,'system1', 'code2',  'system2'",
  })
  void shouldFailCompareCoding(String code1, String system1, String code2, String system2) {
    var coding1 = new Coding();
    coding1.setCode(code1);
    coding1.setSystem(system1);

    var coding2 = new Coding();
    coding2.setCode(code2);
    coding2.setSystem(system2);

    var result = Helper.compareCodings(coding1, coding2);

    assertFalse(result);
  }

  @Test
  void shouldFindSystem() {
    var coding1 = new Coding();
    coding1.setCode("code1");
    coding1.setSystem("system1");

    var coding2 = new Coding();
    coding2.setCode("code2");
    coding2.setSystem("system1");

    var coding3 = new Coding();
    coding3.setCode("code3");
    coding3.setSystem("system3");

    var codings = List.of(coding2, coding3);

    var result = Helper.findBySystem(coding1, codings);

    assertTrue(result.isPresent());
    assertEquals(coding2, result.get());
  }

  @Test
  void shouldFailWhileSearchingSystem() {
    var coding1 = new Coding();
    coding1.setCode("code1");
    coding1.setSystem("system1");

    var coding2 = new Coding();
    coding2.setCode("code2");
    coding2.setSystem("system2");

    var coding3 = new Coding();
    coding3.setCode("code3");
    coding3.setSystem("system3");

    var codings = List.of(coding2, coding3);

    var result = Helper.findBySystem(coding1, codings);

    assertTrue(result.isEmpty());
  }
}
