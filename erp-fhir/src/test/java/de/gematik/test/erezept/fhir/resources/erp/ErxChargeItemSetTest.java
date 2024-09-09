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

package de.gematik.test.erezept.fhir.resources.erp;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemFaker;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import lombok.val;
import org.junit.jupiter.api.Test;

class ErxChargeItemSetTest {

  @Test
  void shouldMapChargeItems() {
    val set = new ErxChargeItemSet();
    set.addEntry()
        .setResource(
            ErxChargeItemFaker.builder()
                .withPrescriptionId(PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_200))
                .fake());
    set.addEntry()
        .setResource(
            ErxChargeItemFaker.builder()
                .withPrescriptionId(PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_200))
                .fake());

    assertEquals(2, set.getChargeItems().size());
  }
}
