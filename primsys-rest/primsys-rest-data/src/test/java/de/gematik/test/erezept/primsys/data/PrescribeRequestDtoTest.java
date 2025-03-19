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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import lombok.val;
import org.junit.jupiter.api.Test;

class PrescribeRequestDtoTest {

  @Test
  void shouldReturnPrescribeRequestDtoBuilder() {
    val builder = PrescribeRequestDto.forKvnr("X110407071");
    assertNotNull(builder);
  }

  @Test
  void shouldBuildPrescribeRequestDto() {
    val builder = PrescribeRequestDto.forKvnr("X110407071");
    val coverageDto = mock(CoverageDto.class);
    val medication = mock(PznMedicationDto.class);
    val medicationRequest = mock(MedicationRequestDto.class);

    val dto =
        builder
            .coveredBy(coverageDto)
            .medication(medication)
            .medicationRequest(medicationRequest)
            .build();
    assertNotNull(dto);
  }
}
