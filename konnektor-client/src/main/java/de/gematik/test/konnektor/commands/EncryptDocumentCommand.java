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

package de.gematik.test.konnektor.commands;

import de.gematik.test.cardterminal.*;
import de.gematik.test.konnektor.soap.*;
import de.gematik.test.smartcard.*;
import de.gematik.ws.conn.connectorcommon.v5.*;
import de.gematik.ws.conn.connectorcommon.v5.DocumentType;
import de.gematik.ws.conn.connectorcontext.v2.*;
import de.gematik.ws.conn.encryptionservice.v6.*;
import de.gematik.ws.conn.encryptionservice.v6.EncryptDocument.*;
import javax.xml.ws.*;
import lombok.*;
import oasis.names.tc.dss._1_0.core.schema.*;

public class EncryptDocumentCommand extends AbstractKonnektorCommand<byte[]> {

  private final CardInfo cardInfo;
  private final byte[] payload;
  private final Crypto algorithm;

  public EncryptDocumentCommand(CardInfo cardInfo, byte[] payload, Crypto algorithm) {
    this.cardInfo = cardInfo;
    this.payload = payload;
    this.algorithm = algorithm;
  }

  public EncryptDocumentCommand(CardInfo cardInfo, byte[] payload) {
    this(cardInfo, payload, Crypto.RSA_2048);
  }

  @Override
  public byte[] execute(ContextType ctx, ServicePortProvider serviceProvider) {
    val servicePort = serviceProvider.getEncryptionServicePortType();

    val recipientKeys = new RecipientKeys();
    val keyOnCardType = new KeyOnCardType();
    keyOnCardType.setCardHandle(cardInfo.getHandle());
    keyOnCardType.setCrypt(algorithm.getAlgorithm());
    recipientKeys.setCertificateOnCard(keyOnCardType);

    val base64Data = new Base64Data();
    base64Data.setValue(payload);
    val document = new Holder<>(new DocumentType());
    document.value.setBase64Data(base64Data);
    val optionalInputs = new OptionalInputs();
    val status = new Holder<Status>();
    val optionalOutputs = new Holder<>();
    this.executeAction(
        () ->
            servicePort.encryptDocument(
                ctx, recipientKeys, document, optionalInputs, status, optionalOutputs));
    return document.value.getBase64Data().getValue();
  }
}
