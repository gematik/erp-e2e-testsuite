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
 */

package de.gematik.test.erezept.primsys.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskAcceptCommand;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import de.gematik.test.konnektor.soap.mock.LocalSigner;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;

class AcceptUseCaseTest extends TestWithActorContext {

  @Test
  void shouldNotInstantiate() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(AcceptUseCase.class));
  }

  @Test
  void shouldAcceptPrescription() {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(1);
    val hba = SmartcardArchive.fromResources().getHbaByICCSN("80276001011699901501");
    val mockClient = pharmacy.getClient();

    val prescriptionId = PrescriptionId.random();
    val taskId = TaskId.from(prescriptionId);
    val accessCode = AccessCode.random();

    val acceptBundle = mock(ErxAcceptBundle.class);
    val task = mock(ErxTask.class);
    when(acceptBundle.getSignedKbvBundle())
        .thenReturn(
            LocalSigner.signQES(hba, CryptoSystem.ECC_256)
                .signDocument(
                    false,
                    parser.encode(
                        KbvErpBundleFaker.builder().withPrescriptionId(prescriptionId).fake(),
                        EncodingType.XML)));

    when(acceptBundle.getTask()).thenReturn(task);
    when(task.getPrescriptionId()).thenReturn(prescriptionId);
    when(task.getAccessCode()).thenReturn(accessCode);
    when(acceptBundle.getSecret()).thenReturn(Secret.fromString("random"));
    val acceptResponse =
        ErpResponse.forPayload(acceptBundle, ErxAcceptBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    when(mockClient.request(any(TaskAcceptCommand.class))).thenReturn(acceptResponse);

    try (val response =
        AcceptUseCase.acceptPrescription(pharmacy, taskId.getValue(), accessCode.getValue())) {
      assertTrue(response.hasEntity());
      assertEquals(200, response.getStatus());
    }
  }
}
