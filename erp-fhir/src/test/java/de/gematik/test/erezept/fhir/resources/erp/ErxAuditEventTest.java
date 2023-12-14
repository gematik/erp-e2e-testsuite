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

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEvent.Representation;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import java.util.Arrays;
import lombok.val;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.jupiter.api.Test;

class ErxAuditEventTest extends ParsingTest {

  private final String BASE_PATH = "fhir/valid/erp/1.1.1/";

  @Test
  void representationTextDoesNotThrow() {
    Arrays.stream(Representation.values()).forEach(r -> assertDoesNotThrow(r::getText));
  }

  @Test
  void shouldEncodeSingleAuditEvent() {
    val fileName = "AuditEvent_01.json";
    val originalEncoding = EncodingType.JSON;
    val flippedEncoding = originalEncoding.flipEncoding();

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val auditEvent = parser.decode(ErxAuditEvent.class, content);

    assertDoesNotThrow(auditEvent::toString);

    assertNotNull(auditEvent, "Valid ErxAuditEvent must be parseable");

    val expectedAction = AuditEvent.AuditEventAction.fromCode("C");
    assertEquals(expectedAction, auditEvent.getAction());

    val expectedIdentifierValue = "606358750";
    val expectedIdentifierSystem = ErpWorkflowNamingSystem.TELEMATIK_ID.getCanonicalUrl();
    val expectedPrescriptionId = "160.123.456.789.123.58";
    assertEquals(
        expectedIdentifierValue, auditEvent.getAgentFirstRep().getWho().getIdentifier().getValue());
    assertEquals(
        expectedIdentifierSystem,
        auditEvent.getAgentFirstRep().getWho().getIdentifier().getSystem());
    assertTrue(auditEvent.getPrescriptionId().isPresent());
    assertEquals(expectedPrescriptionId, auditEvent.getPrescriptionId().orElseThrow().getValue());

    // flip the encoding and check again
    val flippedContent = parser.encode(auditEvent, flippedEncoding);
    val flippedAuditEvent = parser.decode(ErxAuditEvent.class, flippedContent);

    assertEquals(auditEvent.getAction(), flippedAuditEvent.getAction());
    assertEquals(
        auditEvent.getAgentFirstRep().getWho().getIdentifier().getValue(),
        flippedAuditEvent.getAgentFirstRep().getWho().getIdentifier().getValue());
    assertEquals(
        auditEvent.getAgentFirstRep().getWho().getIdentifier().getSystem(),
        flippedAuditEvent.getAgentFirstRep().getWho().getIdentifier().getSystem());
    assertEquals("Sample Text", auditEvent.getFirstText());
    assertEquals("606358750", auditEvent.getAgentId());
    assertEquals("Praxis Dr. Müller", auditEvent.getAgentName());
  }

  @Test
  void shouldDecodeAuditEventWithoutProfile() {
    val fileName = "AuditEventBundle.json";

    val content = ResourceUtils.readFileFromResource(format("fhir/invalid/erp/{0}", fileName));
    val bundle = parser.decode(content);
    assertEquals(ResourceType.Bundle, bundle.getResourceType());
  }
}
