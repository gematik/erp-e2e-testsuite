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

package de.gematik.test.erezept.fhir.builder.eu;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.ActCode;
import de.gematik.bbriccs.fhir.de.valueset.ConsentScope;
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import lombok.val;
import org.hl7.fhir.r4.model.Consent;
import org.junit.jupiter.api.Test;

class EuConsentBuilderTest extends ErpFhirParsingTest {

  @Test
  void shouldBuildConsentWithFixedValues() {
    val kvnr = KVNR.randomPkv();
    val euConsent =
        EuConsentBuilder.forKvnr(kvnr)
            .version(EuVersion.getDefaultVersion())
            .policyRule(ActCode.OPTIN)
            .status(Consent.ConsentState.ACTIVE)
            .scope(ConsentScope.PATIENT_PRIVACY)
            .build();

    assertTrue((ValidatorUtil.encodeAndValidate(parser, euConsent).isSuccessful()));
  }

  @Test
  void shouldSetCorrectKvnr() {
    val kvnr = KVNR.from("X1234567890");
    val euConsent =
        EuConsentBuilder.forKvnr(kvnr)
            .policyRule(ActCode.OPTIN)
            .status(Consent.ConsentState.ACTIVE)
            .scope(ConsentScope.PATIENT_PRIVACY)
            .build();
    assertEquals(kvnr.getValue(), euConsent.getPatient().getIdentifier().getValue());
    assertTrue((ValidatorUtil.encodeAndValidate(parser, euConsent).isSuccessful()));
  }

  @Test
  void shouldSetStatusActiveCorrect() {
    val status = Consent.ConsentState.ACTIVE;
    val euConsent =
        EuConsentBuilder.forKvnr(KVNR.random())
            .policyRule(ActCode.OPTIN)
            .status(status)
            .scope(ConsentScope.PATIENT_PRIVACY)
            .build();
    assertEquals(status, euConsent.getStatus());
    assertTrue((ValidatorUtil.encodeAndValidate(parser, euConsent).isSuccessful()));
  }
}
