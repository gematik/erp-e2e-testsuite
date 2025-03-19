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

package de.gematik.test.konnektor.soap.mock;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.crypto.encryption.cms.CmsAuthEnvelopedData;
import de.gematik.bbriccs.smartcards.InstituteSmartcard;
import de.gematik.ws.conn.connectorcommon.v5.DocumentType;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.encryptionservice.v6.EncryptDocument.OptionalInputs;
import de.gematik.ws.conn.encryptionservice.v6.EncryptDocument.RecipientKeys;
import de.gematik.ws.conn.encryptionservice.v6.KeyOnCardType;
import de.gematik.ws.conn.encryptionservice.wsdl.v6.EncryptionServicePortType;
import jakarta.xml.ws.Holder;
import java.util.List;
import lombok.SneakyThrows;
import lombok.val;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;

public class MockEncryptionPortType extends AbstractMockService
    implements EncryptionServicePortType {
  public MockEncryptionPortType(MockKonnektor mockKonnektor) {
    super(mockKonnektor);
  }

  @SneakyThrows
  @Override
  public void encryptDocument(
      ContextType context,
      RecipientKeys recipientKeys,
      Holder<DocumentType> document,
      OptionalInputs optionalInputs,
      Holder<Status> status,
      Holder<Object> optionalOutputs) {

    val cardHandle = recipientKeys.getCertificateOnCard().getCardHandle();
    val scw = getSmartcardWrapper(cardHandle);
    val smartcard = (InstituteSmartcard) scw.getSmartcard();

    val plain = document.value.getBase64Data().getValue();

    // TODO currently the Soft-Konn only supports RSA for encryption/decryption..this needs to be
    // fixed
    val algorithm = CryptoSystem.RSA_2048;

    val cmsAuthEnvelopedData = new CmsAuthEnvelopedData();
    val decrypted =
        cmsAuthEnvelopedData.encrypt(
            List.of(smartcard.getEncCertificate(algorithm).getX509Certificate()), plain);
    val base64Data = new Base64Data();
    base64Data.setValue(decrypted);
    document.value.setBase64Data(base64Data);
  }

  @SneakyThrows
  @Override
  public void decryptDocument(
      ContextType context,
      KeyOnCardType privateKeyOnCard,
      Holder<DocumentType> document,
      Object optionalInputs,
      Holder<Status> status,
      Holder<Object> optionalOutputs) {

    val cardHandle = privateKeyOnCard.getCardHandle();
    val scw = getSmartcardWrapper(cardHandle);
    val smartcard = (InstituteSmartcard) scw.getSmartcard();

    val encrypted = document.value.getBase64Data().getValue();

    // TODO currently the Soft-Konn only supports RSA for encryption/decryption..this needs to be
    // fixed
    val algorithm = CryptoSystem.RSA_2048;

    val cmsAuthEnvelopedData = new CmsAuthEnvelopedData();
    val decrypted =
        cmsAuthEnvelopedData.decrypt(
            smartcard.getEncCertificate(algorithm).getPrivateKey(), encrypted);
    val base64Data = new Base64Data();
    base64Data.setValue(decrypted);
    document.value.setBase64Data(base64Data);
  }

  private SmartcardWrapper getSmartcardWrapper(String cardHandle)
      throws de.gematik.ws.conn.authsignatureservice.wsdl.v7_4.FaultMessage {
    return mockKonnektor
        .getSmartcardWrapperByCardHandle(cardHandle)
        .orElseThrow(
            () ->
                new de.gematik.ws.conn.authsignatureservice.wsdl.v7_4.FaultMessage(
                    format("No card found with CardHandle {0}", cardHandle),
                    mockKonnektor.createError(cardHandle)));
  }
}
