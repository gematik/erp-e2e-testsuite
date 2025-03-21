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

package de.gematik.test.erezept.primsys.mapping;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.primsys.data.PatientDto;
import de.gematik.test.erezept.primsys.data.PrescribeEvdgaRequestDto;
import lombok.val;
import org.junit.jupiter.api.Test;

class PrescribeEvdgaRequestDataMapperTest extends ErpFhirParsingTest {

  @Test
  void shouldGenerateValidRandomEvdgaPrescription() {
    val patientDto = new PatientDto();
    patientDto.setKvnr("X110407071");
    val requestDto = new PrescribeEvdgaRequestDto();
    requestDto.setPatient(patientDto);
    val prescribeMapper = PrescribeEvdgaRequestDataMapper.from(requestDto);
    val kbvBundle = prescribeMapper.createEvdgaBundle("Bernd Claudius");
    val result = ValidatorUtil.encodeAndValidate(parser, kbvBundle);
    assertTrue(result.isSuccessful());
  }
}
