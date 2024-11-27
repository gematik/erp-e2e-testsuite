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

package de.gematik.test.erezept.primsys.data;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.primsys.data.valuesets.StandardSizeDto;
import de.gematik.test.erezept.primsys.data.valuesets.SupplyFormDto;
import lombok.val;
import org.junit.jupiter.api.Test;

class DiGADispensedMedicationDtoTest {

  @Test
  void shouldBuildFromExistingMedication() {
    val medication =
        PznMedicationDto.medicine("17260627", "Vitamin C axicur 200 mg - 100 St.")
            .standardSize(StandardSizeDto.KA)
            .supplyForm(SupplyFormDto.BAL)
            .asPrescribed();
    val dispensed =
        DiGADispensedMedicationDto.dispensed(medication)
            .withDeepLink("https://www.collien-karrass.name?code=B00012D9TQ")
            .withRedeemCode("B00012D9TQ")
            .build();

    val om = new ObjectMapper();
    assertDoesNotThrow(() -> om.writeValueAsString(dispensed));
  }
}
