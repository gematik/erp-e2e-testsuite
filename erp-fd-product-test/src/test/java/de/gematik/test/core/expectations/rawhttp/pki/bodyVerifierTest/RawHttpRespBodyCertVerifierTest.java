/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.core.expectations.rawhttp.pki.bodyVerifierTest;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.verifier.rawhttpverifier.RawHttpRespBodyCertVerifier;
import de.gematik.test.erezept.actions.rawhttpactions.pki.PKICertificatesDTOEnvelop;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import java.util.List;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@WireMockTest()
class RawHttpRespBodyCertVerifierTest extends ParsingTest {

  private static final String CERT_BODY_PATH = "certexamples/86028_ExampleData.json";

  private PKICertificatesDTOEnvelop body;

  @SneakyThrows
  @BeforeEach
  void setup() {
    CoverageReporter.getInstance().startTestcase("don't care");
    val respBody = ResourceLoader.readFileFromResource(CERT_BODY_PATH);
    val obMapper = new ObjectMapper();
    body = obMapper.readValue(respBody, PKICertificatesDTOEnvelop.class);
  }

  @Test
  void shouldNotInstantiateTaskVerifier() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(RawHttpRespBodyCertVerifier.class));
  }

  @SneakyThrows
  @Test
  @TestcaseId("ut_httpResponseCertVerifier_01")
  @DisplayName("Positive Unit Test for an RawHttpBodyCertVerify to check for add_roots")
  void verifierShouldWork() {
    val step = RawHttpRespBodyCertVerifier.bodyContainsAddRoots();
    step.apply(body);
  }

  @Test
  @TestcaseId("ut_httpResponseCertVerifier_02")
  @DisplayName("Positive Unit Test for an RawHttpBodyCertVerify to check for ca_certs")
  void verifierShouldWorkWithOtherString() {
    val step = RawHttpRespBodyCertVerifier.bodyContainsCaCerts();
    step.apply(body);
  }

  @Test
  @TestcaseId("ut_httpResponseCertVerifier_03")
  @DisplayName("negative Unit Test should throw Exception caused by missing entries")
  void verifierShouldThrowWhileEmptyLists() {
    val body2 = new PKICertificatesDTOEnvelop();
    val step = RawHttpRespBodyCertVerifier.bodyContainsAddRoots();
    assertThrows(AssertionError.class, () -> step.apply(body2));
  }

  @Test
  @TestcaseId("ut_httpResponseCertVerifier_04")
  @DisplayName("negative Unit Test should throw Exception caused by missing add_roots")
  void verifierShouldThrowMissingAddRoots() {
    val body2 = new PKICertificatesDTOEnvelop();
    body2.setCaCerts(List.of("123"));
    val step = RawHttpRespBodyCertVerifier.bodyContainsAddRoots();
    assertThrows(AssertionError.class, () -> step.apply(body2));
  }

  @Test
  @TestcaseId("ut_httpResponseCertVerifier_05")
  @DisplayName("negative Unit Test should throw Exception caused by missing ca_certs")
  void verifierShouldThrow() {
    val body2 = new PKICertificatesDTOEnvelop();
    body2.setAddRoots(List.of("123"));
    val step = RawHttpRespBodyCertVerifier.bodyContainsCaCerts();
    assertThrows(AssertionError.class, () -> step.apply(body2));
  }
}
