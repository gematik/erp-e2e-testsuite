/*
 * Copyright (Change Date see Readme), gematik GmbH
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.fhir.builder.dav.*;
import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemFaker;
import de.gematik.test.erezept.fhir.r4.dav.DavInvoice;
import de.gematik.test.erezept.fhir.r4.dav.DavPkvAbgabedatenBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxChargeItemBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.util.Currency;
import de.gematik.test.erezept.fhir.valuesets.dav.KostenVersicherterKategorie;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.val;
import org.hl7.fhir.r4.model.Invoice;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChargeItemParserTest extends ErpFhirParsingTest {

  private static final String DEFAULT_HANDED_OVER_DATE = "2024-11-13";
  private static final String DEFAULT_ENTERED_DATE = "2024-11-13T10:15";
  private static final String DEFAULT_PHARMACY_NAME = "Apotheke Sally Mander";
  private static final float DEFAULT_TOTAL_PRICE = 100.00f;

  @ParameterizedTest
  @CsvSource(
      value = {"2024-11-13;Heute, 00:00", "2022-03-25;Heute, 00:00"},
      delimiter = ';')
  void shouldReturnCorrectHandedOverDate(String handedOverDate, String expectedHandedOverDate) {
    val bundle =
        getErxChargeItemBundle(
            handedOverDate, DEFAULT_ENTERED_DATE, DEFAULT_PHARMACY_NAME, DEFAULT_TOTAL_PRICE);

    val actualHandedOverDate = getExpectedHandedOverDate(bundle);

    assertEquals(expectedHandedOverDate, actualHandedOverDate);
  }

  @ParameterizedTest
  @CsvSource(
      value = {"Apotheke Sally Mander", "Adler-Apotheke"},
      delimiter = ';')
  void shouldReturnCorrectEnterer(String expectedEnterer) {
    val bundle =
        getErxChargeItemBundle(
            DEFAULT_HANDED_OVER_DATE, DEFAULT_ENTERED_DATE, expectedEnterer, DEFAULT_TOTAL_PRICE);

    val actualEnterer = getExpectedEnterer(bundle);

    assertEquals(expectedEnterer, actualEnterer);
  }

  @ParameterizedTest
  @CsvSource(
      value = {"2024-11-13T23:40;Heute, 23:40", "2022-03-25T04:13;Heute, 04:13"},
      delimiter = ';')
  void shouldHaveTheSameEnteredDate(String enteredDate, String expectedEnteredDate) {
    val bundle =
        getErxChargeItemBundle(
            DEFAULT_HANDED_OVER_DATE, enteredDate, DEFAULT_PHARMACY_NAME, DEFAULT_TOTAL_PRICE);

    val actualEnteredDate = getExpectedEnteredDate(bundle);

    assertEquals(expectedEnteredDate, actualEnteredDate);
  }

  @ParameterizedTest
  @CsvSource(
      value = {"407.53;407,53\u00A0€", "51.48;51,48\u00A0€"},
      delimiter = ';')
  void shouldHaveTheSamePrice(float totalPrice, String expectedPrice) {
    val bundle =
        getErxChargeItemBundle(
            DEFAULT_HANDED_OVER_DATE, DEFAULT_ENTERED_DATE, DEFAULT_PHARMACY_NAME, totalPrice);

    val actualPrice = getExpectedPrice(bundle);

    assertEquals(expectedPrice, actualPrice);
  }

  private static @NonNull ErxChargeItemBundle getErxChargeItemBundle(
      String handedOverDate, String enteredDate, String pharmacyName, float totalPrice) {
    val zone = ZoneId.systemDefault();
    val whenHandedOver = Date.from(LocalDate.parse(handedOverDate).atStartOfDay(zone).toInstant());
    val whenEntered = Date.from(LocalDateTime.parse(enteredDate).atZone(zone).toInstant());

    val pharmacy = PharmacyOrganizationFaker.builder().withName(pharmacyName).fake();
    val invoice =
        DavInvoiceBuilder.builder()
            .currency(Currency.EUR)
            .status(Invoice.InvoiceStatus.ISSUED)
            .addPriceComponent(
                PriceComponentBuilder.builder(KostenVersicherterKategorie.ZUZAHLUNG)
                    .currency(Currency.EUR)
                    .type(Invoice.InvoicePriceComponentType.INFORMATIONAL)
                    .insurantCost(0.0f)
                    .totalCost(totalPrice)
                    .build(),
                "12345678",
                "Testmedikament")
            .build();

    val abgabedatenFaker = DavPkvAbgabedatenFaker.builder();
    val defaultAbgabedaten = abgabedatenFaker.fake();
    val dispensedMedication =
        DavPkvDispensedMedicationBuilder.builder()
            .status(MedicationDispense.MedicationDispenseStatus.COMPLETED)
            .prescription(defaultAbgabedaten.getDispensedMedication().getPrescriptionId())
            .pharmacy(pharmacy)
            .invoice(invoice)
            .whenHandedOver(whenHandedOver)
            .build();
    val abgabedatenBundle =
        abgabedatenFaker.withDispensedMedication(dispensedMedication, pharmacy, invoice).fake();

    val chargeItem =
        ErxChargeItemFaker.builder()
            .withEnteredDate(whenEntered)
            .withAbgabedatensatz(abgabedatenBundle, b -> new byte[0])
            .fake();
    val bundle = new ErxChargeItemBundle();
    bundle.addEntry().setResource(chargeItem);
    bundle.addEntry().setResource(abgabedatenBundle);
    return bundle;
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
