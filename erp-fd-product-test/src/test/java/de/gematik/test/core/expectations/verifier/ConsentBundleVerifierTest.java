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

package de.gematik.test.core.expectations.verifier;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.fhir.resources.erp.ErxConsentBundle;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.testutil.PrivateConstructorsUtil;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsentBundleVerifierTest extends ParsingTest {

  private static final String CONSENT_PATH =
      "fhir/valid/erp/1.2.0/consent/bundle_6daaade4-6523-4136-94bf-cbc5a247cc7b.json";

  @BeforeEach
  void init() {
    CoverageReporter.getInstance().startTestcase("not needed");
  }

  @Test
  void shouldNotInstantiateUtilityClass() {
    assertTrue(
        PrivateConstructorsUtil.throwsInvocationTargetException(ConsentBundleVerifier.class));
  }

  @Test
  void shouldValidateCorrect() {
    val validConsent =
        parser.decode(ErxConsentBundle.class, ResourceUtils.readFileFromResource(CONSENT_PATH));
    val step = ConsentBundleVerifier.containsKvnr(KVNR.from("X110465770"), ErpAfos.A_19018);
    step.apply(validConsent);
  }

  @Test
  void shouldThrowWhileInvalidKvnr() {
    val validConsent =
        parser.decode(ErxConsentBundle.class, ResourceUtils.readFileFromResource(CONSENT_PATH));
    val step = ConsentBundleVerifier.containsKvnr(KVNR.from("X110465779"), ErpAfos.A_19018);
    assertThrows(AssertionError.class, () -> step.apply(validConsent));
  }

  private static final String CHARGE_ITEM =
      "fhir/valid/erp/1.2.0/chargeitembundle/a05a235a-a214-11ed-a8fc-0242ac120002.xml";

  @Test
  void shouldHasConsentTrue() {
    val validConsent =
        parser.decode(ErxConsentBundle.class, ResourceUtils.readFileFromResource(CONSENT_PATH));
    val step = ConsentBundleVerifier.hasConsent(ErpAfos.A_22160);
    step.apply(validConsent);
  }

  @Test
  void shouldHasConsentFalse() {
    val consentBundle = new ErxConsentBundle();
    val step = ConsentBundleVerifier.hasConsent(ErpAfos.A_22160);
    assertThrows(AssertionError.class, () -> step.apply(consentBundle));
  }

  @Test
  void shouldHasNoConsentFalse() {
    val validConsent =
        parser.decode(ErxConsentBundle.class, ResourceUtils.readFileFromResource(CONSENT_PATH));
    val step = ConsentBundleVerifier.hasNoConsent(ErpAfos.A_22160);
    assertThrows(AssertionError.class, () -> step.apply(validConsent));
  }

  @Test
  void shouldHasNoConsent() {
    val consentBundle = new ErxConsentBundle();
    val step = ConsentBundleVerifier.hasNoConsent(ErpAfos.A_22160);
    step.apply(consentBundle);
  }
}