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
import eu.europa.esig.dss.enumerations.CertificateStatus;
import eu.europa.esig.dss.enumerations.Indication;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.client.http.NativeHTTPDataLoader;
import eu.europa.esig.dss.spi.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.spi.x509.revocation.ocsp.OCSPToken;
import eu.europa.esig.dss.validation.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class LocalVerifier {

  private final SignedDocumentValidator documentValidator;

  private LocalVerifier(byte[] input) {
    val trustedCertSource = new CommonTrustedCertificateSource();
    for (BNetzAVLCa ca : BNetzAVLCa.values()) {
      trustedCertSource.addCertificate(new CertificateToken(ca.getCertificate()));
    }

    val httpDataLoader = new NativeHTTPDataLoader();
    httpDataLoader.setReadTimeout(5000);
    httpDataLoader.setConnectTimeout(5000);

    val ocspSource = new OnlineOCSPSource();
    ocspSource.setDataLoader(httpDataLoader);

    val cv = new CommonCertificateVerifier();
    cv.setOcspSource(ocspSource);
    cv.setTrustedCertSources(trustedCertSource);
    documentValidator = SignedDocumentValidator.fromDocument(new InMemoryDocument(input));
    documentValidator.setCertificateVerifier(cv);
  }

  public static LocalVerifier parse(byte[] input) {
    return new LocalVerifier(input);
  }

  public static boolean verify(byte[] input) {
    return LocalVerifier.parse(input).verify();
  }

  private static byte[] asByteArray(DSSDocument document) {
    try (InputStream inputStream = document.openStream()) {
      return inputStream.readAllBytes();
    } catch (IOException e) {
      throw new IllegalArgumentException("Failed to read document bytes", e);
    }
  }

  public List<byte[]> getAllDocuments() {
    return documentValidator.getSignatures().stream()
        .map(documentValidator::getOriginalDocuments)
        .flatMap(Collection::stream)
        .map(LocalVerifier::asByteArray)
        .toList();
  }

  public byte[] getFirstDocument() {
    return getAllDocuments().stream()
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("No document found"));
  }

  public String getDocument() {
    return new String(getFirstDocument(), StandardCharsets.UTF_8);
  }

  public boolean verify() {
    var isCompletelyValid = true;
    try {
      val reports = documentValidator.validateDocument();
      val report = reports.getSimpleReport();
      val signatures = documentValidator.getSignatures();

      for (val signature : signatures) {
        // There are some certificates (e.g. RSA/ECC QES from  adelheid ulmenwald) with OCSP status
        // unknown
        // For this, the validation policy needs to be adjusted, or it needs to be checked why the
        // status is unknown
        val signatureIsValid =
            report.isValid(signature.getId())
                || report.getIndication(signature.getId()) == Indication.INDETERMINATE;
        isCompletelyValid &= signatureIsValid;

        val ocspToken = getOcspToken(signature);
        if (ocspToken.isPresent()) {
          isCompletelyValid &=
              verifyOcspToken(ocspToken.get(), signature.getSigningCertificateToken());
        }

        log.info(
            "CAdES signature signed by {} is {}valid",
            report.getSignedBy(signature.getId()),
            !signatureIsValid ? "in" : "");
      }
    } catch (Throwable t) {
      isCompletelyValid = false;
      log.warn("Failed to verify with a certificate exception", t);
    }

    return isCompletelyValid;
  }

  public List<OCSPToken> getOcspTokens() {
    return documentValidator.getSignatures().stream()
        .map(this::getOcspToken)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  private Optional<OCSPToken> getOcspToken(AdvancedSignature signature) {
    val signingCertToken = signature.getSigningCertificateToken();
    val caToken = new CertificateToken(BNetzAVLCa.getCA(signingCertToken.getCertificate()));

    val httpDataLoader = new NativeHTTPDataLoader();
    httpDataLoader.setConnectTimeout(5000);
    httpDataLoader.setReadTimeout(5000);

    val revocationToken = signature.getOCSPSource().getRevocationToken(signingCertToken, caToken);
    return revocationToken == null ? Optional.empty() : Optional.of((OCSPToken) revocationToken);
  }

  private boolean verifyOcspToken(OCSPToken ocspToken, CertificateToken signingCertToken) {
    log.info(
        "Ocsp Status for signing certificate with {} is {}",
        signingCertToken.getSubject().getCanonical(),
        ocspToken.getStatus());
    if (ocspToken.getStatus() == CertificateStatus.REVOKED) {
      log.warn(
          "The signing certificate with {} has been revoked!",
          signingCertToken.getSubject().getCanonical());
      return false;
    }
    if (!ocspToken.isValid()) {
      log.warn(
          "Ocsp Signature for signing certificate with {} is not valid!",
          signingCertToken.getSubject().getCanonical());
      return false;
    }
    return true;
  }
}
