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

package de.gematik.test.erezept.actions.rawhttpactions.pki;

import de.gematik.pki.gemlibpki.tsl.TslInformationProvider;
import de.gematik.test.erezept.actions.rawhttpactions.pki.dto.TslExtension;
import eu.europa.esig.trustedlist.jaxb.tsl.TrustStatusListType;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;
import lombok.SneakyThrows;
import lombok.val;

public class TslListWrapper {
  private final TrustStatusListType trustStatusListType;
  private final TslInformationProvider tslInformationProvider;

  public TslListWrapper(TrustStatusListType trustStatusListType) {
    this.trustStatusListType = trustStatusListType;
    this.tslInformationProvider = new TslInformationProvider(this.trustStatusListType);
  }

  @SneakyThrows
  private static X509Certificate getX509Cert(byte[] certPem) {
    val certificateFactory = CertificateFactory.getInstance("X.509");
    return (X509Certificate)
        certificateFactory.generateCertificate(new ByteArrayInputStream(certPem));
  }

  public TslInformationProvider getProvider() {
    return this.tslInformationProvider;
  }

  public Set<X509Certificate> getFilteredForFDSicAndEncX509Certificates() {
    val extOidFdEnc = new TslExtension("1.2.276.0.76.4.202", "oid_fd_enc");
    val extOidFdSig = new TslExtension("1.2.276.0.76.4.203", "oid_fd_sig");
    return getFilteredCaList(extOidFdEnc, extOidFdSig);
  }

  public Set<X509Certificate> getFilteredCaList(
      TslExtension extOidFdEnc, TslExtension extOidFdSig) {
    return getFilteredCaList(this.getProvider(), extOidFdEnc, extOidFdSig);
  }

  private Set<X509Certificate> getFilteredCaList(
      TslInformationProvider tslIP, TslExtension extOidFdEnc, TslExtension extOidFdSig) {

    val tspServiceList = tslIP.getTspServices();
    val erpTspServices =
        tspServiceList.stream()
            .filter(
                it -> {
                  val si = it.getTspServiceType().getServiceInformation();
                  val identifier = si.getServiceTypeIdentifier();
                  val extensions = TslExtension.toExtensions(si);
                  return "http://uri.etsi.org/TrstSvc/Svctype/CA/PKC".equals(identifier)
                      && (extensions.contains(extOidFdEnc) || extensions.contains(extOidFdSig));
                })
            .toList();
    val caCertList = new HashSet<X509Certificate>();
    erpTspServices.forEach(
        it ->
            it.getTspServiceType()
                .getServiceInformation()
                .getServiceDigitalIdentity()
                .getDigitalId()
                .forEach(digitalId -> caCertList.add(getX509Cert(digitalId.getX509Certificate()))));
    return caCertList;
  }
}
