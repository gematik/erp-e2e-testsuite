/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.konnektor.commands;

import static java.text.MessageFormat.format;

import de.gematik.test.cardterminal.CardInfo;
import de.gematik.test.konnektor.exceptions.SOAPRequestException;
import de.gematik.test.konnektor.soap.ServicePortProvider;
import de.gematik.test.smartcard.Algorithm;
import de.gematik.ws.conn.certificateservice.v6.CryptType;
import de.gematik.ws.conn.certificateservice.v6.ObjectFactory;
import de.gematik.ws.conn.certificateservicecommon.v2.CertRefEnum;
import de.gematik.ws.conn.certificateservicecommon.v2.X509DataInfoListType;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.xml.ws.Holder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

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
  public ReadCardCertificateCommand(CardInfo cardInfo, Algorithm algorithm) {
    this(cardInfo, CertRefEnum.C_AUT, algorithm);
  }

  public ReadCardCertificateCommand(CardInfo cardInfo, CertRefEnum certRef, Algorithm algorithm) {
    this(cardInfo, certRef, getCryptoType(algorithm));
  }

  private static CryptType getCryptoType(Algorithm algorithm) {
    return switch (algorithm) {
      case RSA_2048, RSA_PSS_2048 -> CryptType.RSA;
      default -> CryptType.ECC;
    };
  }

  private ReadCardCertificateCommand(CardInfo cardInfo, CertRefEnum certRef, CryptType cryptType) {
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
