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

package de.gematik.test.erezept.abilities;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import de.gematik.test.core.exceptions.EpaMockClientException;
import de.gematik.test.eml.tasks.EmlProvidePrescriptionApply;
import de.gematik.test.erezept.eml.EpaMockClient;
import de.gematik.test.erezept.fhir.values.KVNR;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmlProvidePrescriptionApplyTest {

  private UseTheEpaMockClient mockClient;
  private Actor actor;

  @BeforeEach
  void setUp() {
    mockClient = mock(UseTheEpaMockClient.class);
    actor = new Actor("Test Actor");
    actor.can(mockClient);
  }

  @Test
  void shouldProvidePrescription() {
    KVNR kvnr = KVNR.random();
    val task = EmlProvidePrescriptionApply.forKvnr(kvnr);
    when(mockClient.setProvidePrescriptionApply(kvnr)).thenReturn(true);
    task.performAs(actor);
    verify(mockClient).setProvidePrescriptionApply(kvnr);
  }

  @Test
  void shouldThrowExceptionIfPrescriptionProvideApplyFails() {
    KVNR kvnr = KVNR.random();
    val task = EmlProvidePrescriptionApply.forKvnr(kvnr);
    when(mockClient.setProvidePrescriptionApply(kvnr)).thenReturn(false);
    assertThrows(EpaMockClientException.class, () -> task.performAs(actor));
  }

  @Test
  void shouldSetProvidePrescriptionApply() {
    KVNR kvnr = KVNR.random();
    val mockClient = mock(EpaMockClient.class);
    val useTheEpaMockClient = UseTheEpaMockClient.with(mockClient);
    when(mockClient.configRequest(any())).thenReturn(true);
    boolean result = useTheEpaMockClient.setProvidePrescriptionApply(kvnr);
    assertTrue(result);
  }
}
