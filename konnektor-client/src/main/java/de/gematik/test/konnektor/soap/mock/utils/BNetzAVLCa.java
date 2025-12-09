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

import de.gematik.bbriccs.crypto.certificate.X509CertificateWrapper;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public enum BNetzAVLCa {
  GEM_HBA_QCA6_TEST_ONLY("GEM.HBA-qCA6 TEST-ONLY"),
  GEM_HBA_QCA24_TEST_ONLY("GEM.HBA-qCA24 TEST-ONLY"),
  GEM_HBA_QCA51_TEST_ONLY("GEM.HBA-qCA51 TEST-ONLY"),
  GEM_KOMP_CA56_TEST_ONLY("GEM.Komp-CA56 TEST-ONLY"),
  ;

  private final String subjectCA;

  public static X509Certificate getCA(String subjectCN) {
    return Arrays.stream(BNetzAVLCa.values())
        .filter(bNetzAVLCa -> bNetzAVLCa.subjectCA.equalsIgnoreCase(subjectCN))
        .map(BNetzAVLCa::getCertificate)
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    MessageFormat.format("No BNetzAVL CA found with subjectCN: {0}", subjectCN)));
  }

  public static X509Certificate getCA(X509Certificate eeCert) {
    val certWrapper = new X509CertificateWrapper(eeCert);
    return certWrapper.getIssuerCN().map(BNetzAVLCa::getCA).get();
  }

  public X509Certificate getCertificate() {
    val value =
        ResourceLoader.readFileFromResource(
            MessageFormat.format("ca/{0}.pem", subjectCA.toLowerCase().replace(" ", "_")));
    return X509CertificateWrapper.fromPem(value).toCertificate();
  }
}
