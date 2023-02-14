/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.lei.steps;

import static net.serenitybdd.screenplay.GivenWhenThen.and;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static net.serenitybdd.screenplay.GivenWhenThen.then;
import static net.serenitybdd.screenplay.GivenWhenThen.when;

import de.gematik.test.erezept.screenplay.questions.ChargeItemHasChanged;
import de.gematik.test.erezept.screenplay.questions.ChargeItemHasExpectedMarkingFlags;
import de.gematik.test.erezept.screenplay.questions.HasChargeItem;
import de.gematik.test.erezept.screenplay.questions.ResponseOfDeleteChargeItem;
import de.gematik.test.erezept.screenplay.questions.ResponseOfGetChargeItem;
import de.gematik.test.erezept.screenplay.questions.ResponseOfPostCommunication;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.task.BillingInformationConsent;
import de.gematik.test.erezept.screenplay.task.ChangeChargeItemMarkingFlags;
import de.gematik.test.erezept.screenplay.task.CheckTheReturnCode;
import de.gematik.test.erezept.screenplay.task.SendCommunication;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.core.PendingStepException;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.ensure.Ensure;

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
      "^(?:der|die) Versicherte (.+) hat (?:seine|ihre) Einwilligung zum Speichern der Abrechnungsinformationen (erteilt|widerrufen)$")
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
      "^(?:der|die) Versicherte hat (?:seine|ihre) Einwilligung zum Speichern der Abrechnungsinformationen (erteilt|widerrufen)$")
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
      "^(?:der|die) Versicherte (.+) (?:seine|ihre) Einwilligung zur Speicherung der Abrechnungsinformationen (erteilt|widerruft)$")
  public void whenGrantConsent(String patientName, String consentAction) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient).attemptsTo(BillingInformationConsent.forAction(consentAction));
  }

  /**
   * Prüfe, ob auf dem E-Rezept Fachdienst für den Versicherten mit dem Namen {@code patientName}
   * eine Einwilligung zur Speicherung der Abrechnungsinformationen hinterlegt hat.
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
      "^hat (?:der|die) Versicherte (.+) eine Einwilligung zur Speicherung der Abrechnungsinformationen beim E-Rezept Fachdienst$")
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
      "^(?:der|die) Versicherte (.+) hat eine Einwilligung zur Speicherung der Abrechnungsinformationen beim E-Rezept Fachdienst$")
  public void andHasConsent(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    and(thePatient).attemptsTo(BillingInformationConsent.getConsent());
  }

  /**
   * Die <em>theActorInTheSpotlight</em>-Variante von {@link #thenHasConsent(String patientName)}
   */
  @Dann(
      "^hat (?:der|die) Versicherte eine Einwilligung zur Speicherung der Abrechnungsinformationen beim E-Rezept Fachdienst$")
  public void thenHasConsent() {
    val thePatient = OnStage.theActorInTheSpotlight();
    when(thePatient).attemptsTo(BillingInformationConsent.getConsent());
  }

  /** Die <em>theActorInTheSpotlight</em>-Variante von {@link #andHasConsent(String patientName)} */
  @Und(
      "^(?:der|die) Versicherte hat eine Einwilligung zur Speicherung der Abrechnungsinformationen beim E-Rezept Fachdienst$")
  public void andHasConsent() {
    val thePatient = OnStage.theActorInTheSpotlight();
    and(thePatient).attemptsTo(BillingInformationConsent.getConsent());
  }

  @Wenn(
      "^(?:der|die) Versicherte (.+) für das (letzte|erste) E-Rezept die Abrechnungsinformationen abruft$")
  public void whenFdVGetsChargeItem(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(Ensure.that(HasChargeItem.forPrescription(order).asPatient()).isTrue());
  }

  /**
   * Der Step prüft, dass keine Abrechnungsinformationen beim Fachdienst vorliegen. Erwartet wird
   * eine leere Liste
   *
   * @param patientName
   */
  @Dann(
      "^kann (?:der|die) Versicherte (.+) für das (letzte|erste) E-Rezept keine Abrechnungsinformationen abrufen$")
  public void thenFdVGetsNoChargeItems(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfGetChargeItem.forPrescription(order).asPatient())
                .isGreaterEqual(400));
  }

  /**
   * Der Step prüft, dass keine Abrechnungsinformationen zum letzten dispensierten Rezept beim
   * Fachdienst vorliegen. Erwartet wird FehlerCode 404
   *
   * @param patientName
   */
  @Dann(
      "^kann (?:der|die) Versicherte (.+) für das (letzte|erste) E-Rezept keine Abrechnungsinformationen abrufen, weil sie nicht gefunden werden$")
  public void thenFdvGetsNoChargeItemForPrescription(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfGetChargeItem.forPrescription(order).asPatient())
                .isEqualTo(404));
  }

  /**
   * Der Step prüft, dass der Versicherte seine Einwilligung abrufen kann
   *
   * @param patientName
   */
  @Dann("^kann (?:der|die) Versicherte (.+) seine Einwilligung abrufen$")
  public void thenCanGetConsent(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient).attemptsTo(BillingInformationConsent.getConsent());
  }

  /**
   * Der Step löst das Löschen der Abrechnungsinformation zum letzten Rezept aus
   *
   * @param patientName
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) für das (letzte|erste) E-Rezept die Abrechnungsinformationen löscht$")
  public void whenDeleteChargeItem(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfDeleteChargeItem.fromStack(order))
                .matching(rc -> rc == 202 || rc == 204));
  }

  /**
   * Der Step übergibt den AccessCode zum Ändern der Abrechnungsinformationen an die Apotheke
   *
   * @param patientName
   * @param pharmName
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) die Apotheke (.+) via Data Matrix Code zum Ändern der Abrechnungsinformationen berechtigt$")
  public void whenPharmacyGetsEnabledByDMC(String patientName, String pharmName) {
    throw new PendingStepException("Not yet implemented");
  }

  /**
   * Der Step übergibt den AccessCode zum Ändern der Abrechnungsinformationen an die Apotheke mit
   * einer Communication vom Typ ChargChangeRequest
   *
   * @param patientName
   * @param pharmName
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) die Apotheke (.+) per Nachricht zum Ändern der Abrechnungsinformationen berechtigt$")
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
   * @param patientName
   * @param order
   * @param markingFlags
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) die Markierungen für Abrechnungsinformationen des (letzten|erste) E-Rezept setzt:$")
  public void whenSetMarkingFlags(String patientName, String order, DataTable markingFlags) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient)
        .attemptsTo(
            ChangeChargeItemMarkingFlags.forPrescription(order).withDataTable(markingFlags));
  }

  /**
   * Der Step überprüft, ob die Markierungsflags entsprechend der DataTable gesetzt sind
   *
   * @param patientName
   * @param order
   * @param markingFlags
   */
  @Dann(
      "^hat (?:der|die) Versicherte (.+) die Abrechnungsinformationen des (letzten|ersten) E-Rezepts mit folgenden Markierungen:$")
  public void thenHasMarkingFlags(String patientName, String order, DataTable markingFlags) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            Ensure.that(
                    ChargeItemHasExpectedMarkingFlags.fromDataTable(markingFlags)
                        .forPrescription(order))
                .isTrue());
  }

  @Dann(
      "^kann (?:der|die) Versicherte (.+) für das (letzte|erste) E-Rezept die geänderte Abrechnungsinformationen abrufen$")
  public void thenFdvGetsChangedChargeItems(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient).attemptsTo(Ensure.that(ChargeItemHasChanged.forPrescription(order)).isTrue());
  }

  @Dann(
      "^hat (?:der|die) Versicherte (.+) eine Abrechnungsinformation für das (letzte|erste) dispensierte Medikament beim Fachdienst vorliegen$")
  public void thenHasChargeItem(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(Ensure.that(HasChargeItem.forPrescription(order).asPatient()).isTrue());
  }

  @Und(
      "^(?:der|die) Versicherte (.+) hat eine Abrechnungsinformation für das (letzte|erste) dispensierte Medikament beim Fachdienst vorliegen$")
  public void andHasChargeItem(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    and(thePatient)
        .attemptsTo(Ensure.that(HasChargeItem.forPrescription(order).asPatient()).isTrue());
  }

  @Dann(
      "^hat (?:der|die) Versicherte eine Abrechnungsinformation für das (letzte|erste) dispensierte Medikament beim Fachdienst vorliegen$")
  public void thenHasChargeItem(String order) {
    val thePatient = OnStage.theActorInTheSpotlight();
    then(thePatient)
        .attemptsTo(Ensure.that(HasChargeItem.forPrescription(order).asPatient()).isTrue());
  }

  @Und(
      "^(?:der|die) Versicherte hat eine Abrechnungsinformation für das (letzte|erste) dispensierte Medikament beim Fachdienst vorliegen$")
  public void andHasChargeItem(String order) {
    val thePatient = OnStage.theActorInTheSpotlight();
    and(thePatient)
        .attemptsTo(Ensure.that(HasChargeItem.forPrescription(order).asPatient()).isTrue());
  }
}
