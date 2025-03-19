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

package de.gematik.test.erezept.fhir.r4.kbv;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.jupiter.api.Test;

class KbvEvdgaBundleTest extends ErpFhirParsingTest {

  private static final String BASE_PATH_1_1 = "fhir/valid/kbv_evdga/1.1/";

  @Test
  void shouldGetHealthAppRequestFromBundle() {
    val expectedID = "EVDGA_Bundle";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1 + fileName);
    val bundle = parser.decode(KbvEvdgaBundle.class, content);

    assertEquals(PrescriptionFlowType.FLOW_TYPE_162, bundle.getFlowType());
    assertEquals("162.100.000.000.027.75", bundle.getPrescriptionId().getValue());

    val appRequest = bundle.getHealthAppRequest();
    assertNotNull(appRequest);

    val patient = bundle.getPatient();
    assertNotNull(patient);
    assertEquals("X234567890", patient.getKvnr().getValue());

    val practitioner = bundle.getPractitioner();
    assertNotNull(practitioner);
    assertEquals("838382202", practitioner.getANR().getValue());
    assertTrue(bundle.getPractitionerRole().isEmpty());

    val medicalOrg = bundle.getMedicalOrganization();
    assertNotNull(medicalOrg);
    assertEquals("031234567", medicalOrg.getBsnr().getValue());

    val coverage = bundle.getCoverage();
    assertNotNull(coverage);
    assertEquals("104212059", coverage.getIknr().getValue());

    val description = assertDoesNotThrow(bundle::getDescription);
    assertFalse(description.isEmpty());
  }

  @Test
  void shouldReadAllPractitioners() {
    val expectedID = "EVDGA_Bundle_BG_Arbeitsunfall_3";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1 + fileName);
    val bundle = parser.decode(KbvEvdgaBundle.class, content);

    val practitioners = bundle.getAllPractitioners();
    val lanrs = List.of("838382210", "838382202");
    assertEquals(lanrs.size(), practitioners.size());
    practitioners.forEach(
        practitioner -> {
          assertTrue(lanrs.contains(practitioner.getANR().getValue()));
        });
  }

  @Test
  void shouldReadRole() {
    val expectedID = "EVDGA_Bundle_Krankenhaus";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1 + fileName);
    val bundle = parser.decode(KbvEvdgaBundle.class, content);

    val practitionerRole = bundle.getPractitionerRole();
    assertTrue(practitionerRole.isPresent());
  }

  @Test
  void shouldThrowOnMissingDeviceRequest() {
    val expectedID = "EVDGA_Bundle_BG_Arbeitsunfall_3";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1 + fileName);
    val bundle = parser.decode(KbvEvdgaBundle.class, content);

    bundle
        .getEntry()
        .removeIf(
            entry -> entry.getResource().getResourceType().equals(ResourceType.DeviceRequest));
    assertThrows(MissingFieldException.class, bundle::getHealthAppRequest);
  }

  @Test
  void shouldCastPractitionerRole() {
    val expectedID = "EVDGA_Bundle_BG_Arbeitsunfall_3";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1 + fileName);
    val bundle = parser.decode(KbvEvdgaBundle.class, content);

    bundle.addEntry(new BundleEntryComponent().setResource(new PractitionerRole()));
    val practitionerRoleOptional = assertDoesNotThrow(bundle::getPractitionerRole);
    assertTrue(practitionerRoleOptional.isPresent());
    assertEquals(KbvPractitionerRole.class, practitionerRoleOptional.get().getClass());
  }
}
