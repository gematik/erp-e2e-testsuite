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

package de.gematik.test.erezept.primsys.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.primsys.data.valuesets.StandardSizeDto;
import de.gematik.test.erezept.primsys.data.valuesets.SupplyFormDto;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class PznDispensedMedicationDtoTest {

  @SneakyThrows
  @ParameterizedTest(name = "Read example dispensed Medication from {0}")
  @MethodSource(
      "de.gematik.test.erezept.util.ExampleFileProvider#getDispensedPznMedicationExamples")
  void shouldReadFromJson(File example) {
    val om = new ObjectMapper();
    Assertions.assertDoesNotThrow(() -> om.readValue(example, PznDispensedMedicationDto.class));
  }

  @Test
  void shouldBuildFromExistingMedication() {
    val medication =
        PznMedicationDto.medicine("17260627", "Vitamin C axicur 200 mg - 100 St.")
            .standardSize(StandardSizeDto.KA)
            .supplyForm(SupplyFormDto.BAL)
            .asPrescribed();
    val dispensed =
        PznDispensedMedicationDto.dispensed(medication).withBatchInfo("123123", new Date());

    val om = new ObjectMapper();
    Assertions.assertDoesNotThrow(() -> om.writeValueAsString(dispensed));
  }

  @Test
  void shouldBuildWithMultipleMedications() {
    List<PznDispensedMedicationDto> body = new ArrayList<>();

    val medication =
        PznMedicationDto.medicine("17260627", "Vitamin C axicur 200 mg - 100 St.")
            .standardSize(StandardSizeDto.KA)
            .supplyForm(SupplyFormDto.BAL)
            .asPrescribed();
    val dispensed =
        PznDispensedMedicationDto.dispensed(medication).withBatchInfo("123123", new Date());
    body.add(dispensed);

    val medication2 =
        PznMedicationDto.medicine("17260628", "Vitamin C axicur 200 mg - 100 St.")
            .standardSize(StandardSizeDto.N1)
            .supplyForm(SupplyFormDto.APA)
            .asPrescribed();
    val dispensed2 =
        PznDispensedMedicationDto.dispensed(medication2).withBatchInfo("123123", new Date());
    body.add(dispensed2);
    val om = new ObjectMapper();
    Assertions.assertDoesNotThrow(() -> om.writeValueAsString(body));
  }
}
