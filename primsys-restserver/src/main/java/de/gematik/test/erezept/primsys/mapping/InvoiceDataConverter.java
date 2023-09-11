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

package de.gematik.test.erezept.primsys.mapping;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.dav.DavInvoiceBuilder;
import de.gematik.test.erezept.fhir.builder.dav.PriceComponentBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.version.AbdaErpPkvVersion;
import de.gematik.test.erezept.fhir.resources.dav.DavInvoice;
import de.gematik.test.erezept.fhir.util.Currency;
import de.gematik.test.erezept.fhir.valuesets.dav.KostenVersicherterKategorie;
import de.gematik.test.erezept.primsys.rest.data.InvoiceData;
import de.gematik.test.erezept.primsys.rest.data.PriceComponentData;
import lombok.val;

import java.util.List;

import static de.gematik.test.erezept.primsys.utils.Strings.getOrDefault;

public class InvoiceDataConverter {

  private final InvoiceData invoiceData;

  public InvoiceDataConverter(InvoiceData invoiceData) {
    this.invoiceData = invoiceData;
    fakeMissing();
  }

  public InvoiceDataConverter() {
    this(new InvoiceData());
  }

  public DavInvoice convert() {
    val priceComponents = invoiceData.getPriceComponents();
    val davInvoiceBuilder =
        DavInvoiceBuilder.builder()
            .currency(Currency.valueOf(invoiceData.getCurrency()))
            .status("issued") // defaultValue
            .vatRate(invoiceData.getVatRate());

    addPriceComponents(davInvoiceBuilder, priceComponents);
    return davInvoiceBuilder.build();
  }

  private void addPriceComponents(
      DavInvoiceBuilder davInvoiceBuilder, List<PriceComponentData> priceComponents) {
    for (PriceComponentData pc : priceComponents) {
      val inLiItPc =
          PriceComponentBuilder.builder(KostenVersicherterKategorie.fromName(pc.getCategory()))
              .version(AbdaErpPkvVersion.getDefaultVersion())
              .currency(Currency.valueOf(invoiceData.getCurrency()))
              .type(pc.getType())
              .insurantCost(pc.getInsurantCost())
              .totalCost(pc.getTotalCost())
              .build();

      davInvoiceBuilder.addPriceComponent(inLiItPc, pc.getPzn(), pc.getCostReason());
    }
  }

  private void fakeMissing() {
    this.invoiceData.setCurrency(
        getOrDefault(this.invoiceData.getCurrency(), String.valueOf(Currency.EUR)));
    this.invoiceData.setVatRate(getOrDefault(this.invoiceData.getVatRate(), GemFaker::vatRate));
    var pcs = this.invoiceData.getPriceComponents();
    if (pcs.isEmpty()) {
      pcs.add(new PriceComponentData());
    }
    for (PriceComponentData pc : pcs) {
      pc.setCategory(getOrDefault(pc.getCategory(), "ZUZAHLUNG"));
      pc.setType(getOrDefault(pc.getType(), "informational"));
      pc.setCostReason(getOrDefault(pc.getCostReason(), "auch Apotheker müssen leben können"));
      pc.setPzn(getOrDefault(pc.getPzn(), GemFaker.fakerPzn()));
      pc.setTotalCost(getOrDefault(pc.getTotalCost(), GemFaker::cost));
      pc.setInsurantCost(
          getOrDefault(pc.getInsurantCost(), () -> GemFaker.cost(0.0f, pc.getTotalCost())));
    }
  }
}
