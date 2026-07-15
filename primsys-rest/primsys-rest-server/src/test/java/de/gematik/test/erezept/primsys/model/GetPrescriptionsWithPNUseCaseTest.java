/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.primsys.model;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.rest.fd.FhirBResponse;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.client.usecases.TaskGetByExamEvidenceCommand;
import de.gematik.test.erezept.client.usecases.TaskGetByPoppTokenCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxTaskBundle;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import de.gematik.test.erezept.primsys.data.error.ErrorDto;
import jakarta.ws.rs.WebApplicationException;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GetPrescriptionsWithPNUseCaseTest extends TestWithActorContext {
  private static final String TEST_EVIDENCE =
      "H4sIAAAAAAAA/y2NX0+DMBTF3/kUpO9S2uHftF3mWnVOcDpA44tppELnKAtFYPv0dupJ7k3OvfmdQ6ZjvfV71VrdGApQEAJfmY+m0KakIEtvTi6AbztpCrltjKJgryyYMo+sEn/O4/dcPK8Xj8kveWRdmrEUVF23u4JwsEGpatnpr6BQ8FPC3hY13JkB9scm5vlOJF0zHOIoROgcneHTKCTQnf5+gmECxb9ZvbGndDbGfLGPN264mMQ8QzEvJwm/zrPDrdp8CxQ9vOjZXSXb1zGaV7q9LA8YNcu8vh8ogS7Eczth3g8tA6HU/AAAAA==";

  private FhirBResponse<ErxTaskBundle> buildResponse(String content) {
    val erxTaskBundle = parser.decode(ErxTaskBundle.class, content);
    val erpResponseBuilder =
        FhirBResponse.forPayload(ErxTaskBundle.class, erxTaskBundle).withStatusCode(200);
    return erpResponseBuilder.andValidationResult(createEmptyValidationResult());
  }

  private FhirBResponse<ErxTaskBundle> buildResponseAsOpOutcome() {
    val erpResponse =
        FhirBResponse.forPayload(ErxTaskBundle.class, createOperationOutcome()).withStatusCode(400);
    return erpResponse.andValidationResult(createEmptyValidationResult());
  }

  static Stream<Arguments> responseByEvidenceParameter() {
    return Stream.of(
        Arguments.of(TEST_EVIDENCE, "ABC", "dummy"), Arguments.of(TEST_EVIDENCE, null, null));
  }

  @ParameterizedTest
  @MethodSource("responseByEvidenceParameter")
  void shouldGenerateResponseByEvidence(String evidence, String kvnr, String hcv) {
    val content =
        ResourceLoader.readFileFromResource(
            "fhir/valid/erp/1.4.0/taskBundle/5cb82d47-c80d-4404-b57c-099d58767603.xml");
    val pharmacy = ActorContext.getInstance().getPharmacies().getFirst();
    val mockErpClient = pharmacy.getClient();
    val getPresUC = new GetPrescriptionsAsPharmacyUseCase(pharmacy);
    val erpResponse = buildResponse(content);
    erpResponse
        .getResourceOptional()
        .orElseThrow()
        .getTasks()
        .getFirst()
        .getFor()
        .getIdentifier()
        .setValue(null); // to reach else brunch in LambdaExpression
    when(mockErpClient.request(any(TaskGetByExamEvidenceCommand.class))).thenReturn(erpResponse);

    val res =
        hcv == null
            ? getPresUC.getPrescriptionsByEvidence(TEST_EVIDENCE)
            : getPresUC.getPrescriptionsByEvidence(evidence, kvnr, hcv);
    assertTrue(res.hasEntity());
    assertEquals(200, res.getStatus());
  }

  @Test
  void shouldThrowOperationOutcome() {
    val pharmacy = ActorContext.getInstance().getPharmacies().getFirst();
    val mockErpClient = pharmacy.getClient();
    val getPresUC = new GetPrescriptionsAsPharmacyUseCase(pharmacy);
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
  void shouldGenerateResponseByPoppToken() {
    val content =
        ResourceLoader.readFileFromResource(
            "fhir/valid/erp/1.4.0/taskBundle/5cb82d47-c80d-4404-b57c-099d58767603.xml");
    val pharmacy = ActorContext.getInstance().getPharmacies().getFirst();
    val mockErpClient = pharmacy.getClient();
    val getPresUC = new GetPrescriptionsAsPharmacyUseCase(pharmacy);
    val erpResponse = buildResponse(content);

    when(mockErpClient.request(any(TaskGetByPoppTokenCommand.class))).thenReturn(erpResponse);

    val res = getPresUC.getPrescriptionsByPoppToken("some-popp-token");
    assertTrue(res.hasEntity());
    assertEquals(200, res.getStatus());
  }

  @Test
  void shouldThrowOperationOutcomeByPoppToken() {
    val pharmacy = ActorContext.getInstance().getPharmacies().getFirst();
    val mockErpClient = pharmacy.getClient();
    val getPresUC = new GetPrescriptionsAsPharmacyUseCase(pharmacy);
    val erpResponse = buildResponseAsOpOutcome();

    when(mockErpClient.request(any(TaskGetByPoppTokenCommand.class))).thenReturn(erpResponse);

    try (val res = getPresUC.getPrescriptionsByPoppToken("some-popp-token")) {
      fail(
          "GetPrescriptionsWithPNUseCase did not throw the expected Exception and answered with "
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
    val kvnr = KVNR.randomStringValue();
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().getFirst();
    val response = new GetPrescriptionsAsPharmacyUseCase(pharmacy).getPrescriptionByKvnr(kvnr);
    assertEquals(400, response.getStatus());
    assertInstanceOf(ErrorDto.class, response.getEntity());
    assertEquals("not yet implemented", ((ErrorDto) response.getEntity()).getMessage());
  }
}
