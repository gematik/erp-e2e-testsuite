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

package de.gematik.test.erezept.fhir.testutil;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEvent;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEventBundle;
import de.gematik.test.erezept.fhir.values.TelematikID;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventAction;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

public class ErxFhirTestResourceUtil {

  private ErxFhirTestResourceUtil() {
    throw new AssertionError();
  }

  public static ErxAuditEventBundle createErxAuditEventBundle(
      TelematikID telematikId, String agentName) {
    val textItems =
        List.of(
            format("{0} hat mit Ihrer eGK die Liste der offenen E-Rezepte abgerufen.", agentName),
            format(
                "{0} konnte aufgrund eines Fehlerfalls nicht die Liste der offenen E-Rezepte mit"
                    + " Ihrer eGK abrufen.",
                agentName),
            format(
                "{0} hat mit Ihrer eGK die Liste der offenen E-Rezepte abgerufen. (Offline-Check"
                    + " wurde akzeptiert)",
                agentName),
            format(
                "{0} konnte aufgrund eines Fehlerfalls nicht die Liste der offenen E-Rezepte mit"
                    + " Ihrer eGK abrufen. (Offline-Check wurde nicht akzeptiert)",
                agentName));
    val erxAuditEventBundle = new ErxAuditEventBundle();
    textItems.forEach(
        i ->
            erxAuditEventBundle.addEntry(
                new BundleEntryComponent()
                    .setResource(
                        createErxAuditEvent(i, telematikId, agentName, AuditEventAction.R))));
    return erxAuditEventBundle;
  }

  public static ErxAuditEvent createErxAuditEvent(
      String text, TelematikID telematikId, String agentName, AuditEventAction action) {
    val erxAuditEvent = new ErxAuditEvent();
    erxAuditEvent.setMeta(
        new Meta().addProfile(ErpWorkflowStructDef.AUDIT_EVENT.getCanonicalUrl()));
    val narrative = new Narrative();
    narrative.setDivAsString(format("<div xmlns=\"http://www.w3.org/1999/xhtml\">{0}</div>", text));
    erxAuditEvent.setText(narrative);
    erxAuditEvent.setAction(action);
    val agentReference =
        new Reference().setIdentifier(new Identifier().setValue(telematikId.getValue()));
    val agent = erxAuditEvent.addAgent();
    agent.setWho(agentReference);
    agent.setName(agentName);
    return erxAuditEvent;
  }
}
