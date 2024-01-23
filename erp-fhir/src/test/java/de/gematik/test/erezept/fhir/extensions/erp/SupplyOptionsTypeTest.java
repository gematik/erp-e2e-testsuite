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

package de.gematik.test.erezept.fhir.extensions.erp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SupplyOptionsTypeTest {

  @ParameterizedTest
  @CsvSource({
    "onPremise",
    "onpremise",
    "ONPREMISE",
    "nix ",
  })
  void onPremiseShouldWorkCorrect(String supplyType) {
    assertEquals(SupplyOptionsType.ON_PREMISE, SupplyOptionsType.getSupplyOptionType(supplyType));
  }

  @ParameterizedTest
  @CsvSource({
    "delivery",
    "DELIVERY",
    "deLiVery",
  })
  void deliveryShouldWorkCorrect(String supplyType) {
    assertEquals(SupplyOptionsType.DELIVERY, SupplyOptionsType.getSupplyOptionType(supplyType));
  }

  @ParameterizedTest
  @CsvSource({
    "shipment",
    "Shipment",
    "SHIPMent",
  })
  void shipmentShouldWorkCorrect(String supplyType) {
    assertEquals(SupplyOptionsType.SHIPMENT, SupplyOptionsType.getSupplyOptionType(supplyType));
  }

  @Test
  void shouldGetSupplyOptionsAsExtensionWithDefaultVersion() {
    val sot = SupplyOptionsType.createDefault();
    val ext = sot.asExtension();
    assertNotNull(ext);
  }
}
