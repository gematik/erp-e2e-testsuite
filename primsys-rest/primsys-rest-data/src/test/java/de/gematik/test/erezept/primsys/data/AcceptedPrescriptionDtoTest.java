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

package de.gematik.test.erezept.primsys.data;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.primsys.data.valuesets.InsuranceTypeDto;
import de.gematik.test.erezept.primsys.data.valuesets.InsurantStateDto;
import de.gematik.test.erezept.primsys.data.valuesets.PatientInsuranceTypeDto;
import de.gematik.test.erezept.primsys.data.valuesets.PersonGroupDto;
import de.gematik.test.erezept.primsys.data.valuesets.StandardSizeDto;
import de.gematik.test.erezept.primsys.data.valuesets.SupplyFormDto;
import de.gematik.test.erezept.primsys.data.valuesets.WopDto;
import java.io.File;
import java.util.UUID;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class AcceptedPrescriptionDtoTest {

  @SneakyThrows
  @ParameterizedTest(name = "Read example AcceptedData from {0}")
  @MethodSource("de.gematik.test.erezept.util.ExampleFileProvider#getAcceptedExamples")
  void shouldReadFromJson(File example) {
    val om = new ObjectMapper();
    assertDoesNotThrow(() -> om.readValue(example, AcceptedPrescriptionDto.class));
  }

  @Test
  void shouldBuildMedicationDto() {
    val dto =
        AcceptedPrescriptionDto.withPrescriptionId("160.000.210.267.466.08")
            .prescriptionReference(UUID.randomUUID().toString())
            .forKvnr("S040464113", PatientInsuranceTypeDto.GKV)
            .withAccessCode("d10eb8339ac7deceff27078d1f56ecf4e5af1179fb630b3a5424f896df980ee2")
            .withSecret("721e7a306cc74e8155ea0bcdcca6a404f1a62d9d8200c352cca1f1ef6754459a")
            .coveredBy(
                CoverageDto.ofType(InsuranceTypeDto.GKV)
                    .insurantState(InsurantStateDto.MEMBERS)
                    .resident(WopDto.BERLIN)
                    .personGroup(PersonGroupDto.NOT_SET)
                    .named("AOK Nordost")
                    .withIknr("109719018")
                    .build())
            .andMedication(
                PznMedicationDto.medicine("07765007", "NEUPRO 8MG/24H PFT 7 ST")
                    .amount("3", "Stk")
                    .standardSize(StandardSizeDto.N1)
                    .isVaccine(false)
                    .supplyForm(SupplyFormDto.GPA)
                    .asPrescribed());
    val om = new ObjectMapper();
    assertDoesNotThrow(() -> om.writeValueAsString(dto));
  }

  @Test
  void shouldBuildDiGADto() {
    val dto =
        AcceptedPrescriptionDto.withPrescriptionId("162.000.210.267.466.08")
            .prescriptionReference(UUID.randomUUID().toString())
            .forKvnr("S040464113", PatientInsuranceTypeDto.GKV)
            .withAccessCode("d10eb8339ac7deceff27078d1f56ecf4e5af1179fb630b3a5424f896df980ee2")
            .withSecret("721e7a306cc74e8155ea0bcdcca6a404f1a62d9d8200c352cca1f1ef6754459a")
            .coveredBy(
                CoverageDto.ofType(InsuranceTypeDto.GKV)
                    .insurantState(InsurantStateDto.MEMBERS)
                    .resident(WopDto.BERLIN)
                    .personGroup(PersonGroupDto.NOT_SET)
                    .named("AOK Nordost")
                    .withIknr("109719018")
                    .build())
            .andDiGA(
                HealthAppRequestDto.builder()
                    .pzn("19205615")
                    .name("Vantis KHK und Herzinfarkt 001")
                    .relatesToSocialCompensationLaw(false)
                    .build());
    val om = new ObjectMapper();
    assertDoesNotThrow(() -> om.writeValueAsString(dto));
  }
}
