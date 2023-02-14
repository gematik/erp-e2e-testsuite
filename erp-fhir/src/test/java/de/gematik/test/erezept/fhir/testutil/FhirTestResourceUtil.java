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

package de.gematik.test.erezept.fhir.testutil;

import static java.text.MessageFormat.*;

import de.gematik.test.erezept.fhir.parser.profiles.definitions.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.values.*;
import java.util.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.AuditEvent.*;
import org.hl7.fhir.r4.model.Bundle.*;

public class FhirTestResourceUtil {

  private FhirTestResourceUtil() {
    throw new AssertionError();
  }

  public static OperationOutcome createOperationOutcome() {
    val issue = new OperationOutcome.OperationOutcomeIssueComponent();
    issue.setCode(OperationOutcome.IssueType.VALUE);
    issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
    val oo = new OperationOutcome();
    val issueList = new LinkedList<OperationOutcome.OperationOutcomeIssueComponent>();
    issueList.add(issue);
    oo.setIssue(issueList);

    oo.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
    oo.getText().setDivAsString("<div>narrative</div>");
    oo.setId(IdType.newRandomUuid());
    return oo;
  }

  public static ErxAuditEventBundle createErxAuditEventBundle(
      TelematikID telematikId, String agentName, String checksum) {
    val textItems =
        List.of(
            format(
                "{0} hat mit Ihrer Gesundheitskarte alle Ihre einlösbaren E-Rezepte abgerufen (Prüfziffer: {1}).",
                agentName, checksum),
            format(
                "{0} hat mit Ihrer Gesundheitskarte alle Ihre einlösbaren E-Rezepte abgerufen. (Keine Prüfziffer vorhanden)",
                agentName),
            format(
                "{0} konnte aufgrund eines Fehlers Ihre E-Rezepte nicht mit Ihrer Gesundheitskarte abrufen.",
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
