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

package de.gematik.test.core.expectations.verifier;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.builder.eu.EuConsentBuilder;
import de.gematik.test.erezept.fhir.r4.eu.EuConsent;
import de.gematik.test.erezept.fhir.r4.eu.EuConsentBundle;
import de.gematik.test.erezept.fhir.valuesets.eu.EuConsentType;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Test;

class ErxEuConsentVerifierTest {

  @Test
  void shouldVerifyConsentCategoryCorrectly() {
    val consent2 = EuConsentBuilder.forKvnr(KVNR.random()).build();
    val step = ErxEuConsentVerifier.consentCategoryIs(EuConsentType.EUDISPCONS);

    assertTrue(step.getPredicate().test(consent2));
  }

  @Test
  void shouldFailConsentCategoryIfEmpty() {
    val expectedType = EuConsentType.EUDISPCONS;
    val consent = new EuConsent();

    val step = ErxEuConsentVerifier.consentCategoryIs(expectedType);

    assertFalse(step.getPredicate().test(consent));
  }

  @Test
  void shouldVerifyPatientIdentifierCorrectly() {
    val expectedKvnr = KVNR.from("X110609524");

    val patient = new Reference();
    patient.setIdentifier(new Identifier().setValue(expectedKvnr.getValue()));

    val consent = new EuConsent();
    consent.setPatient(patient);

    val step = ErxEuConsentVerifier.patientIdentifierIs(expectedKvnr);

    assertTrue(step.getPredicate().test(consent));
  }

  @Test
  void shouldFailPatientIdentifierIfMissing() {
    val expectedKvnr = KVNR.from("X110609524");

    val consent = new EuConsent();
    consent.setPatient(new Reference());

    val step = ErxEuConsentVerifier.patientIdentifierIs(expectedKvnr);

    assertFalse(step.getPredicate().test(consent));
  }

  @Test
  void shouldVerifyPatientIdentifierInBundle() {
    val expectedKvnr = KVNR.from("X110609524");

    val bundle = new EuConsentBundle();
    bundle.setEntry(
        List.of(
            new Bundle.BundleEntryComponent()
                .setResource(EuConsentBuilder.forKvnr(expectedKvnr).build())));

    val step = ErxEuConsentVerifier.verifyPatientIdentifierInBundle(expectedKvnr);

    assertTrue(step.getPredicate().test(bundle));
  }

  @Test
  void shouldPassWhenDateTimeIsCloseToNow() {
    val consent = new EuConsent();
    consent.setDateTime(Date.from(Instant.now()));

    val step = ErxEuConsentVerifier.hasValidTimestamp();

    assertTrue(
        "Expected predicate to pass when consent dateTime is close to now",
        step.getPredicate().test(consent));
  }

  @Test
  void shouldFailWhenDateTimeIsNull() {
    val consent = new EuConsent();
    consent.setDateTime(null);

    val step = ErxEuConsentVerifier.hasValidTimestamp();

    assertFalse(
        step.getPredicate().test(consent),
        "Expected predicate to fail when consent dateTime is null");
  }

  @Test
  void shouldFailWhenDateTimeIsTooOld() {
    val consent = new EuConsent();
    consent.setDateTime(Date.from(Instant.now().minus(Duration.ofMinutes(5))));

    val step = ErxEuConsentVerifier.hasValidTimestamp();

    assertFalse(
        step.getPredicate().test(consent),
        "Expected predicate to fail when consent dateTime is too far in the past");
  }

  @Test
  void shouldFailWhenDateTimeIsInFuture() {
    val consent = new EuConsent();
    consent.setDateTime(Date.from(Instant.now().plus(Duration.ofMinutes(2))));

    val step = ErxEuConsentVerifier.hasValidTimestamp();

    assertFalse(
        step.getPredicate().test(consent),
        "Expected predicate to fail when consent dateTime is too far in the future");
  }

  @Test
  void shouldPassWhenConsentBundleHasNoEntry() {
    val bundle = new EuConsentBundle();

    val step = ErxEuConsentVerifier.hasNoConsentEntry();

    assertTrue(
        "Expected predicate to pass when bundle has no entry", step.getPredicate().test(bundle));
  }

  @Test
  void shouldFailWhenConsentBundleHasEntry() {
    val consent = new EuConsent();
    consent
        .getMeta()
        .addProfile("https://gematik.de/fhir/erp-eu/StructureDefinition/GEM_ERPEU_PR_Consent");

    val entry = new Bundle.BundleEntryComponent().setResource(consent);

    val bundle = new EuConsentBundle();
    bundle.setEntry(List.of(entry));

    val step = ErxEuConsentVerifier.hasNoConsentEntry();

    assertFalse(
        step.getPredicate().test(bundle),
        "Expected predicate to fail when bundle contains a consent entry");
  }

  @Test
  void shouldPassWhenDateTimeIsDifferentFromForbidden() {
    val consent = new EuConsent();
    val forbiddenDate = Date.from(Instant.parse("2000-01-01T00:00:00Z"));
    consent.setDateTime(Date.from(Instant.now()));

    val step = ErxEuConsentVerifier.datetimeIsNot(forbiddenDate);

    assertTrue(
        "Expected predicate to pass when consent dateTime is different from forbiddenDate",
        step.getPredicate().test(consent));
  }

  @Test
  void shouldFailWhenDateTimeIsEqualToForbidden() {
    val forbiddenDate = Date.from(Instant.parse("2000-01-01T00:00:00Z"));
    val consent = new EuConsent();
    consent.setDateTime(forbiddenDate);

    val step = ErxEuConsentVerifier.datetimeIsNot(forbiddenDate);

    assertFalse(
        step.getPredicate().test(consent),
        "Expected predicate to fail when consent dateTime is equal to forbiddenDate");
  }

  @Test
  void shouldPassWhenDateTimeIsPresent() {
    val consent = new EuConsent();
    consent.setDateTime(Date.from(Instant.now()));

    val step = ErxEuConsentVerifier.hasDateTime();

    assertTrue(
        "Expected predicate to pass when consent dateTime is present (not null)",
        step.getPredicate().test(consent));
  }

  @Test
  void shouldFailWhenDateTimeEqualNull() {
    val consent = new EuConsent();
    consent.setDateTime(null);

    val step = ErxEuConsentVerifier.hasDateTime();

    assertFalse(
        step.getPredicate().test(consent),
        "Expected predicate to fail when consent dateTime is null");
  }
}
