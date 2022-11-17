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

import static org.junit.Assert.assertEquals;

import lombok.val;
import org.junit.Test;

public class TLSConfigurationTest {

  @Test
  public void testSetterGetter() {
    val tls = new TLSConfiguration();
    tls.setKeyStore("my_keystore.p12");
    tls.setKeyStorePassword("00");
    tls.setTrustStore("my_truststore.p12");
    tls.setTrustStorePassword("123456");

    assertEquals("my_keystore.p12", tls.getKeyStore());
    assertEquals("00", tls.getKeyStorePassword());
    assertEquals("my_truststore.p12", tls.getTrustStore());
    assertEquals("123456", tls.getTrustStorePassword());
  }
}