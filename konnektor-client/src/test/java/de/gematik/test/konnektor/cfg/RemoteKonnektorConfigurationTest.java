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

package de.gematik.test.konnektor.cfg;

import static org.junit.Assert.*;

import de.gematik.test.cardterminal.cfg.*;
import de.gematik.test.erezept.config.dto.konnektor.TLSConfiguration;
import de.gematik.test.konnektor.profile.*;
import java.util.*;
import lombok.*;
import org.junit.*;

public class RemoteKonnektorConfigurationTest {

  private TLSConfiguration tls;
  private KonnektorContextConfiguration context;
  private CardTerminalClientConfiguration catsCfg;

  @Before
  public void setUp() {
    tls = new TLSConfiguration();
    tls.setKeyStore("konsim_mandant1_keystore.p12");
    tls.setKeyStorePassword("00");
    tls.setTrustStore("erpe2e_truststore.p12");
    tls.setTrustStorePassword("123456");

    context = new KonnektorContextConfiguration();
    context.setMandantId("Mandant1");
    context.setClientSystemId("CS1");
    context.setWorkplaceId("WP1");

    catsCfg = new CardTerminalClientConfiguration("testCt", "http://localhost");
  }

  @Test
  public void shouldThrowOnMissingAddress() {
    val rkc = new RemoteKonnektorConfiguration();
    rkc.setProtocol("https");
    rkc.setProfile("KONSIM");
    rkc.setContext(context);
    rkc.setTls(tls);

    assertThrows(NullPointerException.class, rkc::create);
  }

  @Test
  public void shouldThrowOnMissingProtocol() {
    val rkc = new RemoteKonnektorConfiguration();
    //      rkc.setProtocol("https");
    rkc.setProfile("KONSIM");
    rkc.setContext(context);
    rkc.setTls(tls);

    assertThrows(NullPointerException.class, rkc::create);
  }

  @Test
  public void shouldThrowOnHttpsWithoutTls() {
    val rkc = new RemoteKonnektorConfiguration();
    rkc.setAddress("localhost:443");
    rkc.setProtocol("https");
    rkc.setProfile("KONSIM");
    rkc.setContext(context);
    //    rkc.setTls(tls);

    assertThrows(IllegalArgumentException.class, rkc::create);
  }

  @Test
  public void shouldCreateHttpWithoutTls() {
    val rkc = new RemoteKonnektorConfiguration();
    rkc.setProtocol("http");
    rkc.setProfile("KONSIM");
    rkc.setAddress("localhost");
    rkc.setContext(context);

    val konnektor = rkc.create();
    assertNotNull(konnektor);
    assertEquals(KonnektorType.REMOTE, konnektor.getType());
  }

  @Test
  public void testSetterGetter() {
    val rkc = new RemoteKonnektorConfiguration();
    rkc.setProtocol("https");
    rkc.setAddress("localhost:443");
    rkc.setProfile("KONSIM");
    rkc.setContext(context);
    rkc.setTls(tls);

    assertEquals(KonnektorType.REMOTE, rkc.getType());
    assertEquals("localhost:443", rkc.getAddress());
    assertEquals("https", rkc.getProtocol());
    assertEquals(ProfileType.KONSIM, rkc.getProfileType());
    assertEquals(tls, rkc.getTls());
  }

  @Test
  public void getCatsConfiguration() {
    val rkc = new RemoteKonnektorConfiguration();
    rkc.setCardTerminalClientConfigurations(List.of(catsCfg));
    assertEquals(catsCfg, rkc.getCardTerminalClientConfigurations().get(0));
  }
}
