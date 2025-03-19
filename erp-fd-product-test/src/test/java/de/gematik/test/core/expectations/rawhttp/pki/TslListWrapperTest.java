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

package de.gematik.test.core.expectations.rawhttp.pki;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.pki.gemlibpki.tsl.TslConverter;
import de.gematik.test.erezept.actions.rawhttpactions.pki.TslListWrapper;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TslListWrapperTest {

  private static final String CORRECT_CERT_BODY_PATH = "tslexamples/ECC_RSA_TSLCerts.txt";
  private static final String CERT_BODY_PATH_WithoutExtensions =
      "tslexamples/ECC_RSA_TSLCerts_Without_ExtensionEndsWith202Or203.txt";

  private static final String CERT_BODY_PATH_Without_202_Ext =
      "tslexamples/ECC_RSA_TSLCerts_Without_ExtensionEndsWith202Or203.txt";

  private TslListWrapper tslListWrapper;

  @BeforeEach
  void setup() {
    val respBody = ResourceLoader.readFileFromResource(CORRECT_CERT_BODY_PATH);
    tslListWrapper = new TslListWrapper(TslConverter.bytesToTslUnsigned(respBody.getBytes()));
  }

  @Test
  void shouldInstantiateConstructor() {
    assertNotNull(tslListWrapper);
  }

  @Test
  void shouldGetProvider() {
    assertNotNull(tslListWrapper.getProvider());
  }

  @Test
  void shouldGetListWithoutExtensions() {
    val respBody = ResourceLoader.readFileFromResource(CERT_BODY_PATH_WithoutExtensions);
    val tslListWrapper1 = new TslListWrapper(TslConverter.bytesToTslUnsigned(respBody.getBytes()));
    assertTrue(tslListWrapper1.getFilteredForFDSicAndEncX509Certificates().isEmpty());
  }

  @Test
  void shouldGetExtensionsThatEndsWith203() {
    val respBody = ResourceLoader.readFileFromResource(CERT_BODY_PATH_Without_202_Ext);
    val tslListWrapper1 = new TslListWrapper(TslConverter.bytesToTslUnsigned(respBody.getBytes()));
    assertTrue(tslListWrapper1.getFilteredForFDSicAndEncX509Certificates().isEmpty());
  }
}
