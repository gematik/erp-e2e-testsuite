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
 */

package de.gematik.test.erezept.screenplay.abilities;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.intThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.config.dto.psp.PSPClientConfig;
import de.gematik.test.erezept.pspwsclient.PSPClient;
import de.gematik.test.erezept.pspwsclient.dataobjects.DeliveryOption;
import java.util.Arrays;
import lombok.val;
import org.junit.jupiter.api.Test;

class UsePspClientTest {

  @Test
  void shouldGetStrippedBaseUrl() {
    val mockClient = mock(PSPClient.class);
    val config = new PSPClientConfig();
    config.setUrl("http://gematik.de/");
    val ability = UsePspClient.with(mockClient).andConfig(config);
    assertFalse(ability.getBaseUrl().endsWith("/"));
  }

  @Test
  void shouldCorrectTrailingSlashes() {
    val mockClient = mock(PSPClient.class);
    when(mockClient.getId()).thenReturn("pspid");
    val config = new PSPClientConfig();
    config.setUrl("http://gematik.de/");
    val ability = UsePspClient.with(mockClient).andConfig(config);
    Arrays.stream(DeliveryOption.values())
        .forEach(
            option -> {
              val fullUrl = ability.getFullUrl(option);
              assertFalse(
                  fullUrl.contains(".de//"),
                  format("{0} must not contain trailing double slashes", fullUrl));
            });
  }

  @Test
  void shouldGetEmptyXAuth() {
    val mockClient = mock(PSPClient.class);
    val config = new PSPClientConfig();
    config.setUrl("http://gematik.de");
    val ability = UsePspClient.with(mockClient).andConfig(config);
    assertTrue(ability.getXAuth().isEmpty());
  }

  @Test
  void shouldGetXAuth() {
    val mockClient = mock(PSPClient.class);
    val config = new PSPClientConfig();
    config.setUrl("http://gematik.de");
    config.setAuth("123");
    val ability = UsePspClient.with(mockClient).andConfig(config);
    assertTrue(ability.getXAuth().isPresent());
    ability.getXAuth().ifPresent(xauth -> assertEquals("123", xauth));
  }

  @Test
  void shouldConnectOnCreation() throws InterruptedException {
    val mockClient = mock(PSPClient.class);
    when(mockClient.getId()).thenReturn("pspid");
    val config = new PSPClientConfig();
    config.setUrl("http://gematik.de/");
    config.setConnectTimeOut(5);

    UsePspClient.with(mockClient).andConfig(config);
    verify(mockClient, times(1)).connectBlocking(intThat(actual -> actual.equals(5)), any());
  }

  @Test
  void shouldCloseOnTeardown() {
    val mockClient = mock(PSPClient.class);
    when(mockClient.getId()).thenReturn("pspid");
    val config = new PSPClientConfig();
    config.setUrl("http://gematik.de/");

    UsePspClient.with(mockClient).andConfig(config).tearDown();
    verify(mockClient, times(1)).close();
  }
}
