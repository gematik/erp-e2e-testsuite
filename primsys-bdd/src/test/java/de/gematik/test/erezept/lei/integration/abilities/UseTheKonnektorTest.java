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

package de.gematik.test.erezept.lei.integration.abilities;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.lei.exceptions.MissingSmartcardException;
import de.gematik.test.erezept.lei.exceptions.VerifyPinFailed;
import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.konnektor.CardHandle;
import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.konnektor.commands.ExternalAuthenticateCommand;
import de.gematik.test.konnektor.commands.GetCardHandleCommand;
import de.gematik.test.konnektor.commands.VerifyPinCommand;
import de.gematik.test.smartcard.SmartcardArchive;
import de.gematik.test.smartcard.SmartcardFactory;
import de.gematik.test.smartcard.SmartcardType;
import de.gematik.ws.conn.cardservicecommon.v2.PinResponseType;
import de.gematik.ws.conn.cardservicecommon.v2.PinResultEnum;
import java.nio.charset.StandardCharsets;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class UseTheKonnektorTest {

  private static SmartcardArchive sca;

  @BeforeAll
  static void setup() {
    sca = SmartcardFactory.getArchive();
  }

  @Test
  void shouldThrowOnExternalAuthenticateWithMissingSmcb() {
    val hba = sca.getHbaCards().get(0);
    val konnektor = mock(Konnektor.class);
    val ability = UseTheKonnektor.with(hba).on(konnektor);
    val challenge = "test".getBytes(StandardCharsets.UTF_8);

    assertThrows(MissingSmartcardException.class, () -> ability.externalAuthenticate(challenge));
  }

  @Test
  void shouldThrowOnExternalAuthenticateWithInvalidPin() {
    val smcb = sca.getSmcbCards().get(0);
    val konnektor = mock(Konnektor.class);

    val smcbHandle =
        CardHandle.builder()
            .type(SmartcardType.SMC_B)
            .handle("handle")
            .iccsn(smcb.getIccsn())
            .build();
    val pinResponse = new PinResponseType();
    pinResponse.setPinResult(PinResultEnum.REJECTED);
    when(konnektor.execute(any(VerifyPinCommand.class))).thenReturn(pinResponse);
    when(konnektor.execute(any(GetCardHandleCommand.class))).thenReturn(smcbHandle);

    val challenge = "test".getBytes(StandardCharsets.UTF_8);
    val ability = UseTheKonnektor.with(smcb).on(konnektor);
    assertThrows(VerifyPinFailed.class, () -> ability.externalAuthenticate(challenge));
  }

  @Test
  void shouldExternallyAuthenticate() {
    val smcb = sca.getSmcbCards().get(0);
    val konnektor = mock(Konnektor.class);

    val smcbHandle =
        CardHandle.builder()
            .type(SmartcardType.SMC_B)
            .handle("handle")
            .iccsn(smcb.getIccsn())
            .build();
    val pinResponse = new PinResponseType();
    pinResponse.setPinResult(PinResultEnum.OK);
    when(konnektor.execute(any(VerifyPinCommand.class))).thenReturn(pinResponse);
    when(konnektor.execute(any(GetCardHandleCommand.class))).thenReturn(smcbHandle);
    when(konnektor.execute(any(ExternalAuthenticateCommand.class)))
        .thenReturn("world".getBytes(StandardCharsets.UTF_8));

    val challenge = "test".getBytes(StandardCharsets.UTF_8);
    val ability = UseTheKonnektor.with(smcb).on(konnektor);
    assertDoesNotThrow(() -> ability.externalAuthenticate(challenge));
  }
}
