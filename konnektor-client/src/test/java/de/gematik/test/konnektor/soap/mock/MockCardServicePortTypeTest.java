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

package de.gematik.test.konnektor.soap.mock;

import de.gematik.test.konnektor.soap.MockKonnektorServiceProvider;
import de.gematik.test.smartcard.SmartcardArchive;
import de.gematik.test.smartcard.SmartcardFactory;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import lombok.val;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MockCardServicePortTypeTest {
  private static ContextType ctx;
  private static SmartcardArchive smartCardArchive;
  private static MockKonnektorServiceProvider mockKonnektor;

  @BeforeAll
  static void setup() {
    smartCardArchive = SmartcardFactory.getArchive();
    mockKonnektor = new MockKonnektorServiceProvider(smartCardArchive);

    ctx = new ContextType();
    ctx.setClientSystemId("cs1");
    ctx.setMandantId("m1");
    ctx.setUserId("u1");
    ctx.setWorkplaceId("w1");
  }

  @Test
  void shouldThrowExceptions() {
    val cardService = mockKonnektor.getCardService();
    Assertions.assertThrows(
        NotImplementedException.class,
        () -> cardService.changePin(ctx, null, null, null, null, null));

    Assertions.assertThrows(
        NotImplementedException.class,
        () -> cardService.disablePin(ctx, null, null, null, null, null));

    Assertions.assertThrows(
        NotImplementedException.class,
        () -> cardService.enablePin(ctx, null, null, null, null, null));

    Assertions.assertThrows(
        NotImplementedException.class,
        () -> cardService.changePin(ctx, null, null, null, null, null));

    Assertions.assertThrows(
        NotImplementedException.class,
        () -> cardService.getPinStatus(ctx, null, null, null, null, null));

    Assertions.assertThrows(
        NotImplementedException.class,
        () -> cardService.unblockPin(ctx, null, null, null, null, null, null));
  }
}
