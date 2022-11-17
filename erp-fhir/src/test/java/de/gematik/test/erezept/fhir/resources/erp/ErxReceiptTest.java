/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.fhir.resources.erp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.DocumentType;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;

class ErxReceiptTest extends ParsingTest {
  private final String BASE_PATH = "fhir/valid/erp/1.1.1/";

  @Test
  void shouldEncodeSingleReceipt() {
    val fileName = "Receipt_01.xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val receipt = parser.decode(ErxReceipt.class, content);
    assertNotNull(receipt, "Valid ErxReceipt must be parseable");
    assertEquals(new PrescriptionId("160.123.456.789.123.58"), receipt.getPrescriptionId());
    assertEquals(DocumentType.RECEIPT, receipt.getDocumentType());
  }

  @Test
  void shouldCreateFromResource() {
    val fileName = "Receipt_01.xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    Resource receiptResource = parser.decode(Bundle.class, content);
    assertNotNull(receiptResource, "Valid ErxReceipt must be parseable");

    val erxReceipt = ErxReceipt.fromBundle(receiptResource);
    assertEquals(new PrescriptionId("160.123.456.789.123.58"), erxReceipt.getPrescriptionId());
  }
}
