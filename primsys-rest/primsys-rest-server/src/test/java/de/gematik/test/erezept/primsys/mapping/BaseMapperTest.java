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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.primsys.data.PatientDto;
import de.gematik.test.erezept.primsys.data.valuesets.InsuranceTypeDto;
import lombok.val;
import org.junit.jupiter.api.Test;

class BaseMapperTest {

  public static class TestMapper extends BaseMapper<PatientDto> {

    protected TestMapper(PatientDto dto) {
      super(dto);
    }

    @Override
    protected void complete() {
      // nothing todo here!
    }
  }

  @Test
  void shouldCheckNullValues() {
    val dto = new PatientDto();
    val mapper = new TestMapper(dto);

    assertTrue(mapper.isNullOrEmpty(dto.getKvnr()));
    assertTrue(mapper.isNullOrEmpty(dto.getInsuranceType()));

    val defaultName = "Bernd";
    val checkedName = mapper.getOrDefault(dto.getFirstName(), () -> defaultName);
    assertEquals(defaultName, checkedName);

    val defaultInsuranceType = InsuranceTypeDto.PKV;
    val checkedInsuranceType =
        mapper.getOrDefault(dto.getInsuranceType(), () -> defaultInsuranceType);
    assertEquals(defaultInsuranceType, checkedInsuranceType);

    dto.setInsuranceType(InsuranceTypeDto.GKV);
    val checkedInsuranceType2 =
        mapper.getOrDefault(dto.getInsuranceType(), () -> defaultInsuranceType);
    assertEquals(InsuranceTypeDto.GKV, checkedInsuranceType2);

    dto.setFirstName("AnotherName");
    val checkedName2 = mapper.getOrDefault(dto.getFirstName(), () -> defaultName);
    assertEquals("AnotherName", checkedName2);

    dto.setFirstName("");
    val checkedName3 = mapper.getOrDefault(dto.getFirstName(), () -> defaultName);
    assertEquals(defaultName, checkedName3);
  }

  @Test
  void shouldReturnDefaultValue() {
    val dto = new PatientDto();
    val mapper = new TestMapper(dto);
    val defaultName = "Bernd";

    val checkedName = mapper.getOrDefault(null, defaultName);
    assertEquals(defaultName, checkedName);

    val checkedName2 = mapper.getOrDefault("", defaultName);
    assertEquals(defaultName, checkedName2);
  }

  @Test
  void shouldDefaultEmptyStrings() {
    val dto = PatientDto.withKvnr("").withInsuranceType(InsuranceTypeDto.GKV).build();
    val mapper = new TestMapper(dto);

    assertNotNull(dto.getKvnr());
    mapper.ensure(dto::getKvnr, dto::setKvnr, () -> "A123123123");
    assertEquals("A123123123", dto.getKvnr());
  }

  @Test
  void shouldCheckEnsureNullValuesFilledWithDefaults() {
    val dto = new PatientDto();
    val mapper = new TestMapper(dto);

    assertNull(dto.getStreet());
    mapper.ensure(dto::getStreet, dto::setStreet, () -> "Friedrichstraße");
    assertEquals("Friedrichstraße", dto.getStreet());

    assertNull(dto.getInsuranceType());
    mapper.ensure(dto::getInsuranceType, dto::setInsuranceType, () -> InsuranceTypeDto.BG);
    assertEquals(InsuranceTypeDto.BG, dto.getInsuranceType());
  }

  @Test
  void shouldKeepExistingValues() {
    val dto = PatientDto.withKvnr("X110406067").withInsuranceType(InsuranceTypeDto.GKV).build();
    val mapper = new TestMapper(dto);

    assertNotNull(dto.getKvnr());
    mapper.ensure(dto::getKvnr, dto::setKvnr, () -> "A123123123");
    assertEquals("X110406067", dto.getKvnr());

    assertNotNull(dto.getInsuranceType());
    mapper.ensure(dto::getInsuranceType, dto::setInsuranceType, () -> InsuranceTypeDto.BG);
    assertEquals(InsuranceTypeDto.GKV, dto.getInsuranceType());
  }

  @Test
  void shouldUseCustomChecks() {
    val dto = PatientDto.withKvnr("X110406067").withInsuranceType(InsuranceTypeDto.GKV).build();
    val mapper = new TestMapper(dto);

    assertNotNull(dto.getKvnr());
    mapper.ensure(() -> !dto.getKvnr().equals("A123123123"), dto::setKvnr, () -> "A123123123");
    assertEquals("A123123123", dto.getKvnr());

    assertNotNull(dto.getInsuranceType());
    mapper.ensure(
        () -> dto.getInsuranceType().equals(InsuranceTypeDto.BEI),
        dto::setInsuranceType,
        () -> InsuranceTypeDto.SOZ);
    assertEquals(InsuranceTypeDto.GKV, dto.getInsuranceType());
  }
}
