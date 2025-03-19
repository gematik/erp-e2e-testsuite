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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import java.util.List;
import java.util.Map;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;

class ManageDataMatrixCodesTest {

  @Test
  void shouldAddAndConsumeDmcs() {
    val manageDmcs = ManageDataMatrixCodes.sheGetsPrescribed();

    val first = TaskId.from("taskId");
    val second = TaskId.from("taskId2");
    manageDmcs.appendDmc(DmcPrescription.ownerDmc(first, AccessCode.random()));
    manageDmcs.appendDmc(DmcPrescription.ownerDmc(second, AccessCode.random()));
    assertEquals(2, manageDmcs.getDmcs().getRawList().size());

    var dmc = DequeStrategy.FIFO.chooseFrom(manageDmcs.getDmcs());
    assertEquals(first, dmc.getTaskId());
    assertEquals(2, manageDmcs.getDmcs().getRawList().size());

    dmc = DequeStrategy.LIFO.chooseFrom(manageDmcs.getDmcs());
    assertEquals(second, dmc.getTaskId());
    assertEquals(2, manageDmcs.getDmcs().getRawList().size());

    dmc = DequeStrategy.FIFO.consume(manageDmcs.getDmcList());
    assertEquals(first, dmc.getTaskId());
    assertEquals(1, manageDmcs.getDmcs().getRawList().size());

    dmc = DequeStrategy.LIFO.consume(manageDmcs.getDmcList());
    assertEquals(second, dmc.getTaskId());
    assertEquals(0, manageDmcs.getDmcs().getRawList().size());
  }

  @Test
  void shouldChooseStacks() {
    val manageDmcs = ManageDataMatrixCodes.sheGetsPrescribed();

    manageDmcs.appendDmc(DmcPrescription.ownerDmc(TaskId.from("taskId"), AccessCode.random()));
    manageDmcs.appendDmc(DmcPrescription.ownerDmc(TaskId.from("taskId2"), AccessCode.random()));

    val activeDmcs = manageDmcs.chooseStack(DmcStack.ACTIVE);
    assertEquals(2, activeDmcs.getRawList().size());

    val deletedDmcs = manageDmcs.chooseStack(DmcStack.DELETED);
    assertTrue(deletedDmcs.isEmpty());
  }

  @Test
  void shouldThrowOnEmptyStacks() {
    val manageDmcs = ManageDataMatrixCodes.sheGetsPrescribed();
    assertEquals(0, manageDmcs.getDmcs().getRawList().size());

    val dmcList = manageDmcs.getDmcList();
    List.of(DequeStrategy.FIFO, DequeStrategy.LIFO)
        .forEach(
            deque -> assertThrows(MissingPreconditionError.class, () -> deque.chooseFrom(dmcList)));
  }

  @Test
  void shouldMoveDmcToDeleted() {
    val manageDmcs = ManageDataMatrixCodes.sheGetsPrescribed();
    val first = TaskId.from("taskId");
    val second = TaskId.from("taskId2");
    manageDmcs.appendDmc(DmcPrescription.ownerDmc(first, AccessCode.random()));
    manageDmcs.moveToDeleted(DmcPrescription.ownerDmc(second, AccessCode.random()));

    assertEquals(1, manageDmcs.getDmcs().getRawList().size());
    assertEquals(1, manageDmcs.getDeletedDmcs().getRawList().size());
  }

  @Test
  void shouldMaintainAggregatedList() {
    val manageDmcs = ManageDataMatrixCodes.sheGetsPrescribed();
    val first = TaskId.from("taskId");
    val second = TaskId.from("taskId2");
    manageDmcs.appendDmc(DmcPrescription.ownerDmc(first, AccessCode.random()));
    manageDmcs.moveToDeleted(DmcPrescription.ownerDmc(second, AccessCode.random()));

    assertEquals(1, manageDmcs.getDmcs().getRawList().size());
    assertEquals(1, manageDmcs.getDeletedDmcs().getRawList().size());
    assertEquals(2, manageDmcs.getAggregatedDmcs().size());

    val firstDeq = DequeStrategy.FIFO.chooseFrom(manageDmcs.getAggregatedDmcs());
    assertEquals(first, firstDeq.getTaskId());

    val secondDeq = DequeStrategy.LIFO.chooseFrom(manageDmcs.getAggregatedDmcs());
    assertEquals(second, secondDeq.getTaskId());
  }

  @Test
  void shouldTeardown() {
    OnStage.setTheStage(new Cast() {});

    val actor = OnStage.theActor("Alice");
    val ability = spy(ManageDataMatrixCodes.sheGetsPrescribed());
    actor.can(ability);

    val dmc = DmcPrescription.ownerDmc(TaskId.from(PrescriptionId.random()), AccessCode.random());
    ability.getDmcList().add(dmc);

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
  void shouldHaveToString() {
    assertTrue(DmcStack.ACTIVE.toString().contains("ausgestellt"));
    assertTrue(DmcStack.DELETED.toString().contains("gelöscht"));
  }
}
