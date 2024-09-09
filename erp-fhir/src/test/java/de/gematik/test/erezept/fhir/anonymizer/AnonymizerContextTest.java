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

package de.gematik.test.erezept.fhir.anonymizer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

class AnonymizerContextTest {

  @Test
  void shouldAnonymizeOnlyStringTypes() {
    val ctx =
        new AnonymizerContext(Map.of(), AnonymizationType.REPLACING, new CharReplacementStrategy());
    val type = new IntegerType(10);
    assertDoesNotThrow(() -> ctx.anonymize(type));
    assertEquals(10, type.getValue());
  }

  @ParameterizedTest
  @MethodSource
  @NullSource
  void shouldAnonymizeEmptyStringTypes(Type type) {
    val ctx =
        new AnonymizerContext(Map.of(), AnonymizationType.REPLACING, new CharReplacementStrategy());
    assertDoesNotThrow(() -> ctx.anonymize(type));
  }

  static Stream<Arguments> shouldAnonymizeEmptyStringTypes() {
    return Stream.of(new StringType(), new StringType("")).map(Arguments::of);
  }
}
