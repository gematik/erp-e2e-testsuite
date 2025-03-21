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

package de.gematik.test.erezept.fhir.values;

import de.gematik.bbriccs.fhir.coding.SemanticValue;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvNamingSystem;

public class AsvFachgruppennummer extends SemanticValue<String, KbvNamingSystem> {

  private AsvFachgruppennummer(String value) {
    super(KbvNamingSystem.ASV_FACHGRUPPENNUMMER, value);
  }

  public static AsvFachgruppennummer from(String s) {
    return new AsvFachgruppennummer(s);
  }
}
