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

package de.gematik.test.erezept.fhir.parser.profiles;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.util.*;
import lombok.*;
import org.junit.jupiter.api.*;

class ProfileExtractorTest {

  @Test
  void shouldExtractProfileFromChargeItemBundle() {
    val content =
        ResourceUtils.readFileFromResource(
            "fhir/valid/erp/1.2.0/chargeitembundle/a05a235a-a214-11ed-a8fc-0242ac120002.xml");
    val p = ProfileExtractor.extractProfile(content);
    assertFalse(p.isEmpty());
  }

  @Test
  void shouldNotFailOnNoProfiles() {
    val content = ResourceUtils.readFileFromResource("fhir/invalid/no_profiles_bundle.xml");
    val p = ProfileExtractor.extractProfile(content);
    assertTrue(p.isEmpty());
  }

  @Test
  void shouldNotFailOnNoMetaTags() {
    val content = ResourceUtils.readFileFromResource("fhir/invalid/no_metas_bundle.xml");
    val p = ProfileExtractor.extractProfile(content);
    assertTrue(p.isEmpty());
  }

  @Test
  void constructorShouldNotBeCallable() {
    assertTrue(PrivateConstructorsUtil.throwsInvocationTargetException(ProfileExtractor.class));
  }
}
