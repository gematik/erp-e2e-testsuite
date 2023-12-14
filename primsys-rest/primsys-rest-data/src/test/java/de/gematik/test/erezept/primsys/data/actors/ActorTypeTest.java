/*
 * Copyright 2023 gematik GmbH
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

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import de.gematik.test.erezept.primsys.exceptions.InvalidActorRoleException;
import java.util.List;
import lombok.val;
import org.junit.Test;

public class ActorTypeTest {

  @Test
  public void createValidDoctorRoles() {
    val docStrings = List.of("Arzt", "arzt", "Doctor", "doctor");
    docStrings.forEach(ds -> assertEquals(ActorType.DOCTOR, ActorType.fromString(ds)));
  }

  @Test
  public void createValidPharmacyRoles() {
    val pharmStrings = List.of("Apotheke", "apotheke", "Pharmacy", "pharmacy");
    pharmStrings.forEach(ds -> assertEquals(ActorType.PHARMACY, ActorType.fromString(ds)));
  }

  @Test
  public void createInvalidActorRoles() {
    val invalid = List.of("", "Artz", "Apoteke", "TEST");
    invalid.forEach(
        inv -> {
          assertThrows(InvalidActorRoleException.class, () -> ActorType.fromString(inv));
          assertFalse(ActorType.optionalFromString(inv).isPresent());
        });
  }
  @Test
  public void shouldReturnReadable(){
    assertNotNull(ActorType.DOCTOR.toString());
  }
  @Test
  public void createValidOptionalDoctor(){
    assertTrue(ActorType.optionalFromString("Doctor").isPresent());
  }
}
