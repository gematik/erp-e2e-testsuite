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

package de.gematik.test.erezept.client.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommunicationGetByIdCommandTest {

  CommunicationGetByIdCommand communicationGetByIdCommand;
  String id;

  @BeforeEach
  void setUp() {
    this.id = "ef0db1ef-ed28-4d01-9b3c-599bcd9c7849";
    this.communicationGetByIdCommand = new CommunicationGetByIdCommand(id);
  }

  @Test
  void getRequestLocatorStartsWithSlash() {
    assertTrue(communicationGetByIdCommand.getRequestLocator().startsWith("/"));
  }

  @Test
  void getRequestLocatorSecondEntryIsCommunication() {
    assertEquals("Communication", communicationGetByIdCommand.getRequestLocator().split("/")[1]);
  }

  @Test
  void getRequestLocatorCorrectID() {
    assertEquals(this.id, communicationGetByIdCommand.getRequestLocator().split("/")[2]);
  }

  @Test
  void getRequestBodyIsEmpty() {
    assertTrue(communicationGetByIdCommand.getRequestBody().isEmpty());
  }
}