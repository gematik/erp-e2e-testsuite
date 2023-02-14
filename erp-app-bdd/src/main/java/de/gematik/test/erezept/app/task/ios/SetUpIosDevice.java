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

import static java.text.MessageFormat.*;

import de.gematik.test.erezept.app.abilities.*;
import de.gematik.test.erezept.app.mobile.*;
import de.gematik.test.erezept.app.mobile.elements.*;
import de.gematik.test.erezept.exceptions.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.util.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import net.serenitybdd.screenplay.*;

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
    app.inputPassword(password, Onboarding.PASSWORD_INPUT_FIELD);
    app.inputPassword(password, Onboarding.PASSWORD_CONFIRMATION_FIELD);
    app.tap(Onboarding.NEXT_BUTTON);
    app.tap(Onboarding.ACCEPT_TERMS_OF_USE_BUTTON);
    app.tap(Onboarding.ACCEPT_PRIVACY_BUTTON);

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
    app.tap(Settings.MENU_BUTTON);
    app.tap(Debug.MENU_BUTTON);

    // first tap will scroll to the element, only the second tap will tap the switch
    app.tap(2, Debug.ENABLE_VIRTUAL_EGK_USAGE_BUTTON); // why do I need to tap twice here?

    val egk = SafeAbility.getAbility(actor, ProvideEGK.class).getEgk();
    val autCertificate = egk.getAutCertificate();
    val pkBase64 = Base64.getEncoder().encodeToString(autCertificate.getPrivateKey().getEncoded());
    val cchBase64 =
        Base64.getEncoder().encodeToString(autCertificate.getX509Certificate().getEncoded());
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

    app.tap(Debug.LEAVE_BUTTON);
    app.tap(Settings.LEAVE_BUTTON);
  }
}
