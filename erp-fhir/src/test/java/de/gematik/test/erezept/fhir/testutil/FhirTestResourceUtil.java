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

package de.gematik.test.erezept.fhir.testutil;

import java.util.LinkedList;
import lombok.val;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.OperationOutcome;

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
}
