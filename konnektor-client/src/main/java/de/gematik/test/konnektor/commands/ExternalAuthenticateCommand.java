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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.konnektor.commands;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.test.cardterminal.CardInfo;
import de.gematik.test.konnektor.commands.options.SignatureType;
import de.gematik.test.konnektor.soap.ServicePortProvider;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.v7_4.BinaryDocumentType;
import de.gematik.ws.conn.signatureservice.v7_4.ExternalAuthenticate.OptionalInputs;
import jakarta.xml.ws.Holder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;
import oasis.names.tc.dss._1_0.core.schema.SignatureObject;
import org.apache.commons.codec.digest.DigestUtils;

@Slf4j
public class ExternalAuthenticateCommand extends AbstractKonnektorCommand<byte[]> {

  private final CardInfo cardInfo;
  private final CryptoSystem algorithm;
  @Setter private byte[] toBeSignedData;

  public ExternalAuthenticateCommand(CardInfo cardInfo, CryptoSystem algorithm) {
    this.cardInfo = cardInfo;
    this.algorithm = algorithm;
  }

  @Override
  public byte[] execute(ContextType ctx, ServicePortProvider serviceProvider) {
    log.trace(
        format(
            "External Authenticate with CardHandle {0} and challenge of length {1}",
            cardInfo.getHandle(), toBeSignedData.length));
    val servicePort = serviceProvider.getAuthSignatureService();

    val binaryDocument = new BinaryDocumentType();

    val base64Data = new Base64Data();
    base64Data.setValue(DigestUtils.sha256(toBeSignedData));
    base64Data.setMimeType("application/octet-stream");
    binaryDocument.setBase64Data(base64Data);

    val optionalInputs = new OptionalInputs();

    if (this.algorithm == CryptoSystem.RSA_2048 || this.algorithm == CryptoSystem.RSA_PSS_2048) {
      optionalInputs.setSignatureType(SignatureType.RFC_3447.getUrn());
      optionalInputs.setSignatureSchemes(CryptoSystem.RSA_PSS_2048.getName());
    } else if (algorithm == CryptoSystem.ECC_256) {
      optionalInputs.setSignatureType(SignatureType.BSI_TR_03111.getUrn());
    }

    val outStatus = new Holder<Status>();
    val signatureObject = new Holder<SignatureObject>();

    this.executeAction(
        () ->
            servicePort.externalAuthenticate(
                cardInfo.getHandle(),
                ctx,
                optionalInputs,
                binaryDocument,
                outStatus,
                signatureObject));

    return signatureObject.value.getBase64Signature().getValue();
  }
}
