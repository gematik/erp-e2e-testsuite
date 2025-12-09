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

package de.gematik.test.erezept.toggle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import de.gematik.bbriccs.toggle.BooleanToggle;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BooleanToggleTest {

  @ParameterizedTest
  @MethodSource("booleanToggleInstances")
  <T extends BooleanToggle> void shouldHaveKey(T instance) {
    assertFalse(instance.getKey().isBlank());
    assertFalse(instance.getKey().isEmpty());
  }

  @ParameterizedTest
  @MethodSource("booleanToggleInstances")
  <T extends BooleanToggle> void shouldHaveDefaultValue(T instance) {
    assertDoesNotThrow(instance::getDefaultValue);
  }

  @ParameterizedTest
  @MethodSource("booleanToggleInstances")
  <T extends BooleanToggle> void shouldHaveConverter(T instance) {
    val converter = instance.getConverter();
    assertEquals(true, converter.apply("Yes"));
    assertEquals(true, converter.apply("true"));
    assertEquals(true, converter.apply("1"));
    assertEquals(false, converter.apply("0"));
  }

  static Stream<Arguments> booleanToggleInstances() {
    // maybe we can also use a reflection lib for this
    return Stream.of(
        Arguments.of(new AnrValidationConfigurationIsErrorToggle()),
        Arguments.of(new EgkPharmacyAcceptPN3Toggle()),
        Arguments.of(new FhirCloseSlicingToggle()),
        Arguments.of(new ErpDarreichungsformAprilActive()),
        Arguments.of(new EgkPharmacyEnforceHcvCheck()),
        Arguments.of(new ErpEnableCheckExclusionPayor()),
        Arguments.of(new PkiQesRsaEnableToggle()),
        Arguments.of(new RefenreceValidationActive()));
  }
}
