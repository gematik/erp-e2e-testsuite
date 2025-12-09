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

package de.gematik.test.erezept.client;

import de.gematik.test.erezept.client.exceptions.InvalidClientTypeException;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClientType {
  PS("Primärsystem", "l"), /* Primärsysteme */
  FDV("Benutzer-App", "v"), /* Patienten/Versicherten Systeme wie App (FdV/AdV) */
  NCPEH("NCPeH", "n"), /* NCPeH als eHDSI Anbindung*/
  KTR("Kostenträger", "k"); /* Kostenträger Systeme */

  private final String readableType;
  private final String headerValue;

  public static ClientType fromString(@NonNull String type) {
    return switch (type.toLowerCase()) {
      case "ps", "primärsystem" -> PS;
      case "app", "benutzer-app", "fdv", "adv" -> FDV;
      case "ncpeh", "eu" -> NCPEH;
      case "ktr", "kostenträger", "kostentraeger", "krankenkasse" -> KTR;
      default -> throw new InvalidClientTypeException(type);
    };
  }

  @Override
  public String toString() {
    return readableType;
  }
}
