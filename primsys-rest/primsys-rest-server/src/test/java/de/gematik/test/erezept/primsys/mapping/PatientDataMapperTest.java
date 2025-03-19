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

import de.gematik.test.erezept.fhir.builder.kbv.KbvPatientFaker;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class PatientDataMapperTest extends ErpFhirBuildingTest {

  @Test
  void shouldCreateRandomDto() {
    val dto = PatientDataMapper.randomDto();
    assertNotNull(dto.getKvnr());
    assertNotNull(dto.getInsuranceType());
    assertNotNull(dto.getFirstName());
    assertNotNull(dto.getLastName());
    assertNotNull(dto.getBirthDate());
    assertNotNull(dto.getCity());
    assertNotNull(dto.getPostal());
    assertNotNull(dto.getStreet());
  }

  @RepeatedTest(5)
  void shouldNotMissAnyFields() {
    val patient = KbvPatientFaker.builder().fake();
    val mapper = PatientDataMapper.from(patient);
    val dto = mapper.getDto();

    val mapper2 = PatientDataMapper.from(dto);
    val patient2 = mapper2.convert();

    assertEquals(patient.getKvnr(), patient2.getKvnr());
    assertEquals(patient.getInsuranceKind(), patient2.getInsuranceKind());
    assertEquals(patient.getFullname(), patient2.getFullname());
    assertEquals(patient.getBirthDate(), patient2.getBirthDate());
  }
}
