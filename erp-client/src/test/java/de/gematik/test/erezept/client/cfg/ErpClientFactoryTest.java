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

package de.gematik.test.erezept.client.cfg;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mockConstruction;

import de.gematik.test.erezept.client.ClientType;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

class ErpClientFactoryTest {

  @Test
  void shouldCcreateErpClient() {
    val cfg = new ErpClientConfiguration();
    cfg.setClientId("gematikTestPs");
    cfg.setRedirectUrl("https://gemtest.de");
    cfg.setDiscoveryDocumentUrl("https://idp.gemtest.de/.well-known/openid-configuration");

    cfg.setFdBaseUrl("https://gemtest.de");
    cfg.setTslBaseUrl("not required!!"); // TODO: seems to be not required anymore
    cfg.setClientType(ClientType.PS);
    cfg.setXApiKey("123");
    cfg.setUserAgent("Test-Agent");

    cfg.setAcceptMime("application/fhir+xml;q=1.0");
    cfg.setSendMime("application/fhir+json");

    // mocking FhirParser-Constructor saves some execution time for reading Fhir-Profiles
    try (MockedConstruction<FhirParser> mocked = mockConstruction(FhirParser.class)) {
      assertDoesNotThrow(() -> ErpClientFactory.createErpClient(cfg));
    }
  }
}
