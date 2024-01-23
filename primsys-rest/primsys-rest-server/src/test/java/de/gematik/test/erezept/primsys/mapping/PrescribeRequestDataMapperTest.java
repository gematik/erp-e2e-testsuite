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
import de.gematik.test.erezept.primsys.data.MedicationRequestDto;
import de.gematik.test.erezept.primsys.data.MvoDto;
import de.gematik.test.erezept.primsys.data.PatientDto;
import de.gematik.test.erezept.primsys.data.PrescribeRequestDto;
import de.gematik.test.erezept.primsys.data.valuesets.InsuranceTypeDto;
import jakarta.ws.rs.WebApplicationException;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;

class PrescribeRequestDataMapperTest extends ParsingTest {

  @Test
  void shouldGenerateValidRandomPrescription() {
    val patientDto = new PatientDto();
    patientDto.setKvnr("X110407071");
    val requestDto = new PrescribeRequestDto();
    requestDto.setPatient(patientDto);
    val prescribeMapper = PrescribeRequestDataMapper.from(requestDto);
    val kbvBundle = prescribeMapper.createKbvBundle("Bernd Claudius");
    val result = ValidatorUtil.encodeAndValidate(parser, kbvBundle);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldThrowOnInvalidMvo() {
    val patientDto = new PatientDto();
    patientDto.setKvnr("X110407071");
    val requestDto = new PrescribeRequestDto();
    val medicationRequestDto = new MedicationRequestDto();
    val mvo = new MvoDto();
    mvo.setDenominator(7);
    mvo.setNumerator(10);
    medicationRequestDto.setMvo(mvo);
    requestDto.setMedicationRequest(medicationRequestDto);
    requestDto.setPatient(patientDto);
    val prescribeMapper = PrescribeRequestDataMapper.from(requestDto);
    assertThrows(
        WebApplicationException.class, () -> prescribeMapper.createKbvBundle("Bernd Claudius"));
  }

  @Test
  @SetSystemProperty(key = "kbv.ita.for", value = "1.0.3")
  void shouldCreateKbvBundleWithPkvAndOldProfile() {
    val patientDto = new PatientDto();
    patientDto.setInsuranceType(InsuranceTypeDto.PKV);
    patientDto.setKvnr("X110407071");
    val requestDto = new PrescribeRequestDto();
    requestDto.setPatient(patientDto);
    val prescribeMapper = PrescribeRequestDataMapper.from(requestDto);
    val kbvBundle = prescribeMapper.createKbvBundle("Bernd Claudius");
    assertNotNull(kbvBundle);
  }
}
