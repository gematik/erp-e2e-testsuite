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

package de.gematik.test.erezept.primsys.data.actors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Test;

class DoctorDtoTest {

  @Test
  void shouldCreateDoctorDto() throws JsonProcessingException {
    val om = new ObjectMapper();

    val dto = new DoctorDto();
    dto.setAnr(DoctorNumber.from("LANR", "123123123"));
    dto.addQualifications("ASV-Fachgruppennummer: 555555009");

    val json = assertDoesNotThrow(() -> om.writeValueAsString(dto));
    val dto2 = om.readValue(json, DoctorDto.class);
    assertEquals(dto, dto2);
  }
}
