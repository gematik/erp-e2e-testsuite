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
import de.gematik.test.erezept.fhir.parser.profiles.ErpNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import de.gematik.test.erezept.fhir.values.Secret;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
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
            () ->
                new MissingFieldException(
                    ErxAcceptBundle.class, ErpStructureDefinition.GEM_ERX_TASK));
  }

  public String getTaskId() {
    return this.getTask().getUnqualifiedId();
  }

  public Secret getSecret() {
    return getTask()
        .getSecret()
        .orElseThrow(
            () -> new MissingFieldException(ErxAcceptBundle.class, ErpNamingSystem.SECRET));
  }

  public String getKbvBundleId() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Binary))
        .map(Resource::getId)
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    ErxAcceptBundle.class, ErpStructureDefinition.KBV_BUNDLE));
  }

  public Binary getKbvBundleAsBase64() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Binary))
        .map(Binary.class::cast)
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    ErxAcceptBundle.class, ErpStructureDefinition.KBV_BUNDLE));
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
