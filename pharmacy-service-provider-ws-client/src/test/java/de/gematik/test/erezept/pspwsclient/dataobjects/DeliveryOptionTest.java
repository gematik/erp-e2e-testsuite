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

package de.gematik.test.erezept.pspwsclient.dataobjects;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DeliveryOptionTest {

  @Test
  void shouldResponseNull() {
    assertThrows(
        InvalidDeliveryOptionException.class,
        () -> {
          DeliveryOption.defineDeliveryOption("null");
        });
  }

  @Test
  void shouldResponseNull2() {
    assertThrows(
        InvalidDeliveryOptionException.class,
        () -> {
          DeliveryOption.defineDeliveryOption("apo");
        });
  }

  @Test
  void shouldResponseNull3() {
    assertThrows(
        InvalidDeliveryOptionException.class,
        () -> {
          DeliveryOption.defineDeliveryOption("local");
        });
  }

  @Test
  void shouldResponseNull4() {
    assertThrows(
        InvalidDeliveryOptionException.class,
        () -> {
          DeliveryOption.defineDeliveryOption("pickup");
        });
  }

  @Test
  void shouldResponseNull5() {
    assertThrows(
        InvalidDeliveryOptionException.class,
        () -> {
          DeliveryOption.defineDeliveryOption("getLocal");
        });
  }

  @Test
  void shouldResponseShipment() {
    assertEquals(DeliveryOption.SHIPMENT, DeliveryOption.defineDeliveryOption("shipment"));
  }

  @Test
  void shouldResponseShipment2() {
    assertEquals(DeliveryOption.SHIPMENT, DeliveryOption.defineDeliveryOption("versandapotheke"));
  }

  @Test
  void shouldResponseShipment3() {
    assertEquals(DeliveryOption.SHIPMENT, DeliveryOption.defineDeliveryOption("versand"));
  }

  @Test
  void shouldResponseShipment4() {
    assertEquals(DeliveryOption.SHIPMENT, DeliveryOption.defineDeliveryOption("belieferung"));
  }

  @Test
  void shouldResponseOn_Premise1() {
    assertEquals(DeliveryOption.ON_PREMISE, DeliveryOption.defineDeliveryOption("abholen"));
  }

  @Test
  void shouldResponseOn_Premise2() {
    assertEquals(DeliveryOption.ON_PREMISE, DeliveryOption.defineDeliveryOption("abholung"));
  }

  @Test
  void shouldResponseOn_Premise3() {
    assertEquals(DeliveryOption.ON_PREMISE, DeliveryOption.defineDeliveryOption("reservierung"));
  }

  @Test
  void shouldResponseOn_Premise4() {
    assertEquals(DeliveryOption.ON_PREMISE, DeliveryOption.defineDeliveryOption("pick_up"));
  }

  @Test
  void shouldResponseDelivery1() {
    assertEquals(DeliveryOption.DELIVERY, DeliveryOption.defineDeliveryOption("bote"));
  }

  @Test
  void shouldResponseDelivery2() {
    assertEquals(DeliveryOption.DELIVERY, DeliveryOption.defineDeliveryOption("lokalebelieferung"));
  }

  @Test
  void shouldResponseDelivery3() {
    assertEquals(DeliveryOption.DELIVERY, DeliveryOption.defineDeliveryOption("botendienst"));
  }
}
