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

package de.gematik.test.erezept.screenplay.abilities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.resources.erp.ErxPrescriptionBundle;
import lombok.val;
import org.junit.jupiter.api.Test;

class ManagePatientPrescriptionsTest {

  @Test
  void shouldHaveInitializedPatientPrescriptionsStack() {
    val ability = ManagePatientPrescriptions.heReceived();
    assertTrue(ability.getFullDetailedPrescriptions().isEmpty());
  }

  @Test
  void shouldAddToStack() {
    val ability = ManagePatientPrescriptions.heReceived();
    assertTrue(ability.getFullDetailedPrescriptions().isEmpty());

    val mockBundle = mock(ErxPrescriptionBundle.class);
    ability.appendFullDetailedPrescription(mockBundle);
    assertFalse(ability.getFullDetailedPrescriptions().isEmpty());
  }

  @Test
  void shouldThrowOnAccessingEmptyList() {
    val ability = ManagePatientPrescriptions.heReceived();
    val list = ability.getFullDetailedPrescriptions();
    assertThrows(MissingPreconditionError.class, list::getFirst);
  }
}
