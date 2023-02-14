/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.fhir.extensions.erp;

import static org.junit.jupiter.api.Assertions.*;

import lombok.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class SupplyOptionsTypeTest {

  @Test
  void createDefaultIsInPremise() {
    assertTrue(SupplyOptionsType.createDefault().isOnPremise());
  }

  @Test
  void createDefaultIsNotDelivery() {
    assertFalse(SupplyOptionsType.createDefault().isDelivery());
  }

  @Test
  void onPremiseShouldWork() {
    assertTrue(SupplyOptionsType.onPremise().isOnPremise());
  }

  @Test
  void deliveryShouldWork() {
    assertTrue(SupplyOptionsType.delivery().isDelivery());
  }

  @Test
  void shipmentShouldWork() {
    assertTrue(SupplyOptionsType.shipment().isShipment());
  }

  @Test
  void shipmentIsNotDelivery() {
    assertFalse(SupplyOptionsType.shipment().isDelivery());
  }

  @Test
  void shipmentIsNotOnPremise() {
    assertFalse(SupplyOptionsType.shipment().isOnPremise());
  }

  @Test
  void deliveryIsNotShipment() {
    assertFalse(SupplyOptionsType.delivery().isShipment());
  }

  @Test
  void deliveryIsNotOnPremise() {
    assertFalse(SupplyOptionsType.shipment().isOnPremise());
  }

  @Test
  void onPremiseIsNotDelivery() {
    assertFalse(SupplyOptionsType.onPremise().isDelivery());
  }

  @Test
  void onPremiseIsNotShipment() {
    assertFalse(SupplyOptionsType.onPremise().isShipment());
  }

  @ParameterizedTest
  @CsvSource({
    "onPremise",
    "onpremise",
    "ONPREMISE",
    "nix ",
  })
  void onPremiseShouldWorkCorrect(String supplyType) {
    assertTrue(SupplyOptionsType.getSupplyOptionType(supplyType).isOnPremise());
  }

  @ParameterizedTest
  @CsvSource({
    "delivery",
    "DELIVERY",
    "deLiVery",
  })
  void deliveryShouldWorkCorrect(String supplyType) {
    assertTrue(SupplyOptionsType.getSupplyOptionType(supplyType).isDelivery());
  }

  @ParameterizedTest
  @CsvSource({
    "shipment",
    "Shipment",
    "SHIPMent",
  })
  void shipmentShouldWorkCorrect(String supplyType) {
    assertTrue(SupplyOptionsType.getSupplyOptionType(supplyType).isShipment());
  }

  @Test
  void shouldGetSupplyOptionsAsExtensionWithDefaultVersion() {
    val sot = SupplyOptionsType.createDefault();
    val ext = sot.asExtension();
    assertNotNull(ext);
  }
}
