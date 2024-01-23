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

package de.gematik.test.fuzzing.fhirfuzz.utils;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.RepeatedTest;

class FuzzOperationResultTest {

  @RepeatedTest(REPETITIONS)
  void shouldBuildWithString() {
    String s1 = "S1";
    String s2 = "S2";
    String s3 = "S3";
    FuzzOperationResult fuzzOperationResult = new FuzzOperationResult<>(s1, s2, s3);
    FuzzOperationResult fuzzOperationResul2 = new FuzzOperationResult<>(s1, null, null);
    assertTrue(fuzzOperationResult.toString().contains(s1));
    assertTrue(fuzzOperationResul2.toString().contains(s1));
  }

  @RepeatedTest(REPETITIONS)
  void shouldBuildWithInt() {
    String s1 = "S1";
    int s2 = 123;
    int s3 = 123;
    FuzzOperationResult fuzzOperationResult = new FuzzOperationResult<>(s1, s2, s3);
    FuzzOperationResult fuzzOperationResul2 = new FuzzOperationResult<>(s1, null, null);
    assertTrue(fuzzOperationResult.toString().contains(Integer.toString(s2)));
  }

  @RepeatedTest(REPETITIONS)
  void shouldBuildWithMixed() {
    String s1 = "S1";
    int s2 = 123;
    Float s3 = 123.0f;
    FuzzOperationResult fuzzOperationResult = new FuzzOperationResult<>(s1, s2, s3);
    FuzzOperationResult fuzzOperationResul2 = new FuzzOperationResult<>(s1, null, null);
    assertTrue(fuzzOperationResult.toString().contains(Integer.toString(s2)));
  }

  @RepeatedTest(REPETITIONS)
  void testToStringWithObjects() {

    FuzzConfig fuzzConfig = new FuzzConfig();
    fuzzConfig.setName("testname");
    FuzzConfig fuzzConfig2 = new FuzzConfig();
    fuzzConfig2.setName("testname2");
    String s1 = "S1";
    FuzzOperationResult fuzzOperationResult =
        new FuzzOperationResult<>(s1, fuzzConfig, fuzzConfig2);
    assertTrue(fuzzOperationResult.toString().contains("->"));
  }
}
