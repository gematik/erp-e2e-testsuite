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

package de.gematik.test.erezept.fhir.builder.dav;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.parser.profiles.definitions.*;
import de.gematik.test.erezept.fhir.parser.profiles.version.*;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.util.*;
import de.gematik.test.erezept.fhir.valuesets.dav.*;
import lombok.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class DavInvoiceBuilderTest extends ParsingTest {

  @ParameterizedTest(
      name = "[{index}] -> Build DAV DispensedMedication with E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#abdaErpPkvVersions")
  void buildInvoiceWithFixedValues(AbdaErpPkvVersion version) {
    val pc1 =
        PriceComponentBuilder.builder(KostenVersicherterKategorie.ZUZAHLUNG)
            .version(version)
            .currency(Currency.EUR) // EUR by default
            .type("informational")
            .insurantCost(5.8f)
            .totalCost(289.99f)
            .build();

    val pc2 =
        PriceComponentBuilder.builder()
            .version(version)
            .type("informational")
            .insurantCost(4.2f)
            .totalCost(10.01f)
            .factor(200)
            .build();

    val invoice =
        DavInvoiceBuilder.builder()
            .version(version)
            .currency(Currency.EUR) // EUR by default
            .status("issued")
            .vatRate(19.0f)
            .addPriceComponent(pc1, "11514676", "Amoxicillin/Clavulansure Heumann 875 mg/125 mg 10 St")
            .addPriceComponent(pc2, "87654321", "Asprin")
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
