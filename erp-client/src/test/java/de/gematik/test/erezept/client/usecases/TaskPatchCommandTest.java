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

package de.gematik.test.erezept.client.usecases;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.values.TaskId;
import java.util.Optional;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;

class TaskPatchCommandTest {

  @Test
  void shouldReturnRequestBody() {
    TaskId taskId = TaskId.random();
    Parameters body = new Parameters();

    TaskPatchCommand command = new TaskPatchCommand(taskId, body);
    Optional<Resource> requestBody = command.getRequestBody();

    assertTrue(requestBody.isPresent());
    assertEquals(body, requestBody.get());
  }
}
