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

package de.gematik.test.konnektor.cfg;

import static org.junit.Assert.*;

import de.gematik.test.konnektor.profile.ProfileType;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

public class RemoteKonnetorConfigurationTest {

  private TLSConfiguration tls;
  private ContextConfiguration context;

  @Before
  public void setUp() {
    tls = new TLSConfiguration();
    tls.setKeyStore("konsim_mandant1_keystore.p12");
    tls.setKeyStorePassword("00");
    tls.setTrustStore("erpe2e_truststore.p12");
    tls.setTrustStorePassword("123456");

    context = new ContextConfiguration();
    context.setMandantId("Mandant1");
    context.setClientSystemId("CS1");
    context.setWorkplaceId("WP1");
  }

  @Test
  public void shouldThrowOnMissingAddress() {
    val rkc = new RemoteKonnetorConfiguration();
    rkc.setProtocol("https");
    rkc.setProfile("KONSIM");
    rkc.setContext(context);
    rkc.setTls(tls);

    assertThrows(NullPointerException.class, rkc::create);
  }

  @Test
  public void shouldThrowOnMissingProtocol() {
    val rkc = new RemoteKonnetorConfiguration();
    //      rkc.setProtocol("https");
    rkc.setProfile("KONSIM");
    rkc.setContext(context);
    rkc.setTls(tls);

    assertThrows(NullPointerException.class, rkc::create);
  }

  @Test
  public void shouldThrowOnHttpsWithoutTls() {
    val rkc = new RemoteKonnetorConfiguration();
    rkc.setAddress("localhost:443");
    rkc.setProtocol("https");
    rkc.setProfile("KONSIM");
    rkc.setContext(context);
    //    rkc.setTls(tls);

    assertThrows(IllegalArgumentException.class, rkc::create);
  }

  @Test
  public void shouldCreateHttpWithoutTls() {
    val rkc = new RemoteKonnetorConfiguration();
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
    val rkc = new RemoteKonnetorConfiguration();
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
}
