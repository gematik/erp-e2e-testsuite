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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.abilities.HandleAppAuthentication;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.elements.Debug;
import de.gematik.test.erezept.app.mobile.elements.Onboarding;
import de.gematik.test.erezept.app.mobile.elements.Settings;
import de.gematik.test.erezept.exceptions.FeatureNotImplementedException;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
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
public class SetUpIosDevice implements Task {

  private final VersicherungsArtDeBasis insuranceKind;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);
    val password = SafeAbility.getAbility(actor, HandleAppAuthentication.class).getPassword();

    // walk through onboarding
    app.swipe(SwipeDirection.LEFT);
    app.swipe(SwipeDirection.LEFT);
    app.inputPassword(password, Onboarding.PASSWORD_INPUT);
    app.inputPassword(password, Onboarding.PASSWORD_CONFIRMATION);
    app.tap(Onboarding.NEXT);
    app.tap(Onboarding.ACCEPT_TERMS_OF_USE);
    app.tap(Onboarding.ACCEPT_PRIVACY);

    // go to cardwall
    if (app.useVirtualeGK()) {
      performWithVirtualeGK(actor, app);
    } else {
      performWithRealeGK(app);
    }
  }

  private void performWithRealeGK(UseTheApp<?> app) {
    // walk through cardwall
    throw new FeatureNotImplementedException(
        format("real eGK on iPhone with {0}", app.getDriverName()));
  }

  @SneakyThrows
  private <T extends Actor> void performWithVirtualeGK(T actor, UseTheApp<?> app) {
    app.tap(Settings.SHOW);
    app.tap(Debug.SHOW);

    // first tap will scroll to the element, only the second tap will tap the switch
    app.tap(2, Debug.ACTIVATE_VIRTUAL_EGK); // why do I need to tap twice here?

    val egk = SafeAbility.getAbility(actor, ProvideEGK.class).getEgk();
    val pkBase64 = Base64.getEncoder().encodeToString(egk.getAuthPrivateKey().getEncoded());
    val cchBase64 = Base64.getEncoder().encodeToString(egk.getAuthCertificate().getEncoded());
    app.input(pkBase64, Debug.EGK_PRIVATE_KEY);
    app.tap(2, Debug.EGK_CERTIFICATE_CHAIN); // make sure we change the textbox
    app.input(cchBase64, Debug.EGK_CERTIFICATE_CHAIN);
    // assume we are done now!

    app.tap(2, Debug.LOGIN);

    // TODO: here we should have the SSO Token, read the PatientBaseData from there!
    //    val rawToken = app.getWebElement("Debug Menu Bearer Token").getText();
    //    val token = JWTDecoder.decode(rawToken);
    //    val kvid = token.getPayload().getIdentifier();
    //    val givenName = token.getPayload().getGivenName();
    //    val familyName = token.getPayload().getFamilyName();

    actor.can(
        ProvidePatientBaseData.forPatient(
            egk.getKvnr(),
            egk.getOwner().getGivenName(),
            egk.getOwner().getSurname(),
            insuranceKind));

    app.tap(Debug.LEAVE);
    app.tap(Settings.LEAVE);
  }
}
