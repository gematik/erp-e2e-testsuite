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

package de.gematik.test.erezept.app.task;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.PrimSysBddFactory;
import de.gematik.test.erezept.app.abilities.UseConfigurationData;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.cfg.ErpAppConfiguration;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.BottomNav;
import de.gematik.test.erezept.app.mobile.elements.CardWall;
import de.gematik.test.erezept.app.mobile.elements.Mainscreen;
import de.gematik.test.erezept.app.mobile.elements.Profile;
import de.gematik.test.erezept.app.mobile.elements.ProfileSelectorElement;
import de.gematik.test.erezept.app.mobile.elements.Settings;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.config.ConfigurationReader;
import de.gematik.test.erezept.config.dto.actor.PatientConfiguration;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NavigateThroughCardwallTest {

  private final SmartcardArchive sca = SmartcardArchive.fromResources();
  private final ErpAppConfiguration config =
      ConfigurationReader.forAppConfiguration().wrappedBy(ErpAppConfiguration::fromDto);
  private final PrimSysBddFactory primsysConfig =
      ConfigurationReader.forPrimSysConfiguration()
          .wrappedBy(dto -> PrimSysBddFactory.fromDto(dto, sca));

  private String userName;

  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});
    val app = mock(UseIOSApp.class);
    when(app.getPlatformType()).thenReturn(PlatformType.IOS);

    userName = GemFaker.fakerName();
    val testActor = OnStage.theActorCalled((userName));
    givenThat(testActor).can(app);
    // simply take the config from alice for the tests
    givenThat(testActor).can(UseConfigurationData.forUser("Alice", config));
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldHaveTheCorrectCardwallBehavior() {
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    when(app.getText(Profile.USER_KVNR)).thenReturn("X110406067");
    when(app.isPresent(Settings.SETTINGS_HEADER_LINE)).thenReturn(false).thenReturn(true);

    val task =
        NavigateThroughCardwall.forEnvironment(primsysConfig.getActiveEnvironment())
            .byMappingVirtualEgkFrom(sca);
    try (val erpClientFactory = mockStatic(ErpClientFactory.class)) {
      val erpClient = mock(ErpClient.class);
      erpClientFactory
          .when(() -> ErpClientFactory.createErpClient(any(), any(PatientConfiguration.class)))
          .thenReturn(erpClient);
      assertDoesNotThrow(() -> actor.attemptsTo(task));
    }
    verify(app, times(1)).tap(Mainscreen.LOGIN_BUTTON);
    verify(app, times(1)).tap(CardWall.ADD_HEALTH_CARD_BUTTON);

    verify(app, times(1)).inputPassword("123123", CardWall.CAN_INPUT_FIELD);
    verify(app, times(1)).tap(CardWall.CAN_ACCEPT_BUTTON);
    verify(app, times(1)).inputPassword("123456", CardWall.PIN_INPUT_FIELD);
    verify(app, times(1)).tap(CardWall.PIN_ACCEPT_BUTTON);

    verify(app, times(1)).tap(CardWall.DONT_SAVE_CREDENTIAL_BUTTON);
    verify(app, times(1)).tap(CardWall.CONTINUE_AFTER_BIOMETRY_CHECK_BUTTON);
    verify(app, times(1)).tap(CardWall.START_NFC_READOUT_BUTTON);

    // clicked twice to ensure the SETTINGS_HEADER_LINE is present
    verify(app, times(2)).tap(BottomNav.SETTINGS_BUTTON);
    verify(app, times(1)).tap(any(ProfileSelectorElement.class));

    val aliceIos = OnStage.theActorCalled((userName));
    val egk = aliceIos.abilityTo(ProvideEGK.class);
    assertNotNull(egk);
    assertEquals(sca.getEgkByKvnr("X110406067"), egk.getEgk());
  }
}
