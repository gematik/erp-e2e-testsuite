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

package de.gematik.test.erezept.client.cfg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.client.ClientType;
import de.gematik.test.erezept.client.rest.MediaType;
import de.gematik.test.erezept.config.dto.erpclient.ErpClientConfiguration;
import de.gematik.test.erezept.config.exceptions.ConfigurationException;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class ErpClientConfigurationTest {

  @Test
  void shouldGetClientType() {
    val dto = new ErpClientConfiguration();
    val types = List.of(ClientType.PS, ClientType.FDV);
    types.forEach(
        ct -> {
          // ClientType is not stored as enum but translated to string representation
          dto.setClientType(ct.toString());
          assertEquals(
              ct,
              de.gematik.test.erezept.client.cfg.ErpClientConfiguration.fromDto(dto)
                  .getClientType()); // and mapped back to enum here
        });
  }

  @Test
  void shouldGetCorrectAcceptXmlMimeType() {
    val dto = new ErpClientConfiguration();
    val acceptMimes = List.of("application/fhir+xml", "application/fhir+xml;q=1.0");
    acceptMimes.forEach(
        mime -> {
          dto.setAcceptMime(mime);
          assertEquals(
              MediaType.ACCEPT_FHIR_XML,
              de.gematik.test.erezept.client.cfg.ErpClientConfiguration.fromDto(dto)
                  .getAcceptMimeType());
        });
  }

  @Test
  void shouldGetCorrectAcceptJsonMimeType() {
    val dto = new ErpClientConfiguration();
    val acceptMimes = List.of("application/fhir+json", "application/fhir+json;q=1.0");
    acceptMimes.forEach(
        mime -> {
          dto.setAcceptMime(mime);
          assertEquals(
              MediaType.ACCEPT_FHIR_JSON,
              de.gematik.test.erezept.client.cfg.ErpClientConfiguration.fromDto(dto)
                  .getAcceptMimeType());
        });
  }

  @Test
  void shouldThrowOnIncorrectAcceptMimeType() {
    val dto = new ErpClientConfiguration();
    val acceptMimes = List.of("application/fhir", "json", "xml");
    acceptMimes.forEach(
        mime -> {
          dto.setAcceptMime(mime);
          assertThrows(
              ConfigurationException.class,
              de.gematik.test.erezept.client.cfg.ErpClientConfiguration.fromDto(dto)
                  ::getAcceptMimeType);
        });
  }

  @Test
  void shouldGetCorrectSendXmlMimeType() {
    val dto = new ErpClientConfiguration();
    val sendMimes = List.of("application/fhir+xml", "application/fhir+xml;q=1.0");
    sendMimes.forEach(
        mime -> {
          dto.setSendMime(mime);
          assertEquals(
              MediaType.FHIR_XML,
              de.gematik.test.erezept.client.cfg.ErpClientConfiguration.fromDto(dto)
                  .getSendMimeType());
        });
  }

  @Test
  void shouldGetCorrectSendJsonMimeType() {
    val dto = new ErpClientConfiguration();
    val sendMimes = List.of("application/fhir+json", "application/fhir+json;q=1.0");
    sendMimes.forEach(
        mime -> {
          dto.setSendMime(mime);
          assertEquals(
              MediaType.FHIR_JSON,
              de.gematik.test.erezept.client.cfg.ErpClientConfiguration.fromDto(dto)
                  .getSendMimeType());
        });
  }

  @Test
  void shouldThrowOnIncorrectSendMimeType() {
    val dto = new ErpClientConfiguration();
    val sendMimes = List.of("application/fhir", "json", "xml");
    sendMimes.forEach(
        mime -> {
          dto.setSendMime(mime);
          assertThrows(
              ConfigurationException.class,
              de.gematik.test.erezept.client.cfg.ErpClientConfiguration.fromDto(dto)
                  ::getSendMimeType);
        });
  }

  @Test
  void shouldDetectErpDevScope() {
    val dto = new ErpClientConfiguration();
    dto.setFdBaseUrl("https://erp-dev.fd.de");
    val scopes =
        de.gematik.test.erezept.client.cfg.ErpClientConfiguration.fromDto(dto).getErpScopes();
    assertTrue(scopes.contains("e-rezept-dev"));
    assertFalse(scopes.contains("e-rezept"));
  }

  @Test
  void shouldDetectErpScope() {
    val dto = new ErpClientConfiguration();
    dto.setFdBaseUrl("https://erp-ref.fd.de");
    val scopes =
        de.gematik.test.erezept.client.cfg.ErpClientConfiguration.fromDto(dto).getErpScopes();
    assertFalse(scopes.contains("e-rezept-dev"));
    assertTrue(scopes.contains("e-rezept"));
  }
}
