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

import static de.gematik.test.erezept.app.mocker.ConfigurationMocker.createDefaultTestConfiguration;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.app.abilities.UseConfigurationData;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.ScrollDirection;
import de.gematik.test.erezept.app.mobile.elements.Debug;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SetVirtualEgkTest {

  private final String iosUserName = "Bob";
  private String bobsPrivateKey;
  private UseIOSApp iosAbility;
  private Egk egk;

  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});

    val sma = SmartcardArchive.fromResources();
    egk = sma.getEgk(1);
    bobsPrivateKey = egk.getPrivateKeyBase64();
    iosAbility = mock(UseIOSApp.class);
    when(iosAbility.getPlatformType()).thenReturn(PlatformType.IOS);

    val bobIos = OnStage.theActorCalled(iosUserName);
    givenThat(bobIos).can(iosAbility);
    givenThat(bobIos).can(ProvideEGK.heOwns(egk));

    val config = createDefaultTestConfiguration(iosUserName, egk.getIccsn(), true);
    givenThat(bobIos).can(UseConfigurationData.forUser(iosUserName, config));
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldSetOnIos() {
    val appUser = OnStage.theActorCalled(iosUserName);
    assertDoesNotThrow(() -> appUser.attemptsTo(SetVirtualEgk.withEgk(egk)));

    // check that private key and certificate chain are sent to the driver
    verify(iosAbility, times(1)).input(bobsPrivateKey, Debug.EGK_PRIVATE_KEY);
    verify(iosAbility, times(1)).input(any(), eq(Debug.EGK_CERTIFICATE_CHAIN));
  }

  @Test
  void shouldNotTapVirtualEgkWhenAlreadyActive() {
    val appUser = OnStage.theActorCalled(iosUserName);
    val app = appUser.abilityTo(UseIOSApp.class);
    when(app.getText(Debug.ENABLE_VIRTUAL_EGK_USAGE_BUTTON)).thenReturn("1");

    assertDoesNotThrow(() -> appUser.attemptsTo(SetVirtualEgk.withEgk(egk)));

    verify(app, times(0)).tap(Debug.ENABLE_VIRTUAL_EGK_USAGE_BUTTON);
    verify(app, times(1)).scrollIntoView(ScrollDirection.DOWN, Debug.EGK_CERTIFICATE_CHAIN);
  }
}
