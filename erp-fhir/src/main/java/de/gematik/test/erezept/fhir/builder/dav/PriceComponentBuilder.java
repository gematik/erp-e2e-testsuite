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

package de.gematik.test.erezept.fhir.builder.dav;

import de.gematik.bbriccs.fhir.builder.ElementBuilder;
import de.gematik.test.erezept.fhir.profiles.version.AbdaErpPkvVersion;
import de.gematik.test.erezept.fhir.util.Currency;
import de.gematik.test.erezept.fhir.valuesets.dav.KostenVersicherterKategorie;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Invoice;
import org.hl7.fhir.r4.model.Invoice.InvoiceLineItemPriceComponentComponent;

@Slf4j
public class PriceComponentBuilder
    extends ElementBuilder<InvoiceLineItemPriceComponentComponent, PriceComponentBuilder> {

  @SuppressWarnings("java:S1068") // will be used in the future!
  private AbdaErpPkvVersion abdaErpPkvVersion;

  private final KostenVersicherterKategorie category;
  private Invoice.InvoicePriceComponentType priceComponentType =
      Invoice.InvoicePriceComponentType.INFORMATIONAL;
  private float insurantCost = 0.0f;
  private Currency currency = Currency.EUR;
  private float totalCost;
  private long factor = 1L;

  private PriceComponentBuilder(KostenVersicherterKategorie category) {
    this.category = category;
  }

  public static PriceComponentBuilder builder(KostenVersicherterKategorie category) {
    return new PriceComponentBuilder(category);
  }

  public static PriceComponentBuilder builder() {
    return builder(KostenVersicherterKategorie.ZUZAHLUNG);
  }

  public PriceComponentBuilder version(AbdaErpPkvVersion version) {
    this.abdaErpPkvVersion = version;
    return self();
  }

  public PriceComponentBuilder type(String typeCode) {
    return type(Invoice.InvoicePriceComponentType.fromCode(typeCode));
  }

  public PriceComponentBuilder type(Invoice.InvoicePriceComponentType type) {
    this.priceComponentType = type;
    return self();
  }

  /**
   * Kostenbetrag des Versicherten
   *
   * @param cost of the insurant
   * @return Builder
   */
  public PriceComponentBuilder insurantCost(float cost) {
    this.insurantCost = cost;
    return self();
  }

  public PriceComponentBuilder totalCost(float cost) {
    this.totalCost = cost;
    return self();
  }

  public PriceComponentBuilder factor(long factor) {
    this.factor = factor;
    return self();
  }

  public PriceComponentBuilder currency(Currency currency) {
    this.currency = currency;
    return self();
  }

  @Override
  public Invoice.InvoiceLineItemPriceComponentComponent build() {
    checkRequired();
    val pc = new Invoice.InvoiceLineItemPriceComponentComponent();
    pc.setType(priceComponentType);
    pc.addExtension(DavExtensions.getInsurantCost(category, insurantCost, currency));

    val amount = pc.getAmount();
    amount.setCurrency(currency.getCode()).setValueElement(Currency.asDecimalType(totalCost));
    pc.setFactor(factor);

    return pc;
  }

  private void checkRequired() {
    if (!category.equals(KostenVersicherterKategorie.ZUZAHLUNG)) {
      log.warn(
          "Given {} is {} ({}) which might be not allowed by the profile!",
          KostenVersicherterKategorie.class.getSimpleName(),
          category,
          category.getDisplay());
    }
  }
}
