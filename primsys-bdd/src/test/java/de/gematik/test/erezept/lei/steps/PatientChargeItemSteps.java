/*
 * Copyright 2024 gematik GmbH
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

import static net.serenitybdd.screenplay.GivenWhenThen.*;

import de.gematik.test.erezept.screenplay.questions.*;
import de.gematik.test.erezept.screenplay.strategy.*;
import de.gematik.test.erezept.screenplay.task.*;
import io.cucumber.datatable.*;
import io.cucumber.java.de.*;
import lombok.*;
import net.serenitybdd.screenplay.actors.*;
import net.serenitybdd.screenplay.ensure.*;

public class PatientChargeItemSteps {

  /**
   * Der ausgewählte Versicherte mit dem Namen {@code patientName} führt die Aktion {@code
   * consentAction} auf dem Endpunkt <b>/Consent</b> des Fachdienstes aus. Damit wird
   * sichergestellt, dass der Versicherte für den weiteren Testverlauf auf dem E-Rezept Fachdienst
   * seine Einwilligung erteilt bzw. widerrufen hat.
   *
   * <p>Abhängig von dem angegebenen Parameter {@code consentAction} werden unterschiedliche
   * Operationen ausgeführt:
   *
   * <ul>
   *   <li>mit <b>erteilt</b> wird ein <code>POST /Consent</code> ausgeführt um eine Einwilligung zu
   *       erteilen
   *   <li>mit <b>widerrufen</b> wird ein <code>DELETE /Consent</code> ausgeführt, falls eine
   *       Einwilligung vorliegt, um eine Einwilligung zu widerrufen. Liegt bislang keine
   *       Einwilligung für diesen Versicherten beim E-Rezept Fachdienst vor, dann wird keine Aktion
   *       ausgelöst
   * </ul>
   *
   * @param patientName ist der Name des Versicherten der die Einwilligung erteilen bzw. widerrufen
   *     soll
   * @param consentAction gibt an, ob der Versicherte die Einwilligung erteilen, abrufen oder
   *     widerrufen soll
   */
  @Angenommen(
      "^(?:der|die) Versicherte (.+) hat (?:seine|ihre) Einwilligung zum Speichern der"
          + " PKV-Abrechnungsinformationen (erteilt|widerrufen)$")
  public void givenThatConsentIsGranted(String patientName, String consentAction) {
    val thePatient = OnStage.theActorCalled(patientName);
    givenThat(thePatient).attemptsTo(BillingInformationConsent.forAction(consentAction));
  }

  /**
   * Die <em>theActorInTheSpotlight</em>-Variante von {@link #givenThatConsentIsGranted(String,
   * String)}
   *
   * @param consentAction gibt an, ob der Versicherte die Einwilligung erteilen, abrufen oder
   *     widerrufen soll
   */
  @Angenommen(
      "^(?:der|die) Versicherte hat (?:seine|ihre) Einwilligung zum Speichern der"
          + " PKV-Abrechnungsinformationen (erteilt|widerrufen)$")
  public void givenThatConsentIsGranted(String consentAction) {
    val thePatient = OnStage.theActorInTheSpotlight();
    givenThat(thePatient).attemptsTo(BillingInformationConsent.forAction(consentAction));
  }

  /**
   * Die <em>Wenn</em>-Variante von {@link #givenThatConsentIsGranted(String, String)}
   *
   * @param patientName ist der Name des Versicherten der die Einwilligung erteilen bzw. widerrufen
   *     soll
   * @param consentAction gibt an, ob der Versicherte die Einwilligung erteilen, abrufen oder
   *     widerrufen soll
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) (?:seine|ihre) Einwilligung zur Speicherung der"
          + " PKV-Abrechnungsinformationen (erteilt|widerruft)$")
  public void whenGrantConsent(String patientName, String consentAction) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient).attemptsTo(BillingInformationConsent.forAction(consentAction));
  }

  /**
   * Prüfe, ob auf dem E-Rezept Fachdienst für den Versicherten mit dem Namen {@code patientName}
   * eine Einwilligung zur Speicherung der PKV-Abrechnungsinformationen hinterlegt hat.
   *
   * <p><b>Notiz:</b> aktuell wird hier lediglich eine <code>GET /Consent</code> ausgeführt und bei
   * erfolgreicher Antwort, diese vom Versicherten gemerkt. Eine Prüfung findet aus dem folgenden
   * Grund nicht statt: Der E-Rezept-Fachdienst antwortet nur mit einem gültigen Consent, wenn eine
   * Einwilligung auch erteilt wurde. Wurde bislang keine Einwilligung erteilt (oder
   * zwischenzeitlich), dann antwortet der FD mit einer OperationOutcome und wir bekommen hier eine
   * <code>UnexpectedResponseResourceException</code>
   *
   * @param patientName ist der Name des Versicherten, der die Einwilligung abruft
   */
  @Dann(
      "^hat (?:der|die) Versicherte (.+) eine Einwilligung zur Speicherung der"
          + " PKV-Abrechnungsinformationen beim E-Rezept Fachdienst$")
  public void thenHasConsent(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient).attemptsTo(BillingInformationConsent.getConsent());
  }

  /**
   * Die <em>Und</em>-Variante von {@link #thenHasConsent(String patientName)}
   *
   * @param patientName ist der Name des Versicherten, der die Einwilligung abruft
   */
  @Und(
      "^(?:der|die) Versicherte (.+) hat eine Einwilligung zur Speicherung der"
          + " PKV-Abrechnungsinformationen beim E-Rezept Fachdienst$")
  public void andHasConsent(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    and(thePatient).attemptsTo(BillingInformationConsent.getConsent());
  }

  /**
   * Die <em>theActorInTheSpotlight</em>-Variante von {@link #thenHasConsent(String patientName)}
   */
  @Dann(
      "^hat (?:der|die) Versicherte eine Einwilligung zur Speicherung der"
          + " PKV-Abrechnungsinformationen beim E-Rezept Fachdienst$")
  public void thenHasConsent() {
    val thePatient = OnStage.theActorInTheSpotlight();
    when(thePatient).attemptsTo(BillingInformationConsent.getConsent());
  }

  /** Die <em>theActorInTheSpotlight</em>-Variante von {@link #andHasConsent(String patientName)} */
  @Und(
      "^(?:der|die) Versicherte hat eine Einwilligung zur Speicherung der"
          + " PKV-Abrechnungsinformationen beim E-Rezept Fachdienst$")
  public void andHasConsent() {
    val thePatient = OnStage.theActorInTheSpotlight();
    and(thePatient).attemptsTo(BillingInformationConsent.getConsent());
  }

  @Wenn(
      "^(?:der|die) Versicherte (.+) für das (letzte|erste) E-Rezept die"
          + " PKV-Abrechnungsinformationen abruft$")
  public void whenFdVGetsChargeItem(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(Ensure.that(HasChargeItemBundle.forPrescription(order).asPatient()).isTrue());
  }

  /**
   * Der Step prüft, dass keine PKV-Abrechnungsinformationen beim Fachdienst vorliegen. Erwartet
   * wird eine leere Liste
   *
   * @param patientName der Name des Versicherten, der innerhalb des Szenarios verwendet wird, um
   *     diesen zu identifizieren
   */
  @Dann(
      "^kann (?:der|die) Versicherte (.+) für das (letzte|erste) E-Rezept keine"
          + " PKV-Abrechnungsinformationen (?:mehr abrufen|abrufen)$")
  @Und(
      "^(?:der|die) Versicherte (.+) kann für das (letzte|erste) E-Rezept keine"
          + " PKV-Abrechnungsinformationen (?:mehr abrufen|abrufen)$")
  public void andThenPatientGetsNoChargeItemBundle(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfGetChargeItemBundle.forPrescription(order).asPatient())
                .isGreaterEqual(400));
  }

  /**
   * Der Step prüft, dass keine PKV-Abrechnungsinformationen zum letzten dispensierten Rezept beim
   * Fachdienst vorliegen. Erwartet wird FehlerCode 404
   *
   * @param patientName der Name des Versicherten, der innerhalb des Szenarios verwendet wird, um
   *     diesen zu identifizieren
   */
  @Dann(
      "^kann (?:der|die) Versicherte (.+) für das (letzte|erste) E-Rezept keine"
          + " PKV-Abrechnungsinformationen abrufen, weil sie nicht gefunden werden$")
  public void thenFdvGetsNoChargeItemForPrescription(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfGetChargeItemBundle.forPrescription(order).asPatient())
                .isEqualTo(404));
  }

  /**
   * Der Step prüft, dass der Versicherte seine Einwilligung abrufen kann
   *
   * @param patientName der Name des Versicherten, der innerhalb des Szenarios verwendet wird, um
   *     diesen zu identifizieren
   */
  @Dann("^kann (?:der|die) Versicherte (.+) seine Einwilligung abrufen$")
  public void thenCanGetConsent(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient).attemptsTo(BillingInformationConsent.getConsent());
  }

  /**
   * Der Step löst das Löschen der Abrechnungsinformation zum letzten Rezept aus
   *
   * @param patientName der Name des Versicherten, der innerhalb des Szenarios verwendet wird, um
   *     diesen zu identifizieren
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) für das (letzte|erste) E-Rezept die"
          + " PKV-Abrechnungsinformationen löscht$")
  public void whenDeleteChargeItem(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfDeleteChargeItem.fromStack(order)).isEqualTo(204));
  }

  /**
   * Der Step übergibt den AccessCode und die ID des ChargeItems zum Ändern der
   * PKV-Abrechnungsinformationen an die Apotheke
   *
   * @param patientName der Name des Versicherten, der innerhalb des Szenarios verwendet wird, um
   *     diesen zu identifizieren
   * @param pharmName der Name der Apotheke, die den DMC erhalten soll
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) die Apotheke (.+) via Data Matrix Code zum Ändern des"
          + " (ersten|letzten) PKV-Abgabedatensatzes berechtigt$")
  public void whenPharmacyGetsEnabledByDMC(String patientName, String pharmName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmName);

    when(thePatient)
        .attemptsTo(HandoverChargeItemAuthorization.forChargeItem(order).to(thePharmacy));
  }

  /**
   * Der Step übergibt den AccessCode zum Ändern der PKV-Abrechnungsinformationen an die Apotheke
   * mit einer Communication vom Typ ChargeChangeRequest
   *
   * @param patientName der Name des Versicherten, der innerhalb des Szenarios verwendet wird, um
   *     diesen zu identifizieren
   * @param pharmName der Name der Apotheke, welche die Communication erhalten soll
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) die Apotheke (.+) per Nachricht zum Ändern der"
          + " PKV-Abrechnungsinformationen berechtigt$")
  public void whenPharmacyGetsEnabledByCommunication(String patientName, String pharmName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePatient)
        .attemptsTo(
            SendCommunication.with(
                ResponseOfPostCommunication.changeRequest()
                    .forChargeItemFromBackend(DequeStrategy.LIFO)
                    .sentTo(thePharmacy)
                    .withRandomMessage()));
  }

  /**
   * Der Step löst ein Setzen der Markierungsflags entsprechend der Datatable aus.
   *
   * @param patientName der Name des Versicherten, der innerhalb des Szenarios verwendet wird, um
   *     diesen zu identifizieren
   * @param order die Reihenfolge auf dem Rezept-Stack
   * @param markingFlags die Markierungsflags für die Operation
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) die Markierungen für die PKV-Abrechnungsinformationen des"
          + " (letzten|erste) E-Rezept setzt:$")
  public void whenSetMarkingFlags(String patientName, String order, DataTable markingFlags) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient)
        .attemptsTo(
            ChangeChargeItemMarkingFlags.forPrescription(order).withDataTable(markingFlags));
  }

  /**
   * Der Step überprüft, ob die Markierungsflags entsprechend der DataTable gesetzt sind
   *
   * @param patientName der Name des Versicherten, der innerhalb des Szenarios verwendet wird, um
   *     diesen zu identifizieren
   * @param order die Reihenfolge auf dem Rezept-Stack
   * @param markingFlags die Markierungsflags für die Operation
   */
  @Dann(
      "^hat (?:der|die) Versicherte (.+) die PKV-Abrechnungsinformationen des (letzten|ersten)"
          + " E-Rezepts mit folgenden Markierungen:$")
  public void thenHasMarkingFlags(String patientName, String order, DataTable markingFlags) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            Ensure.that(
                    ChargeItemBundleHasExpectedMarkingFlags.fromDataTable(markingFlags)
                        .forPrescription(order))
                .isTrue());
  }

  @Dann(
      "^kann (?:der|die) Versicherte (.+) für das (letzte|erste) E-Rezept die geänderte"
          + " PKV-Abrechnungsinformationen abrufen$")
  @Und(
      "^(?:der|die) Versicherte (.+) kann für das (letzte|erste) E-Rezept die geänderte"
          + " PKV-Abrechnungsinformationen abrufen$")
  public void andThenFdvGetsChangedChargeItems(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient).attemptsTo(Ensure.that(ChargeItemHasChanged.forPrescription(order)).isTrue());
  }

  @Dann(
      "^hat (?:der|die) Versicherte (.+) die PKV-Abrechnungsinformationen für das (letzte|erste)"
          + " dispensierte Medikament beim Fachdienst vorliegen$")
  public void thenHasChargeItem(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(Ensure.that(HasChargeItemBundle.forPrescription(order).asPatient()).isTrue());
  }

  @Und(
      "^(?:der|die) Versicherte (.+) hat die PKV-Abrechnungsinformationen für das (letzte|erste)"
          + " dispensierte Medikament beim Fachdienst vorliegen$")
  public void andHasChargeItem(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    and(thePatient)
        .attemptsTo(Ensure.that(HasChargeItemBundle.forPrescription(order).asPatient()).isTrue());
  }

  @Dann(
      "^hat (?:der|die) Versicherte die PKV-Abrechnungsinformationen für das (letzte|erste)"
          + " dispensierte Medikament beim Fachdienst vorliegen$")
  public void thenHasChargeItem(String order) {
    val thePatient = OnStage.theActorInTheSpotlight();
    then(thePatient)
        .attemptsTo(Ensure.that(HasChargeItemBundle.forPrescription(order).asPatient()).isTrue());
  }

  @Und(
      "^(?:der|die) Versicherte hat die PKV-Abrechnungsinformationen für das (letzte|erste)"
          + " dispensierte Medikament beim Fachdienst vorliegen$")
  public void andHasChargeItem(String order) {
    val thePatient = OnStage.theActorInTheSpotlight();
    and(thePatient)
        .attemptsTo(Ensure.that(HasChargeItemBundle.forPrescription(order).asPatient()).isTrue());
  }
}
