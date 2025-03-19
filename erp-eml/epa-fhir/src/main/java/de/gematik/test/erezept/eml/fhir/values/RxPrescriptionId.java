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

package de.gematik.test.erezept.eml.fhir.values;

import de.gematik.bbriccs.fhir.coding.SemanticValue;
import de.gematik.test.erezept.eml.fhir.profile.EpaMedicationNamingSystem;
import org.hl7.fhir.r4.model.Identifier;

public class RxPrescriptionId extends SemanticValue<String, EpaMedicationNamingSystem> {

  private RxPrescriptionId(String value) {
    super(EpaMedicationNamingSystem.RX_PRESCRIPTION_ID, value);
  }

  public static RxPrescriptionId from(String value) {
    return new RxPrescriptionId(value);
  }

  public static RxPrescriptionId from(Identifier value) {
    return from(value.getValue());
  }
}
