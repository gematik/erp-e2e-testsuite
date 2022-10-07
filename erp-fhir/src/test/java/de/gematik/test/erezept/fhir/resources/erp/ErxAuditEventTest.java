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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.parser.profiles.ErpNamingSystem;
import de.gematik.test.erezept.fhir.testutil.EncodingUtil;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import lombok.val;
import org.hl7.fhir.r4.model.AuditEvent;
import org.junit.Before;
import org.junit.Test;

public class ErxAuditEventTest {

  private final String BASE_PATH = "fhir/valid/erp/";

  private FhirParser parser;

  @Before
  public void setUp() {
    this.parser = new FhirParser();
  }

  @Test
  public void shouldEncodeSingleAuditEvent() {
    val fileName = "AuditEvent_01.json";
    val originalEncoding = EncodingType.JSON;
    val flippedEncoding = EncodingUtil.flipEncoding(originalEncoding);

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val auditEvent = parser.decode(ErxAuditEvent.class, content);
    assertNotNull("Valid ErxAuditEvent must be parseable", auditEvent);

    val expectedAction = AuditEvent.AuditEventAction.fromCode("C");
    assertEquals(expectedAction, auditEvent.getAction());

    val expectedAgentName = "Praxis Dr. Müller";
    assertEquals(expectedAgentName, auditEvent.getAgentFirstRep().getName());

    val expectedIdentifierValue = "606358750";
    val expectedIdentifierSystem = ErpNamingSystem.TELEMATIK_ID.getCanonicalUrl();
    assertEquals(
        expectedIdentifierValue, auditEvent.getAgentFirstRep().getWho().getIdentifier().getValue());
    assertEquals(
        expectedIdentifierSystem,
        auditEvent.getAgentFirstRep().getWho().getIdentifier().getSystem());

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
  }
}
