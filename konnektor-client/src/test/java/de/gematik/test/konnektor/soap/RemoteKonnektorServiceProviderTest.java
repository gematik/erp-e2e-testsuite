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

package de.gematik.test.konnektor.soap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import com.sun.xml.ws.client.sei.SEIStub;
import de.gematik.test.erezept.config.dto.konnektor.BasicAuthConfiguration;
import de.gematik.test.erezept.config.dto.konnektor.TLSConfiguration;
import de.gematik.test.konnektor.profile.KonSimProfile;
import de.gematik.test.konnektor.profile.ProfileType;
import jakarta.xml.ws.WebServiceClient;
import java.net.URL;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RemoteKonnektorServiceProviderTest {

  private static RemoteKonnektorServiceProvider rksp;

  @BeforeAll
  @SneakyThrows
  static void setupBC() {
    val tlsConfig = new TLSConfiguration();
    tlsConfig.setKeyStore("konsim_mandant1_keystore.p12");
    tlsConfig.setKeyStorePassword("00");
    tlsConfig.setTrustStore("konsim_truststore.jks");
    tlsConfig.setTrustStorePassword("gematik");
    val trust = TrustProvider.from(tlsConfig);

    val basicAuth = new BasicAuthConfiguration();
    basicAuth.setUsername("user1");
    basicAuth.setPassword("password1");
    val url = new URL("https://localhost");
    val sPB = RemoteKonnektorServiceProvider.of(url, new KonSimProfile());

    sPB.trustProvider(trust);
    sPB.username(basicAuth.getUsername());
    sPB.password(basicAuth.getPassword());

    rksp = sPB.build();
  }

  @Test
  void shouldProvideServices() {
    // Well, just ensures code coverage
    assertEquals(ProfileType.KONSIM, rksp.getType());
    assertNotNull(rksp.getAuthSignatureService());
    assertNotNull(rksp.getCertificateService());
    assertNotNull(rksp.getEventService());
    assertNotNull(rksp.getSignatureService());
    assertNotNull(rksp.getCardService());
    assertNotNull(rksp.getCardTerminalService());
    assertNotNull(rksp.getVSDServicePortType());
    assertNotNull(rksp.getEncryptionServicePortType());
    assertEquals("user1", rksp.getUsername());
    assertEquals("password1", rksp.getPassword());
  }

  @SuppressWarnings("java:S1186")
  static class TestServiceWithoutAnnotation {
    public TestServiceWithoutAnnotation() {}
  }

  @WebServiceClient(
      name = "",
      targetNamespace = "",
      wsdlLocation = "file:/path/to/nonexistent.wsdl")
  @SuppressWarnings("java:S1186")
  static class TestServiceNonExistentWsdl {
    public TestServiceNonExistentWsdl() {}
  }

  @SuppressWarnings("java:S1186")
  @WebServiceClient(name = "", targetNamespace = "", wsdlLocation = "")
  static class TestServiceEmptyWsdlLocation {
    public TestServiceEmptyWsdlLocation() {}
  }

  @SneakyThrows
  @ParameterizedTest
  @ValueSource(
      classes = {
        TestServiceWithoutAnnotation.class,
        TestServiceNonExistentWsdl.class,
        TestServiceEmptyWsdlLocation.class
      })
  void testResolveWsdlLocation(Class<?> testServiceClass) {
    val test = mock(SEIStub.class);
    val result = rksp.createAndConfigurePort(testServiceClass, s -> test, "/test");
    assertNotNull(result);
  }
}
