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

import static com.google.inject.internal.Messages.format;
import static de.gematik.test.core.expectations.verifier.AuditEventVerifier.bundleContainsLog;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.ErxEuAccessPermissionVerifier.hasCorrectProfile;
import static de.gematik.test.core.expectations.verifier.ErxEuAccessPermissionVerifier.validUntilWithinOneHour;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.DownloadAuditEvent;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actions.eu.DeleteEuAccessPermission;
import de.gematik.test.erezept.actions.eu.EuGrantConsent;
import de.gematik.test.erezept.actions.eu.GetEuAccessPermission;
import de.gematik.test.erezept.actions.eu.GrantEuAccessPermission;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SearchPrefix;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("EU Access Permission löschen")
@Tag("ErpEu")
class DeleteEuAccessPermissionIT extends ErpTest {

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @TestcaseId("ERP_DELETE_EU_ACCESS_PERMISSION_01")
  @Test
  @DisplayName("Als Versicherter lösche ich die EU-Zugriffsberechtigung")
  void shouldDeleteAccessPermission() {
    sina.performs(EuGrantConsent.forOneSelf().withDefaultConsent());
    val response =
        sina.performs(GrantEuAccessPermission.withRandomAccessCode().forCountry(IsoCountryCode.LI));

    sina.attemptsTo(
        Verify.that(response)
            .withExpectedType()
            .hasResponseWith(returnCode(201))
            .and(validUntilWithinOneHour())
            .and(hasCorrectProfile())
            .isCorrect());

    val deleteResponse = sina.performs(DeleteEuAccessPermission.forOneSelf());
    sina.attemptsTo(
        Verify.that(deleteResponse)
            .withExpectedType()
            .hasResponseWith(returnCode(204))
            .isCorrect());

    val afterDelete = sina.performs(GetEuAccessPermission.forOneSelf());
    sina.attemptsTo(
        Verify.that(afterDelete)
            .withOperationOutcome()
            .hasResponseWith(returnCode(404))
            .isCorrect());

    val searchParams =
        IQueryParameter.search()
            .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.EQ)
            .sortedBy("date", SortOrder.DESCENDING)
            .createParameter();
    val audit = sina.performs(DownloadAuditEvent.withQueryParams(searchParams));

    sina.attemptsTo(
        Verify.that(audit)
            .withExpectedType()
            .and(
                bundleContainsLog(
                    format(
                        "Sie haben die Zugriffsberechtigung zum Einlösen von E-Rezepten für das"
                            + " Land LI gelöscht.")))
            .isCorrect());
  }

  @TestcaseId("ERP_DELETE_EU_ACCESS_PERMISSION_02")
  @Test
  @DisplayName(
      "Als Versicherter darf ich das Löschen der EU-Zugriffsberechtigung mehrfach durchführen")
  void shouldDeleteAccessPermissionMultipleTimes() {
    val firstDelete = sina.performs(DeleteEuAccessPermission.forOneSelf());
    sina.attemptsTo(
        Verify.that(firstDelete).withExpectedType().hasResponseWith(returnCode(204)).isCorrect());

    val getAfterFirstDelete = sina.performs(GetEuAccessPermission.forOneSelf());
    sina.attemptsTo(
        Verify.that(getAfterFirstDelete)
            .withOperationOutcome()
            .hasResponseWith(returnCode(404))
            .isCorrect());

    val secondDelete = sina.performs(DeleteEuAccessPermission.forOneSelf());
    sina.attemptsTo(
        Verify.that(secondDelete).withExpectedType().hasResponseWith(returnCode(204)).isCorrect());

    val getAfterSecondDelete = sina.performs(GetEuAccessPermission.forOneSelf());
    sina.attemptsTo(
        Verify.that(getAfterSecondDelete)
            .withOperationOutcome()
            .hasResponseWith(returnCode(404))
            .isCorrect());
  }
}
