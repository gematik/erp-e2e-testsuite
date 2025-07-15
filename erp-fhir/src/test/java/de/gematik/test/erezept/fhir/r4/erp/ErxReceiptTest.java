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

package de.gematik.test.erezept.fhir.r4.erp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.DocumentType;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;

class ErxReceiptTest extends ErpFhirParsingTest {

  private static final String BASE_PATH = "fhir/valid/erp/";
  private static final String BASE_PATH_1_4 = BASE_PATH + "1.4.0/receiptbundle/";
  private static final String CONCRETE_1_4_BUNDLE =
      BASE_PATH_1_4 + "dffbfd6a-5712-4798-bdc8-07201eb77ab8.json";

  @Test
  void shouldEncodeSingleReceipt() {
    val fileName = "dffbfd6a-5712-4798-bdc8-07201eb77ab8.json";
    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4 + fileName);
    val receipt = parser.decode(ErxReceipt.class, content);
    assertNotNull(receipt, "Valid ErxReceipt must be parseable");
    val exppid = PrescriptionId.from("160.000.033.491.280.78");
    assertEquals(exppid, receipt.getPrescriptionId());
    assertEquals(DocumentType.RECEIPT, receipt.getDocumentType());
  }

  @Test
  void shouldCreateFromResource() {
    val fileName = "dffbfd6a-5712-4798-bdc8-07201eb77ab8.json";
    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4 + fileName);
    Resource receiptResource = parser.decode(Bundle.class, content);
    assertNotNull(receiptResource, "Valid ErxReceipt must be parseable");

    val receipt = ErxReceipt.fromBundle(receiptResource);
    val exppid = PrescriptionId.from("160.000.033.491.280.78");
    assertEquals(exppid, receipt.getPrescriptionId());
  }

  @Test
  void shouldThrowMissingFieldException() {
    val fileName = "dffbfd6a-5712-4798-bdc8-07201eb77ab8.json";
    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4 + fileName);
    val receiptResource = parser.decode(ErxReceipt.class, content);
    receiptResource.getComposition().setType(null);
    assertThrows(MissingFieldException.class, receiptResource::getDocumentType);
  }

  @Test
  void getDocumentTypeShouldWork() {
    val content = ResourceLoader.readFileFromResource(CONCRETE_1_4_BUNDLE);
    val receipt = parser.decode(ErxReceipt.class, content);
    assertNotNull(receipt.getDocumentType());
    assertEquals(DocumentType.RECEIPT, receipt.getDocumentType());
  }

  @Test
  void getCompositionShouldWork() {
    val content = ResourceLoader.readFileFromResource(CONCRETE_1_4_BUNDLE);
    val receipt = parser.decode(ErxReceipt.class, content);
    assertNotNull(receipt.getComposition());
    assertEquals(Composition.CompositionStatus.FINAL, receipt.getComposition().getStatus());
  }

  @Test
  void getAuthorShouldWork() {
    val content = ResourceLoader.readFileFromResource(CONCRETE_1_4_BUNDLE);
    val receipt = parser.decode(ErxReceipt.class, content);
    assertNotNull(receipt.getAuthor());
    assertTrue(receipt.getAuthor().getReference().startsWith("urn:uuid:"));
  }

  @Test
  void getSectionShouldWork() {
    val content = ResourceLoader.readFileFromResource(CONCRETE_1_4_BUNDLE);
    val receipt = parser.decode(ErxReceipt.class, content);
    assertNotNull(receipt.getQesDigestRefInComposSect());
    assertTrue(
        receipt
            .getQesDigestRefInComposSect()
            .getEntryFirstRep()
            .getReference()
            .startsWith("urn:uuid:"));
  }

  @Test
  void getPrescriptionDigitShouldWork() {
    val content = ResourceLoader.readFileFromResource(CONCRETE_1_4_BUNDLE);
    val bundle = parser.decode(ErxReceipt.class, content);
    val qes = bundle.getQesDigestBinary();
    assertEquals("tJg8c5ZtdhzEEhJ0ZpAsUVFx5dKuYgQFs5oKgthi17M=", qes.getContentAsBase64());
  }
}
