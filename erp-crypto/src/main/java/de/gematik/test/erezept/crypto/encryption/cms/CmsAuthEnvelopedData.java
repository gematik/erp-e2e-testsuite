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

package de.gematik.test.erezept.crypto.encryption.cms;

import de.gematik.test.erezept.crypto.*;
import de.gematik.test.erezept.crypto.certificate.*;
import java.security.*;
import java.security.cert.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.spec.*;
import lombok.*;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.cms.*;
import org.bouncycastle.cert.jcajce.*;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.*;
import org.bouncycastle.operator.*;
import org.bouncycastle.operator.jcajce.*;

@AllArgsConstructor
public class CmsAuthEnvelopedData {

  @SneakyThrows
  public byte[] encrypt(List<X509Certificate> recipientCertificates, byte[] plaintext) {

    val certs = recipientCertificates.stream().map(X509CertificateWrapper::new).toList();

    val msg = new CMSProcessableByteArray(plaintext);
    val edGen = new CMSAuthEnvelopedDataGenerator();
    val info = buildRecipientInfo(certs);

    edGen.setUnauthenticatedAttributeGenerator(
        new SimpleAttributeTableGenerator(
            new AttributeTable(
                new Attribute(new ASN1ObjectIdentifier("1.2.276.0.76.4.173"), new DERSet(info)))));

    val jcaConverter = new JcaX509CertificateConverter().setProvider(BC.getSecurityProvider());

    for (X509CertificateWrapper cert : certs) {
      val jcaCert = jcaConverter.getCertificate(cert.toCertificateHolder());
      edGen.addRecipientInfoGenerator(
          new JceKeyTransRecipientInfoGenerator(
                  jcaCert,
                  new JceAsymmetricKeyWrapper(
                      new OAEPParameterSpec(
                          "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT),
                      jcaCert.getPublicKey()))
              .setProvider(BC.getSecurityProvider()));
    }
    val contentEncryptor =
        new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES256_GCM)
            .setProvider(BC.getSecurityProvider())
            .build();
    // encrypt message with AES256 key (symmetric key)
    val ed = edGen.generate(msg, (OutputAEADEncryptor) contentEncryptor);
    return ed.toASN1Structure().getEncoded();
  }

  private ASN1EncodableVector buildRecipientInfo(
      List<X509CertificateWrapper> recipientCertificates) {
    val vector = new ASN1EncodableVector();
    recipientCertificates.forEach(
        recipientCert -> {
          val t = new ASN1EncodableVector();
          t.add(new DERIA5String(recipientCert.getProfessionId().orElseThrow(), true));
          t.add(
              new RecipientIdentifier(
                  new IssuerAndSerialNumber(
                      recipientCert.toCertificateHolder().toASN1Structure())));
          vector.add(new DERSequence(t));
        });
    return vector;
  }

  @SneakyThrows
  public byte[] decrypt(PrivateKey key, byte[] cmsEnvelopedDataAsByte) {
    val cmsEnvelopedData = new CMSAuthEnvelopedData(cmsEnvelopedDataAsByte);
    val recipients = cmsEnvelopedData.getRecipientInfos();
    val recipientInformation = recipients.getRecipients().stream().findFirst();

    return recipientInformation
        .orElseThrow()
        .getContent(new JceKeyTransEnvelopedRecipient(key).setProvider(BC.getSecurityProvider()));
  }
}
