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

import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEvent.Representation;
import de.gematik.test.erezept.fhir.testutil.EncodingUtil;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import java.util.Arrays;
import lombok.val;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ErxAuditEventTest extends ParsingTest {

  private final String BASE_PATH = "fhir/valid/erp/1.1.1/";

  @Test
  void representationTextDoesNotThrow() {
    Arrays.stream(Representation.values()).forEach(r -> Assertions.assertDoesNotThrow(r::getText));
  }

  @Test
  void shouldEncodeSingleAuditEvent() {
    val fileName = "AuditEvent_01.json";
    val originalEncoding = EncodingType.JSON;
    val flippedEncoding = EncodingUtil.flipEncoding(originalEncoding);

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val auditEvent = parser.decode(ErxAuditEvent.class, content);

    Assertions.assertDoesNotThrow(auditEvent::toString);

    Assertions.assertNotNull(auditEvent, "Valid ErxAuditEvent must be parseable");

    val expectedAction = AuditEvent.AuditEventAction.fromCode("C");
    Assertions.assertEquals(expectedAction, auditEvent.getAction());

    val expectedIdentifierValue = "606358750";
    val expectedIdentifierSystem = ErpWorkflowNamingSystem.TELEMATIK_ID.getCanonicalUrl();
    Assertions.assertEquals(
        expectedIdentifierValue, auditEvent.getAgentFirstRep().getWho().getIdentifier().getValue());
    Assertions.assertEquals(
        expectedIdentifierSystem,
        auditEvent.getAgentFirstRep().getWho().getIdentifier().getSystem());

    // flip the encoding and check again
    val flippedContent = parser.encode(auditEvent, flippedEncoding);
    val flippedAuditEvent = parser.decode(ErxAuditEvent.class, flippedContent);

    Assertions.assertEquals(auditEvent.getAction(), flippedAuditEvent.getAction());
    Assertions.assertEquals(
        auditEvent.getAgentFirstRep().getWho().getIdentifier().getValue(),
        flippedAuditEvent.getAgentFirstRep().getWho().getIdentifier().getValue());
    Assertions.assertEquals(
        auditEvent.getAgentFirstRep().getWho().getIdentifier().getSystem(),
        flippedAuditEvent.getAgentFirstRep().getWho().getIdentifier().getSystem());
    Assertions.assertEquals("Sample Text", auditEvent.getFirstText());
    Assertions.assertEquals("606358750", auditEvent.getAgentId());
    Assertions.assertEquals("Praxis Dr. Müller", auditEvent.getAgentName());
  }

  @Test
  void shouldEncodeAuditEventWithoutProfile() {
    val fileName = "AuditEventBundle.json";

    val content = ResourceUtils.readFileFromResource(format("fhir/invalid/erp/{0}", fileName));
    val bundle = parser.decode(content);
    assertEquals(ResourceType.Bundle, bundle.getResourceType());
  }
}
