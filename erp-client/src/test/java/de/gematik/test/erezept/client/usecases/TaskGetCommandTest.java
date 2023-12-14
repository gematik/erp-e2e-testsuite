/*
 * Copyright 2023 gematik GmbH
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;
import org.junit.jupiter.api.Test;

class TaskGetCommandTest {

  @Test
  void getRequestLocatorStartsWithsSlashTest() {
    val taskGetCommand = new TaskGetCommand();
    assertTrue(taskGetCommand.getRequestLocator().startsWith("/"));
  }

  @Test
  void getRequestLocatorSecondPartIsTaskTest() {
    val taskGetComd = new TaskGetCommand().getRequestLocator().split("/");
    assertEquals("Task", taskGetComd[1]);
  }

  @Test
  void requestBodyShuoldBeEmpty() {
    assertTrue(new TaskGetCommand().getRequestBody().isEmpty());
  }
}
