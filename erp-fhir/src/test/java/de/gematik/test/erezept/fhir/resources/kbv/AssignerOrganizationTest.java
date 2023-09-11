/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.fhir.resources.kbv;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import lombok.val;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;

class AssignerOrganizationTest extends ParsingTest {

  private final String BASE_PATH_1_0_2 = "fhir/valid/kbv/1.0.2/bundle/";

  @Test
  void shouldGenerateAssignerOrganizationFromResource() {
    val resource = new Organization();
    val assigner = AssignerOrganization.fromOrganization(resource);
    assertNotNull(assigner);
    assertEquals(AssignerOrganization.class, assigner.getClass());
  }

  @Test
  void shouldRecastFromMedicalOrganization() {
    val expectedID = "sdf6s75f-d959-43f0-8ac4-sd6f7sd6";
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_0_2 + fileName);
    assertTrue(parser.isValid(content));
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    // PKV Prescription must have an assigner Organization
    assertTrue(kbvBundle.getAssignerOrganization().isPresent());
    val assignerOrg = kbvBundle.getAssignerOrganization().orElseThrow();
    val assignerOrg2 = AssignerOrganization.fromOrganization((Resource) assignerOrg);

    // ensure we get back the same object!
    assertEquals(assignerOrg, assignerOrg2);
  }
}
