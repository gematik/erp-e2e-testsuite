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

package de.gematik.test.erezept.app.task;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.app.abilities.UseConfigurationData;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.elements.BottomNav;
import de.gematik.test.erezept.app.questions.UsedSessionKVNR;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@RequiredArgsConstructor
public class EquipWithProvidePatientBaseDataAndErpClient implements Task {
  private final EnvironmentConfiguration environment;
  private final InsuranceTypeDe insuranceKind;
  private final SmartcardArchive sca;

  public static EquipWithProvidePatientBaseDataAndErpClient forInput(
      EnvironmentConfiguration environment, InsuranceTypeDe insuranceKind, SmartcardArchive sca) {
    return new EquipWithProvidePatientBaseDataAndErpClient(environment, insuranceKind, sca);
  }

  @Override
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);
    val userConfig = SafeAbility.getAbility(actor, UseConfigurationData.class);

    val kvnr = actor.asksFor(UsedSessionKVNR.fromUserProfile());
    val egk = sca.getEgkByKvnr(kvnr);
    val provideEgk = ProvideEGK.sheOwns(egk);

    actor.can(provideEgk);
    actor.can(
        ProvidePatientBaseData.forPatient(
            provideEgk.getKvnr(), egk.getOwnerData().getCommonName(), insuranceKind));

    // now get the concrete eGK wich was chosen from the card wall
    val erpClient = ErpClientFactory.createErpClient(environment, userConfig.getUserConfig());
    val useErpClient = UseTheErpClient.with(erpClient);
    useErpClient.authenticateWith(egk);
    actor.can(useErpClient);

    app.tap(BottomNav.PRESCRIPTION_BUTTON);
  }
}
