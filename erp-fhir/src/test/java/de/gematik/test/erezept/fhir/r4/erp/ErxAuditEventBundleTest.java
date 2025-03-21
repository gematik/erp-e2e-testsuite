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

package de.gematik.test.erezept.fhir.r4.erp;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ErxFhirTestResourceUtil;
import de.gematik.test.erezept.fhir.values.TelematikID;
import lombok.val;
import org.junit.jupiter.api.Test;

class ErxAuditEventBundleTest extends ErpFhirParsingTest {

  @Test
  void shouldCorrectFilterAuditEvents() {
    val agentName = "Am Flughafen";
    val agentId = TelematikID.from("3-SMC-B-Testkarte-883110000116873");
    val erxAuditEventBundle = ErxFhirTestResourceUtil.createErxAuditEventBundle(agentId, agentName);

    assertEquals(4, erxAuditEventBundle.getAuditEvents().size());
    assertEquals(4, erxAuditEventBundle.getAuditEvents(agentId).size());
  }
}
