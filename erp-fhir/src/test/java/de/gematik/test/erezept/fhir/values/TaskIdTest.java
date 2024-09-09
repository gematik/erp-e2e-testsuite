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

package de.gematik.test.erezept.fhir.values;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TaskIdTest {

  @Test
  void shouldGenerateFromPrescriptionId() {
    val pid = PrescriptionId.random();
    val tid = TaskId.from(pid);
    assertEquals(pid.getValue(), tid.getValue());
    assertEquals(tid, TaskId.from(pid.getValue()));
  }

  @Test
  void shouldToString() {
    val tid = TaskId.from("123");
    assertEquals("123", tid.toString());
  }

  @Test
  void shouldTranslateToPrescriptionId() {
    val tid = TaskId.from("123");
    val pid = tid.toPrescriptionId();
    assertEquals(tid.getValue(), pid.getValue());
  }

  @ParameterizedTest
  @MethodSource
  void shouldMapToFlowType(TaskId tid, PrescriptionFlowType expected) {
    assertEquals(expected, tid.getFlowType());
  }

  static Stream<Arguments> shouldMapToFlowType() {
    return Stream.of(
        Arguments.of(TaskId.from("160.0.0.1"), PrescriptionFlowType.FLOW_TYPE_160),
        Arguments.of(TaskId.from("169.0.0.1"), PrescriptionFlowType.FLOW_TYPE_169),
        Arguments.of(TaskId.from("200.0.0.1"), PrescriptionFlowType.FLOW_TYPE_200),
        Arguments.of(TaskId.from("209.0.0.1"), PrescriptionFlowType.FLOW_TYPE_209));
  }

  @Test
  void shouldEqualOnSameIds() {
    assertEquals(TaskId.from("123"), TaskId.from("123"));
  }

  @Test
  void shouldNotEqualOnDifferentIds() {
    assertNotEquals(TaskId.from("123"), TaskId.from("456"));
  }

  @Test
  void shouldNotEqualOnNull() {
    assertNotEquals(null, TaskId.from("123"));
  }
}
