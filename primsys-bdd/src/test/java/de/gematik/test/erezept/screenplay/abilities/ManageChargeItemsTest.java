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
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemFaker;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import lombok.val;
import org.junit.jupiter.api.Test;

class ManageChargeItemsTest {

  @Test
  void shouldSimplyAddOnUpdate() {
    val ability = ManageChargeItems.heReceives();
    assertTrue(ability.getChargeItems().getRawList().isEmpty());

    val prescriptionId = PrescriptionId.random();
    val chargeItem = ErxChargeItemFaker.builder().withPrescriptionId(prescriptionId).fake();

    ability.update(chargeItem);
    assertEquals(1, ability.getChargeItems().getRawList().size());
  }

  @Test
  void shouldUpdateOnSecondCall() {
    val ability = ManageChargeItems.heReceives();
    assertTrue(ability.getChargeItems().getRawList().isEmpty());

    val prescriptionId = PrescriptionId.random();
    val chargeItem1 =
        ErxChargeItemFaker.builder()
            .withPrescriptionId(prescriptionId)
            .withMarkingFlag(true, true, true)
            .fake();
    val chargeItem2 =
        ErxChargeItemFaker.builder()
            .withPrescriptionId(prescriptionId)
            .withMarkingFlag(false, false, false)
            .fake();

    ability.update(chargeItem1);
    assertEquals(1, ability.getChargeItems().getRawList().size());

    ability.update(chargeItem2);
    assertEquals(1, ability.getChargeItems().getRawList().size());

    val fetchedChargeItem = ability.getChargeItem(prescriptionId).orElseThrow();
    assertEquals(chargeItem2.hasInsuranceProvider(), fetchedChargeItem.hasInsuranceProvider());
    assertEquals(chargeItem2.hasTaxOffice(), fetchedChargeItem.hasTaxOffice());
    assertEquals(chargeItem2.hasSubsidy(), fetchedChargeItem.hasSubsidy());
  }

  @Test
  void shouldAddAndRemoveMultiple() {
    val ability = ManageChargeItems.heReceives();
    assertTrue(ability.getChargeItems().getRawList().isEmpty());

    val prescriptionId1 = PrescriptionId.random();
    val chargeItem1 = ErxChargeItemFaker.builder().withPrescriptionId(prescriptionId1).fake();

    val prescriptionId2 = PrescriptionId.random();
    val chargeItem2 = ErxChargeItemFaker.builder().withPrescriptionId(prescriptionId2).fake();

    ability.update(chargeItem1);
    ability.update(chargeItem2);
    assertEquals(2, ability.getChargeItems().getRawList().size());
    assertTrue(ability.getChargeItem(prescriptionId1).isPresent());
    assertTrue(ability.getChargeItem(prescriptionId2).isPresent());

    ability.remove(prescriptionId1);
    assertEquals(1, ability.getChargeItems().getRawList().size());
    assertTrue(ability.getChargeItem(prescriptionId1).isEmpty());
    assertTrue(ability.getChargeItem(prescriptionId2).isPresent());
  }
}
