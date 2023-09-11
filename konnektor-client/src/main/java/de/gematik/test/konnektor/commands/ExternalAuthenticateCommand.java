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

import static java.text.MessageFormat.*;

import de.gematik.test.cardterminal.*;
import de.gematik.test.konnektor.soap.*;
import de.gematik.test.smartcard.*;
import de.gematik.ws.conn.connectorcommon.v5.*;
import de.gematik.ws.conn.connectorcontext.v2.*;
import de.gematik.ws.conn.signatureservice.v7_4.*;
import de.gematik.ws.conn.signatureservice.v7_4.ExternalAuthenticate.*;
import javax.xml.ws.*;
import lombok.*;
import lombok.extern.slf4j.*;
import oasis.names.tc.dss._1_0.core.schema.*;
import org.apache.commons.codec.digest.*;

@Slf4j
public class ExternalAuthenticateCommand extends AbstractKonnektorCommand<byte[]> {

  private final CardInfo cardInfo;
  private final Crypto algorithm;
  private byte[] toBeSignedData;

  public ExternalAuthenticateCommand(CardInfo cardInfo) {
    this(cardInfo, Crypto.RSA_PSS_2048);
  }

  public ExternalAuthenticateCommand(CardInfo cardInfo, Crypto algorithm) {
    this.cardInfo = cardInfo;
    this.algorithm = algorithm;
  }

  public void setToBeSignedData(byte[] toBeSignedData) {
    this.toBeSignedData = toBeSignedData;
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

    if (this.algorithm == Crypto.RSA_2048 || this.algorithm == Crypto.RSA_PSS_2048) {
      optionalInputs.setSignatureType("urn:ietf:rfc:3447");
      optionalInputs.setSignatureSchemes(Crypto.RSA_PSS_2048.getAlgorithm());
    } else if (algorithm == Crypto.ECC_256) {
      optionalInputs.setSignatureType("urn:bsi:tr:03111:ecdsa");
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
