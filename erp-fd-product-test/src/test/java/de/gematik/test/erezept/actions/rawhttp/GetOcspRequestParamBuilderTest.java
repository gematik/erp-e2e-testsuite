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

package de.gematik.test.erezept.actions.rawhttp;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.actions.rawhttpactions.OcspRequestParameters;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

class GetOcspRequestParamBuilderTest {

  private static final String ISSUER_NAME = "GEM.SMCB-CA51 TEST-ONLY";
  private static final String CERT_SERIAL_NR = "206706423598360";

  private static final String CERT_PEM =
      "-----BEGIN CERTIFICATE-----\n"
          + "MIID+DCCA5+gAwIBAgIHALv/lvONGDAKBggqhkjOPQQDAjCBmjELMAkGA1UEBhMC\n"
          + "REUxHzAdBgNVBAoMFmdlbWF0aWsgR21iSCBOT1QtVkFMSUQxSDBGBgNVBAsMP0lu\n"
          + "c3RpdHV0aW9uIGRlcyBHZXN1bmRoZWl0c3dlc2Vucy1DQSBkZXIgVGVsZW1hdGlr\n"
          + "aW5mcmFzdHJ1a3R1cjEgMB4GA1UEAwwXR0VNLlNNQ0ItQ0E1MSBURVNULU9OTFkw\n"
          + "HhcNMjMwMTI1MjMwMDAwWhcNMjgwMTI1MjI1OTU5WjCB+DELMAkGA1UEBhMCREUx\n"
          + "DDAKBgNVBAgMA1VsbTEUMBIGA1UEBwwLSW5nb2xmc3RhZHQxDjAMBgNVBBEMBTEy\n"
          + "MzQ2MRUwEwYDVQQJDAxIYXVwdHN0ci4gMTExITAfBgNVBAoMGDMtMi0yMDIzMDEy\n"
          + "NC0xIE5PVC1WQUxJRDEgMB4GA1UEBRMXMTYuODAyNzYwMDEwMTE2OTk5MDIzMDEx\n"
          + "FDASBgNVBAQMC1VsbWVuZG9yZmVyMREwDwYDVQQqDAhBZGVsaGVpZDEwMC4GA1UE\n"
          + "AwwnQXBvdGhla2UgQWRlbGhlaWQgVWxtZW5kb3JmZXIgVEVTVC1PTkxZMFowFAYH\n"
          + "KoZIzj0CAQYJKyQDAwIIAQEHA0IABIt9O6MwSMzM3FIN8+/QeLLH419hU/Z5BpkJ\n"
          + "LVpz2o2HZ7vYATrvaAPqkRmdPDRYTcMsjl6yYQhLlTTb6QplZiejggFtMIIBaTAO\n"
          + "BgNVHQ8BAf8EBAMCAwgwDAYDVR0TAQH/BAIwADBcBgNVHSAEVTBTMDsGCCqCFABM\n"
          + "BIEjMC8wLQYIKwYBBQUHAgEWIWh0dHA6Ly93d3cuZ2VtYXRpay5kZS9nby9wb2xp\n"
          + "Y2llczAJBgcqghQATARMMAkGByqCFABMBGUwTgYFKyQIAwMERTBDMEEwPzA9MDsw\n"
          + "FwwVw5ZmZmVudGxpY2hlIEFwb3RoZWtlMAkGByqCFABMBDYTFTMtMDEuMi4yMDIz\n"
          + "MDAxLjE2LjEwMTAfBgNVHSMEGDAWgBQGmOkCVf/Jn1yjZQ7xXeIg9YT7kzAdBgNV\n"
          + "HQ4EFgQUV1SAEWk6W6ORPAkLkl4tv+a3dgcwOAYIKwYBBQUHAQEELDAqMCgGCCsG\n"
          + "AQUFBzABhhxodHRwOi8vZWhjYS5nZW1hdGlrLmRlL29jc3AvMCEGA1UdEQQaMBig\n"
          + "FgYDVQQDoA8MDU9ldGtlci1HcnVwcGUwCgYIKoZIzj0EAwIDRwAwRAIgKcJKdOEt\n"
          + "iEAb/i3tf2uVuMn+kXewNzt+9ndW5Vr9cUoCIFTgZx/sGG0GZZPLM4UqZpQ0UVU5\n"
          + "2x/P288I/fZiSm41\n"
          + "-----END CERTIFICATE-----";

  @Test
  void shouldBuildWithStringCert() {
    val paramBuilder = OcspRequestParameters.passCert(CERT_PEM);
    assertEquals(ISSUER_NAME, paramBuilder.getIssuerCName());
    assertEquals(CERT_SERIAL_NR, paramBuilder.getCertSerialNr());
    assertNotEquals(ISSUER_NAME, paramBuilder.getCertSerialNr());
    assertNotEquals(CERT_SERIAL_NR, paramBuilder.getIssuerCName());
  }

  @SneakyThrows
  @Test
  void shouldBuildWithCert() {
    val certificateFactory = CertificateFactory.getInstance("X.509");
    val x509Cert =
        (X509Certificate)
            certificateFactory.generateCertificate(
                new ByteArrayInputStream(CERT_PEM.getBytes(StandardCharsets.UTF_8)));

    val paramBuilder = OcspRequestParameters.passCert(x509Cert);
    assertEquals(ISSUER_NAME, paramBuilder.getIssuerCName());
    assertEquals(CERT_SERIAL_NR, paramBuilder.getCertSerialNr());
    assertNotEquals(ISSUER_NAME, paramBuilder.getCertSerialNr());
    assertNotEquals(CERT_SERIAL_NR, paramBuilder.getIssuerCName());
  }

  @Test
  void shouldThrowWhileDecryptAnInvalidCert() {
    assertThrows(
        java.security.cert.CertificateException.class,
        () -> OcspRequestParameters.passCert(ISSUER_NAME));
  }

  @Test
  void shouldBuildWithConcreteQueryParams() {
    val paramBuilder = OcspRequestParameters.passConcreteParams(ISSUER_NAME, CERT_SERIAL_NR);
    assertEquals(ISSUER_NAME, paramBuilder.getIssuerCName());
    assertEquals(CERT_SERIAL_NR, paramBuilder.getCertSerialNr());
    assertNotEquals(ISSUER_NAME, paramBuilder.getCertSerialNr());
    assertNotEquals(CERT_SERIAL_NR, paramBuilder.getIssuerCName());
  }
}
