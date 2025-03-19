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

package de.gematik.test.erezept.cli.param;

import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TaskStatusWrapperTest {

  @Test
  void shouldMapToNullOnAny() {
    val status = TaskStatusWrapper.ANY;
    assertNull(status.getStatus());
  }

  @ParameterizedTest
  @EnumSource(
      value = TaskStatusWrapper.class,
      names = {"ANY"},
      mode = EnumSource.Mode.EXCLUDE)
  void shouldMapAccordingly(TaskStatusWrapper statusWrapper) {
    val taskStatus = statusWrapper.getStatus();
    val expected = Task.TaskStatus.valueOf(statusWrapper.name());
    assertEquals(expected, taskStatus);
  }
}
