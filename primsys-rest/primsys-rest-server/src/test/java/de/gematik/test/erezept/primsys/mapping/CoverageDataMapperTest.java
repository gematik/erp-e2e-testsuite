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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.builder.kbv.KbvCoverageFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvPatientFaker;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.primsys.data.CoverageDto;
import de.gematik.test.erezept.primsys.data.valuesets.PayorTypeDto;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class CoverageDataMapperTest extends ErpFhirBuildingTest {

  @Test
  void shouldGenerateRandomDto() {
    val dto = CoverageDataMapper.randomDto();
    assertNotNull(dto.getInsuranceType());
    assertNotNull(dto.getIknr());
    assertNotNull(dto.getName());
    assertNotNull(dto.getWop());
    assertNotNull(dto.getInsurantState());
    assertNotNull(dto.getPersonGroup());
  }

  @Test
  void shouldCompleteCoverageWithPayorTypeNotNull() {
    val dto = CoverageDto.ofType(PayorTypeDto.SKT).build();
    val beneficiary = mock(KbvPatient.class);
    when(beneficiary.getInsuranceKind()).thenReturn(InsuranceTypeDe.GKV);
    val coverageDataMapper = CoverageDataMapper.from(dto, beneficiary);
    assertNotNull(coverageDataMapper);
  }

  @RepeatedTest(5)
  void shouldNotMissAnyFields() {
    val patient = KbvPatientFaker.builder().fake();
    val coverage = KbvCoverageFaker.builder().fake();
    val mapper = CoverageDataMapper.from(coverage, patient);
    val dto = mapper.getDto();

    val mapper2 = CoverageDataMapper.from(dto, patient);
    val coverage2 = mapper2.convert();

    assertEquals(coverage.getIknr(), coverage2.getIknr());
    assertEquals(coverage.getInsuranceKind(), coverage2.getInsuranceKind());
    assertEquals(coverage.getName(), coverage2.getName());
    assertEquals(coverage.getPayorType(), coverage2.getPayorType());
    assertEquals(coverage.getPersonGroup(), coverage2.getPersonGroup());
  }
}
