/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.app.task.ios;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.app.abilities.HandleAppAuthentication;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.cfg.PlatformType;
import de.gematik.test.erezept.app.task.SetUpDevice;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.smartcard.Egk;
import de.gematik.test.smartcard.SmartcardArchive;
import de.gematik.test.smartcard.SmartcardFactory;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.*;

class SetUpIosDeviceTest {

  private static SmartcardArchive smartcards;
  private UseIOSApp appAbility;
  private Egk egk;

  @BeforeAll
  static void initSmartcards() {
    smartcards = SmartcardFactory.getArchive();
  }

  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});
    egk = smartcards.getEgkCards().get(0);
    appAbility = mock(UseIOSApp.class);
    when(appAbility.getPlatformType()).thenReturn(PlatformType.IOS);

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
  void shouldSetupIosDeviceWithVirtualEgk() {
    when(appAbility.useVirtualeGK()).thenReturn(true);

    val theAppUser = OnStage.theActorInTheSpotlight();
    theAppUser.attemptsTo(SetUpDevice.withInsuranceType("GKV"));

    val baseData = theAppUser.abilityTo(ProvidePatientBaseData.class);
    assertNotNull(baseData);
    assertEquals(egk.getKvnr(), baseData.getKvid());
    assertEquals(VersicherungsArtDeBasis.GKV, baseData.getVersicherungsArt());
  }

  @Test
  void shouldSetupIosDeviceWithRealEgk() {
    when(appAbility.useVirtualeGK()).thenReturn(false);

    val theAppUser = OnStage.theActorInTheSpotlight();
    theAppUser.attemptsTo(SetUpDevice.withInsuranceType("GKV"));

    val baseData = theAppUser.abilityTo(ProvidePatientBaseData.class);
    assertNull(baseData); // because ProvidePatientBaseData with real eGK is not yet implemented
  }
}
