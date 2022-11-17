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

package de.gematik.test.erezept.fhir.resources.erp;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.DocumentType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;

/**
 * @see <a href="https://simplifier.net/erezept-workflow/gemerxtask">Gem_erxTask</a>
 */
@Slf4j
@ResourceDef(name = "Bundle")
@SuppressWarnings({"java:S110"})
public class ErxReceipt extends Bundle {

  public PrescriptionId getPrescriptionId() {
    return new PrescriptionId(this.getIdentifier().getValue());
  }

  public DocumentType getDocumentType() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Composition))
        .map(Composition.class::cast)
        .map(Composition::getType)
        .map(
            compositionType ->
                compositionType.getCoding().stream()
                    .filter(
                        coding ->
                            coding.getSystem().equals(DocumentType.CODE_SYSTEM.getCanonicalUrl()))
                    .findFirst()
                    .orElseThrow(
                        () ->
                            new MissingFieldException(ErxReceipt.class, DocumentType.CODE_SYSTEM)))
        .map(documentType -> DocumentType.fromCode(documentType.getCode()))
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(ErxReceipt.class, DocumentType.CODE_SYSTEM));
  }

  public static ErxReceipt fromBundle(Bundle adaptee) {
    val receipt = new ErxReceipt();
    adaptee.copyValues(receipt);
    return receipt;
  }

  public static ErxReceipt fromBundle(Resource adaptee) {
    return fromBundle((Bundle) adaptee);
  }
}
