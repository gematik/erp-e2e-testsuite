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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import lombok.val;
import org.junit.jupiter.api.Test;

class ErxAcceptBundleTest extends ErpFhirParsingTest {

  private static final String BASE_PATH = "fhir/valid/erp/";
  private static final String BASE_PATH_1_4_0 = BASE_PATH + "1.4.0/acceptbundle/";

  @Test
  void shouldEncodeSingleAcceptBundle120() {
    val fileName = "2c08c718-3d3b-447e-babc-7afef76f0a48.xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4_0 + fileName);
    val acceptBundle = parser.decode(ErxAcceptBundle.class, content);
    assertNotNull(acceptBundle, "Valid ErxAcceptBundle must be parseable");

    val expectedTaskId = "200.000.002.457.470.49";
    val erxTask = acceptBundle.getTask();
    assertEquals(expectedTaskId, erxTask.getTaskId().getValue());

    val expectedKbvBundleId = "c87e7f25-0000-0000-0001-000000000000";
    assertEquals(expectedKbvBundleId, acceptBundle.getKbvBundleId());

    assertEquals(
        "0a33a716b71c4dce81a3edbbd6aaf8296a82f0eead6784766ea3c5a6ce7b7ecc",
        acceptBundle.getSecret().getValue());

    assertTrue(acceptBundle.hasConsent());
  }
}
