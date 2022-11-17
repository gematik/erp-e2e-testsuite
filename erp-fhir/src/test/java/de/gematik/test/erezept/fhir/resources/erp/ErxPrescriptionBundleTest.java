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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.jupiter.api.Test;

class ErxPrescriptionBundleTest extends ParsingTest {

  private final String BASE_PATH = "fhir/valid/erp/1.1.1/";

  @Test
  void shouldEncodeSinglePrescriptionBundle() {
    val fileName = "PrescriptionBundle_01.json";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val prescriptionBundle = parser.decode(ErxPrescriptionBundle.class, content);
    assertNotNull(prescriptionBundle, "Valid ErxPrescriptionBundle must be parseable");

    val task = prescriptionBundle.getTask();
    assertNotNull(task);
    assertEquals("169.000.000.006.874.07", task.getPrescriptionId().getValue());

    val kbvBundle = prescriptionBundle.getKbvBundle();
    assertNotNull(kbvBundle);
    assertEquals("169.000.000.006.874.07", kbvBundle.getPrescriptionId().getValue());

    val receipt = prescriptionBundle.getReceipt();
    assertTrue(receipt.isEmpty());
  }

  @Test
  void shouldFailOnMissingTask() {
    val fileName = "PrescriptionBundle_01.json";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val prescriptionBundle = parser.decode(ErxPrescriptionBundle.class, content);

    // remove the task from bundle
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

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val prescriptionBundle = parser.decode(ErxPrescriptionBundle.class, content);

    // remove the KbvBundle from bundle
    val kbvBundleResources =
        prescriptionBundle.getEntry().stream()
            .filter(
                resource -> resource.getResource().getResourceType().equals(ResourceType.Bundle))
            .toList();
    prescriptionBundle.getEntry().removeAll(kbvBundleResources);

    assertThrows(MissingFieldException.class, prescriptionBundle::getKbvBundle);
  }

  @Test
  void shouldFailOnInvalidKbvBundle() {
    val fileName = "PrescriptionBundle_01.json";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val prescriptionBundle = parser.decode(ErxPrescriptionBundle.class, content);

    // invalidate the Profile in the KbvBundle from bundle
    prescriptionBundle.getEntry().stream()
        .map(Bundle.BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Bundle))
        .forEach(
            bundle -> bundle.getMeta().getProfile().get(0).setValueAsString("invalid profile!"));

    assertThrows(MissingFieldException.class, prescriptionBundle::getKbvBundle);
  }
}
