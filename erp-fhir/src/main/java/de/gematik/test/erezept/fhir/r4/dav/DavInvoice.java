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

package de.gematik.test.erezept.fhir.r4.dav;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import de.gematik.test.erezept.fhir.extensions.dav.InvoiceId;
import de.gematik.test.erezept.fhir.profiles.definitions.AbdaErpBasisStructDef;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Invoice;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@Getter
@ResourceDef(name = "Invoice")
@SuppressWarnings({"java:S110"})
public class DavInvoice extends Invoice {

  public InvoiceId getInvoiceId() {
    return InvoiceId.fromId(this.getId());
  }

  public String getPzn() {
    return this.getPznOptional()
        .orElseThrow(
            () -> new MissingFieldException(DavInvoice.class, DeBasisProfilCodeSystem.PZN));
  }

  public Optional<String> getPznOptional() {
    return this.getLineItemFirstRep().getChargeItemCodeableConcept().getCoding().stream()
        .filter(DeBasisProfilCodeSystem.PZN::matches)
        .map(Coding::getCode)
        .findFirst();
  }

  public float getTotalPrice() {
    return this.getTotalGross().getValue().floatValue();
  }

  public float getTotalCoPayment() {
    return this.getTotalGross().getExtension().stream()
        .filter(AbdaErpBasisStructDef.GESAMTZUZAHLUNG::matches)
        .map(ext -> ext.getValue().castToMoney(ext.getValue()).getValue().floatValue())
        .findFirst()
        .orElse(0.0f);
  }

  public String getCurrency() {
    return this.getTotalGross().getCurrency();
  }

  /**
   * Value-added-tax / Mehrwertsteuer
   *
   * @return Tax percentage
   */
  public float getVAT() {
    return this.getLineItemFirstRep().getPriceComponentFirstRep().getExtension().stream()
        .filter(AbdaErpBasisStructDef.MWST_SATZ::matches)
        .findFirst()
        .map(ext -> Float.parseFloat(ext.getValue().primitiveValue()))
        .orElseThrow(
            () -> new MissingFieldException(DavInvoice.class, AbdaErpBasisStructDef.MWST_SATZ));
  }

  public List<InvoiceLineItemPriceComponentComponent> getPriceComponents() {
    return this.getLineItemFirstRep().getPriceComponent();
  }

  public static DavInvoice fromInvoice(Invoice adaptee) {
    val invoice = new DavInvoice();
    adaptee.copyValues(invoice);
    return invoice;
  }

  public static DavInvoice fromInvoice(Resource adaptee) {
    return fromInvoice((Invoice) adaptee);
  }
}
