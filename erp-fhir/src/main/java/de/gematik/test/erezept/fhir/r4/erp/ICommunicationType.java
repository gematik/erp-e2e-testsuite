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

import de.gematik.bbriccs.fhir.coding.WithNamingSystem;
import de.gematik.bbriccs.fhir.coding.WithStructureDefinition;
import de.gematik.bbriccs.fhir.coding.version.ProfileVersion;
import de.gematik.test.erezept.fhir.exceptions.InvalidCommunicationType;
import java.util.Arrays;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.Reference;

public interface ICommunicationType<T extends ProfileVersion> {

  @SuppressWarnings({"unchecked"})
  static <V extends ProfileVersion> ICommunicationType<V> fromUrl(String profileUrl) {
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

  String name();

  WithStructureDefinition<T> getType();

  WithNamingSystem getRecipientNamingSystem(T version);

  WithNamingSystem getSenderNamingSystem(T version);

  boolean doesMatch(String url);

  default Reference getRecipientReference(String value, T version) {
    val recipientRef = new Reference();
    val recipientSystem = this.getRecipientNamingSystem(version).getCanonicalUrl();
    recipientRef.getIdentifier().setSystem(recipientSystem).setValue(value);
    return recipientRef;
  }

  default Reference getSenderReference(String value, T version) {
    val senderRef = new Reference();
    val senderSystem = this.getSenderNamingSystem(version).getCanonicalUrl();
    senderRef.getIdentifier().setSystem(senderSystem).setValue(value);
    return senderRef;
  }

  default String getTypeUrl() {
    return this.getType().getCanonicalUrl();
  }
}
