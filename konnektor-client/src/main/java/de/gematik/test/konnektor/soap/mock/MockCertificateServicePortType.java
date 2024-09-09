/*
 * Copyright 2024 gematik GmbH
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

import static java.text.MessageFormat.*;

import de.gematik.test.konnektor.exceptions.*;
import de.gematik.ws.conn.certificateservice.v6.*;
import de.gematik.ws.conn.certificateservice.wsdl.v6.*;
import de.gematik.ws.conn.certificateservicecommon.v2.*;
import de.gematik.ws.conn.connectorcommon.v5.*;
import de.gematik.ws.conn.connectorcontext.v2.*;
import jakarta.xml.ws.*;
import java.security.cert.*;
import java.util.*;
import javax.xml.datatype.*;
import lombok.*;
import org.apache.commons.lang3.*;

public class MockCertificateServicePortType extends AbstractMockService
    implements CertificateServicePortType {

  public MockCertificateServicePortType(MockKonnektor mockKonnektor) {
    super(mockKonnektor);
  }

  @Override
  public void checkCertificateExpiration(
      String cardHandle,
      ContextType context,
      CryptType crypt,
      Holder<Status> status,
      Holder<List<CertificateExpirationType>> certificateExpiration) {
    throw new NotImplementedException("Check Certificate Expiration not implemented yet");
  }

  @Override
  public void readCardCertificate(
      String cardHandle,
      ContextType context,
      ReadCardCertificate.CertRefList certRefList,
      CryptType crypt,
      Holder<Status> status,
      Holder<X509DataInfoListType> x509DataInfoList)
      throws FaultMessage {
    val scw =
        mockKonnektor
            .getSmartcardWrapperByCardHandle(cardHandle)
            .orElseThrow(
                () ->
                    new FaultMessage(
                        format("No card found with CardHandle {0}", cardHandle),
                        mockKonnektor.createError(cardHandle)));

    status.value = new Status();
    x509DataInfoList.value = new X509DataInfoListType();
    setX509DataInfoListType(scw, certRefList, status, x509DataInfoList);
  }

  private void setX509DataInfoListType(
      SmartcardWrapper scw,
      ReadCardCertificate.CertRefList certRefList,
      Holder<Status> status,
      Holder<X509DataInfoListType> x509DataInfoList) {
    X509DataInfoListType x509 = new X509DataInfoListType();

    String errorMessage = null;
    for (val cre : certRefList.getCertRef()) {
      val dataInfo = new X509DataInfoListType.X509DataInfo();
      if (cre == CertRefEnum.C_AUT) {
        dataInfo.setX509Data(getX509AuthData(scw));
        dataInfo.setCertRef(cre);
        x509.getX509DataInfo().add(dataInfo);
      } else {
        errorMessage = format("{0} is not yet implemented", cre);
      }

      if (errorMessage == null) {
        status.value.setResult("OK");
        x509DataInfoList.value = x509;
      } else {
        val error = mockKonnektor.createError(errorMessage);
        status.value.setError(error);
      }
    }
  }

  private X509DataInfoListType.X509DataInfo.X509Data getX509AuthData(SmartcardWrapper scw) {
    val data = new X509DataInfoListType.X509DataInfo.X509Data();
    try {
      data.setX509Certificate(
          scw.getSmartcard().getAutCertificate().getX509Certificate().getEncoded());
    } catch (CertificateEncodingException e) {
      throw new SmartcardException(
          format("Smartcard {0} does not have Auth Certificate", scw.getSmartcard().getIccsn()), e);
    }
    return data;
  }

  @Override
  public void verifyCertificate(
      ContextType context,
      byte[] x509Certificate,
      XMLGregorianCalendar verificationTime,
      Holder<Status> status,
      Holder<VerifyCertificateResponse.VerificationStatus> verificationStatus,
      Holder<VerifyCertificateResponse.RoleList> roleList) {
    throw new NotImplementedException("Verify Certificate not implemented yet");
  }
}
