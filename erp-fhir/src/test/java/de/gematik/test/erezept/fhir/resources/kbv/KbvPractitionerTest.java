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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.values.LANR;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import lombok.val;
import org.junit.jupiter.api.Test;

class KbvPractitionerTest extends ParsingTest {

  private final String BASE_PATH = "fhir/valid/kbv/1.0.2/bundle/";

  @Test
  void testEncodingLANRPractitionerFromKbvBundle() {
    val kbvId = "5a3458b0-8364-4682-96e2-b262b2ab16eb";
    val fileName = kbvId + ".xml";

    val expLanr = new LANR("754236701");
    val expName = "Ben Schulz";
    val expQualification = QualificationType.DOCTOR;
    val expAdditionalQualification = "Facharzt für Allgemeinmedizin";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);
    val practitioner = kbvBundle.getPractitioner();

    assertNotNull(practitioner);
    assertEquals(expLanr, practitioner.getANR());
    assertEquals(BaseANR.ANRType.LANR, practitioner.getANRType());
    assertEquals(expName, practitioner.getFullName());
    assertEquals(expQualification, practitioner.getQualificationType());
    assertEquals(1, practitioner.getAdditionalQualifications().size());
    assertEquals(expAdditionalQualification, practitioner.getAdditionalQualifications().get(0));
    // just for the sake of code-coverage
    assertNotNull(practitioner.getDescription());
  }
}
