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

package de.gematik.test.erezept.app.task.android;

import de.gematik.test.erezept.app.abilities.*;
import de.gematik.test.erezept.app.mobile.*;
import de.gematik.test.erezept.app.mobile.elements.*;
import de.gematik.test.erezept.exceptions.*;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.valuesets.*;
import de.gematik.test.erezept.jwt.*;
import de.gematik.test.erezept.operator.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.util.*;
import de.gematik.test.smartcard.Egk;
import de.gematik.test.smartcard.SmartcardArchive;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import net.serenitybdd.screenplay.*;

@Slf4j
@RequiredArgsConstructor
public class SetUpAndroidDevice implements Task {

  private final VersicherungsArtDeBasis insuranceKind;
  private final SmartcardArchive sca;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseTheApp.class);
    val userConfig = SafeAbility.getAbility(actor, UseAppUserConfiguration.class);
    val password = SafeAbility.getAbility(actor, HandleAppAuthentication.class).getPassword();

    // walk through onboarding
    app.swipe(SwipeDirection.LEFT);
    app.swipe(SwipeDirection.LEFT);
    app.input(actor.getName(), Onboarding.USER_PROFILE_FIELD);
    app.tap(Onboarding.NEXT_BUTTON);
    app.inputPassword(password, Onboarding.PASSWORD_INPUT_FIELD);
    app.inputPassword(password, Onboarding.PASSWORD_CONFIRMATION_FIELD);
    app.tap(4, Onboarding.NEXT_BUTTON);
    app.tap(Onboarding.ACCEPT_PRIVACY_BUTTON);
    app.tap(Onboarding.ACCEPT_TERMS_OF_USE_BUTTON);
    app.tap(Onboarding.CONFIRM_TERMS_AND_PRIVACY_SELECTION_BUTTON);

    if (userConfig.useVirtualEgk()) {
      val egk = sca.getEgkByICCSN(userConfig.getEgkIccsn());
      performWithVirtualeGK(actor, app, egk);
    } else {
      performWithRealeGK(app);
      getBaseDataFromDebugMenu(actor, app);
    }
  }

  private void performWithRealeGK(UseTheApp<?> app) {
    // walk through cardwall
    app.tap(Mainscreen.REFRESH_BUTTON);
    app.tap(CardWall.NEXT_BUTTON);
    app.input("123123", CardWall.CAN_INPUT_FIELD); // TODO: find a way to configure
    app.tap(CardWall.NEXT_BUTTON);
    app.input("123456", CardWall.PIN_INPUT_FIELD); // TODO: find a way to configure
    //    app.tap(CardWall.NEXT); // seems to be not required??

    app.tap(CardWall.NEXT_BUTTON);
    UIProvider.getInstructionResult(
        "Get your eGK ready and follow the Instructions on the Screen of your device");
  }

  @SneakyThrows
  private <T extends Actor> void performWithVirtualeGK(T actor, UseTheApp<?> app, Egk egk) {
    app.tap(BottomNav.SETTINGS_BUTTON);
    app.tap(Debug.MENU_BUTTON);

    val provideEgk = ProvideEGK.sheOwns(egk);
    actor.can(provideEgk);
    actor.can(
        ProvidePatientBaseData.forPatient(
            provideEgk.getKvnr(),
            egk.getOwner().getGivenName(),
            egk.getOwner().getSurname(),
            insuranceKind));

    val autCertificate = egk.getAutCertificate();
    val pkBase64 = Base64.getEncoder().encodeToString(autCertificate.getPrivateKey().getEncoded());
    val cchBase64 =
        Base64.getEncoder().encodeToString(autCertificate.getX509Certificate().getEncoded());
    app.swipe(SwipeDirection.UP);
    app.swipe(SwipeDirection.UP); // one swipe is not enough to reach the bottom of the screen
    app.input(pkBase64, Debug.EGK_PRIVATE_KEY);
    app.input(cchBase64, Debug.EGK_CERTIFICATE_CHAIN);
    app.tap(Debug.SET_VIRTUAL_EGK_FIELD);

    app.tap(Debug.LEAVE_BUTTON);
    app.tap(Settings.LEAVE_BUTTON);
  }

  private <T extends Actor> void getBaseDataFromDebugMenu(T actor, UseTheApp<?> app) {
    // visit the settings screen: and fetch KVNR and name from Bearer Token
    app.tap(BottomNav.SETTINGS_BUTTON);
    app.tap(Debug.MENU_BUTTON);
    val rawToken = app.getText(Debug.BEARER_TOKEN);
    val token = JWTDecoder.withCompactWriter().decode(rawToken);
    val kvnrValue = token.getPayload().getIdentifier();
    val givenName = token.getPayload().getGivenName();
    val familyName = token.getPayload().getFamilyName();
    actor.can(
        ProvidePatientBaseData.forPatient(
            KVNR.from(kvnrValue), givenName, familyName, insuranceKind));

    app.tap(Debug.LEAVE_BUTTON);
    app.tap(Settings.LEAVE_BUTTON);
  }

  /**
   * Rather than using the debug-menu and fetching the information from the SSO Token, we might
   * simply ask the human operator to provide this data
   *
   * @param actor
   * @param <T>
   */
  private <T extends Actor> void getBaseDataFromUser(T actor) {
    val kvnrValue = UIProvider.getQuestionResult("What is the KVNR of the eGK you have used?");
    val name = UIProvider.getQuestionResult("What is the Name on the eGK?");

    if (kvnrValue == null || name == null) {
      throw new TestcaseAbortedException("Given KVNR or Name is null");
    }

    actor.can(ProvidePatientBaseData.forPatient(KVNR.from(kvnrValue), name, insuranceKind));
  }
}
