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
 */

package de.gematik.test.erezept.fhir.builder.dav;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.extensions.dav.VatRate;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.AbdaErpBasisStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.AbdaErpPkvStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.AbdaErpPkvVersion;
import de.gematik.test.erezept.fhir.r4.dav.DavInvoice;
import de.gematik.test.erezept.fhir.util.Currency;
import de.gematik.test.erezept.fhir.valuesets.dav.InvoiceType;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import lombok.val;
import org.hl7.fhir.r4.model.Invoice;

public class DavInvoiceBuilder extends ResourceBuilder<DavInvoice, DavInvoiceBuilder> {

  private AbdaErpPkvVersion abdaErpPkvVersion = AbdaErpPkvVersion.getDefaultVersion();

  private final List<PriceComponentData> priceComponents;
  private final InvoiceType type;

  private VatRate vatRate = VatRate.defaultRate();
  private Currency currency = Currency.EUR;
  private Invoice.InvoiceStatus status = Invoice.InvoiceStatus.NULL;

  private DavInvoiceBuilder(InvoiceType type) {
    priceComponents = new LinkedList<>();
    this.type = type;
  }

  public static DavInvoiceBuilder builder() {
    return builder(InvoiceType.ABRECHNUNGSZEILEN);
  }

  public static DavInvoiceBuilder builder(InvoiceType type) {
    return new DavInvoiceBuilder(type);
  }

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public DavInvoiceBuilder version(AbdaErpPkvVersion version) {
    this.abdaErpPkvVersion = version;
    return this;
  }

  public DavInvoiceBuilder vatRate(float floatValue) {
    return vatRate(VatRate.from(floatValue));
  }

  public DavInvoiceBuilder vatRate(VatRate vatRate) {
    this.vatRate = vatRate;
    return self();
  }

  public DavInvoiceBuilder currency(Currency currency) {
    this.currency = currency;
    return self();
  }

  public DavInvoiceBuilder status(String status) {
    return status(Invoice.InvoiceStatus.fromCode(status.toLowerCase()));
  }

  public DavInvoiceBuilder status(Invoice.InvoiceStatus status) {
    this.status = status;
    return self();
  }

  public DavInvoiceBuilder addPriceComponent(
      Invoice.InvoiceLineItemPriceComponentComponent component, String pzn, String text) {
    this.priceComponents.add(new PriceComponentData(component, PZN.from(pzn), text, null));
    return self();
  }

  @Override
  public DavInvoice build() {
    val invoice =
        this.createResource(
            DavInvoice::new, AbdaErpPkvStructDef.PKV_ABRECHNUNGSZEILEN, abdaErpPkvVersion);

    invoice.setType(type.asCodeableConcept());
    invoice.setStatus(this.status);

    for (var i = 0; i < priceComponents.size(); i++) {
      val item = priceComponents.get(i);
      item.pc().addExtension(vatRate);

      val lineItemComponent = invoice.addLineItem();
      lineItemComponent.setSequence(i + 1);
      lineItemComponent.addPriceComponent(item.pc());
      val chargeItem = lineItemComponent.getChargeItemCodeableConcept();
      chargeItem.addCoding(item.pzn.asCoding()).setText(item.text);

      if (item.hasBatch()) {
        lineItemComponent.addExtension(
            AbdaErpBasisStructDef.CHARGENBEZEICHNUNG.asStringExtension(item.batchDesignation));
      }
    }

    val totalCost = calculateTotalCost();
    val insurantCost = calculateInsurantCost();

    val totalGross = currency.asMoney(totalCost);
    totalGross.addExtension(DavExtensions.getGesamtZuzahlung(currency, insurantCost));
    invoice.setTotalGross(totalGross);
    return invoice;
  }

  private float calculateTotalCost() {
    return (float)
        priceComponents.stream()
            .map(PriceComponentData::pc)
            .mapToDouble(pc -> pc.getAmount().getValue().doubleValue())
            .sum();
  }

  private float calculateInsurantCost() {
    return (float)
        priceComponents.stream()
            .map(PriceComponentData::pc)
            .mapToDouble(
                pc ->
                    pc.getExtension().stream()
                        .filter(AbdaErpBasisStructDef.KOSTEN_VERSICHERTER::matches)
                        .map(ext -> ext.getExtensionByUrl("Kostenbetrag"))
                        .mapToDouble(
                            ext ->
                                ext.getValue().castToMoney(ext.getValue()).getValue().doubleValue())
                        .sum())
            .sum();
  }

  /**
   * @param batchDesignation NOTE: batch not yet allowed for PKV
   */
  private record PriceComponentData(
      Invoice.InvoiceLineItemPriceComponentComponent pc,
      PZN pzn,
      String text,
      @Nullable String batchDesignation) {
    public boolean hasBatch() {
      return this.batchDesignation != null;
    }
  }
}
