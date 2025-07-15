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

package de.gematik.test.erezept.fhir.r4.erp;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.r4.erp.ErxAuditEvent.Representation;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import java.util.Arrays;
import lombok.val;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.jupiter.api.Test;

class ErxAuditEventTest extends ErpFhirParsingTest {

  private final String BASE_PATH = "fhir/valid/erp/1.4.0/auditevent/";

  @Test
  void representationTextDoesNotThrow() {
    Arrays.stream(Representation.values()).forEach(r -> assertDoesNotThrow(r::getText));
  }

  @Test
  void shouldEncodeSingleAuditEvent() {
    val fileName = "9361863d-fec0-4ba9-8776-7905cf1b0cfa.json";
    val originalEncoding = EncodingType.JSON;
    val flippedEncoding = originalEncoding.flipEncoding();

    val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
    val auditEvent = parser.decode(ErxAuditEvent.class, content);

    assertDoesNotThrow(auditEvent::toString);

    assertNotNull(auditEvent, "Valid ErxAuditEvent must be parseable");

    val expectedAction = AuditEvent.AuditEventAction.fromCode("C");
    assertEquals(expectedAction, auditEvent.getAction());

    val expectedIdentifierValue = "1-SMC-B-Testkarte-883110000095957";
    val expectedIdentifierSystem = DeBasisProfilNamingSystem.TELEMATIK_ID_SID.getCanonicalUrl();
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
    assertEquals(
        "Praxis Dr. Müller, Bahnhofstr. 78 hat ein E-Rezept 160.123.456.789.123.58 eingestellt",
        auditEvent.getFirstText());
    assertEquals(expectedIdentifierValue, auditEvent.getAgentId());
    assertEquals("Praxis Dr. Müller", auditEvent.getAgentName());
  }

  @Test
  void shouldDecodeAuditEventWithoutProfile() {
    val fileName = "AuditEventBundle.json";

    val content = ResourceLoader.readFileFromResource(format("fhir/invalid/erp/{0}", fileName));
    val bundle = parser.decode(content);
    assertEquals(ResourceType.Bundle, bundle.getResourceType());
  }
}
