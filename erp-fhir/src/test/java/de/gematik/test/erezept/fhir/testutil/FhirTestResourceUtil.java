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

package de.gematik.test.erezept.fhir.testutil;

import static java.text.MessageFormat.format;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEvent;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEventBundle;
import de.gematik.test.erezept.fhir.values.TelematikID;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventAction;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

public class FhirTestResourceUtil {

  private FhirTestResourceUtil() {
    throw new AssertionError();
  }

  public static OperationOutcome createOperationOutcome() {
    val issue = new OperationOutcome.OperationOutcomeIssueComponent();
    issue.setCode(OperationOutcome.IssueType.VALUE);
    issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
    issue.getDetails().setText("error details");
    issue.setDiagnostics("additional diagnostics about the error");
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

  public static ValidationResult createEmptyValidationResult() {
    val vr = mock(ValidationResult.class);
    when(vr.isSuccessful()).thenReturn(true);
    when(vr.getMessages()).thenReturn(List.of());
    return vr;
  }

  public static ValidationResult createFailingValidationResult() {
    val vr = mock(ValidationResult.class);
    when(vr.isSuccessful()).thenReturn(false);
    val errorMessage = new SingleValidationMessage();
    errorMessage.setMessage("mock error message");
    errorMessage.setSeverity(ResultSeverityEnum.ERROR);
    when(vr.getMessages()).thenReturn(List.of(errorMessage));
    return vr;
  }
}
