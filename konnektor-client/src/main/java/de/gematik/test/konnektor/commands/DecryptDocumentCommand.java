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

import de.gematik.test.konnektor.CardHandle;
import de.gematik.test.konnektor.soap.ServicePortProvider;
import de.gematik.ws.conn.connectorcommon.v5.DocumentType;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.encryptionservice.v6.KeyOnCardType;
import javax.xml.ws.Holder;
import lombok.AllArgsConstructor;
import lombok.val;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;

@AllArgsConstructor
public class DecryptDocumentCommand extends AbstractKonnektorCommand<byte[]> {

  private final CardHandle cardHandle;
  private byte[] encryptedData;

  @Override
  public byte[] execute(ContextType ctx, ServicePortProvider serviceProvider) {
    val servicePort = serviceProvider.getEncryptionServicePortType();

    val keyOnCardType = new KeyOnCardType();
    keyOnCardType.setCardHandle(cardHandle.getHandle());

    val base64Data = new Base64Data();
    base64Data.setValue(encryptedData);

    val documentType = new DocumentType();
    documentType.setBase64Data(base64Data);

    val documentTypeHolder = new Holder<DocumentType>();
    documentTypeHolder.value = documentType;

    val statusHolder = new Holder<Status>();
    this.executeAction(
        () ->
            servicePort.decryptDocument(
                ctx, keyOnCardType, documentTypeHolder, null, statusHolder, null));
    return documentTypeHolder.value.getBase64Data().getValue();
  }
}
