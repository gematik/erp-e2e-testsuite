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

package de.gematik.test.erezept.primsys.mapping;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.primsys.data.MedicationRequestDto;
import de.gematik.test.erezept.primsys.data.MvoDto;
import de.gematik.test.erezept.primsys.data.PatientDto;
import de.gematik.test.erezept.primsys.data.PrescribeRequestDto;
import de.gematik.test.erezept.primsys.data.valuesets.InsuranceTypeDto;
import jakarta.ws.rs.WebApplicationException;
import lombok.val;
import org.junit.jupiter.api.Test;

class PrescribeRequestDataMapperTest extends ErpFhirParsingTest {

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
    val mvo = new MvoDto();
    mvo.setDenominator(7);
    mvo.setNumerator(10);

    val medicationRequestDto = MedicationRequestDto.medicationRequest().mvo(mvo).build();

    requestDto.setMedicationRequest(medicationRequestDto);
    requestDto.setPatient(patientDto);
    val prescribeMapper = PrescribeRequestDataMapper.from(requestDto);
    assertThrows(
        WebApplicationException.class, () -> prescribeMapper.createKbvBundle("Bernd Claudius"));
  }

  @Test
  void shouldCreateKbvBundleWithPkv() {
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
