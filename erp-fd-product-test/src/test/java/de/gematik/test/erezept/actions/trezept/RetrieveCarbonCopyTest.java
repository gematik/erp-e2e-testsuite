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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.actions.trezept;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.abilities.UseTheTRegisterMockClient;
import de.gematik.test.erezept.client.exceptions.FhirValidationException;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.trezept.TRegisterLog;
import de.gematik.test.erezept.trezept.TRegisterMockClient;
import de.gematik.test.erezept.trezept.TRegisterMockDownloadRequest;
import java.util.List;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.junit.jupiter.api.Test;

class RetrieveCarbonCopyTest extends ErpFhirParsingTest {

  @Test
  void shouldRetrieveCarbonCopyFromTRegisterMock() {
    Actor actor = Actor.named("Pharmacy");

    UseTheTRegisterMockClient ability = mock(UseTheTRegisterMockClient.class);
    actor.can(ability);

    ErxTask task = mock(ErxTask.class);
    var taskId = mock(de.gematik.test.erezept.fhir.values.TaskId.class);

    when(task.getTaskId()).thenReturn(taskId);
    when(taskId.getValue()).thenReturn("0123456");

    List<TRegisterLog> expectedLogs = List.of(mock(TRegisterLog.class));
    when(ability.pollRequest(any(TRegisterMockDownloadRequest.class))).thenReturn(expectedLogs);

    RetrieveCarbonCopy question = RetrieveCarbonCopy.forTask(task);

    List<TRegisterLog> result = question.answeredBy(actor);

    verify(ability).pollRequest(any(TRegisterMockDownloadRequest.class));
    assertSame(expectedLogs, result);
  }

  @Test
  void shouldRetrieveAndValidateCarbonCopyInTRegisterLog() {
    val content =
        ResourceLoader.readFileFromResource("TPrescription/Parameters-TRP-Carbon-Copy.json");
    val tRegisterMockClientMock = mock(TRegisterMockClient.class);
    val httpRequest = HttpBRequest.method(HttpRequestMethod.GET).withPayload(content);
    when(tRegisterMockClientMock.pollRequest(any(TRegisterMockDownloadRequest.class)))
        .thenReturn(List.of(new TRegisterLog(1700000001L, "req-2", "k2", httpRequest)));

    val useTheTRegisterMockClient = UseTheTRegisterMockClient.with(tRegisterMockClientMock, parser);
    val req = new TRegisterMockDownloadRequest("0123456");

    assertThrows(FhirValidationException.class, () -> useTheTRegisterMockClient.pollRequest(req));
  }
}
