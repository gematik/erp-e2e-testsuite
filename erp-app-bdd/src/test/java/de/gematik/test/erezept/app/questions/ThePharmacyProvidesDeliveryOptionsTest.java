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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.app.questions;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.app.abilities.HandleAppAuthentication;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.PharmacyDeliveryOptions;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import java.util.List;
import java.util.Map;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ThePharmacyProvidesDeliveryOptionsTest {
  private String userName;

  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});

    val app = mock(UseIOSApp.class);
    when(app.getPlatformType()).thenReturn(PlatformType.IOS);

    // assemble the screenplay
    userName = GemFaker.fakerName();
    val theAppUser = OnStage.theActorCalled(userName);
    givenThat(theAppUser).can(app);
    givenThat(theAppUser).can(HandleAppAuthentication.withStrongPassword());
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldWalkThroughPharmacies() {
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    val pharmacyData =
        List.of(
            Map.ofEntries(
                Map.entry("Name", "Apotheke 1"),
                Map.entry("Versand", "true"),
                Map.entry("Botendienst", "false")));

    when(app.isPresent(PharmacyDeliveryOptions.PHARMACY_DETAIL_SHIPMENT)).thenReturn(true);
    when(app.isPresent(PharmacyDeliveryOptions.PHARMACY_DETAIL_DELIVERY)).thenReturn(false);
    when(app.isPresent(PharmacyDeliveryOptions.PHARMACY_DETAIL_PICKUP)).thenReturn(false);

    val walkThrough = ThePharmacyProvidesDeliveryOptions.givenFrom(pharmacyData);
    val result = assertDoesNotThrow(() -> actor.asksFor(walkThrough));
    assertTrue(result);
  }

  @Test
  void shouldWalkThroughPharmaciesWithUnexpectedOptions() {
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    val pharmacyData =
        List.of(
            Map.ofEntries(
                Map.entry("Name", "Apotheke 1"),
                Map.entry("Versand", "true"),
                Map.entry("Botendienst", "false")));

    when(app.isPresent(PharmacyDeliveryOptions.PHARMACY_DETAIL_SHIPMENT)).thenReturn(true);
    // which is expected to be false
    when(app.isPresent(PharmacyDeliveryOptions.PHARMACY_DETAIL_DELIVERY)).thenReturn(true);
    when(app.isPresent(PharmacyDeliveryOptions.PHARMACY_DETAIL_PICKUP)).thenReturn(false);

    val walkThrough = ThePharmacyProvidesDeliveryOptions.givenFrom(pharmacyData);
    val result = assertDoesNotThrow(() -> actor.asksFor(walkThrough));
    assertFalse(result);
  }
}
