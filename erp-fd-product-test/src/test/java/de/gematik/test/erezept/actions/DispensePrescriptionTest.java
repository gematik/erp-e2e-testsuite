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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.DispensePrescriptionAsBundleCommand;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseFaker;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.resources.erp.GemDispenseOperationParameters;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DispensePrescriptionTest {
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

  @Test
  void shouldPerformDispensationCorrectWithMedDsp() {
    val taskId = TaskId.from("123456789");
    val secret = Secret.fromString("123456789");

    val action =
        DispensePrescription.forPrescription(taskId, secret)
            .withMedDsp(List.of(ErxMedicationDispenseFaker.builder().fake()));

    val resource = new ErxMedicationDispenseBundle();
    val response =
        ErpResponse.forPayload(resource, ErxMedicationDispenseBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(DispensePrescriptionAsBundleCommand.class))).thenReturn(response);
    assertDoesNotThrow(() -> pharmacy.performs(action));
  }

  @Test
  void shouldPerformDispensationCorrectWithParameters() {
    val taskId = TaskId.from("123456");
    val secret = Secret.fromString("123456");

    val params = new GemDispenseOperationParameters();
    val action = DispensePrescription.forPrescription(taskId, secret).withParameters(params);

    val resource = new ErxMedicationDispenseBundle();
    val response =
        ErpResponse.forPayload(resource, ErxMedicationDispenseBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(DispensePrescriptionAsBundleCommand.class))).thenReturn(response);
    assertDoesNotThrow(() -> pharmacy.performs(action));
  }
}
