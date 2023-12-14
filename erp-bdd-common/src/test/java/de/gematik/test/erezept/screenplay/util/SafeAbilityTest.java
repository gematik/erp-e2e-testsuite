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

package de.gematik.test.erezept.screenplay.util;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.exceptions.MissingAbilityException;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SafeAbilityTest {

  @Before
  public void setUp() {
    OnStage.setTheStage(new OnlineCast());
  }

  @After
  public void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  public void shouldGetExistingAbility() {
    val actor = OnStage.theActor("Marty");
    val provideBaseData =
        ProvidePatientBaseData.forPatient(
            KVNR.random(),
            GemFaker.fakerName(),
            GemFaker.fakerValueSet(VersicherungsArtDeBasis.class));
    actor.can(provideBaseData);

    val actual = SafeAbility.getAbility(actor, ProvidePatientBaseData.class);
    assertEquals(provideBaseData, actual);

    val actualExtends = SafeAbility.getAbilityThatExtends(actor, ProvidePatientBaseData.class);
    assertEquals(provideBaseData, actualExtends);
  }

  @Test
  public void shouldThrowOnNonExistingAbility() {
    val actor = OnStage.theActor("Emmet");
    assertThrows(
        MissingAbilityException.class,
        () -> SafeAbility.getAbility(actor, ProvidePatientBaseData.class));
    assertThrows(
        MissingAbilityException.class,
        () -> SafeAbility.getAbilityThatExtends(actor, ProvidePatientBaseData.class));
  }
}
