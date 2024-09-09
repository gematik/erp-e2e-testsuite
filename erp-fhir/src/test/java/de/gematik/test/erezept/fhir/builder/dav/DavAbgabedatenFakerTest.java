/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.fhir.builder.dav;

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerPrescriptionId;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.parser.profiles.version.AbdaErpPkvVersion;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.util.Currency;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.valuesets.dav.KostenVersicherterKategorie;
import lombok.val;
import org.hl7.fhir.r4.model.Invoice;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.junit.jupiter.api.Test;

class DavAbgabedatenFakerTest extends ParsingTest {
  @Test
  void buildFakeDavAbgabedatenBundleWithVersion() {
    val davBundle =
        DavAbgabedatenFaker.builder().withVersion(AbdaErpPkvVersion.getDefaultVersion()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, davBundle);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeDavAbgabedatenBundle() {
    val pharmacy = PharmacyOrganizationFaker.builder().fake();
    val prescriptionId = fakerPrescriptionId();
    val invoiceBuilder =
        DavInvoiceBuilder.builder()
            .currency(Currency.EUR)
            .status(Invoice.InvoiceStatus.ISSUED)
            .vatRate(GemFaker.vatRate());

    val amountPriceComponents = GemFaker.fakerAmount(1, 5);
    for (var i = 0; i < amountPriceComponents; i++) {
      val insurantCost = GemFaker.cost();
      val totalCost = GemFaker.cost(insurantCost);
      invoiceBuilder.addPriceComponent(
          PriceComponentBuilder.builder(KostenVersicherterKategorie.ZUZAHLUNG)
              .currency(Currency.EUR)
              .type(Invoice.InvoicePriceComponentType.INFORMATIONAL)
              .insurantCost(insurantCost)
              .totalCost(totalCost)
              .build(),
          PZN.random().getValue(),
          GemFaker.fakerDrugName());
    }
    val invoice = invoiceBuilder.build();
    val dispensedMedication =
        DavDispensedMedicationBuilder.builder()
            .status(MedicationDispense.MedicationDispenseStatus.COMPLETED)
            .prescription(prescriptionId)
            .pharmacy(pharmacy)
            .invoice(invoice)
            .build();
    val davBundle =
        DavAbgabedatenFaker.builder(prescriptionId)
            .withDispensedMedication(dispensedMedication, pharmacy, invoice)
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, davBundle);
    assertTrue(result.isSuccessful());
  }
}
