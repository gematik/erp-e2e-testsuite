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

package de.gematik.test.erezept.primsys.data.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.primsys.exceptions.InvalidActorRoleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ActorTypeTest {

  @ParameterizedTest
  @ValueSource(strings = {"Arzt", "arzt", "Doctor", "doctor"})
  void shouldMapValidDoctorRoles(String input) {
    assertEquals(ActorType.DOCTOR, ActorType.fromString(input));
  }

  @ParameterizedTest
  @ValueSource(strings = {"Apotheke", "apotheke", "Pharmacy", "pharmacy"})
  void shouldMapValidPharmacyRoles(String input) {
    assertEquals(ActorType.PHARMACY, ActorType.fromString(input));
  }

  @ParameterizedTest
  @ValueSource(strings = {"KTR", "ktr", "Krankenkasse", "kostenträger"})
  void shouldMapValidKtrRoles(String input) {
    assertEquals(ActorType.HEALTH_INSURANCE, ActorType.fromString(input));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "Artz", "Apoteke", "TEST"})
  void shouldThrowOnInvalidActorRoles(String input) {
    assertThrows(InvalidActorRoleException.class, () -> ActorType.fromString(input));
    assertFalse(ActorType.optionalFromString(input).isPresent());
  }

  @Test
  void shouldReturnReadable() {
    assertNotNull(ActorType.DOCTOR.toString());
  }

  @Test
  void createValidOptionalDoctor() {
    assertTrue(ActorType.optionalFromString("Doctor").isPresent());
  }
}
