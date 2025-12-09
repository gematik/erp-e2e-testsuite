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

package de.gematik.test.eu.integration;

import static de.gematik.test.core.expectations.verifier.AuditEventVerifier.bundleContainsLog;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.ErxEuConsentVerifier.*;
import static java.text.MessageFormat.format;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.DownloadAuditEvent;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actions.eu.*;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SearchPrefix;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.fhir.builder.eu.EuConsentBuilder;
import de.gematik.test.erezept.fhir.r4.eu.EuConsent;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import de.gematik.test.erezept.fhir.valuesets.eu.EuConsentType;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("EU CONSENT")
@Tag("ErpEu")
class EuConsentIT extends ErpTest {

  @Actor(name = "Leonie Hütter")
  private PatientActor leonie;

  @TestcaseId("ERP_GRANT_EU_CONSENT_01")
  @Test
  @DisplayName("Als Versicherter ein EU-Consent über den Fdv einstellen")
  void shouldSetEuConsent() {

    leonie.attemptsTo(EnsureEuConsent.shouldBeUnset());

    val kvnr = leonie.getKvnr();
    val consent = EuConsentBuilder.forKvnr(kvnr).build();

    val response = leonie.performs(EuGrantConsent.forOneSelf().withConsent(consent));

    leonie.attemptsTo(
        Verify.that(response)
            .withExpectedType()
            .hasResponseWith(returnCode(201))
            .and(consentCategoryIs(EuConsentType.EUDISPCONS))
            .and(patientIdentifierIs(kvnr))
            .and(hasValidTimestamp())
            .isCorrect());

    val searchParams =
        IQueryParameter.search()
            .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.EQ)
            .sortedBy("date", SortOrder.DESCENDING)
            .createParameter();

    val auditEvents = leonie.performs(DownloadAuditEvent.withQueryParams(searchParams));

    leonie.attemptsTo(
        Verify.that(auditEvents)
            .withExpectedType()
            .and(
                bundleContainsLog(
                    format(
                        "{0} {1} hat die Einwilligung zur Einlösung von E-Rezepten in EU-Ländern"
                            + " erteilt.",
                        leonie.getEgk().getOwnerData().getGivenName(),
                        leonie.getEgk().getOwnerData().getSurname())))
            .isCorrect());
  }

  @TestcaseId("ERP_GRANT_EU_CONSENT_02")
  @Test
  @DisplayName(
      "Ein Versicherter darf kein zweiter EU-Consent mit der selben KVNR und Kategorie einstellen")
  void shouldFailByDuplicateConsent() {

    leonie.performs(EuRejectConsent.forOneSelf().build());

    val kvnr = leonie.getKvnr();
    val consent = EuConsentBuilder.forKvnr(kvnr).build();

    val firstResponse = leonie.performs(EuGrantConsent.forOneSelf().withConsent(consent));

    leonie.attemptsTo(
        Verify.that(firstResponse)
            .withExpectedType()
            .hasResponseWith(returnCode(201))
            .and(consentCategoryIs(EuConsentType.EUDISPCONS))
            .and(patientIdentifierIs(kvnr))
            .isCorrect());

    val secondResponse = leonie.performs(EuGrantConsent.forOneSelf().withConsent(consent));

    leonie.attemptsTo(
        Verify.that(secondResponse)
            .withOperationOutcome()
            .hasResponseWith(returnCode(409))
            .isCorrect());
  }

  @TestcaseId("ERP_GRANT_EU_CONSENT_03")
  @Test
  @DisplayName("Als Versicherter ein ungültiges EU-Consent einstellen")
  void shouldRejectInvalidConsent() {

    leonie.performs(EuRejectConsent.forOneSelf().build());

    val kvnr = leonie.getKvnr();

    val category = new CodeableConcept().addCoding(new Coding().setCode("EUDISPCONS"));

    val invalidConsent = new EuConsent();
    invalidConsent.setPatient(
        new Reference().setIdentifier(new Identifier().setValue(kvnr.getValue())));
    invalidConsent.setCategory(List.of(category));

    val response = leonie.performs(EuGrantConsent.forOneSelf().withConsent(invalidConsent));

    leonie.attemptsTo(
        Verify.that(response).withOperationOutcome().hasResponseWith(returnCode(400)).isCorrect());
  }

  @TestcaseId("ERP_GRANT_EU_CONSENT_04")
  @Test
  @DisplayName("Als Versicherter ein EU-Consent mit ungültiger Kategorie einstellen")
  void shouldRejectConsentWithInvalidCategory() {

    leonie.performs(EuRejectConsent.forOneSelf().build());

    val kvnr = leonie.getKvnr();

    // The purpose of this parameter is to define an erroneous category value
    // that modifies the default category value (EUDISPCONS)
    val invalidConsent = EuConsentBuilder.forKvnr(kvnr).build();
    invalidConsent.getCategoryFirstRep().getCodingFirstRep().setCode("INVALIDCATEGORY");

    val response = leonie.performs(EuGrantConsent.forOneSelf().withConsent(invalidConsent));

    leonie.attemptsTo(
        Verify.that(response).withOperationOutcome().hasResponseWith(returnCode(400)).isCorrect());
  }

  @TestcaseId("ERP_GRANT_EU_CONSENT_05")
  @Test
  @DisplayName("Als Versicherter ein EU-Consent über den Fdv einstellen und dann abrufen")
  void shouldGetEuConsent() {

    leonie.performs(EuRejectConsent.forOneSelf().build());

    val kvnr = leonie.getKvnr();
    val consent = EuConsentBuilder.forKvnr(kvnr).build();

    val postResponse = leonie.performs(EuGrantConsent.forOneSelf().withConsent(consent));

    leonie.attemptsTo(
        Verify.that(postResponse)
            .withExpectedType()
            .hasResponseWith(returnCode(201))
            .and(patientIdentifierIs(kvnr))
            .and(consentCategoryIs(EuConsentType.EUDISPCONS))
            .and(hasValidTimestamp())
            .isCorrect());

    val getResponse = leonie.performs(EuReadConsent.forOneSelf());

    leonie.attemptsTo(
        Verify.that(getResponse)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(verifyPatientIdentifierInBundle(kvnr))
            .isCorrect());
  }

  @TestcaseId("ERP_GRANT_EU_CONSENT_06")
  @Test
  @DisplayName("Als Versicherter ein EU-Consent über den Fdv widerrufen")
  void shouldRejectEuConsent() {
    val kvnr = leonie.getKvnr();
    val consent = EuConsentBuilder.forKvnr(kvnr).build();

    val postResponse = leonie.performs(EuGrantConsent.forOneSelf().withConsent(consent));

    leonie.attemptsTo(
        Verify.that(postResponse).withExpectedType().hasResponseWith(returnCode(201)).isCorrect());

    val deleteResponse = leonie.performs(EuRejectConsent.forOneSelf().build());

    leonie.attemptsTo(
        Verify.that(deleteResponse)
            .withExpectedType()
            .hasResponseWith(returnCode(204))
            .isCorrect());

    val searchParams =
        IQueryParameter.search()
            .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.EQ)
            .sortedBy("date", SortOrder.DESCENDING)
            .createParameter();

    val auditEvents = leonie.performs(DownloadAuditEvent.withQueryParams(searchParams));

    leonie.attemptsTo(
        Verify.that(auditEvents)
            .withExpectedType()
            .and(
                bundleContainsLog(
                    format(
                        "{0} {1} hat die Einwilligung zur Einlösung von E-Rezepten in EU-Ländern"
                            + " widerrufen.",
                        leonie.getEgk().getOwnerData().getGivenName(),
                        leonie.getEgk().getOwnerData().getSurname())))
            .isCorrect());
  }

  @TestcaseId("ERP_GRANT_EU_CONSENT_07")
  @Test
  @DisplayName("Als Versicherter ein EU-Consent über den Fdv widerrufen und erneut abrufen")
  void shouldDeleteAndGetConsent() {

    leonie.attemptsTo(EnsureEuConsent.shouldBeSet(true));

    val deleteResponse = leonie.performs(EuRejectConsent.forOneSelf().build());

    leonie.attemptsTo(
        Verify.that(deleteResponse)
            .withExpectedType()
            .hasResponseWith(returnCode(204))
            .isCorrect());

    val getResponse = leonie.performs(EuReadConsent.forOneSelf());

    leonie.attemptsTo(
        Verify.that(getResponse)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(hasNoConsentEntry())
            .isCorrect());
  }

  @TestcaseId("ERP_GRANT_EU_CONSENT_08")
  @Test
  @DisplayName("Als Versicherte ein EU-Consent löschen und EU-Zugriffsberechtigung prüfen")
  void deleteConsentShouldDeleteEuAccessPermissionToo() {
    // precondition: set EU consent
    leonie.attemptsTo(EnsureEuConsent.shouldBeSet(true));

    val accessCode = EuAccessCode.random();
    val grantEuAccessPermission =
        leonie.performs(
            GrantEuAccessPermission.withAccessCode(accessCode).forCountry(IsoCountryCode.LI));
    leonie.attemptsTo(
        Verify.that(grantEuAccessPermission)
            .withExpectedType()
            .hasResponseWith(returnCode(201))
            .isCorrect());

    val rejectEuConsent = leonie.performs(EuRejectConsent.forOneSelf().build());
    leonie.attemptsTo(
        Verify.that(rejectEuConsent)
            .withExpectedType()
            .hasResponseWith(returnCode(204))
            .isCorrect());

    val permissions = leonie.performs(GetEuAccessPermission.forOneSelf());
    leonie.attemptsTo(
        Verify.that(permissions)
            .withOperationOutcome()
            .hasResponseWith(returnCode(404))
            .isCorrect());
  }

  @TestcaseId("ERP_GRANT_EU_CONSENT_09")
  @Test
  @DisplayName(
      "Beim einstellen ein EU-Consent ohne datetime, soll der Fachdienst datetime einsetzen")
  void shouldSetConsentDatetimeIfMissing() {
    leonie.performs(EuRejectConsent.forOneSelf().build());

    val kvnr = leonie.getKvnr();
    val consent = EuConsentBuilder.forKvnr(kvnr).build();
    consent.setDateTime(null);

    val response = leonie.performs(EuGrantConsent.forOneSelf().withConsent(consent));

    leonie.attemptsTo(
        Verify.that(response)
            .withExpectedType()
            .hasResponseWith(returnCode(201))
            .and(patientIdentifierIs(kvnr))
            .and(consentCategoryIs(EuConsentType.EUDISPCONS))
            .and(hasDateTime())
            .isCorrect());
  }

  @TestcaseId("ERP_GRANT_EU_CONSENT_10")
  @Test
  @DisplayName(
      "Beim einstellen ein EU-Consent mit datetime, soll der Fachdienst die datetime überschreiben")
  void shouldOverrideConsentDatetimeIfProvided() {
    leonie.performs(EuRejectConsent.forOneSelf().build());

    val kvnr = leonie.getKvnr();
    val consent = EuConsentBuilder.forKvnr(kvnr).build();
    val consentDate = new Date();
    consent.setDateTime(consentDate);

    val response = leonie.performs(EuGrantConsent.forOneSelf().withConsent(consent));

    leonie.attemptsTo(
        Verify.that(response)
            .withExpectedType()
            .hasResponseWith(returnCode(201))
            .and(patientIdentifierIs(kvnr))
            .and(consentCategoryIs(EuConsentType.EUDISPCONS))
            .and(hasDateTime())
            .and(hasValidTimestamp())
            .and(datetimeIsNot(consentDate))
            .isCorrect());
  }
}
