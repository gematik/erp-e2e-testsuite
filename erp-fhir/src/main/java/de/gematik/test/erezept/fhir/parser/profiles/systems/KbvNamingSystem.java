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

package de.gematik.test.erezept.fhir.parser.profiles.systems;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.parser.profiles.INamingSystem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KbvNamingSystem implements INamingSystem {
  PRUEFNUMMER("https://fhir.kbv.de/NamingSystem/KBV_NS_FOR_Pruefnummer"),
  BASE_BSNR("https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR"),
  BASE_ANR("https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR"),
  ZAHNARZTNUMMER("http://fhir.de/sid/kzbv/zahnarztnummer");

  private final String canonicalUrl;

  @Override
  public String toString() {
    return format("{0}({1})", this.name(), this.canonicalUrl);
  }
}
