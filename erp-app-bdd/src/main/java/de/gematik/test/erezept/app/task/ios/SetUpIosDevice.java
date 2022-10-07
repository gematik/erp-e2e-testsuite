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

package de.gematik.test.erezept.app.task.ios;

import de.gematik.test.erezept.app.abilities.HandleAppAuthentication;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.exceptions.FeatureNotImplementedException;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.smartcard.Crypto;
import de.gematik.test.smartcard.factory.SmartcardFactory;
import java.util.Base64;
import lombok.SneakyThrows;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

public class SetUpIosDevice implements Task {

  @Override
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);
    val password = SafeAbility.getAbility(actor, HandleAppAuthentication.class).getPassword();

    // TODO: Ensure first the actor is on the right screen
    //    actor.attemptsTo(Ensure.that(ElementIsAvailable.withName("Accept Privacy")).isTrue());

    // walk through onboarding
    app.swipe(SwipeDirection.LEFT);
    app.swipe(SwipeDirection.LEFT);
    app.swipe(SwipeDirection.LEFT);
    app.input(actor.getName(), "User Profile Name");
    app.tap(2, "Onboarding Next");
    app.inputPassword(password, "Password Input", true);
    app.inputPassword(password, "Password Confirmation");
    app.tap(2, "Onboarding Next");
    app.tap("Accept Privacy");
    app.tap("Terms of Use");
    app.tap("Confirm Legal");

    // go to cardwall
    if (app.useVirtualeGK()) {
      performWithVirtualeGK(actor, app);
    } else {
      performWithRealeGK(app);
    }

    // refresh prescriptions should not open the CardWall anymore as we have already signed in with
    // a (virtual) eGK
    app.tap("Refresh Prescriptions");

    // TODO: ability required because glue-code will set GKV/PKV after this step
    // Only dummy data for now, because this information is only available after cardwall, which
    // does not work yet!
    actor.can(
        ProvidePatientBaseData.forGkvPatient(
            "X000000000", actor.getName())); // TODO: by default GKV for, change later
  }

  private void performWithRealeGK(UseTheApp app) {
    // walk through cardwall
    throw new FeatureNotImplementedException("virtual eGK on Android");
  }

  @SneakyThrows
  private <T extends Actor> void performWithVirtualeGK(T actor, UseTheApp app) {
    app.tap("Show Settings");
    app.tap("Debug Menu");

    // first tap will scroll to the element, only the second tap will tap the switch
    // TODO: implement something like scrollTo(WebElement)
    app.tap(2, "Debug Activate virtual eGK");

    // TODO: take an eGK by ICCSN from config
    val egk = SmartcardFactory.readArchive().getEgkCards(Crypto.ECC_256).get(0);
    val pkBase64 = Base64.getEncoder().encodeToString(egk.getAuthPrivateKey().getEncoded());
    val cchBase64 = Base64.getEncoder().encodeToString(egk.getAuthCertificate().getEncoded());
    app.input(pkBase64, "Debug Private Key eGK");
    app.input(cchBase64, "Debug Certificate Chain eGK");
    // assume we are done now!

    app.tap("Debug Login");

    Thread.sleep(200); // TODO: what about Pause.perform()?

    // TODO: here we should have the SSO Token, read the PatientBaseData from there!
    //    val rawToken = app.getWebElement("Debug Menu Bearer Token").getText();
    //    val token = JWTDecoder.decode(rawToken);
    //    val kvid = token.getPayload().getIdentifier();
    //    val givenName = token.getPayload().getGivenName();
    //    val familyName = token.getPayload().getFamilyName();
    // actor.can(ProvidePatientBaseData.forGkvPatient("X000000000", actor.getName())); // TODO: by
    // default GKV for, change later

    app.tap("Debug Hide Intro");
    app.tap("Leave Debug Menu");
    app.tap("Leave Settings");
  }
}
