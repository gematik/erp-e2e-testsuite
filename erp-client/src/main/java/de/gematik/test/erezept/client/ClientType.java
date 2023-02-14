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

package de.gematik.test.erezept.client;

import de.gematik.test.erezept.client.exceptions.InvalidClientTypeException;
import lombok.NonNull;

public enum ClientType {
  PS("Primärsystem"), /* Primärsysteme */
  FDV("Benutzer-App") /* Patienten/Versicherten Systeme wie App (FdV/AdV) */;

  private final String readableType;

  ClientType(String readableType) {
    this.readableType = readableType;
  }

  public static ClientType fromString(@NonNull String type) {
    return switch (type.toLowerCase()) {
      case "ps", "primärsystem" -> PS;
      case "app", "benutzer-app", "fdv", "adv" -> FDV;
      default -> throw new InvalidClientTypeException(type);
    };
  }

  @Override
  public String toString() {
    return readableType;
  }
}
