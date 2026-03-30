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

package de.gematik.test.erezept.app.task.ios;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;
import static de.gematik.test.erezept.app.mocker.ConfigurationMocker.createDefaultTestConfiguration;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.app.abilities.HandleAppAuthentication;
import de.gematik.test.erezept.app.abilities.UseConfigurationData;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.FeatureFlagScreen;
import de.gematik.test.erezept.app.mobile.elements.Onboarding;
import de.gematik.test.erezept.app.mobile.elements.Profile;
import de.gematik.test.erezept.app.task.SetUpDevice;
import de.gematik.test.erezept.client.ClientType;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.rest.MediaType;
import de.gematik.test.erezept.client.usecases.ConsentGetCommand;
import de.gematik.test.erezept.client.usecases.eu.EuConsentDeleteCommand;
import de.gematik.test.erezept.client.usecases.eu.EuConsentGetCommand;
import de.gematik.test.erezept.config.dto.actor.PatientConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.parser.ValidatorType;
import de.gematik.test.erezept.fhir.r4.erp.ErxConsentBundle;
import de.gematik.test.erezept.fhir.r4.eu.EuConsentBundle;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import java.util.Map;
import java.util.Optional;
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
    when(app.isDisplayed(Onboarding.START_BUTTON)).thenReturn(true);
    when(app.getText(Profile.USER_KVNR)).thenReturn(egk.getKvnr());
    when(app.getText(Profile.USER_INSURANCE)).thenReturn("GKVInsurance");
    when(app.getText(FeatureFlagScreen.ENABLE_EU_REDEEM_FEATURE_SWITCH)).thenReturn("0");

    try (val erpClientFactory = mockStatic(ErpClientFactory.class)) {
      val erpClient = mock(ErpClient.class);
      val jwt =
          "eyJhbGciOiJCUDI1NlIxIiwidHlwIjoiYXQrSldUIiwia2lkIjoicHVrX2lkcF9zaWcifQ.eyJzdWIiOiJJWERkLTNyUVpLS0ZYVWR4R0dqNFBERG9WNk0wUThaai1xdzF2cjF1XzU4IiwicHJvZmVzc2lvbk9JRCI6IjEuMi4yNzYuMC43Ni40LjQ5Iiwib3JnYW5pemF0aW9uTmFtZSI6ImdlbWF0aWsgTXVzdGVya2Fzc2UxR0tWTk9ULVZBTElEIiwiaWROdW1tZXIiOiJYMTEwNTAyNDE0IiwiYW1yIjpbIm1mYSIsInNjIiwicGluIl0sImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6NTUwMTEvYXV0aC9yZWFsbXMvaWRwLy53ZWxsLWtub3duL29wZW5pZC1jb25maWd1cmF0aW9uIiwiZ2l2ZW5fbmFtZSI6IlJvYmluIEdyYWYiLCJjbGllbnRfaWQiOiJlcnAtdGVzdHN1aXRlLWZkIiwiYWNyIjoiZ2VtYXRpay1laGVhbHRoLWxvYS1oaWdoIiwiYXVkIjoiaHR0cDovL2xvY2FsaG9zdDozMDAwLyIsImF6cCI6ImVycC10ZXN0c3VpdGUtZmQiLCJzY29wZSI6Im9wZW5pZCBlLXJlemVwdCIsImF1dGhfdGltZSI6MTY0MzgwNDczMywiZXhwIjoxNjQzODA1MDMzLCJmYW1pbHlfbmFtZSI6IlbDs3Jtd2lua2VsIiwiaWF0IjoxNjQzODA0NjEzLCJqdGkiOiI2Yjg3NmU0MWNmMGViNGJkIn0.MV5cDnL3JBZ4b6xr9SqiYDmZ7qtZFEWBd1vCrHzVniZeDhkyuSYc7xhf577h2S21CzNgrMp0M6JALNW9Qjnw_g";

      // Note: needed to revoke the charge item consent during the onboarding
      val erxConsentBundle = new ErxConsentBundle();
      val erpChargeItemConsentResponse =
          ErpResponse.forPayload(erxConsentBundle, ErxConsentBundle.class)
              .withStatusCode(200)
              .withHeaders(Map.of())
              .usedJwt(jwt)
              .andValidationResult(createEmptyValidationResult());

      // Note: needed to revoke the EU consent during onboarding (case when consent was granted
      // before)

      // For get command
      val euConsentBundle = new EuConsentBundle();
      val erpEUConsentGetResponse =
          ErpResponse.forPayload(euConsentBundle, EuConsentBundle.class)
              .withStatusCode(200)
              .withHeaders(Map.of())
              .usedJwt(jwt)
              .andValidationResult(createEmptyValidationResult());

      // For delete command
      val emptyResource = new EmptyResource();
      val erpEUConsentDeleteResponse =
          ErpResponse.forPayload(emptyResource, EmptyResource.class)
              .withStatusCode(200)
              .withHeaders(Map.of())
              .usedJwt(jwt)
              .andValidationResult(createEmptyValidationResult());

      when(erpClient.request(any(ConsentGetCommand.class)))
          .thenReturn(erpChargeItemConsentResponse);
      when(erpClient.getAcceptMime()).thenReturn(MediaType.ACCEPT_FHIR_XML);
      when(erpClient.getSendMime()).thenReturn(MediaType.FHIR_XML);
      when(erpClient.getClientType()).thenReturn(ClientType.PS);
      when(erpClient.getFhir()).thenReturn(new FhirParser(ValidatorType.NONE));
      when(erpClient.request(any(EuConsentGetCommand.class))).thenReturn(erpEUConsentGetResponse);
      when(erpClient.request(any(EuConsentDeleteCommand.class)))
          .thenReturn(erpEUConsentDeleteResponse);

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
    assertEquals(InsuranceTypeDe.GKV, baseData.getPatientInsuranceType());
  }

  @Test
  void shouldSetupIosDeviceWithRealEgk() {
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    when(app.isDisplayed(Onboarding.START_BUTTON)).thenReturn(true);
    when(app.getText(Profile.USER_KVNR)).thenReturn("X110406067");
    when(app.getText(Profile.USER_INSURANCE)).thenReturn("GKVInsurance");
    when(app.getText(FeatureFlagScreen.ENABLE_EU_REDEEM_FEATURE_SWITCH)).thenReturn("0");

    val config = createDefaultTestConfiguration("Bob", egk.getIccsn(), true);

    val userConfigAbility = UseConfigurationData.forUser("Bob", config);
    actor.can(userConfigAbility);

    try (val erpClientFactory = mockStatic(ErpClientFactory.class)) {
      val erpClient = mock(ErpClient.class);
      val jwt =
          "eyJhbGciOiJCUDI1NlIxIiwidHlwIjoiYXQrSldUIiwia2lkIjoicHVrX2lkcF9zaWcifQ.eyJzdWIiOiJJWERkLTNyUVpLS0ZYVWR4R0dqNFBERG9WNk0wUThaai1xdzF2cjF1XzU4IiwicHJvZmVzc2lvbk9JRCI6IjEuMi4yNzYuMC43Ni40LjQ5Iiwib3JnYW5pemF0aW9uTmFtZSI6ImdlbWF0aWsgTXVzdGVya2Fzc2UxR0tWTk9ULVZBTElEIiwiaWROdW1tZXIiOiJYMTEwNTAyNDE0IiwiYW1yIjpbIm1mYSIsInNjIiwicGluIl0sImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6NTUwMTEvYXV0aC9yZWFsbXMvaWRwLy53ZWxsLWtub3duL29wZW5pZC1jb25maWd1cmF0aW9uIiwiZ2l2ZW5fbmFtZSI6IlJvYmluIEdyYWYiLCJjbGllbnRfaWQiOiJlcnAtdGVzdHN1aXRlLWZkIiwiYWNyIjoiZ2VtYXRpay1laGVhbHRoLWxvYS1oaWdoIiwiYXVkIjoiaHR0cDovL2xvY2FsaG9zdDozMDAwLyIsImF6cCI6ImVycC10ZXN0c3VpdGUtZmQiLCJzY29wZSI6Im9wZW5pZCBlLXJlemVwdCIsImF1dGhfdGltZSI6MTY0MzgwNDczMywiZXhwIjoxNjQzODA1MDMzLCJmYW1pbHlfbmFtZSI6IlbDs3Jtd2lua2VsIiwiaWF0IjoxNjQzODA0NjEzLCJqdGkiOiI2Yjg3NmU0MWNmMGViNGJkIn0.MV5cDnL3JBZ4b6xr9SqiYDmZ7qtZFEWBd1vCrHzVniZeDhkyuSYc7xhf577h2S21CzNgrMp0M6JALNW9Qjnw_g";

      // Note: needed to revoke the charge item consent during the onboarding
      val erxConsentBundle = new ErxConsentBundle();
      val erpChargeItemConsentResponse =
          ErpResponse.forPayload(erxConsentBundle, ErxConsentBundle.class)
              .withStatusCode(200)
              .withHeaders(Map.of())
              .usedJwt(jwt)
              .andValidationResult(createEmptyValidationResult());

      // Note: needed to revoke the EU consent during onboarding (case when consent was revoked
      // before)
      val euConsentBundleSpy = spy(new EuConsentBundle());
      val erpEUConsentGetResponse =
          ErpResponse.forPayload(euConsentBundleSpy, EuConsentBundle.class)
              .withStatusCode(200)
              .withHeaders(Map.of())
              .usedJwt(jwt)
              .andValidationResult(createEmptyValidationResult());

      when(euConsentBundleSpy.getConsent()).thenReturn(Optional.empty());
      when(erpClient.request(any(ConsentGetCommand.class)))
          .thenReturn(erpChargeItemConsentResponse);
      when(erpClient.getAcceptMime()).thenReturn(MediaType.ACCEPT_FHIR_XML);
      when(erpClient.getSendMime()).thenReturn(MediaType.FHIR_XML);
      when(erpClient.getClientType()).thenReturn(ClientType.PS);
      when(erpClient.getFhir()).thenReturn(new FhirParser(ValidatorType.NONE));
      when(erpClient.request(any(EuConsentGetCommand.class))).thenReturn(erpEUConsentGetResponse);

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
    assertEquals(InsuranceTypeDe.GKV, baseData.getPatientInsuranceType());
  }

  @Test
  void shouldSkipOnboarding() {
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    val config = createDefaultTestConfiguration("Bob", egk.getIccsn(), true);

    val userConfigAbility = UseConfigurationData.forUser("Bob", config);
    actor.can(userConfigAbility);
    when(app.isDisplayed(Onboarding.START_BUTTON)).thenReturn(false);
    when(app.getText(Profile.USER_KVNR)).thenReturn(egk.getKvnr());
    when(app.getText(Profile.USER_INSURANCE)).thenReturn("GKVInsurance");
    when(app.getText(FeatureFlagScreen.ENABLE_EU_REDEEM_FEATURE_SWITCH)).thenReturn("0");

    try (val erpClientFactory = mockStatic(ErpClientFactory.class)) {
      val erpClient = mock(ErpClient.class);
      val jwt =
          "eyJhbGciOiJCUDI1NlIxIiwidHlwIjoiYXQrSldUIiwia2lkIjoicHVrX2lkcF9zaWcifQ.eyJzdWIiOiJJWERkLTNyUVpLS0ZYVWR4R0dqNFBERG9WNk0wUThaai1xdzF2cjF1XzU4IiwicHJvZmVzc2lvbk9JRCI6IjEuMi4yNzYuMC43Ni40LjQ5Iiwib3JnYW5pemF0aW9uTmFtZSI6ImdlbWF0aWsgTXVzdGVya2Fzc2UxR0tWTk9ULVZBTElEIiwiaWROdW1tZXIiOiJYMTEwNTAyNDE0IiwiYW1yIjpbIm1mYSIsInNjIiwicGluIl0sImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6NTUwMTEvYXV0aC9yZWFsbXMvaWRwLy53ZWxsLWtub3duL29wZW5pZC1jb25maWd1cmF0aW9uIiwiZ2l2ZW5fbmFtZSI6IlJvYmluIEdyYWYiLCJjbGllbnRfaWQiOiJlcnAtdGVzdHN1aXRlLWZkIiwiYWNyIjoiZ2VtYXRpay1laGVhbHRoLWxvYS1oaWdoIiwiYXVkIjoiaHR0cDovL2xvY2FsaG9zdDozMDAwLyIsImF6cCI6ImVycC10ZXN0c3VpdGUtZmQiLCJzY29wZSI6Im9wZW5pZCBlLXJlemVwdCIsImF1dGhfdGltZSI6MTY0MzgwNDczMywiZXhwIjoxNjQzODA1MDMzLCJmYW1pbHlfbmFtZSI6IlbDs3Jtd2lua2VsIiwiaWF0IjoxNjQzODA0NjEzLCJqdGkiOiI2Yjg3NmU0MWNmMGViNGJkIn0.MV5cDnL3JBZ4b6xr9SqiYDmZ7qtZFEWBd1vCrHzVniZeDhkyuSYc7xhf577h2S21CzNgrMp0M6JALNW9Qjnw_g";

      // Note: needed to revoke the charge item consent during the onboarding
      val erxConsentBundle = new ErxConsentBundle();
      val erpChargeItemConsentResponse =
          ErpResponse.forPayload(erxConsentBundle, ErxConsentBundle.class)
              .withStatusCode(200)
              .withHeaders(Map.of())
              .usedJwt(jwt)
              .andValidationResult(createEmptyValidationResult());

      // Note: needed to revoke the EU consent during onboarding (case when consent was revoked
      // before)
      val euConsentBundleSpy = spy(new EuConsentBundle());
      val erpEUConsentGetResponse =
          ErpResponse.forPayload(euConsentBundleSpy, EuConsentBundle.class)
              .withStatusCode(200)
              .withHeaders(Map.of())
              .usedJwt(jwt)
              .andValidationResult(createEmptyValidationResult());

      when(euConsentBundleSpy.getConsent()).thenReturn(Optional.empty());
      when(erpClient.request(any(ConsentGetCommand.class)))
          .thenReturn(erpChargeItemConsentResponse);
      when(erpClient.getAcceptMime()).thenReturn(MediaType.ACCEPT_FHIR_XML);
      when(erpClient.getSendMime()).thenReturn(MediaType.FHIR_XML);
      when(erpClient.getClientType()).thenReturn(ClientType.PS);
      when(erpClient.getFhir()).thenReturn(new FhirParser(ValidatorType.NONE));
      when(erpClient.request(any(EuConsentGetCommand.class))).thenReturn(erpEUConsentGetResponse);

      erpClientFactory
          .when(() -> ErpClientFactory.createErpClient(any(), any(PatientConfiguration.class)))
          .thenReturn(erpClient);
      SetUpDevice.forEnvironment(environment)
          .withInsuranceType("GKV")
          .byMappingVirtualEgkFrom(smartcards)
          .performAs(actor);
    }

    verify(app, times(0)).tap(Onboarding.START_BUTTON);
  }
}
