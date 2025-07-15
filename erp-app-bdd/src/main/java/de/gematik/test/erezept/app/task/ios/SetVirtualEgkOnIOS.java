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

package de.gematik.test.erezept.app.task.ios;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.bbriccs.smartcards.exceptions.SmartCardKeyNotFoundException;
import de.gematik.test.erezept.app.abilities.UseConfigurationData;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.elements.Debug;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@RequiredArgsConstructor
public class SetVirtualEgkOnIOS implements Task {

  private final Egk egk;

  public static SetVirtualEgkOnIOS withEgk(Egk egk) {
    return Instrumented.instanceOf(SetVirtualEgkOnIOS.class).withProperties(egk);
  }

  @SneakyThrows
  @Override
  @Step("{0} setzt die virtuelle eGK #egk")
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseTheApp.class);
    val userConfig = SafeAbility.getAbility(actor, UseConfigurationData.class);

    app.logEvent(
        format(
            "Set virtual eGK with KVNR {0} ({1}) for {2}",
            egk.getKvnr(), egk.getIccsn(), actor.getName()));

    app.swipeIntoView(SwipeDirection.UP, Debug.ENABLE_VIRTUAL_EGK_USAGE_BUTTON);

    // if active, the text of TypeSwitch is 1, otherwise 0
    val isActive = "1".equals(app.getText(Debug.ENABLE_VIRTUAL_EGK_USAGE_BUTTON));
    if (!isActive) {
      // tap only the TypeSwitch if not already active
      app.tap(Debug.ENABLE_VIRTUAL_EGK_USAGE_BUTTON);
    } else {
      // otherwise scroll directly to the certificates
      app.swipeIntoView(SwipeDirection.UP, Debug.EGK_CERTIFICATE_CHAIN);
    }

    val pk = egk.getPrivateKeyBase64();
    val cc =
        Base64.getEncoder()
            .encodeToString(
                egk.getAutCertificate(CryptoSystem.ECC_256)
                    .orElseThrow(() -> new SmartCardKeyNotFoundException(egk))
                    .getX509Certificate()
                    .getEncoded());

    app.input(pk, Debug.EGK_PRIVATE_KEY);
    app.input(cc, Debug.EGK_CERTIFICATE_CHAIN);
    app.tap(Debug.LEAVE_BUTTON);
    app.tap(Debug.MENU_BUTTON);

    if (!userConfig.isHasNfc()) {
      app.swipeIntoView(SwipeDirection.UP, Debug.FAKE_DEVICE_CAPABILITIES);

      // fake NFC capabilities
      // if active, the text of TypeSwitch is 1, otherwise 0
      val isFakingCapabilities = "1".equals(app.getText(Debug.FAKE_DEVICE_CAPABILITIES));
      if (!isFakingCapabilities) {
        // tap only the TypeSwitch if not already active
        app.tap(Debug.FAKE_DEVICE_CAPABILITIES);
      }
    }
  }
}
