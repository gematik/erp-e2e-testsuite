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

import static de.gematik.test.core.expectations.verifier.rawhttpverifier.RawHttpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.rawhttpverifier.TSLVerifier.containsX509Certs;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.rawhttpactions.GetTslList;
import de.gematik.test.erezept.actions.rawhttpactions.VerifyRawHttp;
import de.gematik.test.erezept.actions.rawhttpactions.pki.TslListWrapper;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("TSL abrufen")
@Tag("PKI")
class GetTslListIT extends ErpTest {

  @Actor(name = "Sina Hüllmann")
  private PatientActor patient;

  @Disabled("Das SUT dieses Tests ist nicht der E-Rezept Fachdienst")
  @TestcaseId("ERP_TSL_GET_01")
  @Test
  @DisplayName("Download der TSL als Patient und Filterung")
  void verifyTSLResponseAsPatient() {

    this.config.equipForTslDownload(patient);

    // download TLS
    val resp = patient.performs(GetTslList.direct());

    patient.attemptsTo(
        VerifyRawHttp.that(resp, TslListWrapper.class)
            // Verifies that the filtered Response contains Certs
            .and(containsX509Certs())
            .responseWith(returnCode(200))
            .isCorrect());
  }

  @Disabled("Das SUT dieses Tests ist nicht der E-Rezept Fachdienst")
  @TestcaseId("ERP_TSL_GET_02")
  @Test
  @DisplayName("Download der TSL als Doktor und Filterung")
  void verifyTSLResponseAsDoctor() {
    val actorName = "Adelheid Ulmenwald";
    val doc = new DoctorActor(actorName);
    this.config.equipForTslDownload(doc);

    // download TLS
    val resp = doc.performs(GetTslList.direct());

    doc.attemptsTo(
        VerifyRawHttp.that(resp, TslListWrapper.class)
            // Verifies that the filtered Response contains Certs
            .and(containsX509Certs())
            .responseWith(returnCode(200))
            .isCorrect());
  }
}
