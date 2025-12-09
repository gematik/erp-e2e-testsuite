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
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeBetween;
import static de.gematik.test.core.expectations.verifier.ErxEuAccessPermissionVerifier.*;
import static java.text.MessageFormat.format;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.Requirement;
import de.gematik.test.core.expectations.verifier.ErxEuAccessPermissionVerifier;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.DownloadAuditEvent;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actions.eu.*;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SearchPrefix;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.fhir.builder.eu.EuAccessPermissionRequestBuilder;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("EU Prescription Access Permission")
@Tag("ErpEu")
class EuAccessPermissionIT extends ErpTest {

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @Test
  @TestcaseId("ERP_EU_ACCESS_PERMISSION_01")
  @DisplayName(
      "Als Versicherter eine EU-Zugriffsberechtigung für gültiges Land mit korrektem AccessCode"
          + " erstellen")
  void shouldGrantEuAccessForValidCountry() {
    sina.performs(GrantEuConsent.forPatient());

    val accessCode = EuAccessCode.random();
    val response =
        sina.performs(
            GrantEuAccessPermission.withAccessCode(accessCode).forCountry(IsoCountryCode.LI));

    sina.attemptsTo(
        Verify.that(response)
            .withExpectedType()
            .hasResponseWith(returnCode(201))
            .and(validUntilWithinOneHour())
            .and(hasCorrectProfile())
            .isCorrect());

    val searchParams =
        IQueryParameter.search()
            .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.EQ)
            .sortedBy("date", SortOrder.DESCENDING)
            .createParameter();

    val auditEvents = sina.performs(DownloadAuditEvent.withQueryParams(searchParams));

    sina.attemptsTo(
        Verify.that(auditEvents)
            .withExpectedType()
            .and(
                bundleContainsLog(
                    format(
                        "Sie haben eine Zugriffsberechtigung zum Einlösen von E-Rezepten für das"
                            + " Land LI erteilt.")))
            .isCorrect());
  }

  @TestcaseId("ERP_EU_ACCESS_PERMISSION_02")
  @Test
  @DisplayName("Als Versicherter darf ich nur eine EU-Zugriffsberechtigung erstellen")
  void shouldGrantOnlyOneEuAccess() {
    val firstCode = EuAccessCode.random();
    val firstResponse =
        sina.performs(
            GrantEuAccessPermission.withAccessCode(firstCode).forCountry(IsoCountryCode.LI));
    sina.attemptsTo(
        Verify.that(firstResponse).withExpectedType().hasResponseWith(returnCode(201)).isCorrect());

    val secondCode = EuAccessCode.random();
    val secondResponse =
        sina.performs(
            GrantEuAccessPermission.withAccessCode(secondCode).forCountry(IsoCountryCode.LI));
    sina.attemptsTo(
        Verify.that(secondResponse)
            .withExpectedType()
            .hasResponseWith(returnCode(201))
            .isCorrect());

    val bundle = sina.performs(GetEuAccessPermission.forOneSelf());

    sina.attemptsTo(
        Verify.that(bundle)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(ErxEuAccessPermissionVerifier.hasExactlyOnePermission())
            .and(ErxEuAccessPermissionVerifier.hasPermissionWithAccessCode(secondCode))
            .isCorrect());
  }

  @TestcaseId("ERP_EU_ACCESS_PERMISSION_03")
  @Test
  @DisplayName(
      "Als Versicherter darf ich keine EU-Zugriffsberechtigung mit ungültigem Land erstellen")
  void shouldRejectEuAccessWithInvalidCountry() {

    val response =
        sina.performs(GrantEuAccessPermission.withRandomAccessCode().forCountry(IsoCountryCode.ZW));

    sina.attemptsTo(
        Verify.that(response)
            .withOperationOutcome()
            .hasResponseWith(returnCodeBetween(403, 409))
            .isCorrect());
  }

  @TestcaseId("ERP_EU_ACCESS_PERMISSION_04")
  @Test
  @DisplayName(
      "Als Versicherter darf ich keine EU-Zugriffsberechtigung mit ungültigem AccessCode erstellen")
  void shouldReturnBadRequestForInvalidAccessCode() {

    val invalidRawCode = "12!@#";
    val accessCode = EuAccessCode.from(invalidRawCode);
    val response =
        sina.performs(
            GrantEuAccessPermission.withRandomAccessCode()
                .withUncheckedAC(
                    EuAccessPermissionRequestBuilder.withUncheckedAccessCode(accessCode)
                        .countryCode(IsoCountryCode.LI)
                        .build()));

    sina.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_27097)
            .hasResponseWith(returnCode(400))
            .isCorrect());
  }

  @TestcaseId("ERP_EU_ACCESS_PERMISSION_05")
  @Test
  @DisplayName("Als Versicherter darf ich ohne EU-Consent keine Zugriffsberechtigung erstellen")
  void shouldDenyAccessPermissionWithoutConsent() {

    sina.performs(EuRejectConsent.forOneSelf().build());

    val response =
        sina.performs(GrantEuAccessPermission.withRandomAccessCode().forCountry(IsoCountryCode.LI));

    sina.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(
                Requirement.custom(
                    "Das Erstellen einer Zugriffsberechtigung ist erst zulässig, wenn eine"
                        + " Einwilligung durch den Nutzer zum Einlösen von E-Rezepten im"
                        + " europäischen Ausland erteilt wurde."))
            .hasResponseWith(returnCode(403))
            .isCorrect());
  }

  @TestcaseId("ERP_EU_ACCESS_PERMISSION_06")
  @Test
  @DisplayName("Als Versicherter kann ich eine EU-Zugriffsberechtigung erstellen und abrufen")
  void shouldGrantAndThenRetrieveAccessPermission() {
    sina.performs(GrantEuConsent.forPatient());

    val accessCode = EuAccessCode.random();
    val postResponse =
        sina.performs(
            GrantEuAccessPermission.withAccessCode(accessCode).forCountry(IsoCountryCode.LI));

    sina.attemptsTo(
        Verify.that(postResponse)
            .withExpectedType()
            .hasResponseWith(returnCode(201))
            .and(validUntilWithinOneHour())
            .and(hasCorrectProfile())
            .and(hasIsoCountry(IsoCountryCode.LI))
            .and(hasPermissionWithAccessCode(accessCode))
            .isCorrect());

    val getResponse = sina.performs(GetEuAccessPermission.forOneSelf());

    sina.attemptsTo(
        Verify.that(getResponse)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(validUntilWithinOneHour())
            .and(hasCorrectProfile())
            .and(hasIsoCountry(IsoCountryCode.LI))
            .and(hasPermissionWithAccessCode(accessCode))
            .isCorrect());
  }
}
