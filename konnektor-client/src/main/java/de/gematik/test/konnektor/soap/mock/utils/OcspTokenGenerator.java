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

package de.gematik.test.konnektor.soap.mock.utils;

import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.x509.revocation.ocsp.OCSPToken;
import java.security.*;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.ocsp.*;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class OcspTokenGenerator {

  private final CertificateToken signingCertToken;
  private final X509Certificate issuerCert;

  public static OcspTokenGenerator with(CertificateToken signingCertToken) {
    return new OcspTokenGenerator(
        signingCertToken, BNetzAVLCa.getCA(signingCertToken.getCertificate()));
  }

  public static OcspTokenGenerator with(X509Certificate signingCert) {
    return with(new CertificateToken(signingCert));
  }

  public OCSPToken asOnlineToken() {
    val ocspSource = new OnlineOCSPSource();
    return ocspSource.getRevocationToken(signingCertToken, new CertificateToken(issuerCert));
  }

  public OCSPToken asSelfSignedToken(ZonedDateTime producedAt, ZonedDateTime thisUpdate) {
    val basicOCSPResp = createBasicOCSPResponse(CertificateStatus.GOOD, producedAt, thisUpdate);
    return new OCSPToken(
        basicOCSPResp,
        basicOCSPResp.getResponses()[0],
        signingCertToken,
        new CertificateToken(issuerCert));
  }

  public OCSPToken asSelfSignedRevokedToken(ZonedDateTime producedAt, ZonedDateTime thisUpdate) {
    val revokedStatus = new RevokedStatus(Date.from(producedAt.toInstant()));
    val basicOCSPResp = createBasicOCSPResponse(revokedStatus, producedAt, thisUpdate);
    return new OCSPToken(
        basicOCSPResp,
        basicOCSPResp.getResponses()[0],
        signingCertToken,
        new CertificateToken(issuerCert));
  }

  @SneakyThrows
  private BasicOCSPResp createBasicOCSPResponse(
      CertificateStatus certificateStatus, ZonedDateTime producedAt, ZonedDateTime certValidFrom) {
    val thisUpdate = Date.from(certValidFrom.toInstant());
    val nextUpdate = Date.from(certValidFrom.toInstant().plus(1, ChronoUnit.DAYS));

    val dc = new JcaDigestCalculatorProviderBuilder().build().get(RespID.HASH_SHA1);
    val issuerHolder = new JcaX509CertificateHolder(issuerCert);
    val certId =
        new CertificateID(dc, issuerHolder, signingCertToken.getCertificate().getSerialNumber());

    val builder =
        new org.bouncycastle.cert.ocsp.jcajce.JcaBasicOCSPRespBuilder(
            signingCertToken.getCertificate().getPublicKey(), dc);
    builder.addResponse(certId, certificateStatus, thisUpdate, nextUpdate, null);
    return builder.build(
        signWithTempKey(), new X509CertificateHolder[0], Date.from(producedAt.toInstant()));
  }

  @SneakyThrows
  private ContentSigner signWithTempKey() {
    val kpg = KeyPairGenerator.getInstance("EC");
    kpg.initialize(256, new SecureRandom());
    val responderKey = kpg.generateKeyPair().getPrivate();
    return new JcaContentSignerBuilder("SHA256withECDSA").build(responderKey);
  }
}
