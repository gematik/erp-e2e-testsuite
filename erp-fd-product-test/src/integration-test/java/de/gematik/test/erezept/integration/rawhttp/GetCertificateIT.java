/*
 *  Copyright 2023 gematik GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package de.gematik.test.erezept.integration.rawhttp;

import static de.gematik.test.core.expectations.verifier.rawhttpverifier.RawHttpRespBodyCertVerifier.bodyContainsAddRoots;
import static de.gematik.test.core.expectations.verifier.rawhttpverifier.RawHttpRespBodyCertVerifier.bodyContainsCaCerts;
import static de.gematik.test.core.expectations.verifier.rawhttpverifier.RawHttpResponseVerifier.returnCode;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.rawhttpactions.CallCertificateFromBackend;
import de.gematik.test.erezept.actions.rawhttpactions.VerifyRawHttp;
import de.gematik.test.erezept.actions.rawhttpactions.pki.PKICertificatesDTOEnvelop;
import de.gematik.test.erezept.actors.PatientActor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Try to get CA Certificate from FD")
public class GetCertificateIT extends ErpTest {

  @Actor(name = "Sina Hüllmann")
  private PatientActor patient;

  @TestcaseId("GET_CA_CERTIFICATE_01")
  @Test
  @DisplayName("Der Fachdienst liefert ein CA Certificate zurück")
  void verifyOcspResponse() {
    this.config.equipWithRawHttp(patient);

    val resp = patient.performs(CallCertificateFromBackend.withRootCa("GEM.RCA3 TEST-ONLY"));

    patient.attemptsTo(
        VerifyRawHttp.that(resp, PKICertificatesDTOEnvelop.class)
            .responseWith(returnCode(200))
            .and(bodyContainsAddRoots())
            .and(bodyContainsCaCerts())
            .isCorrect());
  }
}
