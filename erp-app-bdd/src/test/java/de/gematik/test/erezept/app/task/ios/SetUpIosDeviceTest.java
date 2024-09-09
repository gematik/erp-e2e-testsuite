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

package de.gematik.test.erezept.app.task.ios;

import static de.gematik.test.erezept.app.mocker.ConfigurationMocker.createDefaultTestConfiguration;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.app.abilities.HandleAppAuthentication;
import de.gematik.test.erezept.app.abilities.UseConfigurationData;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.Profile;
import de.gematik.test.erezept.app.task.SetUpDevice;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.config.dto.actor.PatientConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SerenityJUnit5Extension.class)
class SetUpIosDeviceTest {

  private final SmartcardArchive smartcards = SmartcardArchive.fromResources();
  private Egk egk;
  private EnvironmentConfiguration environment;
  private String userName;

  @BeforeEach
  void init() {
    OnStage.setTheStage(new Cast() {});
    this.egk = smartcards.getEgk(0);
    this.environment = new EnvironmentConfiguration();
    this.environment.setName("TU");

    val appAbility = mock(UseIOSApp.class);
    when(appAbility.getPlatformType()).thenReturn(PlatformType.IOS);

    // assemble the screenplay
    this.userName = GemFaker.fakerName();
    val theAppUser = OnStage.theActorCalled(this.userName);
    givenThat(theAppUser).can(appAbility);
    givenThat(theAppUser).can(HandleAppAuthentication.withStrongPassword());

    givenThat(theAppUser).can(ProvideEGK.sheOwns(this.egk));
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldSetupIosDeviceWithVirtualEgk() {
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    val config = createDefaultTestConfiguration("Bob", egk.getIccsn(), true);

    val userConfigAbility = UseConfigurationData.forUser("Bob", config);
    actor.can(userConfigAbility);
    when(app.getText(Profile.USER_KVNR)).thenReturn(egk.getKvnr());

    try (val erpClientFactory = mockStatic(ErpClientFactory.class)) {
      val erpClient = mock(ErpClient.class);
      erpClientFactory
          .when(() -> ErpClientFactory.createErpClient(any(), any(PatientConfiguration.class)))
          .thenReturn(erpClient);
      SetUpDevice.forEnvironment(environment)
          .withInsuranceType("GKV")
          .byMappingVirtualEgkFrom(smartcards)
          .performAs(actor);
    }

    val baseData = actor.abilityTo(ProvidePatientBaseData.class);
    assertNotNull(baseData);
    assertEquals(egk.getKvnr(), baseData.getKvnr().getValue());
    assertEquals(VersicherungsArtDeBasis.GKV, baseData.getPatientInsuranceType());
  }

  @Test
  void shouldSetupIosDeviceWithRealEgk() {
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    when(app.getText(Profile.USER_KVNR)).thenReturn("X110406067");

    val config = createDefaultTestConfiguration("Bob", egk.getIccsn(), true);

    val userConfigAbility = UseConfigurationData.forUser("Bob", config);
    actor.can(userConfigAbility);

    try (val erpClientFactory = mockStatic(ErpClientFactory.class)) {
      val erpClient = mock(ErpClient.class);
      erpClientFactory
          .when(
              () ->
                  ErpClientFactory.createErpClient(
                      any(),
                      any(de.gematik.test.erezept.config.dto.actor.PatientConfiguration.class)))
          .thenReturn(erpClient);
      SetUpDevice.forEnvironment(environment)
          .withInsuranceType("GKV")
          .byMappingVirtualEgkFrom(smartcards)
          .performAs(actor);
    }

    val baseData = actor.abilityTo(ProvidePatientBaseData.class);
    assertNotNull(baseData);
    assertEquals("X110406067", baseData.getKvnr().getValue());
    assertEquals(VersicherungsArtDeBasis.GKV, baseData.getPatientInsuranceType());
  }
}
