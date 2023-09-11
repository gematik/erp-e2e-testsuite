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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.ScrollDirection;
import de.gematik.test.erezept.app.mobile.elements.*;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.smartcard.Crypto;
import de.gematik.test.smartcard.Egk;
import de.gematik.test.smartcard.exceptions.SmartCardKeyNotFoundException;
import java.util.Base64;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.thucydides.core.annotations.Step;

@Slf4j
@RequiredArgsConstructor
public class SetVirtualEgkOnIOS implements Task {
  
  private final Egk egk;
  
  @SneakyThrows
  @Override
  @Step("{0} setzt die virtuelle eGK #egk")
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseTheApp.class);
    app.logEvent(format("Set virtual eGK with KVNR {0} ({1}) for {2}", egk.getKvnr(), egk.getIccsn(), actor.getName()));
    
    app.tap(BottomNav.SETTINGS_BUTTON);
    app.tap(Settings.DEBUG_BUTTON);
    app.scrollIntoView(ScrollDirection.DOWN, Debug.ENABLE_VIRTUAL_EGK_USAGE_BUTTON);
    app.tap(Debug.ENABLE_VIRTUAL_EGK_USAGE_BUTTON);

    val pk = egk.getPrivateKeyBase64();
    val cc =
        Base64.getEncoder()
            .encodeToString(
                egk.getAutCertificate(Crypto.ECC_256)
                    .orElseThrow(() -> new SmartCardKeyNotFoundException(egk))
                    .getX509Certificate()
                    .getEncoded());

    app.input(pk, Debug.EGK_PRIVATE_KEY);
    app.input(cc, Debug.EGK_CERTIFICATE_CHAIN);

    app.tap(DebugSettings.LEAVE_BUTTON);
    app.tap(BottomNav.PRESCRIPTION_BUTTON);
  }

  public static SetVirtualEgkOnIOS withEgk(Egk egk) {
    return new SetVirtualEgkOnIOS(egk);
  }
}
