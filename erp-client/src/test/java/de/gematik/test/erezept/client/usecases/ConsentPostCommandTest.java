/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.client.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsentPostCommandTest {

  private ConsentPostCommand consentPostCommand;

  @BeforeEach
  void setupBefore() {
    String kvId = "G995030567";
    this.consentPostCommand = new ConsentPostCommand(kvId);
  }

  @Test
  void getRequestLocatorStartsWithSlash() {
    var request = consentPostCommand.getRequestLocator();
    assertTrue(request.startsWith("/"));
  }

  @Test
  void getRequestLocatorFirstPartIsConsent() {
    var request = consentPostCommand.getRequestLocator();
    var requestStringArray = request.split("/");
    assertEquals("Consent", requestStringArray[1]);
  }

  @Test
  void getRequestBodyNotNull() {
    var request = consentPostCommand.getRequestBody();
    assertNotNull(request);
  }

  @Test
  void getRequestBodyIsOptionalPresent() {
    var request = consentPostCommand.getRequestBody();
    assertTrue(request.isPresent());
  }
}
