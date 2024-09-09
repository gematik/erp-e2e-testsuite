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

package de.gematik.test.erezept.fhir.parser.profiles.systems;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.parser.profiles.INamingSystem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeBasisNamingSystem implements INamingSystem {
  IKNR("http://fhir.de/NamingSystem/arge-ik/iknr"),
  IKNR_SID("http://fhir.de/sid/arge-ik/iknr"),
  KVID("http://fhir.de/NamingSystem/gkv/kvid-10"),
  KVID_GKV("http://fhir.de/sid/gkv/kvid-10"),
  KVID_PKV("http://fhir.de/sid/pkv/kvid-10"), // NOTE will be resolved/merged with KVID later
  ZAHNARZTNUMMER("http://fhir.de/NamingSystem/kzbv/zahnarztnummer"),
  TEAMNUMMER("http://fhir.de/NamingSystem/asv/teamnummer"),
  KZVA_ABRECHNUNGSNUMMER("http://fhir.de/NamingSystem/kzbv/kzvabrechnungsnummer");

  private final String canonicalUrl;

  @Override
  public String toString() {
    return format("{0}({1})", this.name(), this.canonicalUrl);
  }
}
