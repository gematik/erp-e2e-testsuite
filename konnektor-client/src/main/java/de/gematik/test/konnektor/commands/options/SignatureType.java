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
 */

package de.gematik.test.konnektor.commands.options;

import lombok.Getter;

@Getter
public enum SignatureType {
  RFC_5652("urn:ietf:rfc:5652"),
  RFC_3447("urn:ietf:rfc:3447"),
  BSI_TR_03111("urn:bsi:tr:03111:ecdsa");

  private final String urn;

  SignatureType(String urn) {
    this.urn = urn;
  }
}
