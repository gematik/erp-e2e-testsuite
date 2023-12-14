/*
 * Copyright 2023 gematik GmbH
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

import de.gematik.test.erezept.config.ConfigurationReader;
import de.gematik.test.konnektor.PinType;
import de.gematik.test.konnektor.cfg.KonnektorModuleFactory;
import de.gematik.test.smartcard.Algorithm;
import de.gematik.test.smartcard.SmartcardFactory;
import de.gematik.test.smartcard.SmcB;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class EncryptDocumentCommandTest {

  private final byte[] data =
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
  private SmcB smcb;
  private KonnektorModuleFactory cfg;

  @BeforeEach
  void setUp() {
    val smartCardArchive = SmartcardFactory.getArchive();
    smcb = smartCardArchive.getSmcbByICCSN("80276883110000116873");
    val templatePath =
        Path.of(this.getClass().getClassLoader().getResource("config.yaml").getPath());
    cfg =
        ConfigurationReader.forKonnektorClient()
            .configFile(templatePath)
            .wrappedBy(KonnektorModuleFactory::fromDto);
  }

  @SneakyThrows
  @Test()
  void cmsEncryptionTest() {
    val konnektor = cfg.createSoftKon();
    val smcbCardHandle = konnektor.execute(GetCardHandleCommand.forSmartcard(smcb)).getPayload();

    val encryptedData =
        konnektor
            .execute(
                new EncryptDocumentCommand(
                    smcbCardHandle, Base64.getDecoder().decode(data), Algorithm.RSA_2048))
            .getPayload();
    val decrypted =
        konnektor
            .execute(new DecryptDocumentCommand(smcbCardHandle, encryptedData, Algorithm.RSA_2048))
            .getPayload();
    Assertions.assertEquals(
        new String(Base64.getEncoder().encode(decrypted), StandardCharsets.UTF_8),
        new String(data, StandardCharsets.UTF_8));
  }

  @SneakyThrows
  @Test
  @Disabled("Integration Test with Konnektor")
  void integrationTestWithKonnektor() {
    val konnektor = cfg.createKonnektorClient("KOCO@kon7");
    val smcbCardHandle = konnektor.execute(GetCardHandleCommand.forSmartcard(smcb)).getPayload();
    konnektor.execute(new VerifyPinCommand(smcbCardHandle, PinType.PIN_SMC));

    val encryptedData =
        konnektor
            .execute(
                new EncryptDocumentCommand(
                    smcbCardHandle, Base64.getDecoder().decode(data), Algorithm.RSA_2048))
            .getPayload();
    val decrypted =
        konnektor
            .execute(new DecryptDocumentCommand(smcbCardHandle, encryptedData, Algorithm.RSA_2048))
            .getPayload();
    Assertions.assertEquals(
        new String(Base64.getEncoder().encode(decrypted), StandardCharsets.UTF_8),
        new String(data, StandardCharsets.UTF_8));
  }

  @SneakyThrows
  @Test
  @Disabled("Integration Test: Encryption with Soft-Konn and Decryption with Konnektor")
  void integrationTestWithSoftKonnAndKonnektor() {
    val softKon = cfg.createSoftKon();
    val konnektor = cfg.createKonnektorClient("KOCO@kon7");

    var smcbCardHandle = softKon.execute(GetCardHandleCommand.forSmartcard(smcb)).getPayload();
    softKon.execute(new VerifyPinCommand(smcbCardHandle, PinType.PIN_SMC));

    val encryptedData =
        softKon
            .execute(
                new EncryptDocumentCommand(
                    smcbCardHandle, Base64.getDecoder().decode(data), Algorithm.RSA_2048))
            .getPayload();

    smcbCardHandle = konnektor.execute(GetCardHandleCommand.forSmartcard(smcb)).getPayload();
    konnektor.execute(new VerifyPinCommand(smcbCardHandle, PinType.PIN_SMC));
    val decrypted =
        konnektor
            .execute(new DecryptDocumentCommand(smcbCardHandle, encryptedData, Algorithm.RSA_2048))
            .getPayload();
    Assertions.assertEquals(
        new String(Base64.getEncoder().encode(decrypted), StandardCharsets.UTF_8),
        new String(data, StandardCharsets.UTF_8));
  }
}
