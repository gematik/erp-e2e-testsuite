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

package de.gematik.test.erezept.primsys.data;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.primsys.data.valuesets.InsuranceTypeDto;
import java.io.File;
import java.util.Date;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class PatientDtoTest {

  @ParameterizedTest(name = "Read example patient from {0}")
  @MethodSource("de.gematik.test.erezept.util.ExampleFileProvider#getPatientExamples")
  void shouldReadFromJson(File example) {
    val om = new ObjectMapper();
    assertDoesNotThrow(() -> om.readValue(example, PatientDto.class));
  }

  @Test
  void shouldRequireKvnr() {
    val example =
        """
  {
  "firstName": "Fridolin",
  "lastName": "Schraßer",
  "birthDate": "10.06.1974",
  "city": "Berlin",
  "postal": "10117",
  "street": "Friedrichstraße 136"
}
""";

    val om = new ObjectMapper();

    // Note: although KVNR is marked with @JsonProperty(required = true) it is not guaranteed the
    // property is checked
    // ensuring a KVNR is given in requests must be ensured by the server manually
    //    assertThrows(JsonProcessingException.class, () -> om.readValue(example,
    // PatientDto.class));
    assertDoesNotThrow(() -> om.readValue(example, PatientDto.class));
  }

  @Test
  void shouldBuildDto() {
    val dto =
        PatientDto.withKvnr("X110406067")
            .withInsuranceType(InsuranceTypeDto.GKV)
            .named("Fridolin", "Schraßer")
            .bornOn(new Date())
            .address("10117", "Berlin", "Friedrichstraße 136")
            .build();
    val om = new ObjectMapper();
    assertDoesNotThrow(() -> om.writeValueAsString(dto));
  }
}
