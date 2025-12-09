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

package de.gematik.test.erezept.app.mobile.elements;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class EVDGADetailsTest {

  @Test
  void shouldHaveLocatorForIos() {
    Arrays.stream(EVDGADetails.values())
        .map(EVDGADetails::getIosLocator)
        .forEach(element -> assertNotNull(element.get()));
  }

  @Test
  void shouldNotHaveLocatorsForAndroid() {
    Arrays.stream(EVDGADetails.values())
        .map(EVDGADetails::getAndroidLocator)
        .forEach(element -> assertNull(element.get()));
  }
}
