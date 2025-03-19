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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.valuesets.DocumentType;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.jupiter.api.Test;

class ErxPrescriptionBundleTest extends ErpFhirParsingTest {

  private static final String BASE_PATH = "fhir/valid/erp/";
  private static final String BASE_PATH_1_1_1 = BASE_PATH + "1.1.1/";
  private static final String BASE_PATH_1_2_0 = BASE_PATH + "1.2.0/prescriptionbundle/";

  @Test
  void shouldEncodeSinglePrescriptionBundle111() {
    val fileName = "PrescriptionBundle_01.json";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_1 + fileName);
    val prescriptionBundle = parser.decode(ErxPrescriptionBundle.class, content);
    assertNotNull(prescriptionBundle, "Valid ErxPrescriptionBundle must be parseable");

    val task = prescriptionBundle.getTask();
    assertNotNull(task);
    assertEquals("169.000.000.006.874.07", task.getPrescriptionId().getValue());

    val kbvBundle = prescriptionBundle.getKbvBundle();
    assertNotNull(kbvBundle);
    assertTrue(kbvBundle.isPresent());
    kbvBundle.ifPresent(
        kbvErpBundle ->
            assertEquals("169.000.000.006.874.07", kbvErpBundle.getPrescriptionId().getValue()));

    kbvBundle.ifPresent(
        kbvErpBundle ->
            assertEquals(PrescriptionFlowType.FLOW_TYPE_169, kbvBundle.get().getFlowType()));
    assertEquals(PrescriptionFlowType.FLOW_TYPE_169, task.getFlowType());

    val receipt = prescriptionBundle.getReceipt();
    assertTrue(receipt.isEmpty());
  }

  @Test
  void shouldEncodeSinglePrescriptionBundle120() {
    val fileName = "51fe2824-aed7-4f6a-803b-3e351137d998.json";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_2_0 + fileName);
    val prescriptionBundle = parser.decode(ErxPrescriptionBundle.class, content);
    assertNotNull(prescriptionBundle, "Valid ErxPrescriptionBundle must be parseable");

    val task = prescriptionBundle.getTask();
    assertNotNull(task);
    assertEquals("160.000.031.325.714.07", task.getPrescriptionId().getValue());

    val kbvBundle = prescriptionBundle.getKbvBundle();
    assertNotNull(kbvBundle);
    kbvBundle.ifPresent(
        kbvErpBundle ->
            assertEquals("160.000.031.325.714.07", kbvErpBundle.getPrescriptionId().getValue()));

    kbvBundle.ifPresent(
        kbvErpBundle ->
            assertEquals(PrescriptionFlowType.FLOW_TYPE_160, kbvBundle.get().getFlowType()));
    assertEquals(PrescriptionFlowType.FLOW_TYPE_160, task.getFlowType());

    val receipt = prescriptionBundle.getReceipt();
    assertTrue(receipt.isEmpty());
  }

  @Test
  void shouldGetKbvBundleFromPrescriptionBundle120() {
    val fileName = "51fe2824-aed7-4f6a-803b-3e351137d998.json";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_2_0 + fileName);
    val prescriptionBundle = parser.decode(ErxPrescriptionBundle.class, content);
    assertNotNull(prescriptionBundle, "Valid ErxPrescriptionBundle must be parseable");

    // make sure no ClassCastExceptions are thrown: see TSERP-120
    assertDoesNotThrow(prescriptionBundle::getKbvBundle);
    prescriptionBundle
        .getKbvBundle()
        .ifPresent(kbvErpBundle -> assertDoesNotThrow(kbvErpBundle::getPatient));
    prescriptionBundle
        .getKbvBundle()
        .ifPresent(kbvErpBundle -> assertDoesNotThrow(kbvErpBundle::getMedication));
    prescriptionBundle
        .getKbvBundle()
        .ifPresent(kbvErpBundle -> assertDoesNotThrow(kbvErpBundle::getMedicationRequest));
    prescriptionBundle
        .getKbvBundle()
        .ifPresent(kbvErpBundle -> assertDoesNotThrow(kbvErpBundle::getMedicalOrganization));
    prescriptionBundle
        .getKbvBundle()
        .ifPresent(kbvErpBundle -> assertDoesNotThrow(kbvErpBundle::getCoverage));
    prescriptionBundle
        .getKbvBundle()
        .ifPresent(kbvErpBundle -> assertDoesNotThrow(kbvErpBundle::getPractitioner));
    assertDoesNotThrow(prescriptionBundle::getTask);
    assertDoesNotThrow(prescriptionBundle.getTask()::getAcceptDate);
  }

  @Test
  void shouldEncodeSinglePrescriptionBundleXml120() {
    val fileName = "d95b3ece-cdd7-439d-a062-17bdc2253962.xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_2_0 + fileName);
    val prescriptionBundle = parser.decode(ErxPrescriptionBundle.class, content);
    assertNotNull(prescriptionBundle, "Valid ErxPrescriptionBundle must be parseable");

    val task = prescriptionBundle.getTask();
    assertNotNull(task);
    assertEquals("160.000.031.325.889.64", task.getPrescriptionId().getValue());

    // TODO: check explicitly, that the prescriptionBundle does not have a KbvErpBundle
    //    val kbvBundle = prescriptionBundle.getKbvBundle();
    //    assertNotNull(kbvBundle);
    //    assertEquals("160.000.031.325.889.64", kbvBundle.getPrescriptionId().getValue());
    //
    //    assertEquals(PrescriptionFlowType.FLOW_TYPE_160, kbvBundle.getFlowType());
    assertEquals(PrescriptionFlowType.FLOW_TYPE_160, task.getFlowType());

    val receipt = prescriptionBundle.getReceipt();
    assertTrue(receipt.isPresent());

    val docType = receipt.orElseThrow().getDocumentType();
    assertEquals(DocumentType.RECEIPT, docType);
  }

  @Test
  void shouldFailOnMissingTask() {
    val fileName = "PrescriptionBundle_01.json";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_1 + fileName);
    val prescriptionBundle = parser.decode(ErxPrescriptionBundle.class, content);

    // remove the task from the bundle
    val taskResources =
        prescriptionBundle.getEntry().stream()
            .filter(resource -> resource.getResource().getResourceType().equals(ResourceType.Task))
            .toList();
    prescriptionBundle.getEntry().removeAll(taskResources);

    assertThrows(MissingFieldException.class, prescriptionBundle::getTask);
  }

  @Test
  void shouldFailOnMissingKbvBundle() {
    val fileName = "PrescriptionBundle_01.json";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_1 + fileName);
    val prescriptionBundle = parser.decode(ErxPrescriptionBundle.class, content);

    // remove the KbvBundle from bundle
    val kbvBundleResources =
        prescriptionBundle.getEntry().stream()
            .filter(
                resource -> resource.getResource().getResourceType().equals(ResourceType.Bundle))
            .toList();
    prescriptionBundle.getEntry().removeAll(kbvBundleResources);
    assertTrue(prescriptionBundle.getKbvBundle().isEmpty());
  }

  @Test
  void shouldFailOnInvalidKbvBundle() {
    val fileName = "PrescriptionBundle_01.json";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_1 + fileName);
    val prescriptionBundle = parser.decode(ErxPrescriptionBundle.class, content);

    // invalidate the Profile in the KbvBundle from bundle
    prescriptionBundle.getEntry().stream()
        .map(Bundle.BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Bundle))
        .forEach(
            bundle -> bundle.getMeta().getProfile().get(0).setValueAsString("invalid profile!"));

    assertTrue(prescriptionBundle.getKbvBundle().isEmpty());
  }
}
