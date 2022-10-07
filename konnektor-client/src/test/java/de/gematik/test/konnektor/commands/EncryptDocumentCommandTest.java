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

import de.gematik.test.konnektor.PinType;
import de.gematik.test.konnektor.cfg.KonnektorModuleConfiguration;
import de.gematik.test.konnektor.soap.MockKonnektorServiceProvider;
import de.gematik.test.smartcard.Crypto;
import de.gematik.test.smartcard.SmcB;
import de.gematik.test.smartcard.factory.SmartcardFactory;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class EncryptDocumentCommandTest {

  private ContextType ctx;
  private SmcB smcb;
  private MockKonnektorServiceProvider mockKonnektor;

  private byte[] data =
      ("eyAKInZlcnNpb24iOiAiMiIsIAoic3VwcGx5T3B0aW9uc1R5cGUiOiAiZGVsaXZlcnki"
              + "LCAKIm5hbWUiOiAiRHIuIE1heGltaWxpYW4gdm9uIE11c3RlciIsIAoiYWRkcmVzcyI6IFsiQnVuZGVzY"
              + "WxsZWUiLCAiMzEyIiwgIjEyMzQ1IiwgIkJlcmxpbiJdLCAKImhpbnQiOiAiQml0dGUgaW0gTW9yc2Vjb2"
              + "RlIGtsaW5nZWxuOiAtLi0uIiwgCiJ0ZXh0IjogIjEyMzQ1NiIsCiJwaG9uZSI6ICIwMDQ5MTYwOTQ4NTg"
              + "xNjgiLAoibWFpbCI6ICJtYXhAbXVzdGVyZnJhdS5kZSIsCiJ0cmFuc2FjdGlvbiI6ICJlZTYzZTQxNS05"
              + "YTk5LTQwNTEtYWIwNy0yNTc2MzJmYWY5ODUiLAoidGFza0lEIjogIjE2MC4xMjMuNDU2Ljc4OS4xMjMuN"
              + "TgiLAoiYWNjZXNzQ29kZSI6ICI3NzdiZWEwZTEzY2M5YzQyY2VlYzE0YWVjM2RkZWUyMjYzMzI1ZGMyYz"
              + "ZjNjk5ZGIxMTVmNThmZTQyMzYwN2VhIgp9IA==")
          .replace("\n", "")
          .getBytes(StandardCharsets.UTF_8);

  @BeforeEach
  void setUp() {
    val smartCardArchive = SmartcardFactory.readArchive();
    smcb = smartCardArchive.getSmcbByICCSN("80276883110000116873", Crypto.RSA_2048);

    mockKonnektor = new MockKonnektorServiceProvider(smartCardArchive);

    ctx = new ContextType();
    ctx.setClientSystemId("cs1");
    ctx.setMandantId("m1");
    ctx.setUserId("u1");
    ctx.setWorkplaceId("w1");
  }

  @SneakyThrows
  @Test
  void cmsEncryptionTest() {

    val cfg = KonnektorModuleConfiguration.getInstance();
    val konnektor = cfg.getKonnektorConfiguration("Soft-Konn").create();

    val smcbCardHandle = konnektor.execute(GetCardHandleCommand.forSmartcard(smcb));

    val encryptedData =
        konnektor.execute(
            new EncryptDocumentCommand(smcbCardHandle, Base64.getDecoder().decode(data)));
    val decrypted = konnektor.execute(new DecryptDocumentCommand(smcbCardHandle, encryptedData));
    Assertions.assertEquals(
        new String(Base64.getEncoder().encode(decrypted), StandardCharsets.UTF_8),
        new String(data, StandardCharsets.UTF_8));
  }

  @SneakyThrows
  @Test
  @Disabled("Integration Test with Konnektor")
  void integrationTestWithKonnektor() {

    val cfg = KonnektorModuleConfiguration.getInstance();
    val konnektorConfiguration = cfg.getKonnektorConfiguration("KOCO@kon7");
    val konnektor = konnektorConfiguration.create();

    val smcbCardHandle = konnektor.execute(GetCardHandleCommand.forSmartcard(smcb));
    konnektor.execute(new VerifyPinCommand(smcbCardHandle, PinType.PIN_SMC));

    val encryptedData =
        konnektor.execute(
            new EncryptDocumentCommand(smcbCardHandle, Base64.getDecoder().decode(data)));
    val decrypted = konnektor.execute(new DecryptDocumentCommand(smcbCardHandle, encryptedData));
    Assertions.assertEquals(
        new String(Base64.getEncoder().encode(decrypted), StandardCharsets.UTF_8),
        new String(data, StandardCharsets.UTF_8));
  }

  @SneakyThrows
  @Test
  @Disabled("Integration Test: Encryption with Soft-Konn and Decryption with Konnektor")
  void integrationTestWithSoftKonnAndKonnektor() {

    val cfg = KonnektorModuleConfiguration.getInstance();
    val softKon = cfg.getKonnektorConfiguration("Soft-Konn").create();
    val konnektor = cfg.getKonnektorConfiguration("KOCO@kon7").create();

    var smcbCardHandle = softKon.execute(GetCardHandleCommand.forSmartcard(smcb));
    softKon.execute(new VerifyPinCommand(smcbCardHandle, PinType.PIN_SMC));

    val encryptedData =
        softKon.execute(
            new EncryptDocumentCommand(smcbCardHandle, Base64.getDecoder().decode(data)));

    smcbCardHandle = konnektor.execute(GetCardHandleCommand.forSmartcard(smcb));
    konnektor.execute(new VerifyPinCommand(smcbCardHandle, PinType.PIN_SMC));
    val decrypted = konnektor.execute(new DecryptDocumentCommand(smcbCardHandle, encryptedData));
    Assertions.assertEquals(
        new String(Base64.getEncoder().encode(decrypted), StandardCharsets.UTF_8),
        new String(data, StandardCharsets.UTF_8));
  }
}
