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

package de.gematik.test.erezept.apimeasure;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.test.erezept.client.ClientType;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskGetCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxTaskBundle;
import java.time.Duration;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;

class LoggingStopwatchTest {

  @Test
  void shouldLogMeasurements() {
    val sw = new LoggingStopwatch();
    val clientType = ClientType.PS;
    val cmd = new TaskGetCommand();
    val vr = mock(ValidationResult.class);
    val response =
        ErpResponse.forPayload(null, ErxTaskBundle.class)
            .withDuration(Duration.ofMillis(200))
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(vr);
    assertDoesNotThrow(() -> sw.measurement(clientType, cmd, response));
    assertDoesNotThrow(sw::close);
  }
}
