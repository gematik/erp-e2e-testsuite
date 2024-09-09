/*
 * Copyright 2024 gematik GmbH
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

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import lombok.val;
import org.junit.jupiter.api.Test;

class ErxTaskBundleTest extends ParsingTest {

  private static final String BASE_PATH = "fhir/valid/erp/1.1.1/";

  @Test
  void shouldGetAllTasksFromBundle() {
    val fileName = "TaskBundle_01.json";

    val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
    val bundle = parser.decode(ErxTaskBundle.class, content);
    assertNotNull(bundle, "Valid ErxTaskBundle must be parseable");
    assertEquals(50, bundle.getTasks().size());
  }

  @Test
  void shouldGetLatestTask() {
    val fileName = "TaskBundle_01.json";

    val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
    val bundle = parser.decode(ErxTaskBundle.class, content);
    assertNotNull(bundle, "Valid ErxTaskBundle must be parseable");
    val latestTask = bundle.getLatestTask();
    assertEquals("160.000.006.259.611.36", latestTask.getPrescriptionId().getValue());
  }
}
