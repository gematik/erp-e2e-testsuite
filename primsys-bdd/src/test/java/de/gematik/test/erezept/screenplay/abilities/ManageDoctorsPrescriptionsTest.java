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

package de.gematik.test.erezept.screenplay.abilities;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;
import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createOperationOutcome;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.TaskId;
import java.util.Map;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;

class ManageDoctorsPrescriptionsTest {

  @Test
  void shouldHaveInitializedDoctorPrescriptionsStack() {
    val ability = ManageDoctorsPrescriptions.sheIssued();
    assertTrue(ability.isEmpty());
    assertDoesNotThrow(ability::toString);
  }

  @Test
  void shouldTeardown() {
    OnStage.setTheStage(new Cast() {});

    val actor = OnStage.theActor("Dr. Schraßer");
    val ability = spy(ManageDoctorsPrescriptions.sheIssued());
    actor.can(ability);

    val erxTask = mock(ErxTask.class);
    when(erxTask.getTaskId()).thenReturn(TaskId.from("169.000.000.000.01"));
    when(erxTask.getAccessCode()).thenReturn(AccessCode.random());
    ability.getPrescriptions().append(erxTask);

    val erpClient = mock(ErpClient.class);
    val erpClientAbility = UseTheErpClient.with(erpClient);
    actor.can(erpClientAbility);

    val mockResponse =
        ErpResponse.forPayload(createOperationOutcome(), Resource.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(erpClient.request(any(TaskAbortCommand.class))).thenReturn(mockResponse);

    OnStage.drawTheCurtain();

    verify(ability, times(1)).tearDown();
  }

  @Test
  void shouldNotFailOnTeardownWithoutErpClient() {
    OnStage.setTheStage(new Cast() {});

    val actor = OnStage.theActor("Adelheid Ulmenwald");
    val ability = spy(ManageDoctorsPrescriptions.sheIssued());
    actor.can(ability);

    val erxTask = mock(ErxTask.class);
    ability.append(erxTask);

    assertDoesNotThrow(OnStage::drawTheCurtain);

    verify(ability, times(1)).tearDown();
  }
}
