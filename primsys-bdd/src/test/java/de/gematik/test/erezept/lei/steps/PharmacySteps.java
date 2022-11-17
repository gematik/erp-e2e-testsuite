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

import static net.serenitybdd.screenplay.GivenWhenThen.*;

import de.gematik.test.erezept.client.*;
import de.gematik.test.erezept.client.exceptions.*;
import de.gematik.test.erezept.exceptions.*;
import de.gematik.test.erezept.lei.cfg.*;
import de.gematik.test.erezept.pspwsclient.*;
import de.gematik.test.erezept.pspwsclient.config.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.questions.*;
import de.gematik.test.erezept.screenplay.strategy.*;
import de.gematik.test.erezept.screenplay.task.*;
import de.gematik.test.erezept.screenplay.util.*;
import de.gematik.test.konnektor.commands.options.*;
import de.gematik.test.smartcard.*;
import io.cucumber.datatable.*;
import io.cucumber.java.*;
import io.cucumber.java.de.*;
import lombok.*;
import net.serenitybdd.core.*;
import net.serenitybdd.screenplay.actors.*;
import net.serenitybdd.screenplay.ensure.*;

/**
 * Testschritte die aus der Perspektive einer Apotheke und Apotheker bzw. Apothekerin ausgeführt
 * werden
 */
public class PharmacySteps {

  private SmartcardArchive smartcards;
  private TestsuiteConfiguration config;

  @Before
  public void setUp() {
    smartcards = SmartcardFactory.getArchive();
    config = TestsuiteConfiguration.getInstance();
    OnStage.setTheStage(new OnlineCast());
  }

  /**
   * Initialisiere einen Apotheker mit einer SMC-B <br>
   * <b>Notiz:</b> Der Name im ersten Parameter entspricht nicht dem echten Namen, der auf der
   * Smartcard hinterlegt ist. Dieser Name wird lediglich für das Screenplay-Pattern benötigt
   *
   * @param pharmName der Name der Apotheke, der innerhalb des Szenarios verwendet wird, um diesen
   *     zu identifizieren
   */
  @Angenommen("^die Apotheke (.+) hat Zugriff auf ihre SMC-B$")
  public void initPharmacy(String pharmName) {
    // fetch the chosen Smartcards
    val pharmConfig = config.getPharmacyConfig(pharmName);
    val smcb =
        smartcards.getSmcbByICCSN(pharmConfig.getSmcbIccsn(), pharmConfig.getCryptoAlgorithm());

    // create the abilities
    val useKonnektor =
        UseTheKonnektor.with(smcb).on(config.instantiatePharmacyKonnektor(pharmConfig));
    val useErpClient =
        UseTheErpClient.with(
            pharmConfig.toErpClientConfig(config.getActiveEnvironment(), ClientType.PS));
    useErpClient.authenticateWith(useKonnektor);
    // assemble the screenplay
    val thePharmacy = OnStage.theActorCalled(pharmName);
    thePharmacy.describedAs(
        "Eine 'E-Rezept-ready' Apotheke die E-Rezepte akzeptieren und dispensieren kann");
    givenThat(thePharmacy).can(useErpClient);
    givenThat(thePharmacy).can(UseSMCB.itHasAccessTo(smcb));
    givenThat(thePharmacy).can(ManagePharmacyPrescriptions.itWorksWith());
    givenThat(thePharmacy).can(ManageCommunications.itExchanges());

    givenThat(thePharmacy).can(UseSubscriptionService.use());
    givenThat(thePharmacy).can(useKonnektor);
  }

  @Angenommen("^(?:der Apotheker|die Apothekerin) (.+) hat Zugriff auf (?:seinen|ihren) HBA$")
  public void initApothecary(String pharmacistName) {
    val apothecaryConfig = config.getApothecaryConfig(pharmacistName);
    val hba =
        smartcards.getHbaByICCSN(
            apothecaryConfig.getHbaIccsn(), apothecaryConfig.getCryptoAlgorithm());

    val useKonnektor =
        UseTheKonnektor.with(hba).on(config.instantiateApothecaryKonnektor(apothecaryConfig));

    val thePharmacist = OnStage.theActorCalled(pharmacistName);
    thePharmacist.describedAs("Ein Apotheker der QES-Signaturen mittels seines HBA erstellen kann");
    givenThat(thePharmacist).can(useKonnektor);
  }

  @Angenommen("^die Apotheke (.+) verbindet sich mit seinem Apothekendienstleister$")
  public void initConnectionToPharmacyServiceProvider(String pharmName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val id = SafeAbility.getAbility(thePharmacy, UseSMCB.class);

    PSPClient pspClient = PSPClientFactory.create(config.getPspClientConfig(), id.getTelematikID());
    val pspClientAbility = UsePspClient.with(pspClient).andConfig(config.getPspClientConfig());
    givenThat(thePharmacy).attemptsTo(Ensure.that(pspClientAbility.isConnected()).isTrue());
    givenThat(thePharmacy).can(pspClientAbility);
  }

  @Wenn(
      "^die Apotheke (.+) das (letzte|erste) (?:zugewiesene|abgerufene) E-Rezept beim Fachdienst akzeptiert$")
  public void whenAcceptPrescription(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy).attemptsTo(AcceptPrescription.fromStack(order));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) zugewiesene E-Rezept beim Fachdienst akzeptieren$")
  public void thenAcceptPrescription(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy).attemptsTo(AcceptPrescription.fromStack(order));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) zugewiesene E-Rezept nicht beim Fachdienst akzeptieren$")
  @Und(
      "^die Apotheke (.+) kann das (letzte|erste) zugewiesen E-Rezept nicht beim Fachdienst akzeptieren$")
  public void thenForbiddenToAcceptPrescription(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            Negate.the(AcceptPrescription.fromStack(order))
                .with(UnexpectedResponseResourceError.class));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) zugewiesene E-Rezept nicht beim Fachdienst akzeptieren, weil es nicht mehr existiert$")
  public void thenForbiddenToAcceptPrescriptionWith410(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAcceptOperation.fromStack(order)).isEqualTo(410));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) zugewiesene E-Rezept nicht beim Fachdienst akzeptieren, weil es noch nicht gültig ist$")
  public void thenForbiddenToAcceptPrescriptionWith403(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAcceptOperation.fromStack(order)).isEqualTo(403));
  }
  /**
   * @param order
   */
  @Dann(
      "^kann die Apotheke das (letzte|erste) zugewiesene E-Rezept nicht beim Fachdienst akzeptieren, weil es nicht mehr existiert$")
  public void thenForbiddenToAcceptPrescriptionWith410(String order) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAcceptOperation.fromStack(order)).isEqualTo(410));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) zugewiesene E-Rezept nicht beim Fachdienst akzeptieren, weil es einen Konflikt gibt$")
  public void thenForbiddenToAcceptPrescriptionWith409(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAcceptOperation.fromStack(order)).isEqualTo(409));
  }

  @Dann(
      "^kann die Apotheke das (letzte|erste) zugewiesene E-Rezept nicht beim Fachdienst akzeptieren, weil es einen Konflikt gibt$")
  public void thenForbiddenToAcceptPrescriptionWith409(String order) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAcceptOperation.fromStack(order)).isEqualTo(409));
  }

  @Dann("^kann die Apotheke (.+) (?:noch)? keine E-Rezepte akzeptieren$")
  @Und("^die Apotheke (.+) kann (?:noch)? keine E-Rezepte akzeptieren$")
  public void thenCannotAcceptPrescription(String pharmName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            Negate.the(AcceptPrescription.fromStack(DequeStrategyEnum.FIFO))
                .with(MissingPreconditionError.class));
  }

  @Wenn("^die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept korrekt an (.+) dispensiert$")
  public void whenDispenseMedicationTo(String pharmName, String order, String patientName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePharmacy)
        .attemptsTo(DispenseMedication.toPatient(order, thePatient).withPrescribedMedications());
  }

  @Wenn("^die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept korrekt dispensiert$")
  public void whenDispenseMedication(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy).attemptsTo(DispenseMedication.fromStack(order).withPrescribedMedications());
  }

  @Wenn("^die Apotheke das (letzte|erste) akzeptierte E-Rezept korrekt dispensiert$")
  public void whenDispenseMedication(String order) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    when(thePharmacy).attemptsTo(DispenseMedication.fromStack(order).withPrescribedMedications());
  }

  @Dann("^kann die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept korrekt dispensieren$")
  @Und("^die Apotheke (.+) kann das (letzte|erste) akzeptierte E-Rezept korrekt dispensieren$")
  public void thenDispenseMedication(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy).attemptsTo(DispenseMedication.fromStack(order).withPrescribedMedications());
  }

  @Wenn(
      "^die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept mit den folgenden Medikamenten korrekt an (.+) dispensiert:$")
  public void whenDispenseAlternativeReplacementMedications(
      String pharmName, String order, String patientName, DataTable medications) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePharmacy)
        .attemptsTo(
            DispenseMedication.toPatient(order, thePatient)
                .withAlternativeMedications(medications));
  }

  @Wenn(
      "^die Apotheke das (letzte|erste) akzeptierte E-Rezept mit den folgenden Medikamenten korrekt an (.+) dispensiert:$")
  public void whenDispenseAlternativeReplacementMedications(
      String order, String patientName, DataTable medications) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePharmacy)
        .attemptsTo(
            DispenseMedication.toPatient(order, thePatient)
                .withAlternativeMedications(medications));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept nicht mit dem falschen Secret (.+) dispensieren$")
  @Und(
      "^die Apotheke (.+) kann das (letzte|erste) akzeptierte E-Rezept nicht mit dem falschen Secret (.+) dispensieren$")
  public void thenDispenseMedicationWithWrongSecret(
      String pharmName, String order, String wrongSecret) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            Negate.the(
                    DispenseMedication.withSecret(order, wrongSecret).withPrescribedMedications())
                .with(UnexpectedResponseResourceError.class));
  }

  @Dann(
      "^kann die Apotheke das (letzte|erste) akzeptierte E-Rezept nicht mit dem falschen Secret (.+) dispensieren$")
  @Und(
      "^die Apotheke kann das (letzte|erste) akzeptierte E-Rezept nicht mit dem falschen Secret (.+) dispensieren$")
  public void thenDispenseMedicationWithWrongSecret(String order, String wrongSecret) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    then(thePharmacy)
        .attemptsTo(
            Negate.the(
                    DispenseMedication.withSecret(order, wrongSecret).withPrescribedMedications())
                .with(UnexpectedResponseResourceError.class));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept nicht an den Versicherten mit KVNR (.+) dispensieren$")
  @Und(
      "^die Apotheke (.+) kann das (letzte|erste) akzeptierte E-Rezept nicht an den Versicherten mit KVNR (.+) dispensieren$")
  public void thenDispenseMedicationToWrongPerson(
      String pharmName, String order, String wrongKvid) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            Negate.the(DispenseMedication.toKvid(order, wrongKvid).withPrescribedMedications())
                .with(UnexpectedResponseResourceError.class));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept nicht mit dem falschen Secret (.+) löschen$")
  @Und(
      "^die Apotheke (.+) kann das (letzte|erste) akzeptierte E-Rezept nicht mit dem falschen Secret (.+) löschen$")
  public void thenDeleteMedicationWithWrongSecret(
      String pharmName, String order, String wrongSecret) {
    val thePharmacy = OnStage.theActorCalled(pharmName);

    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfAbortOperation.asPharmacy()
                        .withInvalidSecret(wrongSecret)
                        .fromStack(order))
                .isEqualTo(403));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept nicht mit einem falschen Secret löschen$")
  @Und(
      "^die Apotheke (.+) kann das (letzte|erste) akzeptierte E-Rezept nicht mit einem falschen Secret löschen$")
  public void thenDeleteMedicationWithWrongSecret(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);

    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfAbortOperation.asPharmacy().withInvalidSecret().fromStack(order))
                .isEqualTo(403));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept nicht mit dem falschen Secret (.+) zurückgeben$")
  @Und(
      "^die Apotheke (.+) kann das (letzte|erste) akzeptierte E-Rezept nicht mit dem falschen Secret (.+) zurückgeben$")
  public void thenRejectMedicationWithWrongSecret(
      String pharmName, String order, String wrongSecret) {

    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfRejectOperation.withInvalidSecret(wrongSecret).fromStack(order))
                .isEqualTo(403));
  }

  @Dann(
      "^kann die Apotheke das (letzte|erste) akzeptierte E-Rezept nicht an den Versicherten mit KVNR (.+) dispensieren$")
  @Und(
      "^die Apotheke kann das (letzte|erste) akzeptierte E-Rezept nicht an den Versicherten mit KVNR (.+) dispensieren$")
  public void thenDispenseMedicationToWrongPerson(String order, String wrongKvid) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    then(thePharmacy)
        .attemptsTo(
            Negate.the(DispenseMedication.toKvid(order, wrongKvid).withPrescribedMedications())
                .with(UnexpectedResponseResourceError.class));
  }

  @Dann("^kann die Apotheke das (letzte|erste) akzeptierte E-Rezept korrekt dispensieren$")
  @Und("^die Apotheke kann das (letzte|erste) akzeptierte E-Rezept korrekt dispensieren$")
  public void thenDispenseMedication(String order) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    then(thePharmacy).attemptsTo(DispenseMedication.fromStack(order).withPrescribedMedications());
  }

  @Dann("^kann die Apotheke (.+) noch kein E-Rezept dispensieren$")
  @Und("^die Apotheke (.+) kann(?: noch)? kein E-Rezept dispensieren$")
  public void thenCannotDispenseMedication(String pharmName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            Negate.the(
                    DispenseMedication.fromStack(DequeStrategyEnum.LIFO)
                        .withPrescribedMedications())
                .with(MissingPreconditionError.class));
  }

  @Dann("^kann die Apotheke(?: noch)? kein E-Rezept dispensieren$")
  @Und("^die Apotheke kann(?: noch)? kein E-Rezept dispensieren$")
  public void thenCannotDispenseMedication() {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    then(thePharmacy)
        .attemptsTo(
            Negate.the(
                    DispenseMedication.fromStack(DequeStrategyEnum.LIFO)
                        .withPrescribedMedications())
                .with(MissingPreconditionError.class));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept nicht dispensieren, weil es nicht mehr existiert$")
  public void thenCannotDispenseMedicationWith410(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfDispenseMedicationOperation.fromStack(order)
                        .forPrescribedMedications())
                .isEqualTo(410));
  }

  @Dann(
      "^kann die Apotheke das (letzte|erste) akzeptierte E-Rezept nicht dispensieren, weil es nicht mehr existiert$")
  public void thenCannotDispenseMedicationWith410(String order) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfDispenseMedicationOperation.fromStack(order)
                        .forPrescribedMedications())
                .isEqualTo(410));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept nicht dispensieren, weil sie nicht das Recht dazu hat$")
  public void thenCannotDispenseMedicationWith403(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfDispenseMedicationOperation.fromStack(order)
                        .forPrescribedMedications())
                .isEqualTo(403));
  }

  @Dann(
      "^kann die Apotheke das (letzte|erste) akzeptierte E-Rezept nicht dispensieren, weil sie nicht das Recht dazu hat$")
  public void thenCannotDispenseMedicationWith403(String order) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfDispenseMedicationOperation.fromStack(order)
                        .forPrescribedMedications())
                .isEqualTo(403));
  }

  @Dann("^hat die Apotheke (.+) (mindestens|maximal|genau) eine Quittung vorliegen$")
  @Und("^die Apotheke (.+) hat (mindestens|maximal|genau) eine Quittung vorliegen$")
  public void hasOneReceipt(String pharmName, String adverb) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy).attemptsTo(Ensure.that(HasReceipts.of(adverb, 1)).isTrue());
  }

  @Dann("^hat die Apotheke (mindestens|maximal|genau) eine Quittung vorliegen$")
  @Und("^die Apotheke hat (mindestens|maximal|genau) eine Quittung vorliegen$")
  public void hasOneReceipt(String adverb) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    then(thePharmacy).attemptsTo(Ensure.that(HasReceipts.of(adverb, 1)).isTrue());
  }

  @Dann("^hat die Apotheke (.+) (mindestens|maximal|genau) (\\d+) Quittung(?:en)? vorliegen$")
  @Und("^die Apotheke hat (.+) (mindestens|maximal|genau) (\\d+) Quittung(?:en)? vorliegen$")
  public void hasReceipts(String pharmName, String adverb, long amount) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy).attemptsTo(Ensure.that(HasReceipts.of(adverb, amount)).isTrue());
  }

  @Dann("^hat die Apotheke (mindestens|maximal|genau) (\\d+) Quittung(?:en)? vorliegen$")
  @Und("^die Apotheke hat (mindestens|maximal|genau) (\\d+) Quittung(?:en)? vorliegen$")
  public void hasReceipts(String adverb, long amount) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    then(thePharmacy).attemptsTo(Ensure.that(HasReceipts.of(adverb, amount)).isTrue());
  }

  @Dann("^hat die Apotheke (mindestens|maximal|genau) (\\d+) Quittung(?:en)? für (.+) vorliegen$")
  @Und("^die Apotheke hat (mindestens|maximal|genau) (\\d+) Quittung(?:en)? für (.+) vorliegen$")
  public void hasReceiptFor(String adverb, long amount, String patientName) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    val thePatient = OnStage.theActorCalled(patientName);
    val kvnr = SafeAbility.getAbility(thePatient, ProvideEGK.class).getKvnr();

    then(thePharmacy)
        .attemptsTo(Ensure.that(HasReceipts.forPatient(adverb, amount, kvnr)).isTrue());
  }

  @Dann(
      "^hat die Apotheke (.+) (mindestens|maximal|genau) (\\d+) Quittung(?:en)? für (.+) vorliegen$")
  @Und(
      "^die Apotheke hat (.+) (mindestens|maximal|genau) (\\d+) Quittung(?:en)? für (.+) vorliegen$")
  public void hasReceiptFor(String pharmName, String adverb, long amount, String patientName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);
    val kvnr = SafeAbility.getAbility(thePatient, ProvideEGK.class).getKvnr();

    then(thePharmacy)
        .attemptsTo(Ensure.that(HasReceipts.forPatient(adverb, amount, kvnr)).isTrue());
  }

  @Dann("^hat die Apotheke (.+) keine Quittung vorliegen$")
  @Und("^die Apotheke (.+) hat(?: noch)? keine Quittung vorliegen$")
  public void hasNoReceipt(String pharmName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy).attemptsTo(Ensure.that(HasReceipts.atMost(0)).isTrue());
  }

  @Dann("^hat die Apotheke(?: noch)? keine Quittung vorliegen$")
  @Und("^die Apotheke hat(?: noch)? keine Quittung vorliegen$")
  public void hasNoReceipt() {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    then(thePharmacy).attemptsTo(Ensure.that(HasReceipts.atMost(0)).isTrue());
  }

  @Wenn("^die Apotheke (.+) sich für die Subscription (.+) registriert$")
  public void whenRegisterForSubscription(String pharmName, String criteria) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy).attemptsTo(RegisterNewSubscription.forCriteria(criteria));
  }

  @Wenn("^die Apotheke (.+) sich mit dem Subscription Service verbindet$")
  public void whenConnectedToSubscriptionService(String pharmName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val pharmacyConfig = config.getPharmacyConfig(pharmName);
    when(thePharmacy)
        .attemptsTo(
            ConnectSubscriptionService.connect(
                config.getActiveEnvironment().getTi().getSubscriptionServiceUrl()));
  }

  @Dann("^wird die Apotheke (.+) durch den Subscription Service informiert$")
  public void hasSubscriptionPing(String pharmName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy).attemptsTo(Ensure.that(HasNewSubscriptionPing.hasNewPing()).isTrue());
  }

  /**
   * TMD-1619
   *
   * @param pharmName ist der Name der Apotheke
   */
  @Wenn("^die Apotheke (.+) das (letzte|erste) akzeptierte Rezept zurückweist$")
  public void whenRejectPrescription(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy).attemptsTo(RejectPrescription.fromStack(order));
  }

  @Wenn("^die Apotheke das (letzte|erste) akzeptierte Rezept zurückweist$")
  public void whenRejectPrescription(String order) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    when(thePharmacy).attemptsTo(RejectPrescription.fromStack(order));
  }

  /**
   * In diesem Schritt versucht eine Apotheke ein bereits dispensiertes E-Rezept erneut zu
   * dispensieren.
   *
   * @param pharmName ist der Name der Apotheke
   * @param order ist die Reihenfolge der Quittung eines bereits dispensierten E-Rezeptes
   */
  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) dispensierte E-Rezept nicht erneut dispensieren$")
  public void thenNotAllowedToDispenseMedication(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfReDispenseMedication.fromStack(order)).isEqualTo(403));
  }

  /**
   * TMD-1621
   *
   * @param pharmName ist der Name der Apotheke
   */
  @Wenn("^die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept löscht$")
  public void whenAbortPrescription(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy).attemptsTo(AbortPrescription.asPharmacy().fromStack(order));
  }

  @Wenn("^die Apotheke das (letzte|erste) akzeptierte E-Rezept löscht$")
  public void whenAbortPrescription(String order) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    when(thePharmacy).attemptsTo(AbortPrescription.asPharmacy().fromStack(order));
  }

  /**
   * Bevor eine Apotheke ein zugewiesenes E-Rezept nicht akzeptiert, hat diese kein zugehöriges
   * Secret für diesen Task. Ohne diesen Secret kann die Apotheke auch folglich das E-Rezept nicht
   * löschen.
   *
   * @param pharmName ist der Name der Apotheke, die versuchen soll ein zugewiesenes E-Rezept, ohne
   *     Secret zu löschen
   * @param order ist die Reihenfolge, in der das zugewiesene E-Rezept vom Accepted-Stack geholt
   *     wird
   */
  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) zugewiesene E-Rezept ohne (?:Secret|zu akzeptieren) nicht löschen$")
  public void thenPharmacyCannotAbortPrescription(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortUnaccepted.asPharmacy().fromStack(order))
                .isEqualTo(403));
  }
  /** TMD-1642 */
  @Wenn("^die Apotheke (.+) die (letzte|erste) Zuweisung per Nachricht von (.+) akzeptiert$")
  public void whenAcceptDispenseRequest(String pharmName, String order, String patientName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePharmacy).attemptsTo(AcceptDispenseRequest.of(order).from(thePatient));
  }

  /**
   * TMD-1645
   *
   * @param pharmName
   * @param patientName
   */
  @Wenn("^die Apotheke (.+) die (letzte|erste) Nachricht von (.+) beantwortet$")
  public void whenPharmacyAnswersToMessage(String pharmName, String order, String patientName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePharmacy)
        .attemptsTo(
            SendCommunication.with(
                ResponseOfPostCommunication.reply()
                    .forCommunicationRequestFromBackend(order) // letzte | erste
                    .receivedFrom(thePatient)
                    .withRandomMessage()));
  }

  @Wenn("^die Apotheke die (letzte|erste) Nachricht von (.+) beantwortet$")
  public void whenPharmacyAnswersToMessage(String order, String patientName) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePharmacy)
        .attemptsTo(
            SendCommunication.with(
                ResponseOfPostCommunication.reply()
                    .forCommunicationRequestFromBackend(order) // letzte | erste
                    .receivedFrom(thePatient)
                    .withRandomMessage()));
  }

  @Dann(
      "^kann (.+) die (letzte|erste) Nachricht von (.+) nicht beantworten, weil sie keine Apotheke ist$")
  public void whenPharmacyCannotAnswersToMessage(
      String pharmName, String order, String patientName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePharmacy)
        .attemptsTo(
            Negate.the(
                    SendCommunication.with(
                        ResponseOfPostCommunication.reply()
                            .forCommunicationRequestFromBackend(order) // letzte | erste
                            .receivedFrom(thePatient)
                            .withRandomMessage()))
                .with(UnexpectedResponseResourceError.class));
  }
  /**
   * TMD-1673
   *
   * @param pharmName ist der Name der Apotheke, die ihre Quittung validieren soll
   */
  @Dann(
      "^kann die Apotheke (.+) die Signatur der (letzten|ersten) Quittung erfolgreich mit dem Konnektor validieren$")
  public void thenPharmacySuccessfullyValidatesSignatureofReceipt(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy).attemptsTo(Ensure.that(VerifyReceiptSignature.fromStack(order)).isTrue());
  }

  /**
   * In diesem Step wird für das letzte dispensierte Rezept des Versicherten ein PKV-Abgabedatensatz
   * erstellt, mit der SMC-B der Apotheke signiert und per POST/chargeItem beim Fachdienst
   * hinterlegt.
   *
   * @param pharmName Name der Apotheke
   * @param patientName Name des Versicherten
   */
  @Wenn(
      "^die Apotheke (.+) für das (letzte|erste) dispensierte E-Rezept einen PKV-Abrechnungsdatensatz mit der SMC-B signiert und beim Fachdienst hinterlegt$")
  public void whenPharmacySignsWithSmcbAndPostsChargeItem(String pharmName, String patientName) {
    throw new PendingStepException("Not yet implemented");
  }

  @Dann(
      "^kann die Apotheke (.+) für das (letzte|erste) dispensierte E-Rezept keinen PKV-Abrechnungsdatensatz beim Fachdienst hinterlegen, weil keine Einwilligung vorliegt$")
  public void thenCannotPostChargeItem403(String pharmName, String order) {
    throw new PendingStepException("Not yet implemented");
    /*   val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
            .attemptsTo(
                    CheckTheReturnCode.of(
                            ResponseOfChargeItemOperationfromStack(order)
                                    .forPrescribedMedications())
                            .isEqualTo(403)); */
  }

  @Dann(
      "^kann die Apotheke (.+) die Abrechnungsinformationen für das letzte dispensierte E-Rezept ändern$")
  public void thenChargeItemCanBeChanged(String pharmName) {
    throw new PendingStepException("Not yet implemented");
  }

  @Wenn(
      "^die Apotheke (.+) die (letzte|erste) Nachricht (?:der|des) Versicherten (.+) mit dem Änderungswunsch empfängt und beantwortet$")
  public void whenReceiveAndAnswerChargChangeReq(
      String order, String pharmName, String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmName);

    when(thePharmacy)
        .attemptsTo(
            SendCommunication.with(
                ResponseOfPostCommunication.changeReply()
                    .forChargeItemFromBackend(order) // letzte | erste
                    .receivedFrom(thePatient)
                    .withRandomMessage()));
  }

  @Dann(
      "^kann die Apotheke (.+) die Abrechnungsinformationen für das letzte dispensierte E-Rezept nicht ändern, weil sie kein Recht dazu hat$")
  public void thenCannotChangeCargeItem403(String pharmName) {
    throw new PendingStepException("Not yet implemented");
    /*   val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
            .attemptsTo(
                    CheckTheReturnCode.of(
                            ResponseOfChargeItemOperationfromStack(order)
                                    .forPrescribedMedications())
                            .isEqualTo(403)); */
  }

  @Wenn(
      "^(?:der Apotheker|die Apothekerin) (.+) in der Apotheke (.+) für das letzte dispensierte E-Rezept einen PKV-Abrechnungsdatensatz mit dem HBA signiert und beim Fachdienst hinterlegt$")
  public void whenPharmacySignsWithHbaAndPostsChargeItem(String pharmName, String patientName) {
    throw new PendingStepException("Not yet implemented");
  }

  @Dann(
      "^kann die Apotheke (.+) für das (letzte|erste) dispensierte E-Rezept keinen PKV-Abrechnungsdatensatz beim Fachdienst hinterlegen, weil es kein PKV-Rezept ist$")
  @Dann(
      "^kann die Apotheke (.+) für das (letzte|erste) dispensierte E-Rezept keinen PKV-Abrechnungsdatensatz beim Fachdienst hinterlegen, weil der Task nicht mehr existiert$")
  public void thenCannotPostChargeItem400(String pharmName) {
    throw new PendingStepException("Not yet implemented");
    /*   val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
            .attemptsTo(
                    CheckTheReturnCode.of(
                            ResponseOfChargeItemOperationfromStack(order)
                                    .forPrescribedMedications())
                            .isEqualTo(400)); */
  }

  @Dann(
      "^kann die Apotheke (.+) für das (letzte|erste) dispensierte E-Rezept die Abrechnungsinformationen vom Fachdienst abrufen$")
  public void whenPharmacygetsChargeItem(String pharmName, String order) {
    throw new PendingStepException("Not yet implemented");
  }

  /**
   * @param pharmName
   */
  @Wenn(
      "^die Apotheke (.+) eine Nachricht mit einer alternativen Zuweisung vom Dienstleister empfängt und entschlüsselt$")
  public void whenPharmacyGetsMessageFromServiceProvider(String pharmName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy).attemptsTo(DecryptPSPMessage.receivedFromPharmacyService());
  }

  /**
   * versendet HTTP-GET-Operation auf einen einzelnen Task mittels "/Task/<id>?secret=..." FD gibt
   * Task + Quittungs-Bundle an den Apotheker zurück
   *
   * @param pharmName
   */
  @Dann("^kann die Apotheke (.+) die (letzte|erste) Quittung erneut abrufen$")
  public void thenPharmacyCanRetrieveReceiptAgain(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy).attemptsTo(RetrieveReceiptAgain.fromStack(order));
  }

  @Dann(
      "^kann die Apotheke (.+) für das letzte dispensierte E-Rezept keinen PKV-Abrechnungsdatensatz mit dem falschen Secret (.+) hinterlegen$")
  public void thenCannotPostChargeitemWrongSecret(String pharmName, String wrongSecret) {
    throw new PendingStepException("Not yet implemented");
  }

  @Dann(
      "^darf die Apotheke (.+) die Dispensierinformationen für das (erste|letzte) dispensierte E-Rezept nicht abrufen$")
  public void thenPharmacyCannotGetMedicationDispense(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            ThatNotAllowedToAsk.the(GetMedicationDispense.asPharmacy().forPrescription(order))
                .with(UnexpectedResponseResourceError.class));
  }

  @Dann(
      "^kann die Apotheke (.+) die (letzte|erste) Nachricht von (.+) nicht abrufen, weil die Nachricht bereits gelöscht wurde$")
  public void thenCannotGetCommunicationFrom(String pharmName, String order, String patientName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);

    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfGetCommunicationFrom.sender(thePatient).onStack(order))
                .isEqualTo(404));
  }

  @Und("^die Apotheke (.+) ihre (letzte|erste) versendete Nachricht löscht$")
  public void whenPharmacyDeletesCommunication(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy).attemptsTo(DeleteSentCommunication.fromStack(order));
  }

  @Wenn("^die Apotheke (.+) die E-Rezepte mit der eGK von (.+) abruft$")
  public void whenPharmacyRequestPrescriptionsWithEgk(String pharmName, String patientName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);
    val egk = SafeAbility.getAbility(thePatient, ProvideEGK.class).getEgk();
    val examEvidence = thePharmacy.asksFor(RetrieveExamEvidence.with(egk)).orElseThrow();
    val question =
        HasDownloadableOpenTask.builder().kvnr(egk.getKvnr()).examEvidence(examEvidence).build();

    when(thePharmacy).attemptsTo(Ensure.that(question).isTrue());
  }

  @Wenn(
      "^die Apotheke (.+) für die eGK von (.+) (?:einen alten|keinen) Prüfungsnachweis (?:verwendet|abruft)$")
  @Wenn("^die Krankenhaus-Apotheke (.+) die E-Rezepte mit der eGK von (.+) abruft$")
  public void whenPharmacyUseExpiredEvidence(String pharmName, String patientName) {
    // dummy step to increase comprehensibility in the test scenario
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
      "^kann die Apotheke (.+) die E-Rezepte von (.+) nicht abrufen, weil der Prüfungsnachweis zeitlich ungültig ist$")
  public void thenPharmacyCanNotRequestPrescriptionsWithExpiredEvidence(
      String pharmName, String patientName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);
    val egk = SafeAbility.getAbility(thePatient, ProvideEGK.class).getEgk();
    val examEvidence = ExamEvidence.NO_UPDATE_WITH_EXPIRED_TIMESTAMP.encodeAsBase64();
    when(thePharmacy)
        .attemptsTo(
            Ensure.that(
                    HasDownloadableOpenTask.builder()
                        .kvnr(egk.getKvnr())
                        .examEvidence(examEvidence)
                        .build())
                .isFalse());
  }

  @Dann(
      "^kann die Apotheke (.+) die E-Rezepte von (.+) nicht abrufen, weil der Prüfungsnachweis nicht abgerufen wurde$")
  public void thenPharmacyCanNotRequestPrescriptionsWithoutEvidence(
      String pharmName, String patientName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);
    val egk = SafeAbility.getAbility(thePatient, ProvideEGK.class).getEgk();

    when(thePharmacy)
        .attemptsTo(
            Ensure.that(HasDownloadableOpenTask.builder().kvnr(egk.getKvnr()).build()).isFalse());
  }

  @Dann(
      "^kann die Apotheke (.+) die E-Rezepte von (.+) nicht abrufen, weil Krankenhaus-Apotheken nicht berechtigt sind$")
  public void thenHospitalPharmacyCanNotRequestPrescriptions(String pharmName, String patientName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);
    val egk = SafeAbility.getAbility(thePatient, ProvideEGK.class).getEgk();

    val examEvidence = ExamEvidence.NO_UPDATES.encodeAsBase64();
    when(thePharmacy)
        .attemptsTo(
            Ensure.that(
                    HasDownloadableOpenTask.builder()
                        .kvnr(egk.getKvnr())
                        .examEvidence(examEvidence)
                        .build())
                .isFalse());
  }
}
