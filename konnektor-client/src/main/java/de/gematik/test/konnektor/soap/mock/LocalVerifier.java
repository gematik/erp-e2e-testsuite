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

import de.gematik.test.konnektor.soap.mock.utils.BNetzAVLCa;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class LocalVerifier {

  public boolean verify(byte[] input) {

    val cv = new CommonCertificateVerifier();
    cv.setOcspSource(new OnlineOCSPSource());
    val trustedCertSource = new CommonTrustedCertificateSource();
    cv.setTrustedCertSources(trustedCertSource);
    SignedDocumentValidator documentValidator;
    try {
      documentValidator = SignedDocumentValidator.fromDocument(new InMemoryDocument(input));
    } catch (java.lang.UnsupportedOperationException e) {
      log.warn("Failed to read Signature or Certificate");
      return false;
    }
    documentValidator.setCertificateVerifier(cv);

    val signature =
        documentValidator.getSignatures().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No signature found"));
    signature.getCertificates().forEach(trustedCertSource::addCertificate);

    val signingCertToken = signature.getSigningCertificateToken();

    // this works only for QES Certs; NonQES Certs have to be handled with TSL
    BNetzAVLCa.getBy(signingCertToken.getCertificate())
        .ifPresent(
            it -> {
              val caToken = new CertificateToken(it.getCertificate());
              val ocsp = signature.getOCSPSource().getRevocationToken(signingCertToken, caToken);
              if (ocsp != null) {
                log.info(
                    "Ocsp Status for signing certificate with {} is {}",
                    signingCertToken.getSubject().getCanonical(),
                    ocsp.getStatus());
              }
            });

    val reports = documentValidator.validateDocument();
    val report = reports.getSimpleReport();

    val isValid = report.isValid(signature.getId());

    log.info(
        "CAdES signature signed by {} is {}valid",
        report.getSignedBy(signature.getId()),
        !isValid ? "in" : "");

    return isValid;
  }
}
