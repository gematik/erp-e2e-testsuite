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

package de.gematik.test.erezept.client.cfg;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.idp.field.IdpScope;
import de.gematik.test.erezept.client.ClientType;
import de.gematik.test.erezept.client.exceptions.ConfigurationException;
import de.gematik.test.erezept.client.rest.MediaType;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class ErpClientConfigurationTest {

  @Test
  void shouldGetClientType() {
    val cfg = new ErpClientConfiguration();
    val types = List.of(ClientType.PS, ClientType.FDV);
    types.forEach(
        ct -> {
          cfg.setClientType(
              ct); // ClientType is not stored as enum but translated to string representation
          assertEquals(ct, cfg.getClientType()); // and mapped back to enum here
        });
  }

  @Test
  void shouldGetCorrectAcceptXmlMimeType() {
    val cfg = new ErpClientConfiguration();
    val acceptMimes = List.of("application/fhir+xml", "application/fhir+xml;q=1.0");
    acceptMimes.forEach(
        mime -> {
          cfg.setAcceptMime(mime);
          assertEquals(MediaType.ACCEPT_FHIR_XML, cfg.getAcceptMimeType());
        });
  }

  @Test
  void shouldGetCorrectAcceptJsonMimeType() {
    val cfg = new ErpClientConfiguration();
    val acceptMimes = List.of("application/fhir+json", "application/fhir+json;q=1.0");
    acceptMimes.forEach(
        mime -> {
          cfg.setAcceptMime(mime);
          assertEquals(MediaType.ACCEPT_FHIR_JSON, cfg.getAcceptMimeType());
        });
  }

  @Test
  void shouldThrowOnIncorrectAcceptMimeType() {
    val cfg = new ErpClientConfiguration();
    val acceptMimes = List.of("application/fhir", "json", "xml");
    acceptMimes.forEach(
        mime -> {
          cfg.setAcceptMime(mime);
          assertThrows(ConfigurationException.class, cfg::getAcceptMimeType);
        });
  }

  @Test
  void shouldGetCorrectSendXmlMimeType() {
    val cfg = new ErpClientConfiguration();
    val sendMimes = List.of("application/fhir+xml", "application/fhir+xml;q=1.0");
    sendMimes.forEach(
        mime -> {
          cfg.setSendMime(mime);
          assertEquals(MediaType.FHIR_XML, cfg.getSendMimeType());
        });
  }

  @Test
  void shouldGetCorrectSendJsonMimeType() {
    val cfg = new ErpClientConfiguration();
    val sendMimes = List.of("application/fhir+json", "application/fhir+json;q=1.0");
    sendMimes.forEach(
        mime -> {
          cfg.setSendMime(mime);
          assertEquals(MediaType.FHIR_JSON, cfg.getSendMimeType());
        });
  }

  @Test
  void shouldThrowOnIncorrectSendMimeType() {
    val cfg = new ErpClientConfiguration();
    val sendMimes = List.of("application/fhir", "json", "xml");
    sendMimes.forEach(
        mime -> {
          cfg.setSendMime(mime);
          assertThrows(ConfigurationException.class, cfg::getSendMimeType);
        });
  }

  @Test
  void shouldDetectErpDevScope() {
    val cfg = new ErpClientConfiguration();
    cfg.setFdBaseUrl("https://erp-dev.fd.de");
    val scopes = cfg.getErpScopes();
    assertTrue(scopes.contains(IdpScope.EREZEPTDEV));
    assertFalse(scopes.contains(IdpScope.EREZEPT));
  }

  @Test
  void shouldDetectErpScope() {
    val cfg = new ErpClientConfiguration();
    cfg.setFdBaseUrl("https://erp-ref.fd.de");
    val scopes = cfg.getErpScopes();
    assertFalse(scopes.contains(IdpScope.EREZEPTDEV));
    assertTrue(scopes.contains(IdpScope.EREZEPT));
  }
}
