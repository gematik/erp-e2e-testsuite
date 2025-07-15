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

package de.gematik.test.erezept.primsys.rest.data;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.primsys.mapping.InvoiceDataMapper;
import java.util.List;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

class InvoiceDataTest extends ErpFhirBuildingTest {

  private final String testString =
      """
                {
                     "currency" : "EUR",
                     "vatRate" : 12.9,
                     "priceComponents" : [ {
                       "category" : "ZUZAHLUNG",
                       "type" : "informational",
                       "insurantCost" : 12.3,
                       "totalCost" : 75.5,
                       "costReason" : "issSo",
                       "pzn" : "123456789"
                     }, {
                     "category" : "ZUZAHLUNG",
                         "type" : "informational",
                         "insurantCost" : 110.3,
                         "totalCost" : 175.5,
                         "costReason" : "Weil immer noch",
                         "pzn" : "987654321"
                       } ]
                     }
                """;
  private final String testStringIncomplete =
      """
                  {

                       "vatRate" : 12.9,
                       "priceComponents" : [ {
                         "category" : "ZUZAHLUNG",
                         "insurantCost" : 12.3,
                         "totalCost" : 75.5,
                         "pzn" : "123456789"
                       }, {
                             "type" : "informational",
                             "insurantCost" : 110.3,
                             "totalCost" : 175.5,
                             "costReason" : "Weil immer noch",
                             "pzn" : "987654321"
                           } ]
                         }
                    """;

  @Test
  void shouldCreateInvoiceData() throws JsonProcessingException {
    val invoiceData = new InvoiceData();
    invoiceData.setCurrency("EUR");
    val priceComponent = new PriceComponentData();
    priceComponent.setType("informational");
    priceComponent.setCategory("ZUZAHLUNG");
    priceComponent.setInsurantCost(12.3f);
    priceComponent.setTotalCost(75.5f);
    priceComponent.setCostReason("issSo");
    priceComponent.setPzn("123456789");
    val priceComponent2 = new PriceComponentData();
    priceComponent2.setType("informational");
    priceComponent2.setInsurantCost(110.3f);
    priceComponent2.setTotalCost(175.5f);
    priceComponent2.setPzn("987654321");
    priceComponent2.setCostReason("Weil immer noch");
    invoiceData.setPriceComponents(List.of(priceComponent, priceComponent2));
    val mapper = new ObjectMapper().writerWithDefaultPrettyPrinter();
    assertDoesNotThrow(() -> mapper.writeValueAsString(invoiceData));
  }

  @Test
  void shouldMapCorrect() throws JsonProcessingException {
    val mapper = new ObjectMapper();
    val result = mapper.readValue(testString, InvoiceData.class);
    assertEquals("EUR", result.getCurrency());
    val priceComponentData = result.getPriceComponents().get(0);
    assertEquals(12.3f, priceComponentData.getInsurantCost());
  }

  @SneakyThrows
  @Test
  void shouldConvertCorrectToDavInvoice() {
    val mapper = new ObjectMapper();
    val result = mapper.readValue(testString, InvoiceData.class);
    val davInvoice = new InvoiceDataMapper(result).convert();
    assertTrue(davInvoice.hasId());
    assertTrue(davInvoice.hasStatus());
    assertEquals("INFORMATIONAL", davInvoice.getPriceComponents().get(0).getType().name());
  }

  @SneakyThrows
  @Test
  void shouldConvertCorrectIncompleteStringToDavInvoice() {
    val mapper = new ObjectMapper();
    val result = mapper.readValue(testStringIncomplete, InvoiceData.class);
    val davInvoice = new InvoiceDataMapper(result).convert();
    assertTrue(davInvoice.hasId());
    assertTrue(davInvoice.hasStatus());
    assertEquals("INFORMATIONAL", davInvoice.getPriceComponents().get(0).getType().name());
    assertEquals("EUR", davInvoice.getCurrency());
  }

  @Test
  void shouldFakeMissingAll() {
    val invoiceDataMapper = new InvoiceDataMapper(new InvoiceData());
    val davInvoice = invoiceDataMapper.convert();
    assertEquals("EUR", davInvoice.getCurrency());
  }

  @Test
  void shouldFakeMissingParts() {
    val invoiceData = new InvoiceData();
    invoiceData.setVatRate(12.23f);
    assertNull(invoiceData.getCurrency());
    val invoiceDataConverter = new InvoiceDataMapper(invoiceData);
    val davInvoice = invoiceDataConverter.convert();
    assertEquals("EUR", davInvoice.getCurrency());
    assertFalse(invoiceData.getPriceComponents().isEmpty());
  }
}
