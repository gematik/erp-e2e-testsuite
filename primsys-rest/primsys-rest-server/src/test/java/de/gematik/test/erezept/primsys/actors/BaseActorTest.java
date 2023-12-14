package de.gematik.test.erezept.primsys.actors;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskCreateCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;

import de.gematik.test.erezept.fhir.testutil.FhirTestResourceUtil;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import de.gematik.test.erezept.primsys.model.ActorContext;

import jakarta.ws.rs.WebApplicationException;

import lombok.val;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BaseActorTest extends TestWithActorContext {

  @Test
  void shouldCreateErpResponse() {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(1);
    val mockClient = pharmacy.getClient();

    val mockResponse =
        ErpResponse.forPayload(FhirTestResourceUtil.createOperationOutcome(), ErxTask.class)
            .withHeaders(Map.of())
            .withStatusCode(500)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    when(mockClient.request(any(TaskCreateCommand.class))).thenReturn(mockResponse);
    val command = new TaskCreateCommand();
    assertThrows(WebApplicationException.class, () -> pharmacy.erpRequest(command));
  }
}
