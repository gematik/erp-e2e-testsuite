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

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.security.Security;
import java.security.cert.CertificateException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedDataParser;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

@Slf4j
public class LocalVerifier {

  static {
    BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
    if (Security.getProvider(bouncyCastleProvider.getName()) == null) {
      Security.addProvider(bouncyCastleProvider);
    }
  }

  public boolean verify(byte[] input) {
    boolean isValid = false;
    try {
      val digestCalculatorProvider =
          new JcaDigestCalculatorProviderBuilder().setProvider("BC").build();
      val parser = new CMSSignedDataParser(digestCalculatorProvider, input);

      val content = parser.getSignedContent();
      content.drain();
      val certStore = parser.getCertificates();

      val signerInfoStore = parser.getSignerInfos();
      val signerInfo = signerInfoStore.getSigners().stream().findFirst().orElseThrow();

      val certCollection = certStore.getMatches(signerInfo.getSID());
      val certIt = certCollection.iterator();
      val certHolder = (X509CertificateHolder) certIt.next();

      isValid =
          signerInfo.verify(
              new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(certHolder));
    } catch (OperatorCreationException | CMSException | IOException e) {
      log.warn(format("Failed to read Signature or Certificate"));
    } catch (CertificateException ce) {
      log.warn("Failed to verify with a certificate exception");
    }

    return isValid;
  }
}
