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

package de.gematik.test.erezept.fhir.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.fhir.util.IdentifierUtil;
import lombok.val;
import org.junit.jupiter.api.Test;

class IdentifierUtilTest {

  @Test
  void testResourcedId() {
    val id = "ef0db1ef-ed28-4d01-9b3c-599bcd9c7849";
    val input = "Coverage/" + id;
    assertEquals(id, IdentifierUtil.getUnqualifiedId(input));
  }

  @Test
  void testFullUrl() {
    val id = "ef0db1ef-ed28-4d01-9b3c-599bcd9c7849";
    val input = "http://pvs.praxis.local/fhir/Medication/" + id;
    assertEquals(id, IdentifierUtil.getUnqualifiedId(input));
  }

  @Test
  void testOnlyId() {
    val id = "ef0db1ef-ed28-4d01-9b3c-599bcd9c7849";
    assertEquals(id, IdentifierUtil.getUnqualifiedId(id));
  }
}
