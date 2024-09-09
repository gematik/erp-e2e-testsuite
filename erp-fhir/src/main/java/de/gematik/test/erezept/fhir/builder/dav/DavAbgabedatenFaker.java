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

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.parser.profiles.version.AbdaErpPkvVersion;
import de.gematik.test.erezept.fhir.resources.dav.DavAbgabedatenBundle;
import de.gematik.test.erezept.fhir.resources.dav.DavDispensedMedication;
import de.gematik.test.erezept.fhir.resources.dav.DavInvoice;
import de.gematik.test.erezept.fhir.resources.dav.PharmacyOrganization;
import de.gematik.test.erezept.fhir.util.Currency;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.fhir.valuesets.dav.KostenVersicherterKategorie;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;
import org.hl7.fhir.r4.model.Invoice;
import org.hl7.fhir.r4.model.MedicationDispense;

public class DavAbgabedatenFaker {
  private final PrescriptionId prescriptionId;
  private final Map<String, Consumer<DavAbgabedatenBuilder>> builderConsumers = new HashMap<>();

  private DavAbgabedatenFaker(PrescriptionId prescriptionId) {
    this.prescriptionId = prescriptionId;
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
    val pharmacy = PharmacyOrganizationFaker.builder().fake();
    val dispensedMedication =
        DavDispensedMedicationBuilder.builder()
            .status(MedicationDispense.MedicationDispenseStatus.COMPLETED)
            .prescription(prescriptionId)
            .pharmacy(pharmacy)
            .invoice(invoice)
            .build();
    builderConsumers.put("pharmacy", b -> b.pharmacy(pharmacy));
    builderConsumers.put("invoice", b -> b.invoice(invoice));
    builderConsumers.put("medication", b -> b.medication(dispensedMedication));
  }

  public static DavAbgabedatenFaker builder() {
    val prescriptionId =
        PrescriptionId.random(
            GemFaker.randomElement(
                PrescriptionFlowType.FLOW_TYPE_200, PrescriptionFlowType.FLOW_TYPE_209));
    return new DavAbgabedatenFaker(prescriptionId);
  }

  public static DavAbgabedatenFaker builder(PrescriptionId prescriptionId) {
    return new DavAbgabedatenFaker(prescriptionId);
  }

  public DavAbgabedatenFaker withVersion(AbdaErpPkvVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public DavAbgabedatenFaker withDispensedMedication(
      DavDispensedMedication medication, PharmacyOrganization pharmacy, DavInvoice invoice) {
    builderConsumers.computeIfPresent("pharmacy", (key, defaultValue) -> b -> b.pharmacy(pharmacy));
    builderConsumers.computeIfPresent("invoice", (key, defaultValue) -> b -> b.invoice(invoice));
    builderConsumers.computeIfPresent(
        "medication", (key, defaultValue) -> b -> b.medication(medication));
    return this;
  }

  public DavAbgabedatenBundle fake() {
    return this.toBuilder().build();
  }

  public DavAbgabedatenBuilder toBuilder() {
    val builder = DavAbgabedatenBuilder.builder(prescriptionId);
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
