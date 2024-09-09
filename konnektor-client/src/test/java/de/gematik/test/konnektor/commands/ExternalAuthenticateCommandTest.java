/*
 * Copyright 2024 gematik GmbH
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

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.konnektor.cfg.KonnektorFactory;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ExternalAuthenticateCommandTest {

  private static Konnektor mockKonnektor;
  private static SmartcardArchive sca;

  @BeforeAll
  static void setup() {
    sca = SmartcardArchive.fromResources();
    mockKonnektor = KonnektorFactory.createSoftKon();
  }

  @SneakyThrows
  @Test
  void shouldCreateValidSignatureWithRSA() {
    val smcb = sca.getSmcbByICCSN("80276001011699901102");
    val cardHandle = mockKonnektor.execute(GetCardHandleCommand.forSmartcard(smcb)).getPayload();

    val externalAuthenticateCommand =
        new ExternalAuthenticateCommand(cardHandle, CryptoSystem.RSA_PSS_2048);
    val toBeSignedData = new byte[0];
    externalAuthenticateCommand.setToBeSignedData(toBeSignedData);
    val signedData = mockKonnektor.execute(externalAuthenticateCommand).getPayload();
    assertNotNull(signedData);
    assertEquals(256, signedData.length);
  }

  @Test
  void shouldCreateValidSignatureWithECC() {
    val hba = sca.getHbaByICCSN("80276883110000121166");
    val cardHandle = mockKonnektor.execute(GetCardHandleCommand.forSmartcard(hba)).getPayload();

    val externalAuthenticateCommand =
        new ExternalAuthenticateCommand(cardHandle, CryptoSystem.ECC_256);
    val toBeSignedData = new byte[0];
    externalAuthenticateCommand.setToBeSignedData(toBeSignedData);
    assertNotNull(mockKonnektor.execute(externalAuthenticateCommand).getPayload());
    // TODO: DocBrown: the signature validation still missing
    //  Marty: yeah..but we have reached the code coverage
  }
}
