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

import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.v7.*;
import de.gematik.ws.conn.signatureservice.wsdl.v7.FaultMessage;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortType;
import de.gematik.ws.tel.error.v2.Error;
import java.util.ArrayList;
import java.util.List;
import javax.xml.datatype.Duration;
import javax.xml.ws.Holder;
import lombok.val;
import oasis.names.tc.dss._1_0.core.schema.Base64Signature;
import oasis.names.tc.dss._1_0.core.schema.SignatureObject;
import org.apache.commons.lang3.NotImplementedException;

public class MockSignatureServicePortType extends AbstractMockService
    implements SignatureServicePortType {

  public MockSignatureServicePortType(MockKonnektor mockKonnektor) {
    super(mockKonnektor);
  }

  @Override
  public void verifyDocument(
      ContextType context,
      String tvMode,
      VerifyDocument.OptionalInputs optionalInputs,
      DocumentType document,
      SignatureObject signatureObject,
      boolean includeRevocationInfo,
      Holder<Status> status,
      Holder<VerificationResultType> verificationResult,
      Holder<VerifyDocumentResponse.OptionalOutputs> optionalOutputs)
      throws FaultMessage {

    val data = signatureObject.getBase64Signature().getValue();
    val isValid = mockKonnektor.verifyDocument(data);

    status.value = new Status();
    if (isValid) {
      status.value.setResult("OK");
    } else {
      status.value.setError(new Error());
    }

    verificationResult.value = new VerificationResultType();
    verificationResult.value.setHighLevelResult("valid");
  }

  @Override
  public List<SignResponse> signDocument(
      String cardHandle,
      String crypt,
      ContextType context,
      String tvMode,
      String jobNumber,
      List<SignRequest> signRequest)
      throws FaultMessage {

    val response = new ArrayList<SignResponse>();
    for (val sr : signRequest) {
      val signedData =
          mockKonnektor.signDocumentWith(cardHandle, sr.getDocument().getBase64Data().getValue());

      val signResponse = new SignResponse();
      signResponse.setRequestID(sr.getRequestID());
      val status = new Status();
      status.setResult("OK");
      signResponse.setStatus(status);

      val signatureObject = new SignatureObject();
      val b64Signature = new Base64Signature();
      b64Signature.setValue(signedData);
      signatureObject.setBase64Signature(b64Signature);
      signResponse.setSignatureObject(signatureObject);
      response.add(signResponse);
    }

    return response;
  }

  @Override
  public String getJobNumber(ContextType context) throws FaultMessage {
    return mockKonnektor.getJobNumber(context);
  }

  @Override
  public Status stopSignature(ContextType context, String jobNumber) throws FaultMessage {
    throw new NotImplementedException("Stop Signature not implemented yet");
  }

  @Override
  public void activateComfortSignature(
      String cardHandle,
      ContextType context,
      Holder<Status> status,
      Holder<SignatureModeEnum> signatureMode)
      throws FaultMessage {
    throw new NotImplementedException("Activate ComfortSignature not implemented yet");
  }

  @Override
  public Status deactivateComfortSignature(List<String> cardHandle) throws FaultMessage {
    throw new NotImplementedException("Deactivate ComfortSignature not implemented yet");
  }

  @Override
  public void getSignatureMode(
      String cardHandle,
      ContextType context,
      Holder<Status> status,
      Holder<ComfortSignatureStatusEnum> comfortSignatureStatus,
      Holder<Integer> comfortSignatureMax,
      Holder<Duration> comfortSignatureTimer,
      Holder<SessionInfo> sessionInfo)
      throws FaultMessage {
    throw new NotImplementedException("Get Signature Mode not implemented yet");
  }
}
