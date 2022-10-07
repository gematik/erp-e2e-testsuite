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
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.ResourceType;

@Slf4j
@ResourceDef(name = "Bundle")
@SuppressWarnings({"java:S110"})
public class ErxPrescriptionBundle extends Bundle {

  public ErxTask getTask() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Task))
        .map(ErxTask::fromTask)
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    ErxPrescriptionBundle.class, ErpStructureDefinition.GEM_ERX_TASK));
  }

  public KbvErpBundle getKbvBundle() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Bundle))
        .filter(
            resource ->
                resource.getMeta().getProfile().stream()
                    .map(PrimitiveType::getValue)
                    .anyMatch(p -> p.equals(ErpStructureDefinition.KBV_BUNDLE.getCanonicalUrl())))
        .map(KbvErpBundle::fromBundle)
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    ErxPrescriptionBundle.class, ErpStructureDefinition.KBV_BUNDLE));
  }

  /**
   * This receipt is only available if the Task is in status complete and this bundle was retrieved
   * by a pharmacy with a secret (A_19226)
   *
   * @return an optional ErxReceipt
   */
  public Optional<ErxReceipt> getReceipt() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Bundle))
        .filter(
            resource ->
                resource.getMeta().getProfile().stream()
                    .map(PrimitiveType::getValue)
                    // match the ErxPrescription by the unversioned Profile as the version is
                    // checked by HAPI anyways
                    .anyMatch(
                        p -> p.contains(ErpStructureDefinition.GEM_RECEIPT.getUnversionedUrl())))
        .map(ErxReceipt::fromBundle)
        .findFirst();
  }
}
