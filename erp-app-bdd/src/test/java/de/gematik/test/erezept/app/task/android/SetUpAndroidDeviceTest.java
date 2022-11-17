/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.app.task.android;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.app.abilities.HandleAppAuthentication;
import de.gematik.test.erezept.app.abilities.UseAndroidApp;
import de.gematik.test.erezept.app.cfg.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.Debug;
import de.gematik.test.erezept.app.task.SetUpDevice;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.operator.UIProvider;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.smartcard.Egk;
import de.gematik.test.smartcard.SmartcardArchive;
import de.gematik.test.smartcard.SmartcardFactory;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openqa.selenium.WebElement;

class SetUpAndroidDeviceTest {
  private static SmartcardArchive smartcards;
  private UseAndroidApp appAbility;
  private Egk egk;

  @BeforeAll
  static void initSmartcards() {
    smartcards = SmartcardFactory.getArchive();
  }

  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});
    egk = smartcards.getEgkCards().get(0);
    appAbility = mock(UseAndroidApp.class);
    when(appAbility.getPlatformType()).thenReturn(PlatformType.ANDROID);

    val bearerTokenElement = mock(WebElement.class);
    val testToken =
        "eyJhbGciOiJCUDI1NlIxIiwidHlwIjoiYXQrSldUIiwia2lkIjoicHVrX2lkcF9zaWcifQ.eyJzdWIiOiJJWERkLTNyUVpLS0ZYVWR4R0dqNFBERG9WNk0wUThaai1xdzF2cjF1XzU4IiwicHJvZmVzc2lvbk9JRCI6IjEuMi4yNzYuMC43Ni40LjQ5Iiwib3JnYW5pemF0aW9uTmFtZSI6ImdlbWF0aWsgTXVzdGVya2Fzc2UxR0tWTk9ULVZBTElEIiwiaWROdW1tZXIiOiJYMTEwNTAyNDE0IiwiYW1yIjpbIm1mYSIsInNjIiwicGluIl0sImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6NTUwMTEvYXV0aC9yZWFsbXMvaWRwLy53ZWxsLWtub3duL29wZW5pZC1jb25maWd1cmF0aW9uIiwiZ2l2ZW5fbmFtZSI6IlJvYmluIEdyYWYiLCJjbGllbnRfaWQiOiJlcnAtdGVzdHN1aXRlLWZkIiwiYWNyIjoiZ2VtYXRpay1laGVhbHRoLWxvYS1oaWdoIiwiYXVkIjoiaHR0cDovL2xvY2FsaG9zdDozMDAwLyIsImF6cCI6ImVycC10ZXN0c3VpdGUtZmQiLCJzY29wZSI6Im9wZW5pZCBlLXJlemVwdCIsImF1dGhfdGltZSI6MTY0MzgwNDczMywiZXhwIjoxNjQzODA1MDMzLCJmYW1pbHlfbmFtZSI6IlbDs3Jtd2lua2VsIiwiaWF0IjoxNjQzODA0NjEzLCJqdGkiOiI2Yjg3NmU0MWNmMGViNGJkIn0.MV5cDnL3JBZ4b6xr9SqiYDmZ7qtZFEWBd1vCrHzVniZeDhkyuSYc7xhf577h2S21CzNgrMp0M6JALNW9Qjnw_g";
    when(appAbility.getWebElement(Debug.BEARER_TOKEN)).thenReturn(bearerTokenElement);
    when(bearerTokenElement.getText()).thenReturn(testToken);

    // assemble the screenplay
    val userName = "Alice";
    val theAppUser = OnStage.theActorCalled(userName);
    givenThat(theAppUser).can(appAbility);
    givenThat(theAppUser).can(HandleAppAuthentication.withStrongPassword());
    givenThat(theAppUser).can(ProvideEGK.sheOwns(egk));
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldSetupAndroidDeviceWithVirtualEgk() {
    when(appAbility.useVirtualeGK()).thenReturn(true);

    val theAppUser = OnStage.theActorInTheSpotlight();
    theAppUser.attemptsTo(SetUpDevice.withInsuranceType(VersicherungsArtDeBasis.GKV));

    val baseData = theAppUser.abilityTo(ProvidePatientBaseData.class);
    val expectedKvid = "X110502414";
    assertNotNull(baseData);
    assertEquals(expectedKvid, baseData.getKvid());
    assertEquals(VersicherungsArtDeBasis.GKV, baseData.getVersicherungsArt());
  }

  @Test
  void shouldSetupAndroidDeviceWithRealEgk() {
    when(appAbility.useVirtualeGK()).thenReturn(false);

    val expectedKvid = "X110502414";
    val theAppUser = OnStage.theActorInTheSpotlight();

    try (MockedStatic<UIProvider> uiProvider = Mockito.mockStatic(UIProvider.class)) {
      uiProvider
          .when(() -> UIProvider.getQuestionResult("What is the KVID of the eGK you have used?"))
          .thenReturn(expectedKvid);
      uiProvider
          .when(() -> UIProvider.getQuestionResult("What is the Name on the eGK?"))
          .thenReturn("Alice");
      theAppUser.attemptsTo(SetUpDevice.withInsuranceType(VersicherungsArtDeBasis.GKV));
    }

    val baseData = theAppUser.abilityTo(ProvidePatientBaseData.class);
    assertNotNull(baseData);
    assertEquals(expectedKvid, baseData.getKvid());
    assertEquals(VersicherungsArtDeBasis.GKV, baseData.getVersicherungsArt());
  }
}
