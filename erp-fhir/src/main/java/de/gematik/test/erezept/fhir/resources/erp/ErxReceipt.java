/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.fhir.resources.erp;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowCodeSystem;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.DocumentType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.*;

/**
 * @see <a href="https://simplifier.net/erezept-workflow/gemerxtask">Gem_erxTask</a>
 */
@Slf4j
@ResourceDef(name = "Bundle")
@SuppressWarnings({"java:S110"})
public class ErxReceipt extends Bundle {

  public static ErxReceipt fromBundle(Bundle adaptee) {
    val receipt = new ErxReceipt();
    adaptee.copyValues(receipt);
    return receipt;
  }

  public static ErxReceipt fromBundle(Resource adaptee) {
    return fromBundle((Bundle) adaptee);
  }

  public PrescriptionId getPrescriptionId() {
    return PrescriptionId.from(this.getIdentifier());
  }

  public DocumentType getDocumentType() {
    val docType =
        this.getComposition().getType().getCoding().stream()
            .filter(
                coding ->
                    ErpWorkflowCodeSystem.DOCUMENT_TYPE.match(coding.getSystem())
                        || ErpWorkflowCodeSystem.GEM_ERP_CS_DOCUMENT_TYPE.match(coding.getSystem()))
            .findFirst()
            .orElseThrow(
                () -> new MissingFieldException(ErxReceipt.class, DocumentType.CODE_SYSTEM));
    return DocumentType.fromCode(docType.getCode());
  }

  public Composition getComposition() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Composition))
        .map(Composition.class::cast)
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(ErxReceipt.class, ResourceType.Composition));
  }

  public Composition.SectionComponent getQesDigestRefInComposSect() {
    return this.getComposition().getSectionFirstRep();
  }

  public Reference getAuthor() {
    return this.getComposition().getAuthorFirstRep();
  }

  public Binary getQesDigestBinary() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Binary))
        .map(Binary.class::cast)
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(ErxReceipt.class, ResourceType.Binary));
  }
}
