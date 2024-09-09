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

package de.gematik.test.erezept.app.mobile;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import java.util.Arrays;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class PlatformTypeTest {

  @Test
  void shouldParseValidPlatforms() {
    val androids = List.of("android", "Android", "ANDROID");
    androids.forEach(a -> assertEquals(PlatformType.ANDROID, PlatformType.fromString(a)));

    val ioses = List.of("ios", "iOS", "IOS");
    ioses.forEach(a -> assertEquals(PlatformType.IOS, PlatformType.fromString(a)));

    val advs = List.of("adv", "ADV", "desktop", "Desktop", "DESKTOP");
    advs.forEach(a -> assertEquals(PlatformType.DESKTOP, PlatformType.fromString(a)));
  }

  @Test
  void shouldThrowOnInvalidPlatforms() {
    val invalids = List.of("androids", "apple", "mac", "linux", "windows", "ABCD", "");
    invalids.forEach(
        invalid ->
            assertThrows(
                UnsupportedPlatformException.class, () -> PlatformType.fromString(invalid)));
  }

  @Test
  void shouldEqualFromString() {
    val androids = List.of("android", "Android", "ANDROID");
    androids.forEach(a -> assertTrue(PlatformType.ANDROID.is(a)));

    val ioses = List.of("ios", "iOS", "IOS");
    ioses.forEach(a -> assertTrue(PlatformType.IOS.is(a)));

    val advs = List.of("adv", "ADV", "desktop", "Desktop", "DESKTOP");
    advs.forEach(a -> assertTrue(PlatformType.DESKTOP.is(a)));
  }

  @Test
  void shouldNotEqualOnInvalidValues() {
    val values = List.of("Droid", "MacOs", "Palm", "Windows", "");
    Arrays.stream(PlatformType.values())
        .forEach(
            type -> {
              values.forEach(input -> assertFalse(type.is(input)));
            });
  }

  @Test
  void shouldNotEqualOnNull() {
    Arrays.stream(PlatformType.values()).forEach(type -> assertFalse(type.is(null)));
  }
}
