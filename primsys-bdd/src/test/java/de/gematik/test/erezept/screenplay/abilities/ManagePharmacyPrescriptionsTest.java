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

package de.gematik.test.erezept.screenplay.abilities;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;
import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createOperationOutcome;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import java.util.Map;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;

class ManagePharmacyPrescriptionsTest {

  @Test
  void shouldHaveInitializedPharmacyPrescriptionStacks() {
    val ability = ManagePharmacyPrescriptions.itWorksWith();
    assertTrue(ability.getAcceptedPrescriptions().isEmpty());
    assertTrue(ability.getAssignedPrescriptions().isEmpty());
    assertTrue(ability.getClosedPrescriptions().isEmpty());
    assertTrue(ability.getDispenseTimestamps().isEmpty());
    assertTrue(ability.getReceiptsList().isEmpty());
    assertDoesNotThrow(ability::toString);
  }

  @Test
  void shouldTeardown() {
    OnStage.setTheStage(new Cast() {});

    val actor = OnStage.theActor("Alice");
    val ability = spy(ManagePharmacyPrescriptions.itWorksWith());
    actor.can(ability);

    val task = mock(ErxTask.class);
    val acceptBundle = mock(ErxAcceptBundle.class);
    when(acceptBundle.getTaskId()).thenReturn(TaskId.from(PrescriptionId.random()));
    when(acceptBundle.getSecret()).thenReturn(Secret.fromString("123"));
    when(acceptBundle.getTask()).thenReturn(task);
    when(task.getAccessCode()).thenReturn(AccessCode.random());

    ability.appendAcceptedPrescription(acceptBundle);

    val erpClient = mock(UseTheErpClient.class);
    actor.can(erpClient);

    val mockResponse =
        ErpResponse.forPayload(createOperationOutcome(), Resource.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(erpClient.request(any(TaskAbortCommand.class))).thenReturn(mockResponse);

    OnStage.drawTheCurtain();

    verify(ability, times(1)).tearDown();
  }
}
