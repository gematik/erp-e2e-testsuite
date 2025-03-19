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
 */

package de.gematik.test.erezept.fhir.valuesets.dav;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.bbriccs.fhir.coding.exceptions.InvalidValueSetException;
import org.junit.jupiter.api.Test;

class ZusatzattributGruppeTest {

  @Test
  void testFromCode() {
    assertEquals(ZusatzattributGruppe.MARKT, ZusatzattributGruppe.fromCode("1"));
    assertEquals(ZusatzattributGruppe.RABATT, ZusatzattributGruppe.fromCode("2"));
    assertEquals(ZusatzattributGruppe.FAM, ZusatzattributGruppe.fromCode("3"));
    assertEquals(ZusatzattributGruppe.IMPORT_FAM, ZusatzattributGruppe.fromCode("4"));
    assertEquals(ZusatzattributGruppe.MEHRKOSTEN, ZusatzattributGruppe.fromCode("5"));
    assertEquals(ZusatzattributGruppe.WUNSCHARZNEIMITTEL, ZusatzattributGruppe.fromCode("6"));
    assertEquals(ZusatzattributGruppe.WIRKSTOFFVERORDNUNG, ZusatzattributGruppe.fromCode("7"));
    assertEquals(ZusatzattributGruppe.ERSATZVERORDNUNG, ZusatzattributGruppe.fromCode("8"));
    assertEquals(ZusatzattributGruppe.KUENSTLICHE_BEFRUCHTUNG, ZusatzattributGruppe.fromCode("9"));
    assertEquals(ZusatzattributGruppe.IMPORT_FERTIGARZNEI, ZusatzattributGruppe.fromCode("10"));
    assertEquals(ZusatzattributGruppe.ABGABE_NOTDIENST, ZusatzattributGruppe.fromCode("11"));
    assertEquals(
        ZusatzattributGruppe.ZUSAETZLICHE_ABGABEDATEN, ZusatzattributGruppe.fromCode("12"));
    assertEquals(ZusatzattributGruppe.GENEHMIGUNG, ZusatzattributGruppe.fromCode("13"));
    assertEquals(ZusatzattributGruppe.TARIFF_KENNZEICHEN, ZusatzattributGruppe.fromCode("14"));
    assertEquals(ZusatzattributGruppe.ZUZAHLUNGSSTATUS, ZusatzattributGruppe.fromCode("15"));
  }

  @Test
  void testInvalidValueSetException() {
    assertThrows(InvalidValueSetException.class, () -> ZusatzattributGruppe.fromCode("16"));
  }
}
