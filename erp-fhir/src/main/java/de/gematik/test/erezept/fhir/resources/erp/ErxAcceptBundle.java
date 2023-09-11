/*
 * Copyright (c) 2023 gematik GmbH
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

import ca.uhn.fhir.model.api.annotation.*;
import de.gematik.test.erezept.fhir.exceptions.*;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.*;
import de.gematik.test.erezept.fhir.parser.profiles.systems.*;
import de.gematik.test.erezept.fhir.util.*;
import de.gematik.test.erezept.fhir.values.*;
import java.nio.charset.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.hl7.fhir.r4.model.*;

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
            () ->
                new MissingFieldException(
                    ErxAcceptBundle.class,
                    ErpWorkflowNamingSystem.SECRET,
                    ErpWorkflowNamingSystem.SECRET_12));
  }

  public String getKbvBundleId() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Binary))
        .map(resource -> IdentifierUtil.getUnqualifiedId(resource.getId()))
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

  public String getKbvBundleAsString() {
    val binary = getKbvBundleAsBase64();
    val encodedBase64 = binary.getContentAsBase64();
    byte[] decodedBytes = Base64.getDecoder().decode(encodedBase64);
    val decodedString =
        new String(decodedBytes, StandardCharsets.UTF_8)
            // erases all the ASCII control characters
            .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
            // strips off all non-ASCII characters
            .replaceAll("[^\\x00-\\x7F]", "")
            // removes non-printable characters from Unicode
            .replaceAll("\\p{C}", "");

    // cut off the signature and everything else what does not belong to the KBV Bundle
    return decodedString.substring(
        decodedString.indexOf("<Bundle"), decodedString.lastIndexOf("</Bundle>") + 9);
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
