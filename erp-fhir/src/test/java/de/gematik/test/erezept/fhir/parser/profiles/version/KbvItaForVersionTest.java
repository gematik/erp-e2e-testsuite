/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.fhir.parser.profiles.version;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

import de.gematik.test.erezept.fhir.parser.profiles.CustomProfiles;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

class KbvItaForVersionTest {

  @Test
  void getDefaultVersionViaCurrentDate() {
    val defaultVersion = KbvItaForVersion.getDefaultVersion();

    // Note: this assertion will break in the future!
    assertEquals(KbvItaForVersion.V1_1_0, defaultVersion);
  }

  @ParameterizedTest(name = "{index}: default version on {0} must be {1}")
  @MethodSource
  void getDefaultVersionViaSpecialDates(LocalDate edgeDate, KbvItaForVersion expected) {
    try (MockedStatic<LocalDate> localDate = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
      localDate.when(LocalDate::now).thenReturn(edgeDate);

      val defaultVersion = KbvItaForVersion.getDefaultVersion();
      assertEquals(expected, defaultVersion);
    }
  }

  static Stream<Arguments> getDefaultVersionViaSpecialDates() {
    return Stream.of(
        arguments(LocalDate.of(2023, 7, 31), KbvItaForVersion.V1_0_3),
        arguments(LocalDate.of(2023, 8, 1), KbvItaForVersion.V1_1_0));
  }

  @Test
  void getDefaultVersionViaSystemProperty() {
    val propertyName = CustomProfiles.KBV_ITA_FOR.getName();
    Arrays.stream(KbvItaForVersion.values())
        .forEach(
            version -> {
              System.setProperty(propertyName, version.getVersion());
              val defaultVersion = KbvItaForVersion.getDefaultVersion();
              assertEquals(version, defaultVersion);
            });
  }

  @AfterEach
  void cleanProperties() {
    val propertyName = CustomProfiles.KBV_ITA_FOR.getName();
    System.clearProperty(propertyName);
  }
}
