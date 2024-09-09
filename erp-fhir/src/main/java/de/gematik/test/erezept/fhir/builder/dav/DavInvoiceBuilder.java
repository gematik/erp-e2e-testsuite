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

import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.extensions.dav.VatRate;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.AbdaErpBasisStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.AbdaErpPkvStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.AbdaErpPkvVersion;
import de.gematik.test.erezept.fhir.resources.dav.DavInvoice;
import de.gematik.test.erezept.fhir.util.Currency;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.valuesets.dav.InvoiceType;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import lombok.Data;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.Invoice;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.StringType;

public class DavInvoiceBuilder extends AbstractResourceBuilder<DavInvoiceBuilder> {

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

  public DavInvoiceBuilder currency(@NonNull Currency currency) {
    this.currency = currency;
    return self();
  }

  public DavInvoiceBuilder status(@NonNull String status) {
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

  public DavInvoice build() {
    val invoice = new DavInvoice();

    val profile =
        AbdaErpPkvStructDef.PKV_ABRECHNUNGSZEILEN.asCanonicalType(abdaErpPkvVersion, true);
    val meta = new Meta().setProfile(List.of(profile));

    // set FHIR-specific values provided by HAPI
    invoice.setId(this.getResourceId());
    invoice.setMeta(meta);

    invoice.setType(type.asCodeableConcept());
    invoice.setStatus(this.status);

    for (var i = 0; i < priceComponents.size(); i++) {
      val item = priceComponents.get(i);
      item.getPc().addExtension(vatRate);

      val lineItemComponent = invoice.addLineItem();
      lineItemComponent.setSequence(i + 1);
      lineItemComponent.addPriceComponent(item.getPc());
      val chargeItem = lineItemComponent.getChargeItemCodeableConcept();
      chargeItem.addCoding(item.pzn.asCoding());
      chargeItem.setText(item.text);

      if (item.hasBatch()) {
        lineItemComponent
            .addExtension()
            .setUrl(AbdaErpBasisStructDef.CHARGENBEZEICHNUNG.getCanonicalUrl())
            .setValue(new StringType(item.batchDesignation));
      }
    }

    val totalCost = calculateTotalCost();
    val insurantCost = calculateInsurantCost();

    val totalGross = invoice.getTotalGross();
    totalGross.setCurrency(currency.getCode()).setValueElement(Currency.asDecimalType(totalCost));
    totalGross.addExtension(DavExtensions.getGesamtZuzahlung(currency, insurantCost));

    return invoice;
  }

  private float calculateTotalCost() {
    return (float)
        priceComponents.stream()
            .map(PriceComponentData::getPc)
            .mapToDouble(pc -> pc.getAmount().getValue().doubleValue())
            .sum();
  }

  private float calculateInsurantCost() {
    return (float)
        priceComponents.stream()
            .map(PriceComponentData::getPc)
            .mapToDouble(
                pc ->
                    pc.getExtension().stream()
                        .filter(
                            ext ->
                                ext.getUrl()
                                    .equals(
                                        AbdaErpBasisStructDef.KOSTEN_VERSICHERTER
                                            .getCanonicalUrl()))
                        .map(ext -> ext.getExtensionByUrl("Kostenbetrag"))
                        .mapToDouble(
                            ext ->
                                ext.getValue().castToMoney(ext.getValue()).getValue().doubleValue())
                        .sum())
            .sum();
  }

  @Data
  private static class PriceComponentData {
    private final Invoice.InvoiceLineItemPriceComponentComponent pc;
    private final PZN pzn;
    private final String text;
    @Nullable private final String batchDesignation; // NOTE: batch not yet allowed for PKV

    public boolean hasBatch() {
      return this.batchDesignation != null;
    }
  }
}
