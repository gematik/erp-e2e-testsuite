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

package de.gematik.test.konnektor.soap.mock;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.crypto.signature.EcdsaSigner;
import de.gematik.test.erezept.crypto.signature.RsaPssSigner;
import de.gematik.ws.conn.authsignatureservice.wsdl.v7_4.AuthSignatureServicePortType;
import de.gematik.ws.conn.authsignatureservice.wsdl.v7_4.FaultMessage;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.v7_4.BinaryDocumentType;
import de.gematik.ws.conn.signatureservice.v7_4.ExternalAuthenticate.OptionalInputs;
import java.security.interfaces.RSAPrivateKey;
import javax.xml.ws.Holder;
import lombok.val;
import oasis.names.tc.dss._1_0.core.schema.Base64Signature;
import oasis.names.tc.dss._1_0.core.schema.SignatureObject;

public class MockAuthSignatureServicePortType extends AbstractMockService
    implements AuthSignatureServicePortType {

  public MockAuthSignatureServicePortType(MockKonnektor mockKonnektor) {
    super(mockKonnektor);
  }

  @Override
  public void externalAuthenticate(
      String cardHandle,
      ContextType context,
      OptionalInputs optionalInputs,
      BinaryDocumentType binaryString,
      Holder<Status> status,
      Holder<SignatureObject> signatureObject)
      throws FaultMessage {

    val scw =
        mockKonnektor
            .getSmartcardWrapperByCardHandle(cardHandle)
            .orElseThrow(
                () ->
                    new FaultMessage(
                        format("No card found with CardHandle {0}", cardHandle),
                        mockKonnektor.createError(cardHandle)));

    val toBeSignedData = binaryString.getBase64Data().getValue();

    val signatureMethod =
        scw.getSmartcard().getAuthPrivateKey() instanceof RSAPrivateKey
            ? RsaPssSigner.sha256withMgf1()
            : new EcdsaSigner();

    val signedData = signatureMethod.sign(scw.getSmartcard().getAuthPrivateKey(), toBeSignedData);

    val base64Signature = new Base64Signature();
    base64Signature.setValue(signedData);

    signatureObject.value = new SignatureObject();
    signatureObject.value.setBase64Signature(base64Signature);

    status.value = new Status();
    status.value.setResult("OK");
  }
}
