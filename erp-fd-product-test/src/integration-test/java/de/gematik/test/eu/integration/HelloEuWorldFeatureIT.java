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

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeBetween;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actions.eu.*;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.EuPharmacyActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Hello World Testscenario")
@Tag("ErpEu")
@Disabled("For Demonstration purpose only!")
class HelloEuWorldFeatureIT extends ErpTest {

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @Actor(name = "Hannes Vogt")
  private EuPharmacyActor hannesVogt;

  @Test
  @TestcaseId("ERP_RC_EU_HELLOWORLD_01")
  @DisplayName("Screenplay Beispiel für das Feature NCPeH")
  @Disabled("For Demonstration purpose only!")
  void testNCPeH() {
    // create & activate EU Prescription e.g. PZN for GKV Patient
    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                // !!!! Important PZN for GKV Patient
                .withRandomKbvBundle());

    // Consent für Patient erteilen
    sina.performs(GrantEuConsent.forPatient());

    // Sina kann jetzt schon bestimmte E-Rezepte für die EU freischalten.
    val taskId = activation.getExpectedResponse().getTaskId();
    sina.performs(PatchPrescriptionForEuRedemption.of(taskId));

    // EU-Land freischalten -> EuAccessCode ist eine 1h gültig
    val accessCode = EuAccessCode.random();
    // damit simulieren wir die Freischaltung des EU-Landes durch das FdV
    sina.performs(GrantEuAccessPermission.withRandomAccessCode().forCountryOf(hannesVogt));

    // Das ist die Magic: jetzt geht Sina in Liechtenstein in die Apotheke zu Hannes Vogt und zeigt
    // das Smartphone bzw. die KVNR und den EuAccessCode vor

    // Hannes Vogt ruft dann die den NCPeH Fachdienst aus Liechtensteinen (Land B), über den NCPeH
    // Fachdienst Deutschland (Land A) am E-Rezept Fachdienst die Daten von Sina ab.
    // wir vereinfachen an dieser Stelle und simulieren für Land A den NCPeH Fachdienst, also den
    // NCPeH Fachdienst Deutschland
    val demographicData =
        hannesVogt.performs(GetDemographicData.forPatient(sina).withAccessCode(accessCode));
    hannesVogt.attemptsTo(
        Verify.that(demographicData)
            .withExpectedType()
            .hasResponseWith(returnCodeBetween(200, 299))
            // .and(isForPatient(sina)) <- not implemented yet
            .isCorrect());

    // ok, die Daten von Sina stimmen -> jetzt holen wir uns alle offenen und freigeschalteten EU
    // Rezepten für Sina (in dem Szenario nur eines)
    val euPrescriptions =
        hannesVogt.performs(GetEuPrescriptions.forPatient(sina).withAccessCode(accessCode));
    val euPrescriptionsList = euPrescriptions.getExpectedResponse().getPrescriptionIds();
    hannesVogt.attemptsTo(
        Verify.that(euPrescriptions)
            .withExpectedType()
            .hasResponseWith(returnCodeBetween(200, 299))
            .isCorrect());

    // wir prüfen ob wir die Prescription tatsächlich dispensieren könnten und falls ja, claimen wir
    // die E-Rezepte
    // hierbei wird tatsächlich vom E-Rezept von ready zu in-progress geändert
    val acceptedEuPrescriptions =
        hannesVogt.performs(
            RetrievalEuPrescriptions.forPatient(sina)
                .withPrescriptionIds(euPrescriptionsList)
                .withAccessCode(accessCode));
    hannesVogt.attemptsTo(
        Verify.that(acceptedEuPrescriptions)
            .withExpectedType()
            .hasResponseWith(returnCodeBetween(200, 299))
            .isCorrect());

    // jetzt geben wir noch an, was wir tatsächlich abgegeben haben
    // also Analog zu $close

    val closeEuPrescriptionResponse =
        hannesVogt.performs(
            CloseEuPrescription.with(accessCode, sina.getKvnr())
                .withAccepted(
                    acceptedEuPrescriptions.getExpectedResponse().getKbvErpBundles().get(0)));

    hannesVogt.attemptsTo(
        Verify.that(closeEuPrescriptionResponse)
            .withExpectedType()
            .hasResponseWith(returnCodeBetween(200, 299))
            .isCorrect());
  }
}
