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

package de.gematik.test.erezept.primsys.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.primsys.data.valuesets.*;
import java.io.File;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CoverageDtoTest {

  @SneakyThrows
  @ParameterizedTest(name = "Read example coverage from {0}")
  @MethodSource("de.gematik.test.erezept.util.ExampleFileProvider#getCoverageExamples")
  void shouldReadFromJson(File example) {
    val om = new ObjectMapper();
    Assertions.assertDoesNotThrow(() -> om.readValue(example, CoverageDto.class));
  }

  @Test
  void shouldBuildDtoWithInsuranceType() {
    val dto =
        CoverageDto.ofType(InsuranceTypeDto.GKV)
            .named("AOK Baden‐Württemberg")
            .withIknr("108018007")
            .insurantState(InsurantStateDto.MEMBERS)
            .resident(WopDto.BADEN_WUERTTEMBERG)
            .personGroup(PersonGroupDto.NOT_SET)
            .build();
    val om = new ObjectMapper();
    Assertions.assertDoesNotThrow(() -> om.writeValueAsString(dto));
  }

  @Test
  void shouldBuildDtoWithPayorType() {
    val dto =
        CoverageDto.ofType(PayorTypeDto.UK)
            .named("Unfallkasse NRW")
            .withIknr("104212059")
            .insurantState(InsurantStateDto.MEMBERS)
            .resident(WopDto.NORDRHEIN)
            .personGroup(PersonGroupDto.NOT_SET)
            .build();
    val om = new ObjectMapper();
    Assertions.assertDoesNotThrow(() -> om.writeValueAsString(dto));
  }
}
