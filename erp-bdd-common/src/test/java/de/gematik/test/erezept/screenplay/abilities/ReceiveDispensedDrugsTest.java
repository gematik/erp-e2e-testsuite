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

package de.gematik.test.erezept.screenplay.abilities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.time.Instant;
import lombok.val;
import org.junit.jupiter.api.Test;

class ReceiveDispensedDrugsTest {

  @Test
  void shouldAddAndGetReceivedDrugs() {
    val ability = ReceiveDispensedDrugs.forHimself();
    val firstId = PrescriptionId.random();
    val secondId = PrescriptionId.random();
    ability.append(firstId, Instant.now());
    ability.append(secondId, Instant.now());

    assertEquals(2, ability.getDispensedDrugsList().size());

    // get
    assertEquals(firstId, ability.getFirstDispensedDrug().prescriptionId());
    assertEquals(secondId, ability.getLastDispensedDrug().prescriptionId());

    // consume
    assertEquals(firstId, ability.consumeFirstDispensedDrug().prescriptionId());
    assertEquals(1, ability.getDispensedDrugsList().size());
    assertEquals(secondId, ability.consumeLastDispensedDrug().prescriptionId());
    assertEquals(0, ability.getDispensedDrugsList().size());
  }
}
