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

package de.gematik.test.erezept.fhir.builder.erp;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.ActCode;
import de.gematik.bbriccs.fhir.de.valueset.ConsentScope;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import lombok.val;
import org.hl7.fhir.r4.model.Consent;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

class ErxConsentBuilderTest extends ErpFhirParsingTest {

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationInfoReq with E-Rezept FHIR Profiles {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpFhirProfileVersions")
  @ClearSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE)
  void buildConsentWithFixedValues(String erpFhirProfileVersion) {
    System.setProperty(ERP_FHIR_PROFILES_TOGGLE, erpFhirProfileVersion);
    val kvnr = KVNR.asPkv("X234567890");
    val erxConsent =
        ErxConsentBuilder.forKvnr(kvnr)
            .policyRule(ActCode.OPTIN)
            .status(Consent.ConsentState.ACTIVE)
            .scope(ConsentScope.PATIENT_PRIVACY)
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
            .policyRule(ActCode.OPTIN)
            .status(Consent.ConsentState.ACTIVE)
            .scope(ConsentScope.PATIENT_PRIVACY)
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, erxConsent);
    assertTrue(result.isSuccessful());
  }
}
