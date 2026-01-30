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

package de.gematik.test.erezept.app.parsers;

import static de.gematik.test.erezept.app.parsers.ChargeItemParser.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.fhir.r4.dav.DavInvoice;
import de.gematik.test.erezept.fhir.r4.dav.DavPkvAbgabedatenBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxChargeItemBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChargeItemParserTest extends ErpFhirParsingTest {

  private final String basePathValid = "fhir/valid/erp/1.4.0/chargeitembundle/";

  @ParameterizedTest
  @CsvSource(
      value = {"Heute, 00:00;bundle_01.json", "Heute, 00:00;bundle_02.xml"},
      delimiter = ';')
  void shouldReturnCorrectHandedOverDate(String expectedHandedOverDate, String fileName) {
    val erxChargeItemBundle =
        getDecodedFromPath(ErxChargeItemBundle.class, basePathValid + fileName);
    val actualHandedOverDate = getExpectedHandedOverDate(erxChargeItemBundle);

    assertEquals(expectedHandedOverDate, actualHandedOverDate);
  }

  @ParameterizedTest
  @CsvSource(
      value = {"Apotheke Sally Mander;bundle_01.json", "Adler-Apotheke;bundle_02.xml"},
      delimiter = ';')
  void shouldReturnCorrectEnterer(String expectedEnterer, String fileName) {
    val erxChargeItemBundle =
        getDecodedFromPath(ErxChargeItemBundle.class, basePathValid + fileName);
    val actualEnterer = getExpectedEnterer(erxChargeItemBundle);

    assertEquals(expectedEnterer, actualEnterer);
  }

  @ParameterizedTest
  @CsvSource(
      value = {"Heute, 23:40;bundle_01.json", "Heute, 04:13;bundle_02.xml"},
      delimiter = ';')
  void shouldHaveTheSameEnteredDate(String expectedEnteredDate, String fileName) {
    val erxChargeItemBundle =
        getDecodedFromPath(ErxChargeItemBundle.class, basePathValid + fileName);
    val actualEnteredDate = getExpectedEnteredDate(erxChargeItemBundle);

    assertEquals(expectedEnteredDate, actualEnteredDate);
  }

  @ParameterizedTest
  @CsvSource(
      value = {"407,53\u00A0€;bundle_01.json", "51,48\u00A0€;bundle_02.xml"},
      delimiter = ';')
  void shouldHaveTheSamePrice(String expectedPrice, String fileName) {
    val erxChargeItemBundle =
        getDecodedFromPath(ErxChargeItemBundle.class, basePathValid + fileName);
    val actualPrice = getExpectedPrice(erxChargeItemBundle);

    assertEquals(expectedPrice, actualPrice);
  }

  @Test
  void shouldParseThePriceWithOneCentCharacter() {
    val erxChargeItemMock = mock(ErxChargeItemBundle.class);
    val davPkvAbgabedatenBundleMock = mock(DavPkvAbgabedatenBundle.class);
    val davInvoiceMock = mock(DavInvoice.class);

    when(erxChargeItemMock.getAbgabedatenBundle()).thenReturn(davPkvAbgabedatenBundleMock);
    when(davPkvAbgabedatenBundleMock.getInvoice()).thenReturn(davInvoiceMock);
    when(davInvoiceMock.getTotalPrice()).thenReturn(1.4f);

    val actualPrice = getExpectedPrice(erxChargeItemMock);

    assertEquals("1,40\u00A0€", actualPrice);
  }
}
