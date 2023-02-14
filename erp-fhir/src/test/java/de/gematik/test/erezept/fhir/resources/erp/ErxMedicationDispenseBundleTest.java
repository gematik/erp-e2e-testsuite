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

package de.gematik.test.erezept.fhir.resources.erp;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import lombok.val;
import org.junit.jupiter.api.Test;

class ErxMedicationDispenseBundleTest extends ParsingTest {

  private final String BASE_PATH = "fhir/valid/erp/1.1.1/";

  @Test
  void shouldCastFromMedicationDispense() {
    val fileName = "MedicationDispenseBundle_01.json";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val vr = parser.validate(content);

    if (!vr.isSuccessful()) {
      System.out.println(
          format("Found {0} errors in {1}", vr.getMessages().size(), BASE_PATH + fileName));
      vr.getMessages().forEach(System.out::println);
    }
    assertTrue(vr.isSuccessful(), format("{0} must be valid, but is not", fileName));

    val bundle = parser.decode(ErxMedicationDispenseBundle.class, content);
    assertNotNull(bundle);
    assertEquals(3, bundle.getMedicationDispenses().size());
  }
}
