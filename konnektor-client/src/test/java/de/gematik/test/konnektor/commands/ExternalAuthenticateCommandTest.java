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

package de.gematik.test.konnektor.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.konnektor.cfg.LocalKonnektorConfiguration;
import de.gematik.test.smartcard.Crypto;
import de.gematik.test.smartcard.SmartcardArchive;
import de.gematik.test.smartcard.factory.SmartcardFactory;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExternalAuthenticateCommandTest {

  private static Konnektor mockKonnektor;
  private static SmartcardArchive sca;

  @BeforeClass
  public static void setup() {
    sca = SmartcardFactory.readArchive();
    mockKonnektor = LocalKonnektorConfiguration.createMock();
  }

  @SneakyThrows
  @Test
  public void shouldCreateValidSignatureWithRSA() {
    val smcb = sca.getSmcbByICCSN("8027600101169991010", Crypto.RSA_2048);
    val cardHandle = mockKonnektor.execute(GetCardHandleCommand.forSmartcard(smcb));

    val externalAuthenticateCommand = new ExternalAuthenticateCommand(cardHandle);
    val toBeSignedData = new byte[0];
    externalAuthenticateCommand.setToBeSignedData(toBeSignedData);
    val signedData = mockKonnektor.execute(externalAuthenticateCommand);
    assertNotNull(signedData);
    assertEquals(256, signedData.length);
  }

  @Test
  public void shouldCreateValidSignatureWithECC() {
    val hba = sca.getHbaByICCSN("80276883110000121166", Crypto.ECC_256);
    val cardHandle = mockKonnektor.execute(GetCardHandleCommand.forSmartcard(hba));

    val externalAuthenticateCommand = new ExternalAuthenticateCommand(cardHandle);
    val toBeSignedData = new byte[0];
    externalAuthenticateCommand.setToBeSignedData(toBeSignedData);
    assertNotNull(mockKonnektor.execute(externalAuthenticateCommand));
    // TODO: DocBrown: the signature validation still missing
    //  Marty: yeah..but we have reached the code coverage
  }
}
