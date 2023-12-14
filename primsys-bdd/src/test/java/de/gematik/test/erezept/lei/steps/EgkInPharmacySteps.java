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

package de.gematik.test.erezept.lei.steps;

import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.questions.HasDownloadableOpenTask;
import de.gematik.test.erezept.screenplay.questions.HasRetrieved;
import de.gematik.test.erezept.screenplay.questions.RetrieveExamEvidence;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidence;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidenceResult;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.ensure.Ensure;

import static net.serenitybdd.screenplay.GivenWhenThen.then;
import static net.serenitybdd.screenplay.GivenWhenThen.when;

public class EgkInPharmacySteps {

  @Wenn("^die Apotheke (.+) die E-Rezepte mit der eGK von (.+) abruft$")
  public void whenPharmacyRequestPrescriptionsWithEgk(String pharmName, String patientName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);
    val egk = SafeAbility.getAbility(thePatient, ProvideEGK.class).getEgk();
    val examEvidence = thePharmacy.asksFor(RetrieveExamEvidence.with(egk));

    when(thePharmacy)
        .attemptsTo(Ensure.that(HasDownloadableOpenTask.withExamEvidence(examEvidence)).isTrue());
  }

  @Wenn(
      "^die Apotheke (.+) für die eGK von (.+) (?:einen alten|keinen) Prüfungsnachweis (?:verwendet|abruft)$")
  @Wenn("^die Krankenhaus-Apotheke (.+) die E-Rezepte mit der eGK von (.+) abruft$")
  public void whenPharmacyUseExpiredEvidence(String pharmName, String patientName) {
    // dummy step to increase comprehensibility in the test scenario
  }

  @Dann(
      "^kann die Apotheke (.+) die E-Rezepte von (.+) nicht abrufen, weil der Prüfungsnachweis zeitlich ungültig ist$")
  public void thenPharmacyCanNotRequestPrescriptionsWithExpiredEvidence(
      String pharmName, String patientName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);
    val examEvidence =
        VsdmExamEvidence.builder(VsdmExamEvidenceResult.NO_UPDATES)
            .withExpiredTimestamp()
            .build()
            .encodeAsBase64();
    when(thePharmacy)
        .attemptsTo(Ensure.that(HasDownloadableOpenTask.withExamEvidence(examEvidence)).isFalse());
  }

  @Dann(
      "^kann die Apotheke (.+) die E-Rezepte von (.+) nicht abrufen, weil der Prüfungsnachweis nicht abgerufen wurde$")
  public void thenPharmacyCanNotRequestPrescriptionsWithoutEvidence(
      String pharmName, String patientName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy)
        .attemptsTo(Ensure.that(HasDownloadableOpenTask.withoutExamEvidence()).isFalse());
  }

  @Dann(
      "^kann die Apotheke (.+) das letzte E-Rezept nicht abrufen, weil die Apotheke (.+) dieses bereits akzeptiert hat$")
  public void thenPharmacyCanNotRequestPrescriptionsAlreadyAccepted(
      String pharmName, String otherPharmName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val otherPharmacy = OnStage.theActorCalled(otherPharmName);
    then(thePharmacy)
        .attemptsTo(
            Ensure.that(HasRetrieved.theLastAcceptedPrescriptionBy(otherPharmacy)).isFalse());
  }

  @Dann(
      "^kann die Apotheke (.+) die E-Rezepte von (.+) nicht abrufen, weil Krankenhaus-Apotheken nicht berechtigt sind$")
  public void thenHospitalPharmacyCanNotRequestPrescriptions(String pharmName, String patientName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);
    val examEvidence =
        VsdmExamEvidence.builder(VsdmExamEvidenceResult.NO_UPDATES).build().encodeAsBase64();
    when(thePharmacy)
        .attemptsTo(Ensure.that(HasDownloadableOpenTask.withExamEvidence(examEvidence)).isFalse());
  }
}
