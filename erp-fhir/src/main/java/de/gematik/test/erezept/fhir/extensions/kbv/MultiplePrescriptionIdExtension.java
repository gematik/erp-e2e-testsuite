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

package de.gematik.test.erezept.fhir.extensions.kbv;

import static java.text.MessageFormat.format;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class MultiplePrescriptionIdExtension {
  public static final String URL = "ID";
  private static final String ID_SYSTEM = "urn:ietf:rfc:3986";
  private final String idValue;
  private final String idSystem;

  private Identifier asIdentifier() {
    return new Identifier().setSystem(idSystem).setValue(idValue);
  }

  public Extension asExtension() {
    return new Extension(URL, asIdentifier());
  }

  public static MultiplePrescriptionIdExtension randomId() {
    return withId(java.util.UUID.randomUUID().toString());
  }

  public static MultiplePrescriptionIdExtension invalidId() {
    return withId("13061707");
  }

  public static MultiplePrescriptionIdExtension withId(String id) {
    return with(id, ID_SYSTEM);
  }

  public static MultiplePrescriptionIdExtension with(String id, String system) {
    var urnIdentifier = id;
    if (!urnIdentifier.startsWith("urn:uuid:")) {
      urnIdentifier = format("urn:uuid:{0}", urnIdentifier);
    }
    return new MultiplePrescriptionIdExtension(urnIdentifier, system);
  }
}
