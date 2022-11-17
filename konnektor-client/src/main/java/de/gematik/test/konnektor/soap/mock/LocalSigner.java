/*
 * Copyright (c) 2022 gematik GmbH
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

import static java.text.MessageFormat.format;

import de.gematik.test.smartcard.Hba;
import eu.europa.esig.dss.cades.CAdESSignatureParameters;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.EncryptionAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.MimeType;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.util.encoders.Base64;

@Slf4j
public class LocalSigner {

  private final Hba smartcard;

  private final CommonCertificateVerifier certVerifier;
  private final CAdESService cades;

  public LocalSigner(Hba smartcard) {
    this.smartcard = smartcard;
    this.certVerifier = new CommonCertificateVerifier();
    this.cades = new CAdESService(certVerifier);
  }

  public byte[] signQES(String data) {
    return signQES(data.getBytes(StandardCharsets.UTF_8));
  }

  @SneakyThrows
  public byte[] signQES(byte[] data) {
    val signingDate = new Date();
    val mimeType = MimeType.XML;
    log.info(
        format(
            "Sign {0} with {1} Bytes at {2}",
            mimeType.getMimeTypeString(), data.length, signingDate));
    log.debug(format("Signed Base64 Data:\n{0}", Base64.toBase64String(data)));
    val inMemDocument = new InMemoryDocument(data);
    inMemDocument.setMimeType(mimeType); // Note: only XML for now!

    log.info(format("Use {0} for signing XML Document", smartcard));
    try (val signingToken =
        new Pkcs12SignatureToken(
            smartcard.getQesP12File(),
            new KeyStore.PasswordProtection(smartcard.getQesP12Password().toCharArray()))) {
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

  public CertificateToken getQesCertificateToken() {
    return new CertificateToken(smartcard.getQesCertificate());
  }

  public List<CertificateToken> getQesCertificateChain() {
    return Arrays.stream(smartcard.getQesCertificateChain())
        .map(c -> new CertificateToken((X509Certificate) c))
        .toList();
  }

  private EncryptionAlgorithm getEncryptionAlgorithm() {
    val ret =
        switch (smartcard.getAlgorithm()) {
          case RSA_2048 -> EncryptionAlgorithm.RSA;
          case ECC_256 -> EncryptionAlgorithm.ECDSA;
          default -> throw new AssertionError(
              format(
                  "Smartcard has Algorithm {0} which is not mapped to "
                      + "eu.europa.esig.dss.EncryptionAlgorithm",
                  smartcard.getAlgorithm()));
        };

    log.trace(
        format("Encryption Algorithm for signing from {0} to {1}", smartcard.getAlgorithm(), ret));
    return ret;
  }
}
