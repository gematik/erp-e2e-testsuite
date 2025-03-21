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
 */

package de.gematik.test.konnektor.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gematik.bbriccs.smartcards.Hba;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.konnektor.PinType;
import de.gematik.test.konnektor.soap.MockKonnektorServiceProvider;
import de.gematik.ws.conn.cardservicecommon.v2.PinResultEnum;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import java.math.BigInteger;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class VerifyPinCommandTest {

  private static ContextType ctx;
  private static Hba hba;
  private static MockKonnektorServiceProvider mockKonnektor;

  @BeforeAll
  static void setUp() {
    val smartCardArchive = SmartcardArchive.fromResources();
    hba = smartCardArchive.getHbaByICCSN("80276883110000095767");

    mockKonnektor = new MockKonnektorServiceProvider(smartCardArchive);

    ctx = new ContextType();
    ctx.setClientSystemId("cs1");
    ctx.setMandantId("m1");
    ctx.setUserId("u1");
    ctx.setWorkplaceId("w1");
  }

  @Test
  void shouldReplyWithVerifiedPinStatus() {
    val hbaCardHandle = GetCardHandleCommand.forSmartcard(hba).execute(ctx, mockKonnektor);

    val pinResponseType =
        new VerifyPinCommand(hbaCardHandle, PinType.PIN_CH).execute(ctx, mockKonnektor);

    assertNotNull(pinResponseType);
    assertEquals(BigInteger.valueOf(3), pinResponseType.getLeftTries());
    assertEquals(PinResultEnum.OK, pinResponseType.getPinResult());
  }
}
