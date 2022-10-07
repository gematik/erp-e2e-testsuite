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

package de.gematik.test.smartcard;

import de.gematik.test.smartcard.exceptions.InvalidFileExtensionException;
import lombok.Getter;
import lombok.NonNull;

public enum KeystoreType {
  P12("PKCS12", ".p12"),
  JKS("JKS", ".jks");

  @Getter private final String name;

  @Getter private final String fileExtension;

  KeystoreType(String name, String extension) {
    this.name = name;
    this.fileExtension = extension;
  }

  public static KeystoreType fromFileExtension(@NonNull String fileExtension) {
    switch (fileExtension.toLowerCase().strip().replace("-", "")) {
      case "p12":
        return P12;
      case "jks":
        return JKS;
      default:
        throw new InvalidFileExtensionException(fileExtension);
    }
  }
}
