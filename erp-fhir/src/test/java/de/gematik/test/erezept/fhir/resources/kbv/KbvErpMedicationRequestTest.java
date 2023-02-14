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
import org.junit.jupiter.api.Test;

class KbvErpMedicationRequestTest extends ParsingTest {

  private final String BASE_PATH = "fhir/valid/kbv/1.0.2/medicationrequest/";

  @Test
  void encodingSingleValidMedicationRequest() {
    val expectedID = "0587787f-3f1b-4578-a412-ce5bae8215b9";
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val medicationRequest = parser.decode(KbvErpMedicationRequest.class, content);

    assertNotNull(medicationRequest);
    assertEquals(expectedID, medicationRequest.getLogicalId());

    assertTrue(medicationRequest.getNoteText().isPresent());
    val expectedNote = "Patient erneut auf Anwendung der Schmelztabletten hinweisen";
    assertEquals(expectedNote, medicationRequest.getNoteTextOrEmpty());
    assertNotNull(medicationRequest.getDescription());
    assertFalse(medicationRequest.allowSubstitution());
    assertTrue(medicationRequest.getDescription().contains("ohne aut-idem"));
  }

  @Test
  void encodeMedicationRequestWithoutNote() {
    val expectedID = "43c2b7ae-ad11-4387-910a-e6b7a3c38d4f";
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val medicationRequest = parser.decode(KbvErpMedicationRequest.class, content);

    assertFalse(medicationRequest.getNoteText().isPresent());
    assertFalse(medicationRequest.isMultiple());
    assertEquals("", medicationRequest.getNoteTextOrEmpty());
    assertEquals("N/A", medicationRequest.getNoteTextOr("N/A"));
  }

  @Test
  void encodeMedicationRequestWithMvo() {
    val expectedID = "43c2b7ae-ad11-4387-910a-e6b7a3c38d3a";
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val medicationRequest = parser.decode(KbvErpMedicationRequest.class, content);

    assertTrue(medicationRequest.isMultiple());
    assertNotNull(medicationRequest.getDescription());
    assertTrue(medicationRequest.getDescription().contains("mit aut-idem"));
  }
}
