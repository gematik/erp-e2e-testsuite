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

package de.gematik.test.erezept.app.cfg;

import static org.junit.Assert.*;

import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import java.util.List;
import lombok.val;
import org.junit.Test;

public class PlatformTypeTest {

  @Test
  public void shouldParseValidPlatforms() {
    val androids = List.of("android", "Android", "ANDROID");
    androids.forEach(a -> assertEquals(PlatformType.ANDROID, PlatformType.fromString(a)));

    val ioses = List.of("ios", "iOS", "IOS");
    ioses.forEach(a -> assertEquals(PlatformType.IOS, PlatformType.fromString(a)));

    val advs = List.of("adv", "ADV", "desktop", "Desktop", "DESKTOP");
    advs.forEach(a -> assertEquals(PlatformType.DESKTOP, PlatformType.fromString(a)));
  }

  @Test
  public void shouldThrowOnInvalidPlatforms() {
    val invalids = List.of("androids", "apple", "mac", "linux", "windows", "ABCD", "");
    invalids.forEach(
        invalid ->
            assertThrows(
                UnsupportedPlatformException.class, () -> PlatformType.fromString(invalid)));
  }
}
