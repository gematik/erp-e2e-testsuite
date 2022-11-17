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

package de.gematik.test.erezept.actors;

import de.gematik.test.erezept.*;
import de.gematik.test.erezept.client.*;
import de.gematik.test.erezept.client.cfg.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import lombok.*;
import lombok.extern.slf4j.*;
import net.serenitybdd.screenplay.*;

@Slf4j
public class ActorDecorator {

  private ActorDecorator() {
    throw new AssertionError("do not instantiate!");
  }

  public static <A extends Actor> void decorateDoctorActor(A actor, ErpConfiguration config) {
    log.info("Decorate Doctor: " + actor.getName());
    actor.describedAs("Ein/e 'E-Rezept-ready' Arzt/Ärztin der/die E-Rezepte verschreiben kann");

    val docConfig = config.getDoctorConfig(actor.getName());
    val smartcards = config.getSmartcards();
    val smcb = smartcards.getSmcbByICCSN(docConfig.getSmcbIccsn(), docConfig.getCryptoAlgorithm());
    val hba = smartcards.getHbaByICCSN(docConfig.getHbaIccsn(), docConfig.getCryptoAlgorithm());

    val useSmcb = UseSMCB.itHasAccessTo(smcb);
    val useKonnektor =
        UseTheKonnektor.with(smcb).and(hba).on(config.instantiateDoctorKonnektor(docConfig));
    val erpClientConfig = docConfig.toErpClientConfig(config.getActiveEnvironment(), ClientType.PS);
    val useErpClient = decorateWithErpClient(actor, erpClientConfig);
    useErpClient.authenticateWith(useKonnektor);

    val provideBaseData = ProvideDoctorBaseData.fromConfiguration(docConfig);

    actor.can(useKonnektor);
    actor.can(useSmcb);
    actor.can(provideBaseData);
  }

  public static <A extends Actor> void decoratePharmacyActor(A actor, ErpConfiguration config) {
    log.info("Decorate Pharmacy: " + actor.getName());
    actor.describedAs(
        "Eine 'E-Rezept-ready' Apotheke die E-Rezepte akzeptieren und dispensieren kann");

    val pharmacyConfig = config.getPharmacyConfig(actor.getName());
    val smartcards = config.getSmartcards();
    val smcb =
        smartcards.getSmcbByICCSN(
            pharmacyConfig.getSmcbIccsn(), pharmacyConfig.getCryptoAlgorithm());

    val useSmcb = UseSMCB.itHasAccessTo(smcb);
    val useKonnektor =
        UseTheKonnektor.with(smcb).on(config.instantiatePharmacyKonnektor(pharmacyConfig));
    val erpClientConfig =
        pharmacyConfig.toErpClientConfig(config.getActiveEnvironment(), ClientType.PS);
    val useErpClient = decorateWithErpClient(actor, erpClientConfig);
    useErpClient.authenticateWith(useKonnektor);

    actor.can(useKonnektor);
    actor.can(useSmcb);
  }

  public static <A extends Actor> void decoratePatientActor(A actor, ErpConfiguration config) {
    log.info("Decorate Patient: " + actor.getName());
    actor.describedAs("Ein/e 'E-Rezept-ready' Versicherte/r der E-Rezepte erhalten kann");

    val patientConfig = config.getPatientConfig(actor.getName());
    val smartcards = config.getSmartcards();
    val egk =
        smartcards.getEgkByICCSN(patientConfig.getEgkIccsn(), patientConfig.getCryptoAlgorithm());

    val erpClientConfig =
        patientConfig.toErpClientConfig(config.getActiveEnvironment(), ClientType.FDV);
    val useTheErpClient = decorateWithErpClient(actor, erpClientConfig);
    useTheErpClient.authenticateWith(egk);
    actor.can(ProvidePatientBaseData.forGkvPatient(egk.getKvnr(), actor.getName()));
  }

  private static <A extends Actor> UseTheErpClient decorateWithErpClient(
      A actor, ErpClientConfiguration config) {
    val useTheErpClient = UseTheErpClient.with(config);
    actor.can(useTheErpClient);
    return useTheErpClient;
  }
}