/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.actions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.client.testutils.ErpResponseUtil;
import de.gematik.test.erezept.client.usecases.CloseTaskCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.utils.ActorMockUtil;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

class ClosePrescriptionWithoutDispensationTest extends ErpFhirBuildingTest {

  @BeforeAll
  static void setup() {
    CoverageReporter.getInstance().startTestcase("don't care");
  }

  @ParameterizedTest
  @ValueSource(strings = {"1.4.0", "1.5.0"})
  @ClearSystemProperty(key = "erp.fhir.profile")
  void shouldPerformClosePrescription(String version) {
    val pharmacyMock = ActorMockUtil.createRandomPharmacy();
    val pharmacyActor = pharmacyMock.getLeft();
    val pharmacyClient = pharmacyMock.getRight();

    System.setProperty("erp.fhir.profile", version);
    val task = new ErxTask();
    task.setId("123456");
    val secret = Secret.from("123456789");
    val action = ClosePrescriptionWithoutDispensation.forTheTask(task, secret);
    val resource = new ErxReceipt();
    val response = ErpResponseUtil.createErpResponse(resource, ErxReceipt.class, 200);

    when(pharmacyClient.request(any(CloseTaskCommand.class))).thenReturn(response);
    assertDoesNotThrow(() -> pharmacyActor.performs(action));
  }
}
