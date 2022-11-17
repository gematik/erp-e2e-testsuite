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

package de.gematik.test.erezept.app.task.android;

import de.gematik.test.erezept.app.abilities.HandleAppAuthentication;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.elements.CardWall;
import de.gematik.test.erezept.app.mobile.elements.Debug;
import de.gematik.test.erezept.app.mobile.elements.Onboarding;
import de.gematik.test.erezept.app.mobile.elements.Prescriptions;
import de.gematik.test.erezept.app.mobile.elements.Settings;
import de.gematik.test.erezept.exceptions.TestcaseAbortedException;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.jwt.JWTDecoder;
import de.gematik.test.erezept.operator.UIProvider;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@RequiredArgsConstructor
public class SetUpAndroidDevice implements Task {

  private final VersicherungsArtDeBasis insuranceKind;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseTheApp.class);
    val password = SafeAbility.getAbility(actor, HandleAppAuthentication.class).getPassword();

    // walk through onboarding
    app.swipe(SwipeDirection.LEFT);
    app.swipe(SwipeDirection.LEFT);
    app.input(actor.getName(), Onboarding.USER_PROFILE);
    app.tap(Onboarding.NEXT);
    app.inputPassword(password, Onboarding.PASSWORD_INPUT);
    app.inputPassword(password, Onboarding.PASSWORD_CONFIRMATION);
    app.tap(4, Onboarding.NEXT);
    app.tap(Onboarding.ACCEPT_PRIVACY);
    app.tap(Onboarding.ACCEPT_TERMS_OF_USE);
    app.tap(Onboarding.CONFIRM_LEGAL);

    if (app.useVirtualeGK()) {
      performWithVirtualeGK(actor, app);
      getBaseDataFromDebugMenu(actor, app);
    } else {
      performWithRealeGK(app);
      getBaseDataFromUser(actor);
    }
  }

  private void performWithRealeGK(UseTheApp<?> app) {
    // walk through cardwall
    app.tap(Prescriptions.REFRESH);
    app.tap(CardWall.NEXT);
    app.input("123123", CardWall.CAN_INPUT); // TODO: find a way to configure
    app.tap(CardWall.NEXT);
    app.input("123456", CardWall.PIN_INPUT); // TODO: find a way to configure
    //    app.tap(CardWall.NEXT); // seems to be not required??

    app.tap(CardWall.SIGN_IN);
    UIProvider.getInstructionResult(
        "Get your eGK ready and follow the Instructions on the Screen of your device");
  }

  @SneakyThrows
  private <T extends Actor> void performWithVirtualeGK(T actor, UseTheApp<?> app) {
    app.tap(Settings.SHOW);
    app.tap(Debug.SHOW);

    val egk = SafeAbility.getAbility(actor, ProvideEGK.class).getEgk();
    val pkBase64 = Base64.getEncoder().encodeToString(egk.getAuthPrivateKey().getEncoded());
    val cchBase64 = Base64.getEncoder().encodeToString(egk.getAuthCertificate().getEncoded());
    app.swipe(SwipeDirection.UP);
    app.swipe(SwipeDirection.UP); // one swipe is not enough to reach the bottom of the screen
    app.input(pkBase64, Debug.EGK_PRIVATE_KEY);
    app.input(cchBase64, Debug.EGK_CERTIFICATE_CHAIN);
    app.tap(Debug.SET_VIRTUAL_EGK);

    app.tap(Debug.LEAVE);
    app.tap(Settings.LEAVE);
  }

  private <T extends Actor> void getBaseDataFromDebugMenu(T actor, UseTheApp<?> app) {
    // visit the settings screen: and fetch KVID and name from Bearer Token
    app.tap(Settings.SHOW);
    app.tap(Debug.SHOW);
    val rawToken = app.getWebElement(Debug.BEARER_TOKEN).getText();
    val token = JWTDecoder.decode(rawToken);
    val kvid = token.getPayload().getIdentifier();
    val givenName = token.getPayload().getGivenName();
    val familyName = token.getPayload().getFamilyName();
    actor.can(ProvidePatientBaseData.forPatient(kvid, givenName, familyName, insuranceKind));

    app.tap(Debug.LEAVE);
    app.tap(Settings.LEAVE);
  }

  /**
   * Rather than using the debug-menu and fetching the information from the SSO Token, we might
   * simply ask the human operator to provide this data
   *
   * @param actor
   * @param <T>
   */
  private <T extends Actor> void getBaseDataFromUser(T actor) {
    val kvid = UIProvider.getQuestionResult("What is the KVID of the eGK you have used?");
    val name = UIProvider.getQuestionResult("What is the Name on the eGK?");

    if (kvid == null || name == null) {
      throw new TestcaseAbortedException("Given KVID or Name is null");
    }

    actor.can(ProvidePatientBaseData.forPatient(kvid, name, insuranceKind));
  }
}
