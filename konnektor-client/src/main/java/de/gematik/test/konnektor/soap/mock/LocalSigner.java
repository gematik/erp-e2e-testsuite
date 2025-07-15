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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.smartcards.Hba;
import de.gematik.bbriccs.smartcards.SmartcardCertificate;
import de.gematik.bbriccs.smartcards.SmcB;
import de.gematik.test.konnektor.soap.mock.utils.BNetzAVLCa;
import eu.europa.esig.dss.cades.CAdESSignatureParameters;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.cades.signature.CMSSignedDocument;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.EncryptionAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.x509.CMSSignedDataBuilder;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.util.encoders.Base64;

@Slf4j
public class LocalSigner {

  private final SmartcardCertificate signingKey;

  private final CAdESService cades;

  private LocalSigner(SmartcardCertificate signingKey) {
    this.signingKey = signingKey;
    this.cades = new CAdESService(new CommonCertificateVerifier());
  }

  public byte[] signDocument(boolean isIncludeRevocationInfo, String data) {
    return signDocument(isIncludeRevocationInfo, data.getBytes(StandardCharsets.UTF_8));
  }

  @SneakyThrows
  public byte[] signDocument(boolean isIncludeRevocationInfo, byte[] data) {
    val signingDate = new Date();
    val mimeType = MimeTypeEnum.XML; // Note: only XML for now!
    log.info("Sign {} with {} Bytes at {}", mimeType.getMimeTypeString(), data.length, signingDate);
    log.debug("To be signed data as Base64:\n{}", Base64.toBase64String(data));
    val inMemDocument = new InMemoryDocument(data);
    inMemDocument.setMimeType(mimeType);

    try (val signingToken =
        new Pkcs12SignatureToken(
            signingKey.getCertificateStream().get(), signingKey.getP12KeyStoreProtection())) {
      val privateKeyEntry = signingToken.getKeys().get(0);

      val signParams = getCAdESSignatureParameters(signingDate, privateKeyEntry);
      val dataToSign = cades.getDataToSign(inMemDocument, signParams);

      val signatureValue =
          signingToken.sign(dataToSign, signParams.getDigestAlgorithm(), privateKeyEntry);

      log.info("Sign XML with {}", signatureValue);
      val signedDocument =
          (CMSSignedDocument) cades.signDocument(inMemDocument, signParams, signatureValue);
      if (isIncludeRevocationInfo) {
        val signingCert = privateKeyEntry.getCertificate();

        // this works only for QES Certs and not for nonQES Certs
        val bNetzAVL = BNetzAVLCa.getBy(signingCert.getCertificate());
        val ocspToken =
            bNetzAVL
                .map(
                    it -> {
                      val ocspSource = new OnlineOCSPSource();
                      return ocspSource.getRevocationToken(
                          signingCert, new CertificateToken(bNetzAVL.get().getCertificate()));
                    })
                .orElseThrow(
                    () ->
                        new CertificateException(
                            format(
                                "OcspResp could not be determined for the signing certificate {0}",
                                signingCert.getCertificate().getSubjectX500Principal().getName())));

        val cmsSignedDataBuilder = new CMSSignedDataBuilder();
        cmsSignedDataBuilder.setOriginalCMSSignedData(signedDocument.getCMSSignedData());
        val cms =
            cmsSignedDataBuilder.extendCMSSignedData(
                Collections.emptyList(), Collections.emptyList(), List.of(ocspToken));
        return cms.getEncoded();
      } else {
        return signedDocument.getCMSSignedData().getEncoded();
      }
    }
  }

  private CAdESSignatureParameters getCAdESSignatureParameters(
      final Date signingDate, DSSPrivateKeyEntry privateKeyEntry) {
    var params = new CAdESSignatureParameters();
    params.bLevel().setSigningDate(signingDate);
    params.setEncryptionAlgorithm(getEncryptionAlgorithm());
    params.setSignatureLevel(SignatureLevel.CAdES_BASELINE_B);
    params.setSignaturePackaging(SignaturePackaging.ENVELOPING);
    params.setDigestAlgorithm(DigestAlgorithm.SHA256);
    params.setSigningCertificate(privateKeyEntry.getCertificate());
    params.setCertificateChain(privateKeyEntry.getCertificateChain());
    params.setContentHintsType(CMSAttributes.contentHint.getId());
    params.setContentHintsDescription("CMSDocument2sign");
    return params;
  }

  private EncryptionAlgorithm getEncryptionAlgorithm() {
    val ret =
        switch (this.signingKey.getCryptoSystem()) {
          case RSA_2048, RSA_PSS_2048 -> EncryptionAlgorithm.RSA;
          case ECC_256 -> EncryptionAlgorithm.ECDSA;
        };

    log.trace(
        "Encryption Algorithm for signing from {} to {}", this.signingKey.getCryptoSystem(), ret);
    return ret;
  }

  public static LocalSigner signQES(Hba hba, CryptoSystem algorithm) {
    return new LocalSigner(hba.getQesCertificate(algorithm));
  }

  public static LocalSigner signNonQES(SmcB smcb, CryptoSystem algorithm) {
    return new LocalSigner(smcb.getOSigCertificate(algorithm));
  }
}
