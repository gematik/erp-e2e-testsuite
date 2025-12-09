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

package de.gematik.test.core.expectations.verifier.pki;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.pki.gemlibpki.tsl.TslConverter;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.actions.rawhttpactions.pki.TslListWrapper;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.*;

class TSLVerifierTest {

  private static final String CORRECT_CERT_BODY_PATH = "tslexamples/ECC_RSA_TSLCerts.txt";
  private static final String MANIPULATED_CERT_BODY_PATH =
      "tslexamples/ECC_RSA_TSLCerts_Without_ExtensionEndsWith202Or203.txt";

  @Test
  void shouldNotInstantiateTaskVerifier() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(TSLVerifier.class));
  }

  @SneakyThrows
  @Test
  void verifierShouldWork() {
    CoverageReporter.getInstance().startTestcase("don't care");
    val respBody = ResourceLoader.readFileFromResource(CORRECT_CERT_BODY_PATH);
    val tslLW = new TslListWrapper(TslConverter.bytesToTslUnsigned(respBody.getBytes()));
    val step = TSLVerifier.containsX509Certs();
    assertDoesNotThrow(() -> step.apply(tslLW));
  }

  @SneakyThrows
  @Test
  void verifierShouldThrowWhileMissingFilterMatch() {
    CoverageReporter.getInstance().startTestcase("don't care");
    val respBody = ResourceLoader.readFileFromResource(MANIPULATED_CERT_BODY_PATH);
    val tslLW = new TslListWrapper(TslConverter.bytesToTslUnsigned(respBody.getBytes()));
    val step = TSLVerifier.containsX509Certs();
    assertThrows(AssertionError.class, () -> step.apply(tslLW));
  }
}
