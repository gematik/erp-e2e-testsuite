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

import de.gematik.test.erezept.fhir.exceptions.*;
import de.gematik.test.erezept.fhir.parser.profiles.*;
import de.gematik.test.erezept.fhir.parser.profiles.version.*;
import java.util.*;
import java.util.stream.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;

public interface ICommunicationType<T extends ProfileVersion<?>> {

  @SuppressWarnings({"unchecked"})
  static <V extends ProfileVersion<?>> ICommunicationType<V> fromUrl(
      @NonNull final String profileUrl) {
    val unversionedUrl = profileUrl.split("\\|")[0];

    val ret =
        Stream.concat(
                Arrays.stream(CommunicationType.values()),
                Arrays.stream(ChargeItemCommunicationType.values()))
            .filter(type -> type.doesMatch(unversionedUrl))
            .findFirst()
            .orElseThrow(() -> new InvalidCommunicationType(profileUrl));

    return (ICommunicationType<V>) ret;
  }

  /**
   * @deprecated will be removed because a communication does not necessarily have a fixed
   *     StructureDefinition but is dependent on the version
   * @return the StructureDefinition of the concrete Communication Type
   */
  @Deprecated(forRemoval = true, since = "0.3.0")
  IStructureDefinition<T> getType();

  INamingSystem getRecipientNamingSystem(ErpWorkflowVersion version);

  INamingSystem getSenderNamingSystem(ErpWorkflowVersion version);

  boolean doesMatch(String url);

  default Reference getRecipientReference(ErpWorkflowVersion version, @NonNull String value) {
    val recipientRef = new Reference();
    val recipientSystem = this.getRecipientNamingSystem(version).getCanonicalUrl();
    recipientRef.getIdentifier().setSystem(recipientSystem).setValue(value);
    return recipientRef;
  }

  default Reference getSenderReference(ErpWorkflowVersion version, @NonNull String value) {
    val senderRef = new Reference();
    val senderSystem = this.getSenderNamingSystem(version).getCanonicalUrl();
    senderRef.getIdentifier().setSystem(senderSystem).setValue(value);
    return senderRef;
  }

  default String getTypeUrl() {
    return this.getType().getCanonicalUrl();
  }
}
