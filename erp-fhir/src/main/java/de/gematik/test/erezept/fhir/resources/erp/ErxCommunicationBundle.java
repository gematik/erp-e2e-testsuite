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

package de.gematik.test.erezept.fhir.resources.erp;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.ResourceType;

@Slf4j
@ResourceDef(name = "Bundle")
@SuppressWarnings({"java:S110"})
public class ErxCommunicationBundle extends ErxBundle {

  public List<ErxCommunication> getCommunications() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Communication))
        .map(ErxCommunication::fromCommunication)
        .toList();
  }

  public List<ErxCommunication> getCommunicationsFromSender(String senderId) {
    return this.getCommunications().stream()
        .filter(com -> com.getSenderId().equals(senderId))
        .toList();
  }

  public List<ErxCommunication> getCommunicationsForReceiver(String receiverId) {
    return this.getCommunications().stream()
        .filter(com -> com.getRecipientId().equals(receiverId))
        .toList();
  }
}
