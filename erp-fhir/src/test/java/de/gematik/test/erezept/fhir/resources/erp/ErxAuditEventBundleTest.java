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

import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.values.*;
import lombok.*;
import org.junit.jupiter.api.*;

class ErxAuditEventBundleTest {

  @BeforeEach
  void setUp() {}

  @Test
  void shouldCorrectFilterAuditEvents() {
    val agentName = "Am Flughafen";
    val agentId = TelematikID.from("3-SMC-B-Testkarte-883110000116873");
    val checksum = "abc";
    val erxAuditEventBundle =
        FhirTestResourceUtil.createErxAuditEventBundle(agentId, agentName, checksum);

    Assertions.assertEquals(3, erxAuditEventBundle.getAuditEvents().size());
    Assertions.assertEquals(3, erxAuditEventBundle.getAuditEvents(agentId).size());
  }
}
