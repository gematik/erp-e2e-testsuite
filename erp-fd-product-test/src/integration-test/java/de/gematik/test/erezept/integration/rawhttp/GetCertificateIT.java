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

package de.gematik.test.erezept.integration.rawhttp;

import static de.gematik.test.core.expectations.verifier.rawhttpverifier.RawHttpRespBodyCertVerifier.bodyContainsAddRoots;
import static de.gematik.test.core.expectations.verifier.rawhttpverifier.RawHttpRespBodyCertVerifier.bodyContainsCaCerts;
import static de.gematik.test.core.expectations.verifier.rawhttpverifier.RawHttpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.rawhttpverifier.RawHttpResponseVerifier.returnCodeIsIn;

import de.gematik.bbriccs.utils.CertificateAuthoritySupplier;
import de.gematik.bbriccs.utils.RootCertificateAuthorityList;
import de.gematik.bbriccs.utils.TiTrustedEnvironmentAnchor;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.rawhttpactions.CallCertificateFromBackend;
import de.gematik.test.erezept.actions.rawhttpactions.GetTslList;
import de.gematik.test.erezept.actions.rawhttpactions.VerifyRawHttp;
import de.gematik.test.erezept.actions.rawhttpactions.pki.PKICertificatesDTOEnvelop;
import de.gematik.test.erezept.actors.PatientActor;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@Tag("PKI")
class GetCertificateIT extends ErpTest {

  private static final Function<Integer, String> subjectCNFun =
      value -> MessageFormat.format("GEM.RCA{0} TEST-ONLY", value);

  private static RootCertificateAuthorityList rootCAs;
  private static Set<X509Certificate> expectedCAs;

  @BeforeEach
  void setup() {
    if (rootCAs == null || expectedCAs == null) {
      this.config.equipForTslDownload(patient);
      expectedCAs =
          patient
              .performs(GetTslList.direct())
              .getBody()
              .getFilteredForFDSicAndEncX509Certificates();

      var rootCABuilder = CertificateAuthoritySupplier.builder().useInternet();

      switch (this.config.getActiveEnvironment().getName()) {
        case "TU":
          rootCAs =
              rootCABuilder
                  .withEnvironmentAnchor(TiTrustedEnvironmentAnchor.TU)
                  .getRootCAsFromBackend();
          break;
        case "RU", "RU-DEV":
          rootCAs =
              rootCABuilder
                  .withEnvironmentAnchor(TiTrustedEnvironmentAnchor.RU)
                  .getRootCAsFromBackend();
          break;
        default:
          log.warn("No valid environment found. Using default environment: TU");
          rootCAs = rootCABuilder.getRootCAsFromBackend();
          break;
      }
    }
  }

  @Actor(name = "Sina Hüllmann")
  private PatientActor patient;

  @TestcaseId("GET_PKI_CERTIFICATES_01")
  @Test
  @DisplayName("Der Fachdienst liefert alle KompCAs der TSL zurück")
  void verifyCaCerts() {
    this.config.equipWithRawHttp(patient);
    val resp = patient.performs(CallCertificateFromBackend.withRootCa(subjectCNFun.apply(3)));
    patient.attemptsTo(
        VerifyRawHttp.that(resp, PKICertificatesDTOEnvelop.class)
            .responseWith(returnCode(200))
            .and(bodyContainsCaCerts(expectedCAs))
            .isCorrect());
  }

  @TestcaseId("GET_PKI_CERTIFICATES_02")
  @ParameterizedTest
  @DisplayName(
      "Der Fachdienst liefert die korrekte Cross-RootCA-Chain zu allen KompCAs von einer gegebenen"
          + " RootCA zurück")
  @MethodSource("rootCASubjectCNs")
  void verifyAddRoots(String rootCaSubjectCN) {

    this.config.equipWithRawHttp(patient);

    val resp = patient.performs(CallCertificateFromBackend.withRootCa(rootCaSubjectCN));

    val chainRootCrossCAs =
        rootCAs.getChainOfCrossRootCAByCompCAs(
            expectedCAs, Objects.requireNonNull(rootCAs.getRootCABy(rootCaSubjectCN)));

    patient.attemptsTo(
        VerifyRawHttp.that(resp, PKICertificatesDTOEnvelop.class)
            .responseWith(returnCode(200))
            .and(bodyContainsAddRoots(chainRootCrossCAs))
            .isCorrect());
  }

  @TestcaseId("GET_PKI_CERTIFICATES_03")
  @ParameterizedTest
  @DisplayName("Der Fachdienst liefert einen StatusCode 40x für eine invalide Anfrage zurück")
  @MethodSource("invalidRootCASubjectCNs")
  void verifyInvalidInput(String invalidSubjectCN) {
    this.config.equipWithRawHttp(patient);

    val resp = patient.performs(CallCertificateFromBackend.withRootCa(invalidSubjectCN));
    patient.attemptsTo(
        VerifyRawHttp.that(resp, PKICertificatesDTOEnvelop.class)
            .responseWith(returnCodeIsIn(400, 404))
            .isCorrect());
  }

  public static Stream<Arguments> rootCASubjectCNs() {
    return IntStream.range(1, 8).mapToObj(it -> Arguments.of(subjectCNFun.apply(it)));
  }

  public static Stream<Arguments> invalidRootCASubjectCNs() {
    return Stream.of(
        Arguments.of(subjectCNFun.apply(-1)),
        Arguments.of(subjectCNFun.apply(0)),
        Arguments.of(subjectCNFun.apply(9)),
        Arguments.of(subjectCNFun.apply(99)),
        Arguments.of(""),
        Arguments.of(" "),
        Arguments.of(subjectCNFun.apply(3).repeat(11)));
  }
}
