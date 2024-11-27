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

package de.gematik.test.erezept.primsys.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationFaker;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.primsys.data.PznDispensedMedicationDto;
import de.gematik.test.erezept.primsys.data.valuesets.MedicationTypeDto;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;

class GemErpMedicationDataMapperTest extends ParsingTest {

  @RepeatedTest(value = 10)
  @SetSystemProperty(key = "erp.fhir.profile", value = "1.4.0")
  void shouldGenerateRandomly() {
    val mapper = GemErpMedicationDataMapper.random();
    val erpMedication = mapper.convert();
    val vr = ValidatorUtil.encodeAndValidate(parser, erpMedication);
    assertTrue(vr.isSuccessful());
  }

  @Test
  void shouldGenerateWithMedicationTypeDtoNotPzn() {
    val dto = new PznDispensedMedicationDto();
    dto.setType(MedicationTypeDto.FREETEXT);
    val mapper = GemErpMedicationDataMapper.from(dto);
    assertNotNull(mapper);
  }

  @RepeatedTest(5)
  void shouldNotMissAnyFields() {
    val medication = GemErpMedicationFaker.builder().fake();
    val mapper = GemErpMedicationDataMapper.from(medication);
    val dto = mapper.getDto();

    val mapper2 = GemErpMedicationDataMapper.from(dto);
    val medication2 = mapper2.convert();

    assertEquals(medication.getCategory(), medication2.getCategory());
    assertEquals(medication.isVaccine(), medication2.isVaccine());
    assertEquals(medication.getStandardSize(), medication2.getStandardSize());
    assertEquals(medication.getDarreichungsform(), medication2.getDarreichungsform());
    assertEquals(medication.getAmountNumerator(), medication2.getAmountNumerator());
    assertEquals(medication.getAmountNumeratorUnit(), medication2.getAmountNumeratorUnit());
    assertEquals(medication.getPzn(), medication2.getPzn());
    assertEquals(medication.getName(), medication2.getName());
    assertEquals(
        medication.getBatchLotNumber(),
        medication2.getBatchLotNumber(),
        "batch number must be equal");
  }
}
