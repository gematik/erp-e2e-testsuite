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

package de.gematik.test.erezept.client.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommunicationGetCommandTest {
  private CommunicationGetCommand communicationGetCommand1;

  @BeforeEach
  void setupConstr() {
    this.communicationGetCommand1 = new CommunicationGetCommand();
  }

  @Test
  void getRequestLocatorFhireResource() {
    assertEquals("/Communication", communicationGetCommand1.getRequestLocator());
  }

  @Test
  void getRequestLocatorStartsWithSlash() {
    assertTrue(communicationGetCommand1.getRequestLocator().startsWith("/"));
  }

  @Test
  void getRequestLocatorSecondEntryIsCommunication() {
    assertEquals("Communication", communicationGetCommand1.getRequestLocator().split("/")[1]);
  }

  @Test
  void getRequestBodyIsEmpty() {
    assertTrue(communicationGetCommand1.getRequestBody().isEmpty());
  }

  @Test
  void getRequestBodyStartsWithOptional() {
    assertNotNull(communicationGetCommand1.getRequestBody());
  }
}
