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
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.util.IdentifierUtil;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import java.util.Base64;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ResourceType;

@Slf4j
@ResourceDef(name = "Bundle")
@SuppressWarnings({"java:S110"})
public class ErxAcceptBundle extends Bundle {

  public ErxTask getTask() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Task))
        .map(ErxTask::fromTask)
        .findFirst()
        .orElseThrow(
            () -> new MissingFieldException(ErxAcceptBundle.class, ErpWorkflowStructDef.TASK));
  }

  public TaskId getTaskId() {
    return this.getTask().getTaskId();
  }

  public Secret getSecret() {
    return getTask()
        .getSecret()
        .orElseThrow(
            () -> new MissingFieldException(ErxAcceptBundle.class, ErpWorkflowNamingSystem.SECRET));
  }

  public String getKbvBundleId() {
    return this.getEntry().stream()
        .filter(entry -> entry.getResource().getResourceType().equals(ResourceType.Binary))
        .map(entry -> IdentifierUtil.getUnqualifiedId(entry.getFullUrlElement()))
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(ErxAcceptBundle.class, ResourceType.Binary));
  }

  public Binary getKbvBundleAsBase64() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Binary))
        .map(Binary.class::cast)
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(ErxAcceptBundle.class, ResourceType.Binary));
  }

  public byte[] getSignedKbvBundle() {
    val binary = getKbvBundleAsBase64();
    val encodedBase64 = binary.getContentAsBase64();

    return Base64.getDecoder().decode(encodedBase64);
  }

  public boolean hasConsent() {
    return this.getConsent().isPresent();
  }

  public Optional<ErxConsent> getConsent() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Consent))
        .map(ErxConsent::fromConsent)
        .findFirst();
  }
}
