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

package de.gematik.test.erezept.primsys.rest.response;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;
import org.junit.Before;
import org.junit.Test;

public class ErrorResponseTest {

  private ObjectMapper mapper;

  @Before
  public void setup() {
    mapper = new ObjectMapper();
  }

  @Test
  public void shouldEncodeSimpleErrorResponse() throws JsonProcessingException {
    val response = new ErrorResponse("error");

    val json = mapper.writeValueAsString(response);
    assertNotNull(json);
  }

  @Test
  public void shouldEncodeOperationOutcomeErrorResponse() throws JsonProcessingException {
    val oo = createOperationOutcome();
    ErpResponse<ErxCommunication> erpResponse = mock(ErpResponse.class);
    when(erpResponse.isOperationOutcome()).thenReturn(true);
    when(erpResponse.getAsOperationOutcome()).thenReturn(oo);

    val response = new ErrorResponse(erpResponse);
    assertNotNull(response.getMessage());
    assertFalse(response.getMessage().contains("Unknown Error:"));
    val json = mapper.writeValueAsString(response);
    assertNotNull(json);
  }

  @Test
  public void shouldEncodeUnexpectedResourceErrorResponse() throws JsonProcessingException {
    val erxTask = mock(ErxTask.class);
    ErpResponse<ErxTask> erpResponse = mock(ErpResponse.class);
    when(erpResponse.isOperationOutcome()).thenReturn(false);
    when(erpResponse.getExpectedResource()).thenReturn(erxTask);

    val response = new ErrorResponse(erpResponse);
    assertNotNull(response.getMessage());
    assertTrue(response.getMessage().contains("Unknown Error:"));
    val json = mapper.writeValueAsString(response);
    assertNotNull(json);
  }

  @Test
  public void shouldEncodeErrorResponseFromEmptyResponse() throws JsonProcessingException {
    ErpResponse<Resource> erpResponse = mock(ErpResponse.class);
    when(erpResponse.isEmptyBody()).thenReturn(true);

    val response = new ErrorResponse(erpResponse);
    assertNotNull(response.getMessage());
    assertTrue(response.getMessage().contains("Unknown Error"));
    val json = mapper.writeValueAsString(response);
    assertNotNull(json);
  }

  private OperationOutcome createOperationOutcome() {
    val issue = new OperationOutcome.OperationOutcomeIssueComponent();
    issue.setCode(OperationOutcome.IssueType.VALUE);
    issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
    val oo = new OperationOutcome();
    oo.setIssue(List.of(issue));

    oo.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
    oo.getText().setDivAsString("<div>narrative</div>");

    return oo;
  }
}
