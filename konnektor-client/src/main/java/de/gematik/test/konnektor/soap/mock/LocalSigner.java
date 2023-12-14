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

package de.gematik.test.konnektor.soap.mock;

import de.gematik.test.smartcard.Algorithm;
import de.gematik.test.smartcard.Hba;
import de.gematik.test.smartcard.SmartcardCertificate;
import de.gematik.test.smartcard.SmcB;
import eu.europa.esig.dss.cades.CAdESSignatureParameters;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.enumerations.*;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.util.encoders.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Date;

import static java.text.MessageFormat.format;

@Slf4j
public class LocalSigner {

  private final SmartcardCertificate signingKey;

  private final CAdESService cades;

  private LocalSigner(SmartcardCertificate signingKey) {
    this.signingKey = signingKey;
    CommonCertificateVerifier certVerifier = new CommonCertificateVerifier();
    this.cades = new CAdESService(certVerifier);
  }

  public byte[] signDocument(String data) {
    return signDocument(data.getBytes(StandardCharsets.UTF_8));
  }

  @SneakyThrows
  public byte[] signDocument(byte[] data) {
    val signingDate = new Date();
    val mimeType = MimeTypeEnum.XML; // Note: only XML for now!
    log.info(
        format(
            "Sign {0} with {1} Bytes at {2}",
            mimeType.getMimeTypeString(), data.length, signingDate));
    log.debug(format("Signed Base64 Data:\n{0}", Base64.toBase64String(data)));
    val inMemDocument = new InMemoryDocument(data);
    inMemDocument.setMimeType(mimeType);

    try (val signingToken =
        new Pkcs12SignatureToken(
            signingKey.getInputStreamSupplier().get(),
            new KeyStore.PasswordProtection(signingKey.getP12KeyStorePassword().toCharArray()))) {
      val privateKey = signingToken.getKeys().get(0);

      val signParams = getCAdESSignatureParameters(signingDate, privateKey);
      val dataToSign = cades.getDataToSign(inMemDocument, signParams);

      val signatureValue =
          signingToken.sign(dataToSign, signParams.getDigestAlgorithm(), privateKey);

      log.info("Sign XML with " + signatureValue);
      val signedDocument = cades.signDocument(inMemDocument, signParams, signatureValue);
      return IOUtils.toByteArray(signedDocument.openStream());
    }
  }

  private CAdESSignatureParameters getCAdESSignatureParameters(
      final Date signingDate, DSSPrivateKeyEntry pk) {
    var params = new CAdESSignatureParameters();
    params.bLevel().setSigningDate(signingDate);
    params.setEncryptionAlgorithm(getEncryptionAlgorithm());
    params.setSignatureLevel(SignatureLevel.CAdES_BASELINE_B);
    params.setSignaturePackaging(SignaturePackaging.ENVELOPING);
    params.setDigestAlgorithm(DigestAlgorithm.SHA256);
    params.setSigningCertificate(pk.getCertificate());
    params.setCertificateChain(pk.getCertificateChain());
    params.setContentHintsType(CMSAttributes.contentHint.getId());
    params.setContentHintsDescription("CMSDocument2sign");
    return params;
  }

  private EncryptionAlgorithm getEncryptionAlgorithm() {
    val ret =
        switch (this.signingKey.getAlgorithm()) {
          case RSA_2048, RSA_PSS_2048 -> EncryptionAlgorithm.RSA;
          case ECC_256 -> EncryptionAlgorithm.ECDSA;
        };

    log.trace(
        format(
            "Encryption Algorithm for signing from {0} to {1}",
            this.signingKey.getAlgorithm(), ret));
    return ret;
  }

  public static LocalSigner signQES(Hba hba, Algorithm algorithm) {
    return new LocalSigner(hba.getQesCertificate(algorithm));
  }

  public static LocalSigner signNonQES(SmcB smcb, Algorithm algorithm) {
    return new LocalSigner(smcb.getOSigCertificate(algorithm));
  }
}
