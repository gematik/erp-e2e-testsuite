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

package de.gematik.test.erezept.fhir.builder.dav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import de.gematik.test.erezept.fhir.parser.profiles.definitions.AbdaErpBasisStructDef;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.util.Currency;
import de.gematik.test.erezept.fhir.valuesets.dav.KostenVersicherterKategorie;
import lombok.val;
import org.junit.Test;

public class DavInvoiceBuilderTest extends ParsingTest {

  @Test
  public void buildInvoiceWithFixedValues() {
    val pc1 =
        PriceComponentBuilder.builder(KostenVersicherterKategorie.ZUZAHLUNG)
            .currency(Currency.EUR) // EUR by default
            .type("informational")
            .insurantCost(5.8f)
            .totalCost(289.99f)
            .build();

    val pc2 =
        PriceComponentBuilder.builder()
            .type("informational")
            .insurantCost(4.2f)
            .totalCost(10.01f)
            .factor(200)
            .build();

    val invoice =
        DavInvoiceBuilder.builder()
            .currency(Currency.EUR) // EUR by default
            .status("issued")
            .vatRate(19.0f)
            .addPriceComponent(pc1, "12345678")
            .addPriceComponent(pc2, "87654321")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, invoice);
    assertTrue(result.isSuccessful());
    assertEquals(300.0, invoice.getTotalPrice(), 0.001);
    assertEquals(10.0, invoice.getTotalCoPayment(), 0.001);

    // make sure the VAT was set for each price component correctly
    invoice
        .getPriceComponents()
        .forEach(
            pc ->
                pc.getExtension().stream()
                    .filter(
                        ext ->
                            ext.getUrl().equals(AbdaErpBasisStructDef.MWST_SATZ.getCanonicalUrl()))
                    .map(
                        ext -> ext.getValue().castToDecimal(ext.getValue()).getValue().floatValue())
                    .forEach(vat -> assertEquals(19.0f, vat, 0.001)));
  }
}
