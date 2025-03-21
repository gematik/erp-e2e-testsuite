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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.primsys.data.PznMedicationDto;
import de.gematik.test.erezept.primsys.data.valuesets.MedicationTypeDto;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class KbvPznMedicationDataMapperTest extends ErpFhirParsingTest {

  @RepeatedTest(value = 10)
  void shouldGenerateRandomly() {
    val mapper = KbvPznMedicationDataMapper.random();
    val kbvMedication = mapper.convert();
    val vr = ValidatorUtil.encodeAndValidate(parser, kbvMedication);
    assertTrue(vr.isSuccessful());
  }

  @Test
  void shouldGenerateWithMedicationTypeDtoNotPzn() {
    val dto = new PznMedicationDto();
    dto.setType(MedicationTypeDto.FREETEXT);
    val mapper = KbvPznMedicationDataMapper.from(dto);
    assertNotNull(mapper);
  }

  @RepeatedTest(5)
  void shouldNotMissAnyFields() {
    val medication = KbvErpMedicationPZNFaker.builder().fake();
    val mapper = KbvPznMedicationDataMapper.from(medication);
    val dto = mapper.getDto();

    val mapper2 = KbvPznMedicationDataMapper.from(dto);
    val medication2 = mapper2.convert();

    assertEquals(medication.getMedicationType(), medication2.getMedicationType());
    assertEquals(medication.getCatagory(), medication2.getCatagory());
    assertEquals(medication.isVaccine(), medication2.isVaccine());
    assertEquals(medication.getStandardSize(), medication2.getStandardSize());
    assertEquals(medication.getDarreichungsform(), medication2.getDarreichungsform());
    assertEquals(medication.getPackagingSizeOrEmpty(), medication2.getPackagingSizeOrEmpty());
    assertEquals(medication.getPackagingSize(), medication2.getPackagingSize());
    assertEquals(medication.getPackagingUnit(), medication2.getPackagingUnit());
    assertEquals(medication.getPzn(), medication2.getPzn());
    assertEquals(medication.getMedicationName(), medication2.getMedicationName());
  }
}
