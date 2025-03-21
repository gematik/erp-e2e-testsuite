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

package de.gematik.test.erezept.primsys.data;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class HealthAppRequestDtoTest {

  @SneakyThrows
  @ParameterizedTest(name = "Read example patient from {0}")
  @MethodSource("de.gematik.test.erezept.util.ExampleFileProvider#getHealthAppRequestExamples")
  void shouldReadFromJson(File example) {
    val om = new ObjectMapper();
    assertDoesNotThrow(() -> om.readValue(example, HealthAppRequestDto.class));
  }

  @Test
  void shouldBuildMedicationRequestDto() {
    val dtoBuilder =
        HealthAppRequestDto.builder()
            .pzn("1234567890")
            .name("Test App")
            .relatesToSocialCompensationLaw(true);
    assertDoesNotThrow(dtoBuilder::build);
  }
}
