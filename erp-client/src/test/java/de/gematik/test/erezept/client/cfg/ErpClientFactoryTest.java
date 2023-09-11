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

package de.gematik.test.erezept.client.cfg;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mockConstruction;

import de.gematik.test.erezept.config.dto.actor.DoctorConfiguration;
import de.gematik.test.erezept.config.dto.actor.PatientConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.BackendRouteConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

class ErpClientFactoryTest {

  private EnvironmentConfiguration createEnvironmentConfiguration() {
    val env = new EnvironmentConfiguration();
    env.setTslBaseUrl("not required!!");

    val envRoute = new BackendRouteConfiguration();
    env.setTi(envRoute);
    env.setInternet(envRoute);
    envRoute.setClientId("gematikTestPs");
    envRoute.setRedirectUrl("https://gemtest.de");
    envRoute.setDiscoveryDocumentUrl("https://idp.gemtest.de/.well-known/openid-configuration");
    envRoute.setFdBaseUrl("https://gemtest.de");
    envRoute.setUserAgent("Test-Agent");
    envRoute.setXapiKey("123");

    return env;
  }

  @Test
  void shouldCreateDoctorErpClient() {
    val env = createEnvironmentConfiguration();
    val actor = new DoctorConfiguration();

    // mocking FhirParser-Constructor saves some execution time for reading Fhir-Profiles
    try (MockedConstruction<FhirParser> mocked = mockConstruction(FhirParser.class)) {
      assertDoesNotThrow(() -> ErpClientFactory.createErpClient(env, actor));
    }
  }

  @Test
  void shouldCreatePatientErpClient() {
    val env = createEnvironmentConfiguration();
    val actor = new PatientConfiguration();

    // mocking FhirParser-Constructor saves some execution time for reading Fhir-Profiles
    try (MockedConstruction<FhirParser> mocked = mockConstruction(FhirParser.class)) {
      assertDoesNotThrow(() -> ErpClientFactory.createErpClient(env, actor));
    }
  }
}
