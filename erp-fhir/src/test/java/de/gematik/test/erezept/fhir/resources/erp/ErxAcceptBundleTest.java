/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.fhir.resources.erp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import de.gematik.test.erezept.fhir.values.TaskId;
import lombok.val;
import org.junit.jupiter.api.Test;

class ErxAcceptBundleTest extends ParsingTest {

  private final String BASE_PATH = "fhir/valid/erp/";
  private final String BASE_PATH_1_1_1 = BASE_PATH + "1.1.1/";
  private final String BASE_PATH_1_2_0 = BASE_PATH + "1.2.0/acceptbundle/";

  @Test
  void shouldEncodeSingleAcceptBundle() {
    val fileName = "TaskAcceptBundle_01.xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_1_1 + fileName);
    val acceptBundle = parser.decode(ErxAcceptBundle.class, content);
    assertNotNull(acceptBundle, "Valid ErxAcceptBundle must be parseable");

    val expectedTaskId = TaskId.from("a4c72c7d-1ee3-11b2-825b-c505a820f066");
    val erxTask = acceptBundle.getTask();
    assertEquals(expectedTaskId, erxTask.getTaskId());
    assertEquals(expectedTaskId, acceptBundle.getTaskId());

    val expectedKbvBundleId = "a54bfb25-1ee3-11b2-825c-c505a820f066";
    assertEquals(expectedKbvBundleId, acceptBundle.getKbvBundleId());

    val kbvString = acceptBundle.getKbvBundleAsString();
    assertTrue(kbvString.startsWith("<Bundle xmlns=\"http://hl7.org/fhir\">"));
    assertTrue(acceptBundle.getSignedKbvBundle().length > 0);
    val kbvBundle = parser.decode(KbvErpBundle.class, kbvString);
    assertEquals("160.002.397.306.500.57", kbvBundle.getPrescriptionId().getValue());
  }

  @Test
  void shouldEncodeSingleAcceptBundle120() {
    val fileName = "cef4b960-7ce4-4755-b4ce-3b01a30ec2f0.xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_2_0 + fileName);
    val acceptBundle = parser.decode(ErxAcceptBundle.class, content);
    assertNotNull(acceptBundle, "Valid ErxAcceptBundle must be parseable");

    val expectedTaskId = "160.000.031.325.758.69";
    val erxTask = acceptBundle.getTask();
    assertEquals(expectedTaskId, erxTask.getTaskId().getValue());

    val expectedKbvBundleId = "urn:uuid:a03efedd-0100-0000-0001-000000000000";
    assertEquals(expectedKbvBundleId, acceptBundle.getKbvBundleId());

    val kbvString = acceptBundle.getKbvBundleAsString();
    assertTrue(kbvString.startsWith("<Bundle xmlns=\"http://hl7.org/fhir\">"));
    val kbvBundle = parser.decode(KbvErpBundle.class, kbvString);
    assertEquals("160.000.031.325.758.69", kbvBundle.getPrescriptionId().getValue());

    assertEquals(
        "e2715cb0d512591a0c8dece2178b2998c38ea7bfb78b4cd5f805096cbd809f26",
        acceptBundle.getSecret().getValue());
  }

  @Test
  void shouldEncodeSingleAcceptBundleWithConsent() {
    val fileName = "TaskAcceptBundle_02.xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_1_1 + fileName);
    val acceptBundle = parser.decode(ErxAcceptBundle.class, content);
    assertNotNull(acceptBundle, "Valid ErxAcceptBundle must be parseable");

    assertTrue(acceptBundle.hasConsent());
  }
}
