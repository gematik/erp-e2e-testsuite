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

package de.gematik.test.erezept.abilities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.fhir.fuzzing.FuzzingEngine;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingSessionLogbook;
import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.actions.MockActorsUtils;
import de.gematik.test.erezept.client.usecases.TaskCreateCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Parameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UseHapiFuzzerTest {

  @Test
  void shouldDelegateToFuzzingEngine() {
    val sessionLogbook = mock(FuzzingSessionLogbook.class);
    val engine = mock(FuzzingEngine.class);
    when(engine.fuzz(any())).thenReturn(sessionLogbook);

    val ability = UseHapiFuzzer.withFuzzingEngine(engine);

    val log = ability.fuzz(new Bundle());
    assertEquals(sessionLogbook, log);
    verify(engine, times(1)).fuzz(any(Bundle.class));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void shouldUseFuzzerWhenApplied(boolean shouldThrowOnReporting) {
    StopwatchProvider.init();
    val mockUtil = new MockActorsUtils();
    val actorStage = mockUtil.actorStage;
    val pharmacyActor = actorStage.getPharmacyNamed("Stadtapotheke");

    val sessionLogbook = mock(FuzzingSessionLogbook.class);
    if (shouldThrowOnReporting) {
      // if reporting of the FuzzingLog fails, the testrun MUST still continue
      // actually this should never happen, but the exception needs to be handled anyway
      when(sessionLogbook.getDuration()).thenThrow(new RuntimeException("Test"));
    }

    val engine = mock(FuzzingEngine.class);
    when(engine.fuzz(any())).thenReturn(sessionLogbook);
    pharmacyActor.can(UseHapiFuzzer.withFuzzingEngine(engine));

    val payload = new ErxTask();
    val res = mockUtil.createErpResponse(payload, ErxTask.class);
    when(mockUtil.erpClientMock.request(any(TaskCreateCommand.class))).thenReturn(res);

    assertDoesNotThrow(() -> pharmacyActor.performs(new TestErpAction()));
    verify(engine, times(1)).fuzz(any(Parameters.class));
  }

  private static class TestErpAction extends ErpAction<ErxTask> {

    @Override
    public ErpInteraction<ErxTask> answeredBy(Actor actor) {
      return this.performCommandAs(new TaskCreateCommand(), actor);
    }
  }
}
