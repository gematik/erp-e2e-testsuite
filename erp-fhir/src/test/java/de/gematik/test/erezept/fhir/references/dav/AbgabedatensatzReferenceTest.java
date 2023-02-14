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

package de.gematik.test.erezept.fhir.references.dav;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.fhir.parser.profiles.definitions.AbdaErpPkvStructDef;
import lombok.val;
import org.junit.jupiter.api.Test;

class AbgabedatensatzReferenceTest {

  @Test
  void shouldHaveNonContainedId() {
    val id = "123";
    val ref = new AbgabedatensatzReference(id);
    assertEquals(id, ref.getReference());
    assertEquals(AbdaErpPkvStructDef.PKV_ABGABEDATENSATZ.getCanonicalUrl(), ref.getType());
  }

  @Test
  void shouldHaveContainedId() {
    val id = "123";
    val ref = new AbgabedatensatzReference(id);
    ref.makeContained();
    assertEquals(format("#{0}", id), ref.getReference());
    assertEquals("Binary", ref.getType());
  }

  @Test
  void shouldGetNonContainedId() {
    val id = "123";
    val ref = new AbgabedatensatzReference(id);
    ref.makeContained();
    assertEquals(id, ref.getReference(false));
    assertEquals("Binary", ref.getType());
  }

  @Test
  void shouldGetContainedId() {
    val id = "123";
    val ref = new AbgabedatensatzReference(id);
    ref.makeContained();
    assertEquals(format("#{0}", id), ref.getReference(true));
    assertEquals("Binary", ref.getType());
  }
}
