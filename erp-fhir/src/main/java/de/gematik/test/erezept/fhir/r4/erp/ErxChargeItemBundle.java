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

package de.gematik.test.erezept.fhir.r4.erp;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.profiles.definitions.AbdaErpPkvStructDef;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.r4.dav.DavPkvAbgabedatenBundle;
import java.util.Optional;
import lombok.Getter;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ResourceType;

@Getter
@ResourceDef(name = "Bundle")
@SuppressWarnings({"java:S110"})
public class ErxChargeItemBundle extends Bundle {

  public ErxChargeItem getChargeItem() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(x -> x.getResourceType().equals(ResourceType.ChargeItem))
        .map(ErxChargeItem::fromChargeItem)
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(ErxChargeItem.class, ResourceType.ChargeItem));
  }

  public Optional<ErxReceipt> getReceipt() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Bundle))
        .filter(ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE::matches)
        .map(ErxReceipt::fromBundle)
        .findFirst();
  }

  public DavPkvAbgabedatenBundle getAbgabedatenBundle() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Bundle))
        .filter(AbdaErpPkvStructDef.PKV_ABGABEDATENSATZ::matches)
        .map(DavPkvAbgabedatenBundle.class::cast)
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    this.getClass(), AbdaErpPkvStructDef.PKV_ABGABEDATENSATZ));
  }
}
