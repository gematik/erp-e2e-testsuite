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
package de.gematik.test.erezept.app.task;

import de.gematik.test.erezept.app.abilities.UseAppUserConfiguration;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.elements.*;
import de.gematik.test.erezept.app.questions.UsedSessionKVNR;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.smartcard.SmartcardArchive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor
public class NavigateThroughCardwall implements Task {

  private final EnvironmentConfiguration environment;
  private final SmartcardArchive sca;
  private final VersicherungsArtDeBasis insuranceKind;

  @Override
  @Step("{0} meldet sich mit seiner/ihrer eGK von der #insuranceKind in der E-Rezept App an")
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseTheApp.class);
    val userConfig = SafeAbility.getAbility(actor, UseAppUserConfiguration.class);

    /*
    here the bottom drawer pops up sporadically suggesting to log in:
    1. tap two times on prescriptions to ensure the bottom drawer pops up
    2. ensure the bottom drawer is declined by probing twice
     */
    app.tap(2, BottomNav.PRESCRIPTION_BUTTON);
    app.tapIfDisplayed(2, Utility.DECLINE_LOGIN);

    app.tap(Mainscreen.LOGIN_BUTTON);

    app.tap(CardWall.ADD_HEALTH_CARD_BUTTON);
    app.input("123123", CardWall.CAN_INPUT_FIELD);
    app.tap(CardWall.CAN_ACCEPT_BUTTON);
    app.inputPassword("123456", CardWall.PIN_INPUT_FIELD);
    app.tap(CardWall.PIN_ACCEPT_BUTTON);
    app.tap(CardWall.DONT_SAVE_CREDENTIAL_BUTTON);
    app.tap(CardWall.CONTINUE_AFTER_BIOMETRY_CHECK_BUTTON);

    app.tap(CardWall.START_NFC_READOUT_BUTTON);
    app.waitUntilElementIsVisible(BottomNav.SETTINGS_BUTTON); // wait until the pairing finished

    val kvnr = actor.asksFor(UsedSessionKVNR.fromUserProfile());
    val egk = sca.getEgkByKvnr(kvnr);
    val provideEgk = ProvideEGK.sheOwns(egk);

    actor.can(provideEgk);
    actor.can(
        ProvidePatientBaseData.forPatient(
            provideEgk.getKvnr(),
            egk.getOwner().getGivenName(),
            egk.getOwner().getSurname(),
            insuranceKind));

    // now get the concrete eGK wich was chosen from the cardwall
    val erpClient = ErpClientFactory.createErpClient(environment, userConfig.getConfiguration());
    val useErpClient = UseTheErpClient.with(erpClient);
    useErpClient.authenticateWith(egk);
    actor.can(useErpClient);

    app.tap(BottomNav.PRESCRIPTION_BUTTON);
  }

  public static Builder forEnvironment(EnvironmentConfiguration environment) {
    return new Builder(environment);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final EnvironmentConfiguration environment;

    public NavigateThroughCardwall byMappingVirtualEgkFrom(SmartcardArchive sca) {
      return byMappingVirtualEgkFrom(sca, VersicherungsArtDeBasis.GKV);
    }

    public NavigateThroughCardwall byMappingVirtualEgkFrom(
            SmartcardArchive sca, VersicherungsArtDeBasis insuranceKind) {
      return Instrumented.instanceOf(NavigateThroughCardwall.class).withProperties(environment, sca, insuranceKind);
    }
  }
}
