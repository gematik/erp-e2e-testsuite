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

package de.gematik.test.erezept.primsys.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskGetByExamEvidenceCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxTaskBundle;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import de.gematik.test.erezept.primsys.data.error.ErrorDto;
import jakarta.ws.rs.WebApplicationException;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;

class GetPrescriptionsWithPNUseCaseTest extends TestWithActorContext {
  private final String TEST_EVIDENCE =
      "H4sIAAAAAAAA/y2NX0+DMBTF3/kUpO9S2uHftF3mWnVOcDpA44tppELnKAtFYPv0dupJ7k3OvfmdQ6ZjvfV71VrdGApQEAJfmY+m0KakIEtvTi6AbztpCrltjKJgryyYMo+sEn/O4/dcPK8Xj8kveWRdmrEUVF23u4JwsEGpatnpr6BQ8FPC3hY13JkB9scm5vlOJF0zHOIoROgcneHTKCTQnf5+gmECxb9ZvbGndDbGfLGPN264mMQ8QzEvJwm/zrPDrdp8CxQ9vOjZXSXb1zGaV7q9LA8YNcu8vh8ogS7Eczth3g8tA6HU/AAAAA==";

  private ErpResponse<ErxTaskBundle> buildResponse(String content) {
    val erxTaskBundle = fhir.decode(ErxTaskBundle.class, content);
    val erpResponseBuilder =
        ErpResponse.forPayload(erxTaskBundle, ErxTaskBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(200);
    return erpResponseBuilder.andValidationResult(
        FhirTestResourceUtil.createEmptyValidationResult());
  }

  private ErpResponse<ErxTaskBundle> buildResponseAsOpOutcome() {
    val erpResponse =
        ErpResponse.forPayload(FhirTestResourceUtil.createOperationOutcome(), ErxTaskBundle.class)
            .withStatusCode(400)
            .withHeaders(Map.of());
    return erpResponse.andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
  }

  @Test
  void shouldGenerateResponseByEvidence() {
    val content =
        ResourceLoader.readFileFromResource(
            "fhir/valid/erp/1.2.0/taskbundle/289c56b8-e4de-4da8-8f87-dd55b37dd4ae.xml");
    val pharmacy = ActorContext.getInstance().getPharmacies().get(0);
    val mockErpClient = pharmacy.getClient();
    val getPresUC = new GetPrescriptionsWithPNUseCase(pharmacy);
    val erpResponse = buildResponse(content);
    erpResponse
        .getResourceOptional()
        .orElseThrow()
        .getTasks()
        .get(0)
        .getFor()
        .getIdentifier()
        .setValue(null); // to reach else brunch in LambdaExpression
    when(mockErpClient.request(any(TaskGetByExamEvidenceCommand.class))).thenReturn(erpResponse);
    val res = getPresUC.getPrescriptionsByEvidence(TEST_EVIDENCE);
    assertTrue(res.hasEntity());
    assertEquals(200, res.getStatus());
  }

  @Test
  void shouldThrowOperationOutcome() {
    val pharmacy = ActorContext.getInstance().getPharmacies().get(0);
    val mockErpClient = pharmacy.getClient();
    val getPresUC = new GetPrescriptionsWithPNUseCase(pharmacy);
    val erpResponse = buildResponseAsOpOutcome();
    when(mockErpClient.request(any(TaskGetByExamEvidenceCommand.class))).thenReturn(erpResponse);
    try (val res = getPresUC.getPrescriptionsByEvidence(TEST_EVIDENCE)) {
      fail(
          "GetPrescriptionsUsesCase did not throw the expected Exception and answered with "
              + res.getStatus());
    } catch (WebApplicationException wae) {
      assertEquals(WebApplicationException.class, wae.getClass());
      assertEquals(400, wae.getResponse().getStatus());
      assertEquals(ErrorDto.class, wae.getResponse().getEntity().getClass());
      assertTrue(((ErrorDto) wae.getResponse().getEntity()).getMessage().contains("ERROR"));
    }
  }

  @Test
  void getPrescriptionByKvnr() {
    val kvnr = KVNR.random().toString();
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(0);
    val response = new GetPrescriptionsWithPNUseCase(pharmacy).getPrescriptionByKvnr(kvnr);
    assertEquals(400, response.getStatus());
    assertEquals("not yet implemented", response.getEntity().toString());
  }
}
