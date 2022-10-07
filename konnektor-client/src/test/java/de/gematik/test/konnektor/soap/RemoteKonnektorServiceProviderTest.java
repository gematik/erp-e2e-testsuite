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

package de.gematik.test.konnektor.soap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import de.gematik.test.konnektor.cfg.BasicAuthConfiguration;
import de.gematik.test.konnektor.cfg.TLSConfiguration;
import de.gematik.test.konnektor.profile.KonSimProfile;
import de.gematik.test.konnektor.profile.ProfileType;
import java.net.URL;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;

public class RemoteKonnektorServiceProviderTest {

  @Test
  @SneakyThrows
  public void shouldProvideServices() {
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
    val rksp = sPB.build();

    // Well, just ensures code coverage
    assertEquals(ProfileType.KONSIM, rksp.getType());
    assertNotNull(rksp.getAuthSignatureService());
    assertNotNull(rksp.getCertificateService());
    assertNotNull(rksp.getEventService());
    assertNotNull(rksp.getSignatureService());
    assertNotNull(rksp.getCardService());
    assertNotNull(rksp.getCardTerminalService());
    assertEquals("user1", rksp.getUsername());
    assertEquals("password1", rksp.getPassword());
  }
}
