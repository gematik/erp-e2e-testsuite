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
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
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
                new MissingFieldException(ErxPrescriptionBundle.class, ErpWorkflowStructDef.TASK));
  }

  // TODO: KbvErpBundle is not always included!! make return Optional
  public KbvErpBundle getKbvBundle() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Bundle))
        .filter(
            resource ->
                resource.getMeta().getProfile().stream()
                    .map(PrimitiveType::getValue)
                    .anyMatch(KbvItaErpStructDef.BUNDLE::match))
        .map(KbvErpBundle.class::cast)
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(ErxPrescriptionBundle.class, KbvItaErpStructDef.BUNDLE));
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
                    // checked by HAPI anyway
                    .anyMatch(
                        p ->
                            ErpWorkflowStructDef.RECEIPT.match(p)
                                || ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE.match(p)))
        .map(ErxReceipt::fromBundle)
        .findFirst();
  }
}
