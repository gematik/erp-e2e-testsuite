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
import de.gematik.test.erezept.exceptions.TestcaseAbortedException;
import de.gematik.test.erezept.jwt.JWTDecoder;
import de.gematik.test.erezept.operator.UIProvider;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.smartcard.Crypto;
import de.gematik.test.smartcard.factory.SmartcardFactory;
import java.util.Base64;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
public class SetUpAndroidDevice implements Task {

  @Override
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseTheApp.class);
    val password = SafeAbility.getAbility(actor, HandleAppAuthentication.class).getPassword();

    // walk through onboarding
    app.swipe(SwipeDirection.LEFT);
    app.swipe(SwipeDirection.LEFT);
    app.input(actor.getName(), "User Profile Name");
    app.tap("Onboarding Next");
    app.inputPassword(password, "Password Input", true);
    app.inputPassword(password, "Password Confirmation");
    app.tap(4, "Onboarding Next");
    app.tap("Accept Privacy");
    app.tap("Terms of Use");
    app.tap("Confirm Legal");

    if (app.useVirtualeGK()) {
      performWithVirtualeGK(actor, app);
    } else {
      performWithRealeGK(app);
    }

    getBaseDataFromDebugMenu(actor, app);
  }

  private void performWithRealeGK(UseTheApp app) {
    // walk through cardwall
    app.tap("Refresh Prescriptions");
    app.tap("Cardwall Next");
    app.input("123123", "Cardwall CAN Input"); // TODO: find a way to configure
    app.tap("Cardwall Next");
    app.input("123456", "Cardwall PIN Input"); // TODO: find a way to configure
    //    app.tap("Cardwall Next"); // seems to be not required??

    app.tap("Cardwall Sign in");
    UIProvider.getInstructionResult(
        "Get your eGK ready and follow the Instructions on the Screen of your device");
  }

  @SneakyThrows
  private <T extends Actor> void performWithVirtualeGK(T actor, UseTheApp app) {
    app.tap("Show Settings");
    app.tap("Debug Menu");

    // TODO: take an eGK by ICCSN from config
    val egk = SmartcardFactory.readArchive().getEgkCards(Crypto.ECC_256).get(0);
    val pkBase64 = Base64.getEncoder().encodeToString(egk.getAuthPrivateKey().getEncoded());
    val cchBase64 = Base64.getEncoder().encodeToString(egk.getAuthCertificate().getEncoded());
    app.swipe(SwipeDirection.UP);
    app.swipe(SwipeDirection.UP); // one swipe is not enough to reach the bottom of the screen
    app.input(pkBase64, "Debug Private Key eGK");
    app.input(cchBase64, "Debug Certificate Chain eGK");
    app.tap("Debug set virtual eGK");

    app.tap("Leave Debug Menu");
    app.tap("Leave Settings");
  }

  private <T extends Actor> void getBaseDataFromDebugMenu(T actor, UseTheApp app) {
    // visit the settings screen: and fetch KVID and name from Bearer Token
    app.tap("Show Settings");
    app.tap("Debug Menu");
    val rawToken = app.getWebElement("Debug Menu Bearer Token").getText();
    val token = JWTDecoder.decode(rawToken);
    val kvid = token.getPayload().getIdentifier();
    val givenName = token.getPayload().getGivenName();
    val familyName = token.getPayload().getFamilyName();
    actor.can(
        ProvidePatientBaseData.forGkvPatient(
            kvid, givenName, familyName)); // TODO: by default GKV for, change later

    app.tap("Leave Debug Menu");
    app.tap("Leave Settings");
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

    actor.can(
        ProvidePatientBaseData.forGkvPatient(kvid, name)); // TODO: by default GKV for, change later
  }
}
