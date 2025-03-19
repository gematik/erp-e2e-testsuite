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

package de.gematik.test.erezept.client.usecases;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.val;
import org.junit.jupiter.api.Test;

class MedicationDispenseSearchByIdCommandTest {

  @Test
  void shouldRequestCorrectResource() {
    val prescriptionId = PrescriptionId.random();
    val cmd = new MedicationDispenseSearchByIdCommand(prescriptionId);
    // the Identifier for the search as described in A_22070
    val identifier =
        URLEncoder.encode(
            format("{0}|{1}", prescriptionId.getSystemUrl(), prescriptionId.getValue()),
            StandardCharsets.UTF_8);
    assertEquals(format("/MedicationDispense?identifier={0}", identifier), cmd.getRequestLocator());
    assertEquals(HttpRequestMethod.GET, cmd.getMethod());
  }

  @Test
  void shouldHaveEmptyBody() {
    val prescriptionId = PrescriptionId.random();
    val cmd = new MedicationDispenseSearchByIdCommand(prescriptionId);
    assertTrue(cmd.getRequestBody().isEmpty());
  }
}
