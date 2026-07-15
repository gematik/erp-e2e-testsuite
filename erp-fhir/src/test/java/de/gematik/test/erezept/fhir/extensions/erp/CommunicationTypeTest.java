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

package de.gematik.test.erezept.fhir.extensions.erp;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

class CommunicationTypeTest {

  @Test
  void shouldHaveCorrectLabels() {
    assertEquals("text", CommunicationType.TEXT.getLabel());
    assertEquals("link", CommunicationType.LINK.getLabel());
    assertEquals("paymentInfo", CommunicationType.PAYMENT_INFO.getLabel());
    assertEquals("deliveryStatus", CommunicationType.DELIVERY_STATUS.getLabel());
    assertEquals("reservationStatus", CommunicationType.RESERVATION_STATUS.getLabel());
    assertEquals("order", CommunicationType.ORDER.getLabel());
  }
}
