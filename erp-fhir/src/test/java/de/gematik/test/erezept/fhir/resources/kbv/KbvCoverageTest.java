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

package de.gematik.test.erezept.fhir.resources.kbv;

import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;

class KbvCoverageTest {

  @Test
  void shouldGetFromResource() {
    val resource = new Coverage();
    val kbvCoverage = KbvCoverage.fromCoverage((Resource) resource);
    assertNotNull(kbvCoverage);
    assertNotEquals(resource, kbvCoverage);
  }

  @Test
  void shouldNotCast() {
    val resource = new Coverage();
    val kbvCoverage = KbvCoverage.fromCoverage(resource);
    val kbvCoverage2 = KbvCoverage.fromCoverage(kbvCoverage);
    assertEquals(kbvCoverage, kbvCoverage2);
  }
}
