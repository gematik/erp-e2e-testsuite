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

package de.gematik.test.erezept.app.task;

import static de.gematik.test.erezept.app.mocker.ConfigurationMocker.createErpActorConfig;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.app.abilities.HandleAppAuthentication;
import de.gematik.test.erezept.app.abilities.UseAppUserConfiguration;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.Profile;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.config.dto.actor.PatientConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.smartcard.Egk;
import de.gematik.test.smartcard.SmartcardArchive;
import de.gematik.test.smartcard.SmartcardFactory;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UseInstalledAppTest {

  private final SmartcardArchive smartcards = SmartcardFactory.getArchive();
  private Egk deviceOwnerEgk;
  private EnvironmentConfiguration environment;

  private String userName;

  @BeforeEach
  void init() {
    OnStage.setTheStage(new Cast() {});
    this.deviceOwnerEgk = smartcards.getEgkCards().get(0);
    this.environment = new EnvironmentConfiguration();
    this.environment.setName("TU");

    val appAbility = mock(UseIOSApp.class);
    when(appAbility.getPlatformType()).thenReturn(PlatformType.IOS);

    // assemble the screenplay
    userName = GemFaker.fakerName();
    val theAppUser = OnStage.theActorCalled(userName);
    givenThat(theAppUser).can(appAbility);
    givenThat(theAppUser).can(HandleAppAuthentication.withStrongPassword());

    deviceOwnerEgk = smartcards.getEgkCards().get(0);
    givenThat(theAppUser).can(ProvideEGK.sheOwns(deviceOwnerEgk));
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldReuseInstalledApp() {
    val deviceOwner = OnStage.theActorCalled(userName);
    val app = deviceOwner.abilityTo(UseIOSApp.class);
    val actor = OnStage.theActorCalled("bob");

    val userConfigAbility =
        UseAppUserConfiguration.from(createErpActorConfig(deviceOwnerEgk.getIccsn(), true));
    deviceOwner.can(userConfigAbility);
    actor.can(userConfigAbility);

    when(app.getText(Profile.USER_KVNR)).thenReturn(deviceOwnerEgk.getKvnr());

    try (val erpClientFactory = mockStatic(ErpClientFactory.class)) {
      val erpClient = mock(ErpClient.class);
      erpClientFactory
          .when(() -> ErpClientFactory.createErpClient(any(), any(PatientConfiguration.class)))
          .thenReturn(erpClient);
      actor.attemptsTo(
          UseInstalledApp.ownedBy(deviceOwner)
              .forEnvironment(null)
              .withInsuranceType(VersicherungsArtDeBasis.GKV)
              .byMappingVirtualEgkFrom(smartcards));
    }

    assertEquals(app, actor.abilityTo(UseIOSApp.class));
    verify(app, times(1)).setCurrentUserProfile(actor.getName());
  }
}
