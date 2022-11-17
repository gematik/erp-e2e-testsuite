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

import static org.junit.Assert.*;

import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

public class ErxAcceptBundleTest {

  private final String BASE_PATH = "fhir/valid/erp/1.1.1/";

  private FhirParser parser;

  @Before
  public void setUp() {
    this.parser = new FhirParser();
  }

  @Test
  public void shouldEncodeSingleAcceptBundle() {
    val fileName = "TaskAcceptBundle_01.xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val acceptBundle = parser.decode(ErxAcceptBundle.class, content);
    assertNotNull("Valid ErxAcceptBundle must be parseable", acceptBundle);

    val expectedTaskId = "a4c72c7d-1ee3-11b2-825b-c505a820f066";
    val erxTask = acceptBundle.getTask();
    assertEquals(expectedTaskId, erxTask.getUnqualifiedId());

    val expectedKbvBundleId = "a54bfb25-1ee3-11b2-825c-c505a820f066";
    assertEquals(expectedKbvBundleId, acceptBundle.getKbvBundleId());

    val kbvString = acceptBundle.getKbvBundleAsString();
    assertTrue(kbvString.startsWith("<Bundle xmlns=\"http://hl7.org/fhir\">"));
    val kbvBundle = parser.decode(KbvErpBundle.class, kbvString);
    assertEquals("160.002.397.306.500.57", kbvBundle.getPrescriptionId().getValue());
  }

  @Test
  public void shouldEncodeSingleAcceptBundleWithConsent() {
    val fileName = "TaskAcceptBundle_02.xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val acceptBundle = parser.decode(ErxAcceptBundle.class, content);
    assertNotNull("Valid ErxAcceptBundle must be parseable", acceptBundle);

    assertTrue(acceptBundle.hasConsent());
  }
}
