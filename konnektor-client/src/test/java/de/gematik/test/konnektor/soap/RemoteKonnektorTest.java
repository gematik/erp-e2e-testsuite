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

package de.gematik.test.konnektor.soap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.cardterminal.CardInfo;
import de.gematik.test.erezept.config.ConfigurationReader;
import de.gematik.test.konnektor.cfg.KonnektorFactory;
import de.gematik.test.konnektor.cfg.KonnektorModuleFactory;
import de.gematik.test.konnektor.commands.GetCardHandleCommand;
import de.gematik.test.konnektor.commands.ReadCardCertificateCommand;
import de.gematik.test.konnektor.commands.SignXMLDocumentCommand;
import de.gematik.test.konnektor.commands.VerifyDocumentCommand;
import de.gematik.test.konnektor.exceptions.SOAPRequestException;
import de.gematik.test.smartcard.Algorithm;
import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import de.gematik.ws.conn.certificateservicecommon.v2.CertRefEnum;
import java.nio.file.Path;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RemoteKonnektorTest {

  private static KonnektorModuleFactory cfg;

  @SneakyThrows
  @BeforeAll
  static void setup() {
    val templatePath =
        Path.of(RemoteKonnektorTest.class.getClassLoader().getResource("config.yaml").getPath());
    cfg =
        ConfigurationReader.forKonnektorClient()
            .configFile(templatePath)
            .wrappedBy(KonnektorModuleFactory::fromDto);
  }

  @Test
  @SneakyThrows
  void shouldPerformSimpleRoundtripOnRemoteKonnektor() {
    val konnektor = cfg.createKonnektorClient("Soft-Konn");

    // get a cardHandle
    val cardHandle =
        konnektor.execute(GetCardHandleCommand.forIccsn("80276001081699900579")).getPayload();

    // read the Auth certificate from card
    val cardAuthCertCmd =
        new ReadCardCertificateCommand(cardHandle, CertRefEnum.C_AUT, Algorithm.RSA_2048);
    val cardAuthCertificate = konnektor.execute(cardAuthCertCmd).getPayload();
    assertNotNull(cardAuthCertificate);

    // read the Sig certificate from card
    // NOT YET supported on KonSim
    //    val cardSigCertCmd = new ReadCardCertificateCommand(cardHandle, CertRefEnum.C_SIG);
    //    val cardSigCertificate = konnektor.execute(cardSigCertCmd);
    //    assertNotNull(cardSigCertificate);

    // read the Sig certificate from card
    // NOT YET supported on KonSim
    //    val cardEncCertCmd = new ReadCardCertificateCommand(cardHandle, CertRefEnum.C_ENC);
    //    val cardEncCertificate = konnektor.execute(cardEncCertCmd);
    //    assertNotNull(cardEncCertificate);

    // read the Sig certificate from card
    // NOT YET supported on KonSim
    //    val cardQesCertCmd = new ReadCardCertificateCommand(cardHandle, CertRefEnum.C_QES);
    //    val cardQesCertificate = konnektor.execute(cardQesCertCmd);
    //    assertNotNull(cardQesCertificate);

    // sign an exemplary XML document
    val signCmd = new SignXMLDocumentCommand(cardHandle, "<xml>TEST</xml>", Algorithm.RSA_2048);
    val signRsp = konnektor.execute(signCmd).getPayload();
    assertNotNull(signRsp);
    assertTrue(signRsp.length > 0);

    // verify the document from previous step
    val verifyCmd = new VerifyDocumentCommand(signRsp);
    val verifyRsp = konnektor.execute(verifyCmd).getPayload();
    assertTrue(verifyRsp);

    //    val extAuthCmd = new ExternalAuthenticateCommand(cardHandle);
    //    val token = konnektor.execute(extAuthCmd);
  }

  @Test
  void shouldPerformSimpleRoundTripWithMockKonnektor() {
    // instantiate Soft-Konn
    val konnektor = cfg.createKonnektorClient("Soft-Konn");

    // get a cardHandle
    val cardHandle =
        konnektor.execute(GetCardHandleCommand.forIccsn("80276001081699900579")).getPayload();

    // read the Auth certificate from card
    val cardAuthCertCmd = new ReadCardCertificateCommand(cardHandle, Algorithm.RSA_2048);
    val cardAuthCertificate = konnektor.execute(cardAuthCertCmd).getPayload();
    assertNotNull(cardAuthCertificate);

    // sign an exemplary XML document
    val signCmd = new SignXMLDocumentCommand(cardHandle, "<xml>TEST</xml>", Algorithm.RSA_2048);
    val signRsp = konnektor.execute(signCmd).getPayload();
    assertNotNull(signRsp);
    assertTrue(signRsp.length > 0);

    val verifyCmd = new VerifyDocumentCommand(signRsp);
    val verifyRsp = konnektor.execute(verifyCmd).getPayload();
    assertTrue(verifyRsp, "Signed Response shall be valid");

    val verifyInvalidCmd = new VerifyDocumentCommand("not valid".getBytes());
    val verifyInvalidRsp = konnektor.execute(verifyInvalidCmd).getPayload();
    assertFalse(verifyInvalidRsp, "Invalid Document shall be invalid");

    //    val extAuthCmd = new ExternalAuthenticateCommand(cardHandle);
    //    val token = konnektor.execute(extAuthCmd);
  }

  @Test
  void shouldReceiveResponse() {
    val mockCmd = mock(GetCardHandleCommand.class);
    val cit = new CardInfoType();
    cit.setIccsn("80276001011699910102");
    cit.setCardHandle("my_test_handle");
    cit.setCtId("Ct01");
    cit.setCardType(CardTypeType.HBA);
    val cardHandle = CardInfo.fromCardInfoType(cit);
    when(mockCmd.execute(any(), any())).thenReturn(cardHandle);

    cfg.getKonnektors()
        .forEach(
            konnektorConfiguration -> {
              val konnektor = KonnektorFactory.createKonnektor(konnektorConfiguration);
              val response = konnektor.execute(mockCmd).getPayload();
              assertEquals(cardHandle, response);
            });
  }

  @Test
  void shouldReceiveResponseOnSafeExecute() {
    val mockCmd = mock(GetCardHandleCommand.class);
    val cit = new CardInfoType();
    cit.setIccsn("80276001011699910102");
    cit.setCardHandle("my_test_handle");
    cit.setCtId("Ct01");
    cit.setCardType(CardTypeType.HBA);
    val cardHandle = CardInfo.fromCardInfoType(cit);
    when(mockCmd.execute(any(), any())).thenReturn(cardHandle);

    cfg.getKonnektors()
        .forEach(
            konnektorConfiguration -> {
              val konnektor = KonnektorFactory.createKonnektor(konnektorConfiguration);
              val response = konnektor.safeExecute(mockCmd);
              assertTrue(response.isPresent());
              assertEquals(cardHandle, response.orElseThrow().getPayload());
            });
  }

  @Test
  void shouldThrowSOAPException() {
    val mockCmd = mock(GetCardHandleCommand.class);
    when(mockCmd.execute(any(), any())).thenThrow(SOAPRequestException.class);

    cfg.getKonnektors()
        .forEach(
            konnektorConfiguration -> {
              val konnektor = KonnektorFactory.createKonnektor(konnektorConfiguration);
              assertThrows(SOAPRequestException.class, () -> konnektor.execute(mockCmd));
            });
  }

  @Test
  void shouldNotThrowOnSafeExecute() {
    val mockCmd = mock(GetCardHandleCommand.class);
    when(mockCmd.execute(any(), any())).thenThrow(SOAPRequestException.class);

    cfg.getKonnektors()
        .forEach(
            konnektorConfiguration -> {
              val konnektor = KonnektorFactory.createKonnektor(konnektorConfiguration);
              val response = konnektor.safeExecute(mockCmd);
              assertTrue(response.isEmpty());
            });
  }
}
