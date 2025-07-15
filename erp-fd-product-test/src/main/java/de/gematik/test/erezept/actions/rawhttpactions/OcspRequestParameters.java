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

package de.gematik.test.erezept.actions.rawhttpactions;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;
import javax.naming.ldap.LdapName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;

@Data
@AllArgsConstructor
public class OcspRequestParameters {

  private final String issuerCName;
  private final String certSerialNr;

  public static OcspRequestParameters passCert(String certPem) {
    val cert = getX509Cert(certPem);
    return passCert(cert);
  }

  public static OcspRequestParameters passCert(X509Certificate certPem) {
    return passConcreteParams(getIssuerCnName(certPem), String.valueOf(certPem.getSerialNumber()));
  }

  public static OcspRequestParameters passConcreteParams(String issuerName, String certSerialNr) {
    return new OcspRequestParameters(issuerName, certSerialNr);
  }

  public Map<String, Object> getQuery() {
    return Map.of("issuer-cn", issuerCName, "serial-nr", certSerialNr);
  }

  @SneakyThrows
  private static String getIssuerCnName(X509Certificate x509Certificate) {
    val x500Principal = x509Certificate.getIssuerX500Principal();
    val ldapDN = new LdapName(x500Principal.getName());

    return ldapDN.getRdns().stream()
        .filter(rdn -> rdn.getType().equalsIgnoreCase("cn"))
        .map(rdn -> rdn.getValue().toString())
        .findAny()
        .orElseThrow(
            () ->
                new BuilderException(
                    format("Principal {0} does not contain a CN", x500Principal.getName())));
  }

  @SneakyThrows
  private static X509Certificate getX509Cert(String certPem) {
    val certificateFactory = CertificateFactory.getInstance("X.509");
    return (X509Certificate)
        certificateFactory.generateCertificate(
            new ByteArrayInputStream(certPem.getBytes(StandardCharsets.UTF_8)));
  }
}
