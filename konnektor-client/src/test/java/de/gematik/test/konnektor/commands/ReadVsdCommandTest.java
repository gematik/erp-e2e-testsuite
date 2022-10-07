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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gematik.test.konnektor.PinType;
import de.gematik.test.konnektor.cfg.KonnektorModuleConfiguration;
import de.gematik.test.konnektor.soap.MockKonnektorServiceProvider;
import de.gematik.test.konnektor.soap.RemoteKonnektorTest;
import de.gematik.test.smartcard.Crypto;
import de.gematik.test.smartcard.Egk;
import de.gematik.test.smartcard.Hba;
import de.gematik.test.smartcard.SmartcardArchive;
import de.gematik.test.smartcard.factory.SmartcardFactory;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ReadVsdCommandTest {

  private static ContextType ctx;
  private static SmartcardArchive smartCardArchive;
  private static Hba hba;
  private static Egk egk;
  private static MockKonnektorServiceProvider mockKonnektor;

  @BeforeAll
  static void setUp() {
    smartCardArchive = SmartcardFactory.readArchive();
    hba = smartCardArchive.getHbaByICCSN("80276883110000095767", Crypto.RSA_2048);
    egk = smartCardArchive.getEgkByICCSN("80276881025548431301", Crypto.RSA_2048);

    mockKonnektor = new MockKonnektorServiceProvider(smartCardArchive);

    ctx = new ContextType();
    ctx.setClientSystemId("cs1");
    ctx.setMandantId("m1");
    ctx.setUserId("u1");
    ctx.setWorkplaceId("w1");
  }

  @AfterAll
  static void tearDown() {
    smartCardArchive.destroy();
  }

  @Test
  void shouldReplyWithExamEvidence() {
    val egkCardHandle = GetCardHandleCommand.forSmartcard(egk).execute(ctx, mockKonnektor);
    val hbaCardHandle = GetCardHandleCommand.forSmartcard(hba).execute(ctx, mockKonnektor);

    val pinResponseType =
        new VerifyPinCommand(hbaCardHandle, PinType.PIN_CH).execute(ctx, mockKonnektor);

    val readVsdCommand =
        new ReadVsdCommand(egkCardHandle, hbaCardHandle, true, true).execute(ctx, mockKonnektor);
    assertNotNull(readVsdCommand.getPruefungsnachweis());
  }

  @Test
  @Disabled("Integration Test with Konnektor")
  void exampleIntegrationTest() {
    val is =
        Objects.requireNonNull(
            RemoteKonnektorTest.class.getClassLoader().getResourceAsStream("config.yaml"));
    val yaml =
        new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.joining("\n"));
    val cfg = KonnektorModuleConfiguration.getInstance(yaml);

    val konnektorConfiguration = cfg.getKonnektorConfiguration("KOCO@kon7");
    val konnektor = konnektorConfiguration.create();

    val egkCardHandle = konnektor.execute(GetCardHandleCommand.forSmartcard(egk));
    val hbaCardHandle = konnektor.execute(GetCardHandleCommand.forSmartcard(hba));
    konnektor.execute(new VerifyPinCommand(hbaCardHandle, PinType.PIN_CH));

    val readVSDResponse =
        konnektor.execute(new ReadVsdCommand(egkCardHandle, hbaCardHandle, true, true));
    assertNotNull(readVSDResponse.getPruefungsnachweis());
  }
}
