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
 */

package de.gematik.test.erezept.fhir.r4.erp;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.DocumentType;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;

class ErxReceiptTest extends ErpFhirParsingTest {
  private static final String BASE_PATH = "fhir/valid/erp/1.1.1/";

  @Test
  void shouldEncodeSingleReceipt() {
    val fileName = "Receipt_01.xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
    val receipt = parser.decode(ErxReceipt.class, content);
    assertNotNull(receipt, "Valid ErxReceipt must be parseable");
    assertEquals(
        new PrescriptionId(ErpWorkflowNamingSystem.PRESCRIPTION_ID, "160.123.456.789.123.58"),
        receipt.getPrescriptionId());
    assertEquals(DocumentType.RECEIPT, receipt.getDocumentType());
  }

  @Test
  void shouldCreateFromResource() {
    val fileName = "Receipt_01.xml";
    val content = ResourceLoader.readFileFromResource("fhir/valid/erp/1.1.1/" + fileName);
    Resource receiptResource = parser.decode(Bundle.class, content);
    assertNotNull(receiptResource, "Valid ErxReceipt must be parseable");

    val erxReceipt = ErxReceipt.fromBundle(receiptResource);
    assertEquals(
        new PrescriptionId(ErpWorkflowNamingSystem.PRESCRIPTION_ID, "160.123.456.789.123.58"),
        erxReceipt.getPrescriptionId());
  }

  @Test
  void shouldThrowMissingFieldException() {
    val fileName = "Receipt_01.xml";
    val content = ResourceLoader.readFileFromResource("fhir/valid/erp/1.1.1/" + fileName);
    val receiptResource = parser.decode(ErxReceipt.class, content);
    receiptResource.getComposition().setType(null);
    assertThrows(MissingFieldException.class, receiptResource::getDocumentType);
  }

  @Test
  void getDocumentTypeShouldWork() {
    val fileName = "NeueVersion_0e0f861-0000-0000-0003-000000000000.xml";
    val content =
        ResourceLoader.readFileFromResource("fhir/valid/erp/1.2.0/receiptbundle/" + fileName);
    val receipt = parser.decode(ErxReceipt.class, content);
    assertNotNull(receipt.getDocumentType());
    assertEquals(DocumentType.RECEIPT, receipt.getDocumentType());
  }

  @Test
  void getCompositionShouldWork() {
    val fileName = "dffbfd6a-5712-4798-bdc8-07201eb77ab8.xml";
    val content =
        ResourceLoader.readFileFromResource("fhir/valid/erp/1.2.0/receiptbundle/" + fileName);
    val receipt = parser.decode(ErxReceipt.class, content);
    assertNotNull(receipt.getComposition());
    assertEquals(Composition.CompositionStatus.FINAL, receipt.getComposition().getStatus());
  }

  @Test
  void getAuthorShouldWork() {
    val fileName = "NeueVersion_0e0f861-0000-0000-0003-000000000000.xml";
    val content =
        ResourceLoader.readFileFromResource("fhir/valid/erp/1.2.0/receiptbundle/" + fileName);
    val receipt = parser.decode(ErxReceipt.class, content);
    assertNotNull(receipt.getAuthor());
    assertTrue(receipt.getAuthor().getReference().startsWith("urn:uuid:"));
  }

  @Test
  void getSectionShouldWork() {
    val fileName = "NeueVersion_0e0f861-0000-0000-0003-000000000000.xml";
    val content =
        ResourceLoader.readFileFromResource("fhir/valid/erp/1.2.0/receiptbundle/" + fileName);
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
    val fileName = "org_fd_response.xml";
    val content =
        ResourceLoader.readFileFromResource("fhir/valid/erp/1.2.0/receiptbundle/" + fileName);
    val bundle = parser.decode(ErxReceipt.class, content);
    val binary = bundle.getQesDigestBinary();
    val code = binary;
    assertEquals("eNMcZvslUsx75vwaQd4McWdHcfjHELsX/y02wsbRjlo=", code.getContentAsBase64());
  }
}
