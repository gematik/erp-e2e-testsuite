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

package de.gematik.test.konnektor.soap.mock;

import static java.text.MessageFormat.*;

import de.gematik.test.erezept.crypto.signature.*;
import de.gematik.test.smartcard.*;
import de.gematik.test.smartcard.exceptions.*;
import de.gematik.ws.conn.authsignatureservice.wsdl.v7_4.*;
import de.gematik.ws.conn.connectorcommon.v5.*;
import de.gematik.ws.conn.connectorcontext.v2.*;
import de.gematik.ws.conn.signatureservice.v7_4.*;
import de.gematik.ws.conn.signatureservice.v7_4.ExternalAuthenticate.*;
import java.security.interfaces.*;
import javax.xml.ws.*;
import lombok.*;
import oasis.names.tc.dss._1_0.core.schema.*;

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
    val algorithm = Crypto.fromSpecificationUrn(optionalInputs.getSignatureType());

    val autCert =
        scw.getSmartcard()
            .getAutCertificate(algorithm)
            .orElseThrow(
                () ->
                    new SmartCardKeyNotFoundException(
                        scw.getSmartcard(), scw.getSmartcard().getAutOids(), algorithm));

    val signatureMethod =
        autCert.getPrivateKey() instanceof RSAPrivateKey
            ? RsaPssSigner.sha256withMgf1()
            : new EcdsaSigner();

    val signedData = signatureMethod.sign(autCert.getPrivateKey(), toBeSignedData);

    val base64Signature = new Base64Signature();
    base64Signature.setValue(signedData);

    signatureObject.value = new SignatureObject();
    signatureObject.value.setBase64Signature(base64Signature);

    status.value = new Status();
    status.value.setResult("OK");
  }
}
