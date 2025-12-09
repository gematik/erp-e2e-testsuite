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

package de.gematik.test.erezept.fhir.r4.eu;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.builder.eu.EuConsentBuilder;
import de.gematik.test.erezept.fhir.profiles.definitions.GemErpEuStructDef;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;

class EuConsentBundleTest extends ErpFhirParsingTest {

  @Test
  void shouldGetGetConsent() {
    val euConsentBundle = EuConsentBuilder.forKvnr(KVNR.random()).build();
    EuConsentBundle bundle = new EuConsentBundle();
    bundle
        .setType(Bundle.BundleType.SEARCHSET)
        .setTotal(1)
        .setLink(
            List.of(
                new Bundle.BundleLinkComponent()
                    .setRelation("self")
                    .setUrl(
                        "https://erp-dev.zentral.erp.splitdns.ti-dienste.de/Consent?category=EUDISPCONS")));
    bundle.addEntry().setResource(euConsentBundle);
    val bundleResult = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(bundleResult.isSuccessful());
    assertTrue(bundle.getConsent().isPresent());
    val euConsent = bundle.getConsent().orElseThrow();
    assertNotNull(euConsent);
    val consResult = ValidatorUtil.encodeAndValidate(parser, euConsent);
    assertTrue(consResult.isSuccessful());
    assertTrue(
        euConsent
            .getMeta()
            .getProfile()
            .get(0)
            .asStringValue()
            .contains(GemErpEuStructDef.CONSENT.getCanonicalUrl()),
        "Consent should have the correct profile");
  }
}
