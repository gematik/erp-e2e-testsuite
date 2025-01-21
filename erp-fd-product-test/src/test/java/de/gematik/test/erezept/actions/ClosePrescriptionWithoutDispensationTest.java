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

package de.gematik.test.erezept.actions;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.CloseTaskCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

class ClosePrescriptionWithoutDispensationTest {

  private static PharmacyActor pharmacy;
  private static UseTheErpClient useErpClient;

  @BeforeAll
  static void setup() {
    CoverageReporter.getInstance().startTestcase("don't care");
    // init pharmacy
    pharmacy = new PharmacyActor("Am Flughafen");
    useErpClient = mock(UseTheErpClient.class);
    pharmacy.can(useErpClient);
  }

  @ParameterizedTest
  @ValueSource(strings = {"1.3.0", "1.4.0"})
  @ClearSystemProperty(key = "erp.fhir.profile")
  void shouldPerformClosePrescription(String version) {
    System.setProperty("erp.fhir.profile", version);
    val task = new ErxTask();
    task.setId("123456");
    val secret = Secret.fromString("123456789");
    val action = ClosePrescriptionWithoutDispensation.forTheTask(task, secret);
    val resource = new ErxReceipt();
    val response =
        ErpResponse.forPayload(resource, ErxReceipt.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(CloseTaskCommand.class))).thenReturn(response);
    assertDoesNotThrow(() -> pharmacy.performs(action));
  }
}
