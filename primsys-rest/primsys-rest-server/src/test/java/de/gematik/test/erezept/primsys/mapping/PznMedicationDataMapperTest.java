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

package de.gematik.test.erezept.primsys.mapping;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.primsys.data.PznMedicationDto;
import de.gematik.test.erezept.primsys.data.valuesets.MedicationTypeDto;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class PznMedicationDataMapperTest extends ParsingTest {

  @RepeatedTest(value = 10)
  void shouldGenerateRandomly() {
    val mapper = PznMedicationDataMapper.random();
    val kbvMedication = mapper.convert();
    val vr = ValidatorUtil.encodeAndValidate(parser, kbvMedication);
    assertTrue(vr.isSuccessful());
  }

  @Test
  void shouldGenerateWithMedicationTypeDtoNotPzn(){
    val dto = new PznMedicationDto();
    dto.setType(MedicationTypeDto.FREETEXT);
    val mapper = PznMedicationDataMapper.from(dto);
    assertNotNull(mapper);
  }
}
