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

package de.gematik.test.erezept.pspwsclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;

class PSPClientProxySetupTest {

  String testUri = "abcTest";
  String apoId = "apoId";

  @ClearSystemProperty.ClearSystemProperties(
      value = {
        @ClearSystemProperty(key = "https.proxyHost"),
        @ClearSystemProperty(key = "https.proxyPort")
      })
  @Test
  void shouldSetEnvVariable() {
    System.setProperty("https.proxyHost", "http://goToHost.de");
    System.setProperty("https.proxyPort", "123");
    val proxyAddress = System.getProperty("https.proxyHost");
    assertEquals("http://goToHost.de", proxyAddress);
    val proxyPort = System.getProperty("https.proxyPort");
    assertEquals("123", proxyPort);
    val client = new PharmaServiceProviderWSClient(testUri, apoId, "asd");
    assertEquals(testUri + "/" + apoId, client.getURI().getPath());
  }

  @ClearSystemProperty.ClearSystemProperties(
      value = {
        @ClearSystemProperty(key = "https.proxyHost"),
        @ClearSystemProperty(key = "https.proxyPort")
      })
  @Test
  void shouldThrowNumberFormatException() {
    System.setProperty("https.proxyHost", "http://goToHost.de");
    System.setProperty("https.proxyPort", "abc");
    val sysPropPort = System.getProperty("https.proxyPort");
    assertEquals("abc", sysPropPort);
    assertThrows(
        NumberFormatException.class,
        () -> {
          new PharmaServiceProviderWSClient(testUri, apoId);
        });
  }

  @ClearSystemProperty.ClearSystemProperties(
      value = {
        @ClearSystemProperty(key = "https.proxyHost"),
        @ClearSystemProperty(key = "https.proxyPort")
      })
  @Test
  void shouldSetEnvirVariable() {
    System.setProperty("https.proxyHost", "http://goToHost.de");
    System.setProperty("https.proxyPort", "123");
    val proxyAddress = System.getProperty("https.proxyHost");
    assertEquals("http://goToHost.de", proxyAddress);
    val proxyPort = System.getProperty("https.proxyPort");
    assertEquals("123", proxyPort);
    val client = new PharmaServiceProviderWSClient(testUri, apoId, "asd");
    assertEquals(testUri + "/" + apoId, client.getURI().getPath());
  }
}
