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

package de.gematik.test.erezept.client.usecases;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.client.usecases.search.AuditEventSearch;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import lombok.val;
import org.junit.jupiter.api.Test;

class AuditEventGetByIdCommandTest {

  @Test
  void getRequestLocator() {
    val prescriptionId = PrescriptionId.random();
    val cmd = AuditEventSearch.getAuditEventsFor(prescriptionId);

    val actual = cmd.getRequestLocator();
    assertTrue(actual.contains("/Task")); // Not AuditEvent because of _revinclude
    assertTrue(actual.contains("_revinclude=AuditEvent")); // from Task to AuditEvent via revinclude
    assertTrue(actual.contains("entity.what"));
    assertTrue(actual.contains(prescriptionId.getValue()));
  }

  @Test
  void shouldNotHaveBody() {
    val prescriptionId = PrescriptionId.random();
    val cmd = AuditEventSearch.getAuditEventsFor(prescriptionId);
    assertTrue(cmd.getRequestBody().isEmpty());
  }
}
