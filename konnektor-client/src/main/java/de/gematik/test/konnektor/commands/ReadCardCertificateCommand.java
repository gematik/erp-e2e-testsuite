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
import de.gematik.test.konnektor.exceptions.*;
import de.gematik.test.konnektor.soap.*;
import de.gematik.ws.conn.certificateservice.v6.*;
import de.gematik.ws.conn.certificateservice.v6.ObjectFactory;
import de.gematik.ws.conn.certificateservicecommon.v2.*;
import de.gematik.ws.conn.connectorcommon.v5.*;
import de.gematik.ws.conn.connectorcontext.v2.*;
import java.io.*;
import java.security.cert.*;
import javax.xml.ws.*;
import lombok.*;
import lombok.extern.slf4j.*;

@Slf4j
public class ReadCardCertificateCommand extends AbstractKonnektorCommand<X509Certificate> {

  private final CardInfo cardInfo;
  private final CertRefEnum certRef;
  private final CryptType cryptType;

  /**
   * Reads the C_AUT certificate from given card
   *
   * @param cardInfo identifies the card on the Konnektor
   */
  public ReadCardCertificateCommand(CardInfo cardInfo) {
    this(cardInfo, CertRefEnum.C_AUT);
  }

  public ReadCardCertificateCommand(CardInfo cardInfo, CertRefEnum certRef) {
    this(cardInfo, certRef, CryptType.RSA);
  }

  public ReadCardCertificateCommand(CardInfo cardInfo, CertRefEnum certRef, CryptType cryptType) {
    this.cardInfo = cardInfo;
    this.certRef = certRef;
    this.cryptType = cryptType;
  }

  @Override
  @SneakyThrows
  public X509Certificate execute(ContextType ctx, ServicePortProvider serviceProvider) {
    log.trace(format("Read {0} Certificate from Card {1}", certRef, cardInfo));
    val servicePort = serviceProvider.getCertificateService();
    val factory = new ObjectFactory();

    val certRefList = factory.createReadCardCertificateCertRefList();
    certRefList.getCertRef().add(certRef);

    val outStatus = new Holder<Status>();
    val outX509DataInfoList = new Holder<X509DataInfoListType>();

    this.executeAction(
        () ->
            servicePort.readCardCertificate(
                cardInfo.getHandle(), ctx, certRefList, cryptType, outStatus, outX509DataInfoList));

    val x509Bytes =
        outX509DataInfoList.value.getX509DataInfo().stream()
            .filter(di -> di.getCertRef().equals(certRef))
            .map(di -> di.getX509Data().getX509Certificate())
            .findFirst()
            .orElseThrow(
                () ->
                    new SOAPRequestException(
                        this.getClass(),
                        format(
                            "Response does not contain any {0} certificate for {1}",
                            certRef, cardInfo)));

    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
    return (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(x509Bytes));
  }
}
