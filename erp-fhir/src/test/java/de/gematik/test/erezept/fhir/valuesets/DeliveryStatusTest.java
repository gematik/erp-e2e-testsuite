/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.fhir.valuesets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import lombok.val;
import org.junit.jupiter.api.Test;

class DeliveryStatusTest {

  @Test
  void shouldHaveExpectedCodes() {
    assertEquals("preparedWaiting", DeliveryStatus.PREPARED_WAITING.getCode());
    assertEquals("inTransport", DeliveryStatus.IN_TRANSPORT.getCode());
    assertEquals("delivered", DeliveryStatus.DELIVERED.getCode());
    assertEquals("incident", DeliveryStatus.INCIDENT.getCode());
  }

  @Test
  void shouldHaveExpectedDisplays() {
    assertEquals(
        "Lieferung vorbereitet, wartet auf Abholung", DeliveryStatus.PREPARED_WAITING.getDisplay());
    assertEquals(
        "an Lieferdienst übergeben und in Auslieferung", DeliveryStatus.IN_TRANSPORT.getDisplay());
    assertEquals("Übergeben", DeliveryStatus.DELIVERED.getDisplay());
    assertEquals("Störung bei Lieferung", DeliveryStatus.INCIDENT.getDisplay());
  }

  @Test
  void shouldParseFromCode() {
    val values = Arrays.asList(DeliveryStatus.values());
    values.forEach(v -> assertEquals(v, DeliveryStatus.fromCode(v.getCode())));
  }

  @Test
  void shouldThrowOnInvalidCode() {
    assertThrows(IllegalArgumentException.class, () -> DeliveryStatus.fromCode("invalid"));
  }
}
