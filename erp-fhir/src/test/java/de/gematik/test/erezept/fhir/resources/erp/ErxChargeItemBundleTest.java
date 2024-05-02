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

package de.gematik.test.erezept.fhir.resources.erp;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.exceptions.*;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.util.*;
import lombok.*;
import org.junit.jupiter.api.*;

class ErxChargeItemBundleTest extends ParsingTest {

  private final String BASE_PATH_VALID = "fhir/valid/erp/1.2.0/chargeitembundle/";

  @Test
  void getChargeItemAsPharmacy() {
    val fileName = "ea33a992-a214-11ed-a8fc-0242ac120002.xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH_VALID + fileName);
    val bundle = parser.decode(ErxChargeItemBundle.class, content);
    assertNotNull(bundle, "Valid ErxTaskBundle must be parseable");
    assertNotNull(bundle.getChargeItem());
    assertEquals("200.086.824.605.539.20", bundle.getChargeItem().getPrescriptionId().getValue());
    assertTrue(bundle.getChargeItem().getAccessCode().isEmpty());
    assertDoesNotThrow(bundle::getAbgabedatenBundle);
  }

  @Test
  void getChargeItemAsPatient() {
    val fileName = "abc825bc-bc30-45f8-b109-1b343fff5c45.json";

    val content = ResourceUtils.readFileFromResource(BASE_PATH_VALID + fileName);
    val bundle = parser.decode(ErxChargeItemBundle.class, content);
    assertNotNull(bundle, "Valid ErxTaskBundle must be parseable");
    assertNotNull(bundle.getChargeItem());
    assertEquals("200.086.824.605.539.20", bundle.getChargeItem().getPrescriptionId().getValue());
    assertEquals(
        "777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea",
        bundle.getChargeItem().getAccessCode().orElseThrow().getValue());
    assertEquals(
        "Bundle/dffbfd6a-5712-4798-bdc8-07201eb77ab8",
        bundle.getChargeItem().getReceiptReference().orElseThrow());
    assertTrue(bundle.getReceipt().isPresent());
  }

  @Test
  void shouldThrowException() {
    val fileName = "InValidBundle_40057350-a305-11ed-a8fc-0242ac120002.xml";
    String BASE_PATH_INVALID = "fhir/invalid/erp/1.2.0/chargeitembundle/";
    val content = ResourceUtils.readFileFromResource(BASE_PATH_INVALID + fileName);
    val bundle = parser.decode(ErxChargeItemBundle.class, content);
    assertThrows(MissingFieldException.class, bundle::getChargeItem);
  }
}
