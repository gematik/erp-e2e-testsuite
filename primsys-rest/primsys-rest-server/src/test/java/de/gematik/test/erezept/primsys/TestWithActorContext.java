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
 */

package de.gematik.test.erezept.primsys;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.config.ConfigurationReader;
import de.gematik.test.erezept.config.dto.actor.DoctorConfiguration;
import de.gematik.test.erezept.config.dto.actor.HealthInsuranceConfiguration;
import de.gematik.test.erezept.config.dto.actor.PharmacyConfiguration;
import de.gematik.test.erezept.config.dto.actor.PsActorConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.BackendRouteConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.config.dto.primsys.PrimsysConfigurationDto;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.primsys.actors.Doctor;
import de.gematik.test.erezept.primsys.actors.HealthInsurance;
import de.gematik.test.erezept.primsys.actors.Pharmacy;
import de.gematik.test.erezept.primsys.model.ActorContext;
import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.konnektor.cfg.KonnektorFactory;
import java.util.List;
import lombok.val;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.stubbing.Answer;

public abstract class TestWithActorContext extends ErpFhirParsingTest {

  protected static final PrimsysConfigurationDto configDto;
  protected static final EnvironmentConfiguration env;
  protected static final SmartcardArchive sca;
  protected static final Konnektor softKonn;

  static {
    configDto = ConfigurationReader.forPrimSysConfiguration().create();
    env = createActiveEnvironment();
    sca = SmartcardArchive.fromResources();
    softKonn = KonnektorFactory.createSoftKon();

    val mockFactory = mock(PrimSysRestFactory.class);

    try (val erpClientFactoryMockedStatic = mockStatic(ErpClientFactory.class)) {
      erpClientFactoryMockedStatic
          .when(() -> ErpClientFactory.createErpClient(any(), any(PsActorConfiguration.class)))
          .thenReturn(mock(ErpClient.class));

      val mockedDoctors = TestWithActorContext.createDoctorActors();
      val mockedPharmacies = TestWithActorContext.createPharmacyActors();
      val mockKtrs = TestWithActorContext.createKtrActors();

      when(mockFactory.getActiveEnvironment()).thenReturn(env);
      when(mockFactory.createDoctorActors()).thenReturn(mockedDoctors);
      when(mockFactory.createPharmacyActors()).thenReturn(mockedPharmacies);
      when(mockFactory.createHealthInsuranceActors()).thenReturn(mockKtrs);

      ActorContext.init(mockFactory);
    }
  }

  private static List<Doctor> createDoctorActors() {
    return TestWithActorContext.configDto.getActors().getDoctors().stream()
        .map(TestWithActorContext::createMockDoctor)
        .toList();
  }

  private static List<Pharmacy> createPharmacyActors() {
    return TestWithActorContext.configDto.getActors().getPharmacies().stream()
        .map(TestWithActorContext::createMockedPharmacy)
        .toList();
  }

  private static List<HealthInsurance> createKtrActors() {
    return TestWithActorContext.configDto.getActors().getHealthInsurances().stream()
        .map(TestWithActorContext::createMockedHealthInsurance)
        .toList();
  }

  private static Doctor createMockDoctor(DoctorConfiguration cfg) {
    return new Doctor(cfg, env, softKonn, sca);
  }

  private static Pharmacy createMockedPharmacy(PharmacyConfiguration cfg) {
    return new Pharmacy(cfg, env, softKonn, sca);
  }

  private static HealthInsurance createMockedHealthInsurance(HealthInsuranceConfiguration cfg) {
    return new HealthInsurance(cfg, env, softKonn, sca);
  }

  private static EnvironmentConfiguration createActiveEnvironment() {
    val activeEnvironment = new EnvironmentConfiguration();
    activeEnvironment.setName("Unit-Test Mock");

    val ti = new BackendRouteConfiguration();
    ti.setDiscoveryDocumentUrl(
        "http://127.0.0.1:8590/auth/realms/idp/.well-known/openid-configuration");
    ti.setFdBaseUrl("http://127.0.0.1:3000");
    ti.setSubscriptionServiceUrl("wss://127.0.0.1:3000/subscription");
    ti.setUserAgent("eRp-Testsuite");
    activeEnvironment.setTi(ti);

    val internet = new BackendRouteConfiguration();
    internet.setDiscoveryDocumentUrl(
        "http://127.0.0.1:8590/auth/realms/idp/.well-known/openid-configuration");
    internet.setFdBaseUrl("http://127.0.0.1:3000");
    internet.setSubscriptionServiceUrl("wss://127.0.0.1:3000/subscription");
    internet.setUserAgent("eRp-Testsuite");
    internet.setXapiKey("xapikey");
    activeEnvironment.setTi(internet);

    return activeEnvironment;
  }

  @BeforeEach
  void resetMocks() {
    ActorContext.getInstance()
        .getActors()
        .forEach(
            a -> {
              val erpClient = a.getClient();
              reset(erpClient); // clear all previous mocks from the client

              // now mock the fhir parser back in
              when(erpClient.getFhir()).thenReturn(parser);
              when(erpClient.encode(any(), any()))
                  .thenAnswer(
                      (Answer<String>)
                          invocationOnMock -> {
                            val args = invocationOnMock.getArguments();
                            return parser.encode((IBaseResource) args[0], (EncodingType) args[1]);
                          });
            });
  }
}
