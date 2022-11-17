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

import static org.junit.Assert.assertTrue;

import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.util.Currency;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.Country;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.fhir.valuesets.dav.KostenVersicherterKategorie;
import lombok.val;
import org.junit.Test;

public class DavAbgabedatenBuilderTest extends ParsingTest {

  @Test
  public void buildDavAbgabedatenBundleWithFixedValues() {
    val prescriptionId = PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_200);

    val pharmacy =
        PharmacyOrganizationBuilder.builder()
            .name("Adler-Apotheke")
            .iknr("757299999")
            .address(Country.D, "Berlin", "10623", "Wegelystraße 3")
            .build();

    val pc1 =
        PriceComponentBuilder.builder(KostenVersicherterKategorie.ZUZAHLUNG)
            .currency(Currency.EUR) // EUR by default
            .type("informational")
            .insurantCost(5.8f)
            .totalCost(289.99f)
            .build();

    val invoice =
        DavInvoiceBuilder.builder()
            .currency(Currency.EUR) // EUR by default
            .status("issued")
            .vatRate(19.0f)
            .addPriceComponent(pc1, "12345678")
            .build();

    val dispensedMedication =
        DavDispensedMedicationBuilder.builder()
            .status("completed")
            .prescription(prescriptionId)
            .pharmacy(pharmacy)
            .invoice(invoice)
            .build();

    val davBundle =
        DavAbgabedatenBuilder.builder(prescriptionId)
            .pharmacy(pharmacy)
            .medication(dispensedMedication)
            .invoice(invoice)
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, davBundle);
    assertTrue(result.isSuccessful());
  }
}
