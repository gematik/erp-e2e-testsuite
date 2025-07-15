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

package de.gematik.test.erezept.actions.rawhttpactions.pki;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Set;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;

@Data
public class PKICertificatesDTOEnvelop {
  @JsonProperty("ca_certs")
  @JsonDeserialize(contentUsing = X509CertificateDeserializer.class)
  private Set<X509Certificate> caCerts = Set.of();

  @JsonProperty("add_roots")
  @JsonDeserialize(contentUsing = X509CertificateDeserializer.class)
  private Set<X509Certificate> addRoots = Set.of();

  private static class X509CertificateDeserializer extends JsonDeserializer<X509Certificate> {
    @SneakyThrows
    @Override
    public X509Certificate deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
      val decoded = Base64.getDecoder().decode(p.getText());
      val factory = CertificateFactory.getInstance("X.509");
      return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(decoded));
    }
  }
}
