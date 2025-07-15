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

package de.gematik.test.konnektor.soap.mock;

import de.gematik.test.konnektor.soap.mock.utils.BNetzAVLCa;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.validation.AdvancedSignature;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class LocalVerifier {

  private final SignedDocumentValidator documentValidator;
  private final CommonTrustedCertificateSource trustedCertSource =
      new CommonTrustedCertificateSource();

  private LocalVerifier(byte[] input) {
    val cv = new CommonCertificateVerifier();
    cv.setOcspSource(new OnlineOCSPSource());
    cv.setTrustedCertSources(trustedCertSource);
    documentValidator = SignedDocumentValidator.fromDocument(new InMemoryDocument(input));
    documentValidator.setCertificateVerifier(cv);
  }

  public static LocalVerifier parse(byte[] input) {
    return new LocalVerifier(input);
  }

  public static boolean verify(byte[] input) {
    val localVerifier = new LocalVerifier(input);
    return localVerifier.verify();
  }

  private AdvancedSignature getSignature() {
    val signature =
        documentValidator.getSignatures().stream()
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("No signature found"));
    signature.getCertificates().forEach(trustedCertSource::addCertificate);
    return signature;
  }

  private static String convertToString(DSSDocument document) throws IOException {
    try (InputStream inputStream = document.openStream();
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
  }

  @SneakyThrows
  public String getDocument() {
    val originalDocuments = documentValidator.getOriginalDocuments(getSignature().getId());
    val original =
        originalDocuments.stream()
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("No document found"));
    return convertToString(original);
  }

  public boolean verify() {
    val signature = getSignature();
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
