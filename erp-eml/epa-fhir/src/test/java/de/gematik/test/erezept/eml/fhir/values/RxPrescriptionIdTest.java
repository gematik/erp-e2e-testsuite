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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.eml.fhir.profile.EpaMedicationNamingSystem;
import lombok.val;
import org.hl7.fhir.r4.model.Identifier;
import org.junit.jupiter.api.Test;

class RxPrescriptionIdTest {

  @Test
  void shouldBuildFromIdentifierCorrect() {
    val identifier = new Identifier();
    val system = "testSystem";
    val value = "testValue";
    identifier.setSystem(system).setValue(value);
    val rxPrsId = RxPrescriptionId.from(identifier);
    assertNotEquals(system, rxPrsId.getSystem().getCanonicalUrl());
    assertEquals(value, rxPrsId.getValue());
  }

  @Test
  void shouldBuildFromStringCorrect() {
    val value = "testValue";
    val rxPrescId = RxPrescriptionId.from(value);
    assertEquals(EpaMedicationNamingSystem.RX_PRESCRIPTION_ID, rxPrescId.getSystem());
    assertEquals(value, rxPrescId.getValue());
  }
}
