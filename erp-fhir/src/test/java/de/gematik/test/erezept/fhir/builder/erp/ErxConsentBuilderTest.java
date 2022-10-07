/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.fhir.builder.erp;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.util.ParsingTest;
import de.gematik.test.erezept.fhir.util.ValidatorUtil;
import de.gematik.test.erezept.fhir.valuesets.ActCode;
import de.gematik.test.erezept.fhir.valuesets.ConsentScope;
import lombok.val;
import org.hl7.fhir.r4.model.Consent;
import org.junit.jupiter.api.Test;

class ErxConsentBuilderTest extends ParsingTest {

  @Test
  void buildConsentWithFixedValues() {
    val kvid = "X234567890";
    val erxConsent =
        ErxConsentBuilder.forKvid(kvid)
            .policyRule(ActCode.OPTIN) // by default Opt-in
            .status(Consent.ConsentState.ACTIVE) // by default ACTIVE
            .scope(ConsentScope.PATIENT_PRIVACY) // by default Patient-Privacy
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, erxConsent);
    assertTrue(result.isSuccessful());
  }
}
