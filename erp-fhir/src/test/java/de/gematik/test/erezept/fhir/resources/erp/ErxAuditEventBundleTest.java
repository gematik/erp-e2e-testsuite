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

import de.gematik.test.erezept.fhir.testutil.FhirTestResourceUtil;
import de.gematik.test.erezept.fhir.values.TelematikID;
import lombok.val;
import org.junit.jupiter.api.Test;

class ErxAuditEventBundleTest {

  @Test
  void shouldCorrectFilterAuditEvents() {
    val agentName = "Am Flughafen";
    val agentId = TelematikID.from("3-SMC-B-Testkarte-883110000116873");
    val erxAuditEventBundle = FhirTestResourceUtil.createErxAuditEventBundle(agentId, agentName);

    assertEquals(4, erxAuditEventBundle.getAuditEvents().size());
    assertEquals(4, erxAuditEventBundle.getAuditEvents(agentId).size());
  }
}
