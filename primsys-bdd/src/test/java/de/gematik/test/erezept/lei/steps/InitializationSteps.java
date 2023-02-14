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

import static java.text.MessageFormat.*;
import static net.serenitybdd.screenplay.GivenWhenThen.*;

import de.gematik.test.erezept.client.*;
import de.gematik.test.erezept.lei.cfg.*;
import de.gematik.test.erezept.pspwsclient.*;
import de.gematik.test.erezept.pspwsclient.config.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.task.*;
import de.gematik.test.erezept.screenplay.util.*;
import de.gematik.test.smartcard.*;
import io.cucumber.java.*;
import io.cucumber.java.de.*;
import lombok.*;
import lombok.extern.slf4j.*;
import net.serenitybdd.screenplay.actors.*;
import net.serenitybdd.screenplay.ensure.*;

@Slf4j
public class InitializationSteps {

  private SmartcardArchive smartcards;
  private TestsuiteConfiguration config;

  @Before
  public void setUp() {
    smartcards = SmartcardFactory.getArchive();
    config = TestsuiteConfiguration.getInstance();
    OnStage.setTheStage(Cast.ofStandardActors());
  }

  @After
  public void teardown() {
    OnStage.drawTheCurtain();
  }

  /**
   * Initialisiere einen Apotheker mit einer SMC-B <br>
   * <b>Notiz:</b> der Name im ersten Parameter entspricht nicht zwangsläufig dem echten Namen, der
   * auf der Smartcard hinterlegt ist. Dieser Name wird lediglich für das Screenplay-Pattern
   * benötigt
   *
   * @param pharmName der Name der Apotheke, der innerhalb des Szenarios verwendet wird, um diesen
   *     zu identifizieren
   */
  @Angenommen("^die Apotheke (.+) hat Zugriff auf ihre SMC-B$")
  public void initPharmacy(String pharmName) {
    // fetch the chosen Smartcards
    val pharmConfig = config.getPharmacyConfig(pharmName);
    val smcb = smartcards.getSmcbByICCSN(pharmConfig.getSmcbIccsn());

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
    val hba = smartcards.getHbaByICCSN(apothecaryConfig.getHbaIccsn());

    val useKonnektor =
        UseTheKonnektor.with(hba).on(config.instantiateApothecaryKonnektor(apothecaryConfig));

    val thePharmacist = OnStage.theActorCalled(pharmacistName);
    thePharmacist.describedAs("Ein Apotheker der QES-Signaturen mittels seines HBA erstellen kann");
    givenThat(thePharmacist).can(useKonnektor);
  }

  /**
   * Initialisiere einen Arzt mit den beiden Smartcards SMC-B und HBA
   *
   * <p><b>Notiz:</b> Der Name im ersten Parameter entspricht nicht dem echten Namen der auf HBA
   * hinterlegt ist. Dieser Name wird lediglich für das Screenplay-Pattern benötigt
   *
   * @param docName der Name des Arztes, der innerhalb des Szenarios verwendet wird, um diesen zu
   *     identifizieren
   */
  @Angenommen(
      "^(?:der Arzt|die Ärztin) (.+) hat Zugriff auf (?:seinen|ihren) HBA und auf die SMC-B der Praxis$")
  public void initDoctor(String docName) {
    log.trace(format("Initialize Doctor {0}", docName));
    // fetch the chosen Smartcards
    val docConfig = config.getDoctorConfig(docName);
    val smcb = smartcards.getSmcbByICCSN(docConfig.getSmcbIccsn());
    val hba = smartcards.getHbaByICCSN(docConfig.getHbaIccsn());

    // create the abilities
    val useKonnektor =
        UseTheKonnektor.with(smcb).and(hba).on(config.instantiateDoctorKonnektor(docConfig));
    val useErpClient =
        UseTheErpClient.with(
            docConfig.toErpClientConfig(config.getActiveEnvironment(), ClientType.PS));
    useErpClient.authenticateWith(useKonnektor);

    val provideBaseData = ProvideDoctorBaseData.fromConfiguration(docConfig);
    val manageIssuedPrescriptions = ManageDoctorsPrescriptions.sheIssued();

    // assemble the screenplay
    val theDoctor = OnStage.theActorCalled(docName);
    theDoctor.describedAs("Eine 'E-Rezept-ready' Arzt der E-Rezepte verschreiben kann");
    givenThat(theDoctor).can(useErpClient);
    givenThat(theDoctor).can(useKonnektor);
    givenThat(theDoctor).can(provideBaseData);
    givenThat(theDoctor).can(manageIssuedPrescriptions);
    log.trace(format("Doctor {0} initialized with SMC-B {1}", docName, smcb.getIccsn()));
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
    val egk = smartcards.getEgkByICCSN(patientConfig.getEgkIccsn());

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

    if (provideBaseData.isPKV()) {
      givenThat(thePatient).can(ManageChargeItems.sheReceives());
    }

    givenThat(thePatient).can(ProvideEGK.sheOwns(egk));
    givenThat(thePatient).can(ManageDataMatrixCodes.sheGetsPrescribed());
    givenThat(thePatient).can(receiveDrugs);
    givenThat(thePatient).can(useErpClient);
    givenThat(thePatient).can(provideBaseData);
    givenThat(thePatient).can(ManagePatientPrescriptions.itWorksWith());
    givenThat(thePatient).can(ManageCommunications.sheExchanges());
    givenThat(thePatient).can(DecideUserBehaviour.fromConfiguration(config));
    useErpClient.authenticateWith(egk);
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

  @Wenn("^die Apotheke (.+) sich mit dem Subscription Service verbindet$")
  public void whenConnectedToSubscriptionService(String pharmName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy)
        .attemptsTo(
            ConnectSubscriptionService.connect(
                config.getActiveEnvironment().getTi().getSubscriptionServiceUrl()));
  }
}
