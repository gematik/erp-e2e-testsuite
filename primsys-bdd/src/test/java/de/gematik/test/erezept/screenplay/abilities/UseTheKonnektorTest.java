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

package de.gematik.test.erezept.screenplay.abilities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import de.gematik.test.cardterminal.CardInfo;
import de.gematik.test.erezept.config.dto.konnektor.LocalKonnektorConfiguration;
import de.gematik.test.erezept.crypto.BC;
import de.gematik.test.erezept.exceptions.MissingSmartcardException;
import de.gematik.test.erezept.exceptions.VerifyPinFailed;
import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.konnektor.KonnektorResponse;
import de.gematik.test.konnektor.cfg.KonnektorFactory;
import de.gematik.test.konnektor.commands.ExternalAuthenticateCommand;
import de.gematik.test.konnektor.commands.GetCardHandleCommand;
import de.gematik.test.konnektor.commands.VerifyPinCommand;
import de.gematik.test.smartcard.Algorithm;
import de.gematik.test.smartcard.SmartcardArchive;
import de.gematik.test.smartcard.SmartcardFactory;
import de.gematik.test.smartcard.SmartcardType;
import de.gematik.ws.conn.cardservicecommon.v2.PinResponseType;
import de.gematik.ws.conn.cardservicecommon.v2.PinResultEnum;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.core.reports.AndContent;
import net.serenitybdd.core.reports.WithTitle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;

@Slf4j
class UseTheKonnektorTest {

  private static SmartcardArchive sca;

  @BeforeAll
  static void setup() {
    // Support for brainpool curves
    BC.init();
    sca = SmartcardFactory.getArchive();
  }

  @Test
  void shouldThrowOnExternalAuthenticateWithMissingSmcb() {
    val hba = sca.getHbaCards().get(0);
    val konnektor = mock(Konnektor.class);
    val hbaHandle =
        CardInfo.builder()
            .type(SmartcardType.SMC_B)
            .handle("handle")
            .iccsn(hba.getIccsn())
            .ctId("Ct01")
            .build();
    when(konnektor.execute(any(GetCardHandleCommand.class)))
        .thenReturn(new KonnektorResponse<>(hbaHandle));

    val ability = UseTheKonnektor.with(hba).on(konnektor);
    val challenge = "test".getBytes(StandardCharsets.UTF_8);

    assertThrows(MissingSmartcardException.class, () -> ability.externalAuthenticate(challenge));
  }

  @Test
  void shouldThrowOnExternalAuthenticateWithInvalidPin() {
    val smcb = sca.getSmcbCards().get(0);
    val konnektor = mock(Konnektor.class);

    val smcbHandle =
        CardInfo.builder()
            .type(SmartcardType.SMC_B)
            .handle("handle")
            .ctId("Ct01")
            .iccsn(smcb.getIccsn())
            .build();
    val pinResponse = new PinResponseType();
    pinResponse.setPinResult(PinResultEnum.REJECTED);
    when(konnektor.execute(any(VerifyPinCommand.class)))
        .thenReturn(new KonnektorResponse<>(pinResponse));
    when(konnektor.execute(any(GetCardHandleCommand.class)))
        .thenReturn(new KonnektorResponse<>(smcbHandle));

    val challenge = "test".getBytes(StandardCharsets.UTF_8);
    val ability = UseTheKonnektor.with(smcb).on(konnektor);
    assertThrows(VerifyPinFailed.class, () -> ability.externalAuthenticate(challenge));
  }

  @Test
  void shouldExternallyAuthenticate() {
    val smcb = sca.getSmcbCards().get(0);
    val konnektor = mock(Konnektor.class);

    val smcbHandle =
        CardInfo.builder()
            .type(SmartcardType.SMC_B)
            .handle("handle")
            .ctId("Ct01")
            .iccsn(smcb.getIccsn())
            .build();
    val pinResponse = new PinResponseType();
    pinResponse.setPinResult(PinResultEnum.OK);
    when(konnektor.execute(any(VerifyPinCommand.class)))
        .thenReturn(new KonnektorResponse<>(pinResponse));
    when(konnektor.execute(any(GetCardHandleCommand.class)))
        .thenReturn(new KonnektorResponse<>(smcbHandle));
    when(konnektor.execute(any(ExternalAuthenticateCommand.class)))
        .thenReturn(new KonnektorResponse<>("world".getBytes(StandardCharsets.UTF_8)));

    val challenge = "test".getBytes(StandardCharsets.UTF_8);
    val ability = UseTheKonnektor.with(smcb).on(konnektor);
    assertDoesNotThrow(() -> ability.externalAuthenticate(challenge));
  }

  @ParameterizedTest
  @EnumSource(Algorithm.class)
  void shouldSignAndVerifyWithHba(Algorithm algorithm) {
    val smcb = sca.getSmcbCards().get(0);
    val hba = sca.getHbaCards().get(0);
    val konnektor = KonnektorFactory.createSoftKon();
    val ability = UseTheKonnektor.with(smcb).and(hba).and(algorithm).on(konnektor);

    try (MockedStatic<Serenity> serenityMockedStatic = mockStatic(Serenity.class)) {
      val mockWithTitle = mock(WithTitle.class);
      val mockAndContent = mock(AndContent.class);
      serenityMockedStatic.when(Serenity::recordReportData).thenReturn(mockWithTitle);
      when(mockWithTitle.withTitle(anyString())).thenReturn(mockAndContent);
      val signed = ability.signDocumentWithHba("Hello World").getPayload();
      assertTrue(ability.verifyDocument(signed).getPayload());
    }
  }

  @Test
  void shouldGetAuthCertificate() {
    val smcb = sca.getSmcbCards().get(0);
    val konnektor = KonnektorFactory.createSoftKon();
    val ability = UseTheKonnektor.with(smcb).on(konnektor);

    val authCertificate = ability.getSmcbAuthCertificate();
    assertNotNull(authCertificate);
  }

  @Test
  void shouldRequestEvidenceForEgk() {
    val smcb = sca.getSmcbCards().get(0);
    val egk = sca.getEgkCards().get(0);
    val konnektor = KonnektorFactory.createSoftKon();
    val ability = UseTheKonnektor.with(smcb).on(konnektor);

    val evidence = ability.requestEvidenceForEgk(egk);
    assertNotNull(evidence);
  }

  @Test
  void shouldEncryptAndDecrypt() {
    val smcb = sca.getSmcbByICCSN("80276883110000116873");
    val konnektor = KonnektorFactory.createSoftKon();
    val ability = UseTheKonnektor.with(smcb).on(konnektor);

    val encrypted = ability.encrypt("Hello World").getPayload();
    val decrypted = ability.decrypt(encrypted).getPayload();
    assertEquals("Hello World", new String(decrypted, StandardCharsets.UTF_8));
  }

  @Test
  void shouldCreateAbilityFromConfig() {
    val smcb = sca.getSmcbCards().get(0);
    val hba = sca.getHbaCards().get(0);
    val cfg = new LocalKonnektorConfiguration();
    val ability = UseTheKonnektor.with(hba).and(smcb).on(cfg);
    assertDoesNotThrow(ability::toString);
  }
}
