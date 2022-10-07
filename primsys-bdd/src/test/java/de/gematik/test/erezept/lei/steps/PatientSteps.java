/*
 * Copyright (c) 2022 gematik GmbH
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

import static java.text.MessageFormat.format;
import static net.serenitybdd.screenplay.GivenWhenThen.*;

import de.gematik.test.erezept.client.ClientType;
import de.gematik.test.erezept.lei.cfg.TestsuiteConfiguration;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.questions.*;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategyEnum;
import de.gematik.test.erezept.screenplay.task.*;
import de.gematik.test.smartcard.SmartcardArchive;
import de.gematik.test.smartcard.factory.SmartcardFactory;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.de.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.core.PendingStepException;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;
import net.serenitybdd.screenplay.ensure.Ensure;

/**
 * Testschritte die aus der Perspektive eines Versicherten bzw. einer Versicherten ausgeführt werden
 */
@Slf4j
public class PatientSteps {

  private SmartcardArchive smartcards;
  private TestsuiteConfiguration config;

  @Before
  public void setUp() {
    smartcards = SmartcardFactory.readArchive();
    config = TestsuiteConfiguration.getInstance();
    OnStage.setTheStage(new OnlineCast());
  }

  @After
  public void tearDown() {
    OnStage.drawTheCurtain();
  }

  /**
   * Initialisiere einen Versicherten mit einer eGK und der Versicherungsart aus {@code
   * insuranceType}
   *
   * <p>Gültige Versicherungsarten sind:
   *
   * <ul>
   *   <li>GKV: gesetzliche Krankenversicherung
   *   <li>PKV: private Krankenversicherung
   *   <li>BG: Berufsgenossenschaft
   *   <li>SEL: Selbstzahler
   *   <li>SOZ: Sozialamt
   *   <li>GPV: gesetzliche Pflegeversicherung
   *   <li>PPV: private Pflegeversicherung
   *   <li>BEI: Beihilfe
   * </ul>
   *
   * @param insuranceType ist eine der gültigen Versicherungsarten, mit der dieser Versicherte im
   *     Laufe des Szenarios agieren wird
   * @param patientName der Name des Versicherten, der innerhalb des Szenarios verwendet wird, um
   *     diesen zu identifizieren
   */
  @Angenommen(
      "^(?:der|die) (GKV|PKV|BG|SEL|SOZ|GPV|PPV|BEI) Versicherte (.+) hat Zugriff auf (?:seine|ihre) (?:digitale Identität|eGK)$")
  public void initPatient(String insuranceType, String patientName) {
    // fetch the chosen Smartcards
    val patientConfig = config.getPatientConfig(patientName);
    val egk =
        smartcards.getEgkByICCSN(patientConfig.getEgkIccsn(), patientConfig.getCryptoAlgorithm());

    // create the abilities
    val useErpClient =
        UseTheErpClient.with(
            patientConfig.toErpClientConfig(config.getActiveEnvironment(), ClientType.FDV));
    val receiveDrugs = new ReceiveDispensedDrugs();
    val provideBaseData =
        ProvidePatientBaseData.forPatient(egk.getKvnr(), patientName, insuranceType);

    // assemble the screenplay
    val thePatient = OnStage.theActorCalled(patientName);
    thePatient.describedAs(
        format(
            "Ein {0} Krankenversicherter der E-Rezepte verschrieben bekommt und in Apotheken einlöst",
            insuranceType));
    givenThat(thePatient).can(ProvideEGK.sheOwns(egk));
    givenThat(thePatient).can(ManageDataMatrixCodes.sheGetsPrescribed());
    givenThat(thePatient).can(receiveDrugs);
    givenThat(thePatient).can(useErpClient);
    givenThat(thePatient).can(provideBaseData);
    givenThat(thePatient).can(ManagePatientPrescriptions.itWorksWith());
    givenThat(thePatient).can(ManageCommunications.sheExchanges());
    useErpClient.authenticateWith(egk);
  }

  /**
   * Prüfe, ob der angegebene Versicherte mit dem Namen aus {@code patientName} die angegebene
   * Versicherungsart aus {@code insuranceType} zugewiesen bekommen hat.
   *
   * <p>Dieser Schritt führt <b>keine</b> Aktion beim Fachdienst aus, sondern prüft lediglich die
   * Versicherungsart dem Versicherten zugewiesen wurde
   *
   * @param patientName ist der Name des Versicherten, dessen Versicherungsart geprüft wird
   * @param insuranceType ist die erwartete Versicherungsart
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) die Versicherungsart (GKV|PKV|BG|SEL|SOZ|GPV|PPV|BEI) aufweist$")
  public void whenInsuranceTypeIs(String patientName, String insuranceType) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient)
        .attemptsTo(Ensure.that(HisInsuranceType.equalsExpected(insuranceType)).isTrue());
  }

  /**
   * Dieser Schritt bildet den Vorgang in einer realen Apotheke nach. Der Versicherte mit dem Namen
   * aus {@code patientName} ruft zunächst alle seine E-Rezepte beim Fachdienst ab und wählt das
   * gewünschte E-Rezept anhand von {@code order} aus. Im Anschluss erfolgt die Zuweisung über den
   * Data Matrix Code an die Apotheke mit dem Namen {@code pharmacyName}.
   *
   * <p>Mit der Zuweisung über den DMC (Data * Matrix Code) findet keine weitere Interaktion durch
   * den Versicherten mit dem Fachdienst statt. Stattdessen wird dem Apotheker der DMC vorgezeigt.
   *
   * @param patientName ist der Name des Versicherten, der ein E-Rezept bei einer Apotheke einlösen
   *     möchte
   * @param order ist die Reihenfolge, mit der das gewünschte E-Rezept ausgewählt wird
   * @param pharmacyName ist die Apotheke, welche den DMC zugewiesen bekommt
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) (ausgestellte|gelöschte) E-Rezept der Apotheke (.+) via Data Matrix Code zuweist$")
  public void whenAssignDataMatrxiCodeFromStack(
      String patientName, String order, String dmcStack, String pharmacyName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmacyName);

    when(thePatient)
        .attemptsTo(HandoverDataMatrixCode.fromStack(dmcStack).with(order).to(thePharmacy));
  }

  @Wenn(
      "^(?:der|die) Versicherte (?:sein|ihr) (letztes|erstes) (ausgestellte|gelöschte) E-Rezept der Apotheke (.+) via Data Matrix Code zuweist$")
  public void whenAssignDataMatrxiCodeFromStack(
      String order, String dmcStack, String pharmacyName) {
    val thePatient = OnStage.theActorInTheSpotlight();
    val thePharmacy = OnStage.theActorCalled(pharmacyName);

    when(thePatient)
        .attemptsTo(HandoverDataMatrixCode.fromStack(dmcStack).with(order).to(thePharmacy));
  }

  @Wenn(
      "^(?:der Vertreter|die Vertreterin) (.+) (?:sein|ihr) (letztes|erstes) von (.+) zugewiesenes E-Rezept der Apotheke (.+) via Data Matrix Code zuweist$")
  public void whenAssignDispenseRequestPhysicallyAsRepresentative(
      String representativeName, String order, String patientName, String pharmacyName) {
    val theRepresentative = OnStage.theActorCalled(representativeName);
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmacyName);

    when(theRepresentative)
        .attemptsTo(
            HandoverDispenseRequestAsRepresentative.fromStack(order)
                .ofTheOwner(thePatient)
                .to(thePharmacy));
  }

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
   * erfolgreicher Antwort, diese vom Versicherten gemerkt. Ein Prüfung findet aus dem folgenden
   * Grund nicht statt: der E-Rezept Fachdienst antwortet nur mit einem gültigen Consent, wenn eine
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

  @Dann("^hat (?:der|die) Versicherte (.+) noch kein E-Rezept über DMC erhalten$")
  public void thenDidNotReceiveDmc(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient).attemptsTo(Ensure.that(HasDataMatrixCodes.exactly(0)).isTrue());
  }

  @Dann(
      "^hat (?:der|die) Versicherte (.+) (mindestens|maximal|genau) (\\d+) Medikament(?:e)? erhalten$")
  public void thenReceivedDrugs(String patientName, String adverb, long amount) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient).attemptsTo(Ensure.that(HasDispensedDrugs.of(adverb, amount)).isTrue());
  }

  @Und(
      "^(?:der|die) Versicherte (.+) hat (mindestens|maximal|genau) (\\d+) Medikament(?:e)? erhalten$")
  public void andReceivedDrugs(String patientName, String adverb, long amount) {
    val thePatient = OnStage.theActorCalled(patientName);
    and(thePatient).attemptsTo(Ensure.that(HasDispensedDrugs.of(adverb, amount)).isTrue());
  }

  @Dann(
      "^hat (?:der|die) Versicherte (.+) eine Abrechnungsinformation für das letzte dispensierte Medikament beim Fachdienst vorliegen$")
  public void thenHasChargeItem(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient).attemptsTo(Ensure.that(HasChargeItem.forLastDispensedDrug()).isTrue());
  }

  @Und(
      "^(?:der|die) Versicherte (.+) hat eine Abrechnungsinformation für das letzte dispensierte Medikament beim Fachdienst vorliegen$")
  public void andHasChargeItem(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    and(thePatient).attemptsTo(Ensure.that(HasChargeItem.forLastDispensedDrug()).isTrue());
  }

  @Dann(
      "^hat (?:der|die) Versicherte eine Abrechnungsinformation für das letzte dispensierte Medikament beim Fachdienst vorliegen$")
  public void thenHasChargeItem() {
    val thePatient = OnStage.theActorInTheSpotlight();
    then(thePatient).attemptsTo(Ensure.that(HasChargeItem.forLastDispensedDrug()).isTrue());
  }

  @Und(
      "^(?:der|die) Versicherte hat eine Abrechnungsinformation für das letzte dispensierte Medikament beim Fachdienst vorliegen$")
  public void andHasChargeItem() {
    val thePatient = OnStage.theActorInTheSpotlight();
    and(thePatient).attemptsTo(Ensure.that(HasChargeItem.forLastDispensedDrug()).isTrue());
  }

  @Wenn(
      "^(?:der|die) Versicherte (.+) das (letztes|erstes) (?:ihm|ihr) zugewiesene E-Rezept herunterlädt$")
  public void whenDownloadPrescription(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient).attemptsTo(RetrievePrescriptionFromServer.andChooseWith(order));
  }

  @Wenn(
      "^(?:der|die) Versicherte (.+) das (letztes|erstes) heruntergeladene E-Rezept der Apotheke (.+) zuweist$")
  public void whenAssignPrescription(String patientName, String order, String pharmName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacist = OnStage.theActorCalled(pharmName);
    when(thePatient)
        .attemptsTo(RedeemPrescription.assign(thePharmacist, DequeStrategyEnum.fromString(order)));
  }

  @Wenn(
      "^(?:der|die) Versicherte (.+) für das (letztes|erstes) heruntergeladene E-Rezept eine Anfrage an die Apotheke (.+) schickt$")
  public void whenReserveRequest(String patientName, String order, String pharmName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacist = OnStage.theActorCalled(pharmName);
    when(thePatient)
        .attemptsTo(RedeemPrescription.reserve(thePharmacist, DequeStrategyEnum.fromString(order)));
  }

  /**
   * Der angegebene Patient ruft das letzte verschriebene Rezept auf dem Patientenstapel beim FD ab
   *
   * @see <a href="https://service.gematik.de/browse/TMD-1605">TMD-1605</a>
   * @param patientName ist der Name des Versicherten
   */
  @Dann("^wird (?:der Versicherten|dem Versicherten) (.+) das neue E-Rezept im FdV angezeigt$")
  @Dann(
      "^wird (?:der Versicherten|dem Versicherten) (.+) das neue E-Rezept im FdV angezeigt ohne AccessCode$")
  public void thenFetchPrescriptionFromBackend(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(Ensure.that(TheLastPrescription.prescribed().existsInBackend()).isTrue());
  }

  @Dann("^wird (?:dem Vertreter|der Vertreterin) (.+) das neue E-Rezept im FdV angezeigt$")
  public void thenFetchPrescriptionAsRepresentative(String representativeName) {
    val theRepresentative = OnStage.theActorCalled(representativeName);
    then(theRepresentative)
        .attemptsTo(Ensure.that(TheLastPrescription.prescribed().existsInBackend()).isTrue());
  }

  /**
   * Der angegebene Patient ruft das letzte verschriebene Rezept auf dem Patientenstapel beim FD ab
   *
   * @see <a href="https://service.gematik.de/browse/TMD-1605">TMD-1605</a>
   */
  @Dann("^wird (?:der Versicherten|dem Versicherten) das neue E-Rezept im FdV angezeigt$")
  public void thenFetchPrescriptionFromBackend() {
    val thePatient = OnStage.theActorInTheSpotlight();
    then(thePatient)
        .attemptsTo(Ensure.that(TheLastPrescription.prescribed().existsInBackend()).isTrue());
  }

  /**
   * Negierung von TMD-1605
   *
   * @param patientName ist der Name des Versicherten
   */
  @Dann(
      "^wird (?:der|dem) Versicherten (.+) (?:sein|ihr) letztes (ausgestellte|gelöschte) E-Rezept nicht mehr im FdV angezeigt$")
  public void thenPrescriptionNotDisplayed(String patientName, String stack) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(Ensure.that(TheLastPrescription.from(stack).existsInBackend()).isFalse());
  }

  @Dann(
      "^wird (?:der|dem) Versicherten (?:sein|ihr) letztes (ausgestellte|gelöschte) E-Rezept nicht mehr im FdV angezeigt$")
  public void thenPrescriptionNotDisplayed(String stack) {
    val thePatient = OnStage.theActorInTheSpotlight();
    then(thePatient)
        .attemptsTo(Ensure.that(TheLastPrescription.from(stack).existsInBackend()).isFalse());
  }

  /**
   * TMD-1624
   *
   * @param patientName ist der Name des Versicherten
   */
  @Wenn("^(?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) E-Rezept löscht$")
  public void whenDeletePrescription(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient).attemptsTo(AbortPrescription.asPatient().fromStack(order));
  }

  @Wenn("^(?:der|die) Versicherte (?:sein|ihr) (letztes|erstes) E-Rezept löscht$")
  public void whenDeletePrescription(String order) {
    val thePatient = OnStage.theActorInTheSpotlight();
    when(thePatient).attemptsTo(AbortPrescription.asPatient().fromStack(order));
  }

  @Dann("^kann (?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) E-Rezept nicht löschen$")
  public void thenCannotDeletePrescription(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(400));
  }

  @Dann(
      "^kann (?:der Vertreter|die Vertreterin) (.+) das (letzte|erste) von (.+) zugewiesene E-Rezept ohne AccessCode nicht löschen$")
  public void thenRepresentativeCannotDeletePrescription(
      String representativeName, String order, String patientName) {
    val theRepresentative = OnStage.theActorCalled(representativeName);
    val thePatient = OnStage.theActorCalled(patientName);
    then(theRepresentative)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(403));
  }

  @Aber(
      "^(?:der Vertreter|die Vertreterin) (.+) kann das (letzte|erste) von (.+) zugewiesene E-Rezept mit AccessCode löschen$")
  public void thenRepresentativeCanDeletePrescription(
      String representativeName, String order, String patientName) {
    val theRepresentative = OnStage.theActorCalled(representativeName);
    val thePatient = OnStage.theActorCalled(patientName);
    then(theRepresentative)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asRepresentative().fromStack(order))
                .isEqualTo(204));
  }

  @Dann("^kann (?:der|die) Versicherte (?:sein|ihr) (letztes|erstes) E-Rezept nicht löschen$")
  public void thenCannotDeletePrescription(String order) {
    val thePatient = OnStage.theActorInTheSpotlight();
    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(400));
  }

  @Und("^(?:der|die) Versicherte (.+) kann (?:sein|ihr) (letztes|erstes) E-Rezept nicht löschen$")
  public void andCannotDeletePrescription(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    and(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(400));
  }

  @Und("^(?:der|die) Versicherte kann (?:sein|ihr) (letztes|erstes) E-Rezept nicht löschen$")
  public void andCannotDeletePrescription(String order) {
    val thePatient = OnStage.theActorInTheSpotlight();
    and(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(400));
  }

  @Und(
      "^(?:der|die) Versicherte (.+) kann (?:sein|ihr) (letztes|erstes) E-Rezept nicht löschen, weil (?:sie|er) nicht das Recht dazu hat$")
  public void andCannotDeletePrescription403(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    and(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(403));
  }

  @Und(
      "^(?:der|die) Versicherte kann (?:sein|ihr) (letztes|erstes) E-Rezept nicht löschen, weil (?:sie|er) nicht das Recht dazu hat$")
  public void andCannotDeletePrescription403(String order) {
    val thePatient = OnStage.theActorInTheSpotlight();
    and(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(403));
  }

  /**
   * Negierung von TMD-1624
   *
   * @param patientName ist der Name des Versicherten
   */
  @Dann(
      "^kann (?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) E-Rezept nicht löschen, weil es einen Konflikt gibt$")
  public void thenCannotDeletePrescription409(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(409));
  }

  @Dann(
      "^kann (?:der|die) Versicherte (?:sein|ihr) (letztes|erstes) E-Rezept nicht löschen, weil es einen Konflikt gibt$")
  public void thenCannotDeletePrescription409(String order) {
    val thePatient = OnStage.theActorInTheSpotlight();
    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(409));
  }

  @Und(
      "^(?:der|die) Versicherte (.+) kann (?:seine|ihr) (letztes|erstes) E-Rezept nicht löschen, weil es einen Konflikt gibt$")
  public void andCannotDeletePrescription409(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(409));
  }

  @Und(
      "^(?:der|die) Versicherte kann (?:seine|ihr) (letztes|erstes) E-Rezept nicht löschen, weil es einen Konflikt gibt$")
  public void andCannotDeletePrescription409(String order) {
    val thePatient = OnStage.theActorInTheSpotlight();
    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(409));
  }

  @Dann(
      "^kann (?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) E-Rezept nicht löschen, weil (?:sie|er) nicht das Recht dazu hat$")
  public void thenCannotDeletePrescription403(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(403));
  }

  @Dann(
      "^kann (?:der|die) Versicherte (?:sein|ihr) (letztes|erstes) E-Rezept nicht löschen, weil (?:sie|er) nicht das Recht dazu hat$")
  public void thenCannotDeletePrescription403(String order) {
    val thePatient = OnStage.theActorInTheSpotlight();
    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(403));
  }

  /**
   * TMD-1640
   *
   * @param patientName
   * @param order
   * @param pharmName
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) E-Rezept der Apotheke (.+) per Nachricht zuweist$")
  public void whenRequestDispenseViaCommunication(
      String patientName, String order, String pharmName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePatient)
        .attemptsTo(
            SendCommunication.with(
                ResponseOfPostCommunication.dispenseRequest()
                    .forPrescriptionFromBackend(order)
                    .sentTo(thePharmacy)
                    .withRandomMessage()));
  }

  /**
   * TMD-1644
   *
   * @param patientName
   * @param order
   * @param pharmName
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) zu (?:seinem|ihrem) (letzten|ersten) E-Rezept der Apotheke (.+) eine Anfrage schickt$")
  public void whenRequestInformationViaCommunication(
      String patientName, String order, String pharmName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePatient)
        .attemptsTo(
            SendCommunication.with(
                ResponseOfPostCommunication.infoRequest()
                    .forPrescriptionFromBackend(order) // letzte | erste
                    .sentTo(thePharmacy)
                    .withRandomMessage()));
  }

  @Wenn(
      "^(?:der|die) Versicherte zu (?:seinem|ihrem) (letzten|ersten) E-Rezept der Apotheke (.+) eine Anfrage schickt$")
  public void whenRequestInformationViaCommunication(String order, String pharmName) {
    val thePatient = OnStage.theActorInTheSpotlight();
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePatient)
        .attemptsTo(
            SendCommunication.with(
                ResponseOfPostCommunication.infoRequest()
                    .forPrescriptionFromBackend(order) // letzte | erste
                    .sentTo(thePharmacy)
                    .withRandomMessage()));
  }

  /**
   * TMD-1646
   *
   * @param patientName
   * @param pharmName
   */
  @Dann("^hat (?:der|die) Versicherte (.+) eine Antwort von der Apotheke (.+) erhalten$")
  public void thenHasReceivedResponseFrom(String patientName, String pharmName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePatient)
        .attemptsTo(Ensure.that(HasReceivedCommunication.reply().from(thePharmacy)).isTrue());
  }

  @Dann(
      "^hat (?:der|die) Versicherte (.+) keine Antwort von der Apotheke (.+) für das (letzte|erste) E-Rezept erhalten$")
  public void thenHasNotReceivedResponseFrom(String patientName, String pharmName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmName);

    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfGetCommunicationFrom.sender(thePharmacy).onStack(order))
                .isEqualTo(404));
  }

  @Dann("^hat (?:der|die) Versicherte eine Antwort von der Apotheke (.+) erhalten$")
  public void thenHasReceivedResponseFrom(String pharmName) {
    val thePatient = OnStage.theActorInTheSpotlight();
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePatient)
        .attemptsTo(Ensure.that(HasReceivedCommunication.reply().from(thePharmacy)).isTrue());
  }

  /**
   * TMD-1648
   *
   * @param patientName
   * @param representativeName
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) E-Rezept per Nachricht an (?:den Vertreter|die Vertreterin) (.+) schickt$")
  public void whenSendCommunicationRepresentative(
      String patientName, String order, String representativeName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val theRepresentative = OnStage.theActorCalled(representativeName);
    when(thePatient)
        .attemptsTo(
            SendCommunication.with(
                ResponseOfPostCommunication.representative()
                    .forPrescriptionFromBackend(order) // letzte | erste
                    .sentTo(theRepresentative)
                    .withRandomMessage()));
  }

  @Dann(
      "^kann (?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) E-Rezept nicht per Nachricht an (?:den Vertreter|die Vertreterin) (.+) schicken$")
  public void thenCanotSendCommunicationRepresentative400(
      String patientName, String order, String representativeName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val theRepresentative = OnStage.theActorCalled(representativeName);
    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfPostCommunication.representative()
                        .forPrescriptionFromBackend(order) // letzte | erste
                        .sentTo(theRepresentative)
                        .withRandomMessage())
                .isEqualTo(400));
  }

  /**
   * TMD-1649
   *
   * @param representativeName Name des Vertreters
   * @param patientName Name des versicherten
   */
  @Dann(
      "^hat (?:der Vertreter|die Vertreterin) (.+) die Nachricht mit dem Rezept (?:der Versicherten|des Versicherten) (.+) empfangen$")
  @Und(
      "^(?:der Vertreter|die Vertreterin) (.+) hat die Nachricht mit dem Rezept (?:der Versicherten|des Versicherten) (.+) empfangen$")
  public void thenRepresentativeReceivedMessageFrom(String representativeName, String patientName) {
    val theRepresentative = OnStage.theActorCalled(representativeName);
    val thePatient = OnStage.theActorCalled(patientName);
    then(theRepresentative)
        .attemptsTo(
            Ensure.that(HasReceivedCommunication.representative().from(thePatient)).isTrue());
  }

  /**
   * Der Step rüft, ob für das letzte dispensierte Rezept des Versicherten Abrechnungsinformationen
   * beim Fachdienst vorliegen
   *
   * @param patientName
   */
  @Dann(
      "^kann (?:der|die) Versicherte (.+) für das letzte E-Rezept die Abrechnungsinformationen im FdV abrufen$")
  public void thenFdVgetsChargeItem(String patientName) {
    throw new PendingStepException("Not yet implemented");
  }

  @Wenn(
      "^(?:der|die) Versicherte (.+) für das letzte E-Rezept die Abrechnungsinformationen im FdV abruft$")
  public void whenFdVgetsChargeItem(String patientName) {
    throw new PendingStepException("Not yet implemented");
  }

  /**
   * Der Step prüft, dass keine Abrechnungsinformationen beim Fachdienst vorliegen Erwartet wird
   * eine leere Liste
   *
   * @param patientName
   */
  @Dann("^kann (?:der|die) Versicherte (.+) keine Abrechnungsinformationen im FdV abrufen$")
  public void thenFdVgestsNoChargeItems(String patientName) {
    throw new PendingStepException("Not yet implemented");
  }
  /**
   * Der Step prüft, dass keine Abrechnungsinformationen zum letzten dispensierten Rezept beim
   * Fachdienst vorliegen Erwartet wird FehlerCode 404
   *
   * @param patientName
   */
  @Dann(
      "^kann (?:der|die) Versicherte (.+) für das letzte E-Rezept keine Abrechnungsinformationen im FdV abrufen, weil sie nicht gefunden werden$")
  public void thenFdVgestsNoChargeItemforPrescription(String patientName) {
    throw new PendingStepException("Not yet implemented");
  }

  /**
   * Der Step prüft, dass der Versicherte seine Einwilligung abrufen kann
   *
   * @param patientName
   */
  @Dann("^kann (?:der|die) Versicherte (.+) seine Einwilligung im FdV abrufen$")
  public void thenCangetConsent(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient).attemptsTo(BillingInformationConsent.getConsent());
  }

  /**
   * Der Step löst das Löschen der Abrechnungsinformation zum letzten Rezept aus
   *
   * @param patientName
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) für das letzte E-Rezept die Abrechnungsinformationen im FdV löscht$")
  public void whenDeleteChargeItem(String patientName) {
    throw new PendingStepException("Not yet implemented");
  }

  /**
   * Der Step übergibt den AccessCode zum Ändern der Abrechnungsinformationen an die Apotheke
   *
   * @param patientName
   * @param pharmName
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) die Apotheke (.+) via Data Matrix Code zum Ändern der Abrechnungsinformationen berechtigt$")
  public void whenPharmacyGetsEnabledbyDMC(String patientName, String pharmName) {
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
                    .forChargeItemFromBackend(DequeStrategyEnum.LIFO) // letzte | erste
                    .sentTo(thePharmacy)
                    .withRandomMessage()));
  }

  /**
   * Der Step prüft, dass für den Versicherten eine Nachricht vom Typ ChargChangeReplay von der
   * Apotheke vorliegt
   *
   * @param patientName ist der Name des Versicherten Patienten
   * @param pharmName ist der Name der Apotheke, von der die Antwort erwartet wird
   */
  @Dann(
      "^hat (?:der|die) Versicherte (.+) eine Antwort auf (?:seinen|ihren) Änderungswunsch von der Apotheke (.+) erhalten$")
  public void thenHasReceivedResponseToChangeRequestFrom(String patientName, String pharmName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmName);

    then(thePatient)
        .attemptsTo(Ensure.that(HasReceivedCommunication.changeReply().from(thePharmacy)).isTrue());
  }

  /**
   * Der Step löst ein Setzen der Markierungsflags entsprechend der Datatable aus.
   *
   * @param patientName
   * @param markingFlags
   */
  @Wenn(
      "^(?:der Versicherte|die Versicherte) (.+) die Markierungen für Abrechnungsinformationen des letzten E-Rezept setzt:$")
  public void whenSetMarkingFlags(String patientName, DataTable markingFlags) {
    throw new PendingStepException("Not yet implemented");
  }

  /**
   * Der Step überprüft, ob die Markierungsflags entsprechend der DataTable gesetzt sind
   *
   * @param patientName
   * @param markingFlags
   */
  @Dann(
      "^hat (?:der Versicherte|die Versicherte) (.+) die Abrechnungsinformationen des letzten E-Rezepts mit folgenden Markierungen:$")
  public void thenHasMarkingFlags(String patientName, DataTable markingFlags) {
    throw new PendingStepException("Not yet implemented");
  }

  @Dann(
      "^kann (?:der Versicherte|die Versicherte) (.+) für das letzte E-Rezept geänderte Abrechnungsinformationen im FdV abrufen$")
  public void thenFdvGetsChangedChargeItems(String patientName) {
    throw new PendingStepException("Not yet implemented");
  }

  /**
   * Der Step erzeugt und zeigt den DMC zum erzeugten E-Rezept, damit er mit der E-Rezept
   * eingescannt werden kann
   *
   * @param patientName
   */
  @Wenn("^(?:der Versicherte|die Versicherte) (.+) ein DMC zum Rezept erhält$")
  public void whenDmcIsGivenToPatient(String patientName) {
    throw new PendingStepException("Not yet implemented");
  }

  /**
   * Manueller Teststep zum Scannen des DMC mit dem FdV
   *
   * @param patientName
   */
  @Wenn("^(?:der Versicherte|die Versicherte) (.+) das E-Rezept per DMC in das FdV einscannt$")
  public void whenPatientScansDmc(String patientName) {
    throw new PendingStepException("Not yet implemented");
  }

  /**
   * Manueller Teststep zum Auslösen der alternativen Zuweisung im FdV
   *
   * @param patientName
   * @param pharmName
   * @param option
   */
  @Wenn(
      "^(?:der Versicherte|die Versicherte) (.+) für das E-Rezept die alternative Zuweisung an die Apotheke (.+) mit der Option (.+) auslöst$")
  public void whenPatientInitiatsAlternativeAssignment(
      String patientName, String pharmName, String option) {
    throw new PendingStepException("Not yet implemented");
  }

  @Dann(
      "^kann (?:der|die) Versicherte (.+) (\\d+) Dispensierinformation(?:en)? für (?:sein|ihr) (erstes|letztes) E-Rezept abrufen$")
  public void thenPatientGetsMedicationDispense(String patientName, long amount, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            Ensure.that(
                    MedicationDispenseContains.forThePatient()
                        .andPrescription(order)
                        .numberOfMedicationDispenses(amount))
                .isTrue());
  }

  @Dann(
      "^kann (?:der|die) Versicherte (.+) nicht mehr die Nachrichten zu (?:seinem|ihrem) (ersten|letzten) E-Rezept abrufen$")
  public void thenPatientCannotGetCommunicationsBasedOnTask(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(Ensure.that(PatientDoesHaveMessagesForTask.fromStack(order)).isFalse());
  }

  @Wenn("^(?:der|die) Versicherte (.+) alle (?:seinem|ihre) versendeten Nachrichten löscht$")
  public void whenPatientDeletesAllCommunications(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient).attemptsTo(DeleteAllSentCommunications.fromBackend());
  }

  @Wenn("^(?:der|die) Versicherte (.+) (?:ihre|seine) (letzte|erste) versendete Nachricht löscht$")
  public void whenPharmacyDeletesCommunication(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient).attemptsTo(DeleteSentCommunication.fromStack(order));
  }

  @Dann(
      "^kann (?:der|die) Versicherte (.+) keine (?:ihrer|seiner) versendeten Nachrichten mehr abrufen$")
  public void thenPatientCannotGetAnyCommunications(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(Ensure.that(HistSentCommunications.onBackend().noneExistAnymore()).isTrue());
  }

  @Dann(
      "^kann (?:der|die) Versicherte (.+) (?:ihre|seine) (letzte|erste) Nachricht nicht mehr abrufen$")
  public void thenPatientCannotGetASentCommunication(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            Ensure.that(HistSentCommunications.onBackend().fromQueueStillExists(order)).isFalse());
  }

  @Dann(
      "^hat (?:der Versicherte|die Versicherte) (.+) für das (.+) dispensiert E-Rezept im Zugriffsprotokoll des FdV einen Protokolleintrag$")
  public void thenPatientHasNewEntryInAccessProtocolForPrescription(
      String patientName, String sort) {
    throw new PendingStepException("Not yet implemented");
  }
}
