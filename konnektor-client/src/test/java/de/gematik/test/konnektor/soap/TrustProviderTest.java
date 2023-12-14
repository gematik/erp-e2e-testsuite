/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.konnektor.soap;

import static org.junit.Assert.assertNotNull;

import de.gematik.test.erezept.config.dto.konnektor.TLSConfiguration;
import lombok.val;
import org.junit.Test;

public class TrustProviderTest {

  @Test
  public void shouldCreateTrustStoreFromConfig() {
    val cfg = new TLSConfiguration();
    cfg.setKeyStore("konsim_mandant1_keystore.p12");
    cfg.setKeyStorePassword("00");
    cfg.setTrustStore("konsim_truststore.jks");
    cfg.setTrustStorePassword("gematik");

    val trust = TrustProvider.from(cfg);
    val sf = trust.getSocketFactory();
    assertNotNull(sf);
  }
}
