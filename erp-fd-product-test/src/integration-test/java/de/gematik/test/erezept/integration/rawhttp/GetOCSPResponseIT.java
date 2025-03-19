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

package de.gematik.test.erezept.integration.rawhttp;

import static de.gematik.test.core.expectations.verifier.rawhttpverifier.OCSPRespVerifier.*;
import static de.gematik.test.core.expectations.verifier.rawhttpverifier.RawHttpResponseVerifier.returnCode;
import static de.gematik.test.erezept.actions.rawhttpactions.OcspRequestParameters.passCert;
import static de.gematik.test.erezept.actions.rawhttpactions.OcspRequestParameters.passConcreteParams;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.rawhttpactions.GetOCSPRequest;
import de.gematik.test.erezept.actions.rawhttpactions.VerifyRawHttp;
import de.gematik.test.erezept.actions.rawhttpactions.pki.OCSPBodyWrapper;
import de.gematik.test.erezept.actors.PatientActor;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("OCSP Response Test")
@Tag("PKI")
class GetOCSPResponseIT extends ErpTest {

  private static final String ISSUER_NAME = "GEM.SMCB-CA51 TEST-ONLY";
  private static final String CERT_SERIAL_NR = "206706423598360";

  @Actor(name = "Günther Angermänn")
  private PatientActor guenther;

  @TestcaseId("ERP_GET_OCSP_RESPONSE_01")
  @Test
  @DisplayName(
      "Überprüft, ob der Fachdienst ein OCSP_RESPONSE zurückliefert dessen Status `GOOD`ist")
  void verifyOcspResponseStatus() {
    this.config.equipForOCSP(guenther);
    val x509Guenther = guenther.getEgk().getAutCertificate().getX509Certificate();
    val response = guenther.performs(GetOCSPRequest.with(passCert(x509Guenther)));
    guenther.attemptsTo(
        VerifyRawHttp.that(response, OCSPBodyWrapper.class)
            .responseWith(returnCode(200))
            .and(ocspResponseStatusIsGood())
            .isCorrect());
  }

  @TestcaseId("ERP_GET_OCSP_RESPONSE_02")
  @Test
  @DisplayName(
      "Negativ Test, Überprüft, ob der Fachdienst ein OCSP_RESPONSE zurückliefert wenn ein falscher"
          + " IssuerCn Query Parameter übergeben wird")
  void verifyOcspResponseWithWrongQueryIssuerCn() {
    this.config.equipForOCSP(guenther);
    val resp =
        guenther.performs(GetOCSPRequest.with(passConcreteParams("GEM.SMCB-CA51", CERT_SERIAL_NR)));
    guenther.attemptsTo(
        VerifyRawHttp.that(resp, OCSPBodyWrapper.class)
            .responseWith(returnCode(400))
            .and(responseContainsDescription("Issuer CN is not valid or unknown"))
            .isCorrect());
  }

  @TestcaseId("ERP_GET_OCSP_RESPONSE_03")
  @Test
  @DisplayName(
      "Negativ Test, Überprüft, ob der Fachdienst ein OCSP_RESPONSE zurückliefert wenn falsche"
          + " Zertifikats-Seriennummer im Query Parameter übergeben werden")
  void verifyOcspResponseWithWrongQueryCertSerial() {
    this.config.equipForOCSP(guenther);
    val resp =
        guenther.performs(GetOCSPRequest.with(passConcreteParams(ISSUER_NAME, "CERT_SERIAL_NR")));
    guenther.attemptsTo(
        VerifyRawHttp.that(resp, OCSPBodyWrapper.class)
            .responseWith(returnCode(400))
            .and(responseContainsDescription("Query parameter serial-nr is not a number"))
            .isCorrect());
  }

  @TestcaseId("ERP_GET_OCSP_RESPONSE_04")
  @Test
  @DisplayName(
      "Negativ Test, Überprüft, ob der Fachdienst ein OCSP_RESPONSE zurückliefert wenn keine Query"
          + " Parameter übergeben werden")
  void verifyOcspResponseWithNoQuery() {
    this.config.equipForOCSP(guenther);
    val resp = guenther.performs(GetOCSPRequest.with(passConcreteParams("", "")));
    guenther.attemptsTo(
        VerifyRawHttp.that(resp, OCSPBodyWrapper.class)
            .responseWith(returnCode(400))
            .and(responseContainsDescription("Missing mandatory query parameter issuer-cn"))
            .isCorrect());
  }

  @TestcaseId("ERP_GET_OCSP_RESPONSE_05")
  @Test
  @DisplayName(
      "Negativ Test, Überprüft, ob der Fachdienst ein OCSP_RESPONSE zurückliefert wenn falsche"
          + " Query Parameter übergeben werden")
  void verifyOcspResponseWithWrongQuery() {
    this.config.equipForOCSP(guenther);
    val resp =
        guenther.performs(GetOCSPRequest.with(passConcreteParams(CERT_SERIAL_NR, ISSUER_NAME)));
    guenther.attemptsTo(
        VerifyRawHttp.that(resp, OCSPBodyWrapper.class)
            .responseWith(returnCode(400))
            .and(responseContainsDescription("Query parameter serial-nr is not a number"))
            .isCorrect());
  }
}
