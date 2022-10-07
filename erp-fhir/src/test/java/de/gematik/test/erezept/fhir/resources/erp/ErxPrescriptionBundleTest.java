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

import de.gematik.test.erezept.fhir.util.ParsingTest;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import lombok.val;
import org.junit.jupiter.api.Test;

class ErxPrescriptionBundleTest extends ParsingTest {
  private final String BASE_PATH = "fhir/valid/erp/";

  @Test
  void shouldEncodeSinglePrescriptionBundle() {
    val fileName = "PrescriptionBundle_01.json";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);

    val vr = parser.validate(content);

    // give some help on debugging if still errors in "valid resources"
    if (!vr.isSuccessful()) {
      System.out.println("Errors: " + vr.getMessages().size() + " in File " + fileName);
      vr.getMessages().forEach(System.out::println);
    }

    assertTrue(vr.isSuccessful());

    val prescriptionBundle = parser.decode(ErxPrescriptionBundle.class, content);
    assertNotNull(prescriptionBundle, "Valid ErxPrescriptionBundle must be parseable");

    val expectedTaskId = "1a7e4116-1e53-11b2-80ba-c505a820f066";
    val erxTask = prescriptionBundle.getTask();
    assertNotNull(erxTask);
    assertEquals(expectedTaskId, erxTask.getUnqualifiedId());
    assertEquals(PrescriptionFlowType.FLOW_TYPE_160, erxTask.getFlowType());

    val expectedPrescriptionId = "160.002.370.468.800.53";
    val kbvBundle = prescriptionBundle.getKbvBundle();
    assertEquals(expectedPrescriptionId, kbvBundle.getPrescriptionId().getValue());
  }

  @Test
  void shouldNotContainAReceipt() {
    val fileName = "PrescriptionBundle_01.json";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val prescriptionBundle = parser.decode(ErxPrescriptionBundle.class, content);

    assertTrue(prescriptionBundle.getReceipt().isEmpty());
  }
}
