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

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerBool;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvCoverageFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.builder.kbv.MedicationRequestFaker;
import de.gematik.test.erezept.fhir.builder.kbv.PatientFaker;
import de.gematik.test.erezept.fhir.builder.kbv.PractitionerFaker;
import de.gematik.test.erezept.primsys.data.MedicationRequestDto;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class MedicationRequestDataMapperTest {

  @Test
  void shouldValidMvoOnNull() {
    val mapper =
        MedicationRequestDataMapper.from(MedicationRequestDto.medicationRequest().build())
            .forMedication(null);
    assertTrue(mapper.isMvoValid());
  }

  @Test
  void shouldValidMvo() {
    val medReqDto = MedicationRequestDto.medicationRequest().mvo(MvoDataMapper.randomDto()).build();
    val mapper = MedicationRequestDataMapper.from(medReqDto).forMedication(null);
    assertTrue(mapper.isMvoValid());
  }

  @Test
  void shouldBeInvalidMvo() {
    val mvoDto = MvoDataMapper.randomDto();
    mvoDto.setNumerator(7);
    val medReqDto = MedicationRequestDto.medicationRequest().mvo(mvoDto).build();

    val mapper = MedicationRequestDataMapper.from(medReqDto).forMedication(null);
    assertFalse(mapper.isMvoValid());
  }

  @RepeatedTest(5)
  void shouldNotMissAnyFields() {
    val practitioner = PractitionerFaker.builder().fake();
    val patient = PatientFaker.builder().fake();
    val coverage = KbvCoverageFaker.builder().fake();
    val medication = KbvErpMedicationPZNFaker.builder().fake();

    val mrFaker = MedicationRequestFaker.builder();

    if (fakerBool()) {
      mrFaker.withNote(GemFaker.getFaker().backToTheFuture().quote());
    }

    val medicationRequest = mrFaker.fake();
    val mapper =
        MedicationRequestDataMapper.from(medicationRequest)
            .requestedBy(practitioner)
            .requestedFor(patient)
            .coveredBy(coverage)
            .forMedication(medication);
    val dto = mapper.getDto();

    val mapper2 =
        MedicationRequestDataMapper.from(dto)
            .requestedBy(practitioner)
            .requestedFor(patient)
            .coveredBy(coverage)
            .forMedication(medication);
    val medicationRequest2 = mapper2.convert();

    assertEquals(
        medicationRequest.getDosageInstructionAsText(),
        medicationRequest2.getDosageInstructionAsText());
    assertEquals(medicationRequest.getDispenseQuantity(), medicationRequest2.getDispenseQuantity());

    medicationRequest
        .getNoteText()
        .ifPresent(
            note -> {
              assertTrue(medicationRequest2.getNoteText().isPresent());
              assertEquals(note, medicationRequest2.getNoteText().get());
            });
    assertEquals(medicationRequest.allowSubstitution(), medicationRequest2.allowSubstitution());
    assertEquals(medicationRequest.isBvg(), medicationRequest2.isBvg());
    assertEquals(
        medicationRequest.hasEmergencyServiceFee(), medicationRequest2.hasEmergencyServiceFee());

    assertEquals(medicationRequest.isMultiple(), medicationRequest2.isMultiple());
    assertEquals(medicationRequest.getMvoStart(), medicationRequest2.getMvoStart());
    assertEquals(medicationRequest.getMvoEnd(), medicationRequest2.getMvoEnd());
    assertEquals(medicationRequest.getMvoPeriod(), medicationRequest2.getMvoPeriod());
    assertEquals(medicationRequest.getMvoRatio(), medicationRequest2.getMvoRatio());
  }
}
