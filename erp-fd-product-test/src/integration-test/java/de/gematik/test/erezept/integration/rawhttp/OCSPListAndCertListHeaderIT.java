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

import static de.gematik.test.core.expectations.verifier.rawhttpverifier.RawHttpResponseVerifier.containsHeaderWith;
import static de.gematik.test.core.expectations.verifier.rawhttpverifier.RawHttpResponseVerifier.hasNoHeaderWith;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.rawhttpactions.GetCertListResponse;
import de.gematik.test.erezept.actions.rawhttpactions.GetOcspListResponse;
import de.gematik.test.erezept.actions.rawhttpactions.VerifyRawHttp;
import de.gematik.test.erezept.actors.PatientActor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Der OCSP_Response muss einen Sunset Header enthalten")
@Tag("PKI")
class OCSPListAndCertListHeaderIT extends ErpTest {

  @Actor(name = "Sina Hüllmann")
  private PatientActor patientActor;

  @SneakyThrows
  @TestcaseId("ERP_OCSPList_CerList_HEADER_VALIDATION_01")
  @Test
  @DisplayName(
      "Es mus sicher gestellt werden, dass beim Aufruf des Endpunktes OCSPList ein Sunset Header"
          + " zurück gegeben wird.")
  void verifyOcspResponse() {

    this.config.equipWithRawHttp(patientActor);
    val r = patientActor.asksFor(new GetOcspListResponse());
    patientActor.attemptsTo(
        VerifyRawHttp.that(r, String.class)
            .andHttp(containsHeaderWith("Sunset", "Wed, 31 Dec 2025 22:59:59 UTC", ErpAfos.A_25057))
            .isCorrect());
  }

  @SneakyThrows
  @TestcaseId("ERP_OCSPList_CerList_HEADER_VALIDATION_02")
  @Test
  @DisplayName(
      "Es mus sicher gestellt werden, dass beim Aufruf des Endpunktes CertList einen Sunset Header"
          + " zurück gibt.")
  void verifyCertListResponse() {

    this.config.equipWithRawHttp(patientActor);
    val r = patientActor.asksFor(new GetCertListResponse());
    patientActor.attemptsTo(
        VerifyRawHttp.that(r, String.class)
            .andHttp(containsHeaderWith("Sunset", "Wed, 31 Dec 2025 22:59:59 UTC", ErpAfos.A_25057))
            .isCorrect());
  }

  @SneakyThrows
  @TestcaseId("ERP_OCSPList_CerList_HEADER_VALIDATION_03")
  @Test
  @DisplayName(
      "Es mus sicher gestellt werden, dass beim Aufruf des Endpunktes OCSPList kein Deprecation"
          + " Header zurück gegeben wird.")
  void verifyOcspResponseHasNoDeprecationHeader() {

    this.config.equipWithRawHttp(patientActor);
    val r = patientActor.asksFor(new GetOcspListResponse());
    patientActor.attemptsTo(
        VerifyRawHttp.that(r, String.class)
            .andHttp(hasNoHeaderWith("Deprecation", ErpAfos.A_25057))
            .andHttp(hasNoHeaderWith("Deprication", ErpAfos.A_25057))
            .isCorrect());
  }

  @SneakyThrows
  @TestcaseId("ERP_OCSPList_CerList_HEADER_VALIDATION_04")
  @Test
  @DisplayName(
      "Es mus sicher gestellt werden, dass beim Aufruf des Endpunktes CertList kein Deprecation"
          + " Header zurück gegeben wird.")
  void verifyCertListResponseHasNoDeprecationHeader() {

    this.config.equipWithRawHttp(patientActor);
    val r = patientActor.asksFor(new GetCertListResponse());
    patientActor.attemptsTo(
        VerifyRawHttp.that(r, String.class)
            .andHttp(hasNoHeaderWith("Deprecation", ErpAfos.A_25057))
            .andHttp(hasNoHeaderWith("Deprication", ErpAfos.A_25057))
            .isCorrect());
  }
}
