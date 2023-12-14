/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.fhir.builder.erp;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.parser.profiles.version.*;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.valuesets.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.junitpioneer.jupiter.*;

class ErxConsentBuilderTest extends ParsingTest {

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationInfoReq with E-Rezept FHIR Profiles {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpFhirProfileVersions")
  @ClearSystemProperty(key = "erp.fhir.profile")
  void buildConsentWithFixedValues(String erpFhirProfileVersion) {
    System.setProperty("erp.fhir.profile", erpFhirProfileVersion);
    val kvnr = KVNR.from("X234567890");
    val erxConsent =
        ErxConsentBuilder.forKvnr(kvnr)
            .policyRule(ActCode.OPTIN) // by default Opt-in
            .status(Consent.ConsentState.ACTIVE) // by default ACTIVE
            .scope(ConsentScope.PATIENT_PRIVACY) // by default Patient-Privacy
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, erxConsent);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationInfoReq with E-Rezept WorkflowVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void buildVersionedConsentWithFixedValues(ErpWorkflowVersion version) {
    val kvid = KVNR.from("X234567890");
    val erxConsent =
        ErxConsentBuilder.forKvnr(kvid)
            .version(version)
            .policyRule(ActCode.OPTIN) // by default Opt-in
            .status(Consent.ConsentState.ACTIVE) // by default ACTIVE
            .scope(ConsentScope.PATIENT_PRIVACY) // by default Patient-Privacy
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, erxConsent);
    assertTrue(result.isSuccessful());
  }
}
