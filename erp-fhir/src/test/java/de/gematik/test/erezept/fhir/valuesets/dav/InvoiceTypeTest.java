/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.fhir.valuesets.dav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import de.gematik.test.erezept.fhir.exceptions.InvalidValueSetException;
import org.junit.Test;

public class InvoiceTypeTest {

  @Test
  public void testFromCode() {
    assertEquals(InvoiceType.ABRECHNUNGSZEILEN, InvoiceType.fromCode("Abrechnungszeilen"));
    assertEquals(InvoiceType.ABRECHNUNGSZEILEN, InvoiceType.fromCode("abrechnungsZeilen"));
    assertEquals(InvoiceType.ZUSATZDATENEINHEIT, InvoiceType.fromCode("ZusatzdatenEinheit"));
    assertEquals(InvoiceType.ZUSATZDATENEINHEIT, InvoiceType.fromCode("zusatzdateneinheit"));
  }

  @Test(expected = InvalidValueSetException.class)
  public void testInvalidValueSetException() {
    InvoiceType.fromCode("abcd");
  }

  @Test
  public void shouldHaveDefinition() {
    assertNotNull(InvoiceType.ABRECHNUNGSZEILEN.getDefinition());
  }
}