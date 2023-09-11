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

package de.gematik.test.erezept.app.task.ios;

import de.gematik.test.erezept.app.abilities.*;
import de.gematik.test.erezept.app.mobile.*;
import de.gematik.test.erezept.app.mobile.elements.*;
import de.gematik.test.erezept.app.task.ChangeTheEnvironment;
import de.gematik.test.erezept.app.task.NavigateThroughCardwall;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.fhir.valuesets.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.util.*;
import de.gematik.test.smartcard.SmartcardArchive;
import lombok.*;
import lombok.extern.slf4j.*;
import net.serenitybdd.screenplay.*;

@Slf4j
@RequiredArgsConstructor
public class SetUpIosDevice implements Task {

  private final EnvironmentConfiguration environment;
  private final VersicherungsArtDeBasis insuranceKind;
  private final SmartcardArchive sca;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);
    val userConfig = SafeAbility.getAbility(actor, UseAppUserConfiguration.class);
    val password = SafeAbility.getAbility(actor, HandleAppAuthentication.class).getPassword();

    // walk through onboarding
    app.tap(Onboarding.NEXT_BUTTON);
    app.tap(Onboarding.CHECK_PRIVACY_AND_TOU_BUTTON);
    app.tap(Onboarding.ACCEPT_PRIVACY_AND_TOU_BUTTON);
    app.inputPassword(password, Onboarding.PASSWORD_INPUT_FIELD);
    app.swipe(SwipeDirection.UP);
    app.inputPassword(password, Onboarding.PASSWORD_CONFIRMATION_FIELD);
    app.tap(Onboarding.ACCEPT_PASSWORD_BUTTON);

    app.tap(Onboarding.CONTINUE_ANALYTICS_SCREEN_BUTTON);
    app.tap(Onboarding.NOT_ACCEPT_ANALYTICS_BUTTON);

    app.tapIfDisplayed(Onboarding.HIDE_SUGGESTION_PIN_SELECTION_BUTTON);
    app.tapIfDisplayed(Onboarding.ACCEPT_SUGGESTION_PIN_SELECTION_BUTTON);

    actor.attemptsTo(
        ChangeTheEnvironment.bySwitchInTheDebugMenuTo(
            Environment.fromString(environment.getName())));
    
    if (userConfig.useVirtualEgk()) {
      val egk = sca.getEgkByICCSN(userConfig.getEgkIccsn());
      actor.attemptsTo(SetVirtualEgkOnIOS.withEgk(egk));
    }

    actor.attemptsTo(NavigateThroughCardwall.byMappingVirtualEgkFrom(sca, insuranceKind));
    
    // now get the concrete eGK wich was chosen from the cardwall
    val egk = actor.abilityTo(ProvideEGK.class).getEgk();
    val erpClient = ErpClientFactory.createErpClient(environment, userConfig.getConfiguration());
    val useErpClient = UseTheErpClient.with(erpClient);
    useErpClient.authenticateWith(egk);
    actor.can(useErpClient);
  }
}
