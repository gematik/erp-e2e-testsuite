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
import de.gematik.test.erezept.actions.chargeitem.GetChargeItems;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.testutils.ErpResponseUtil;
import de.gematik.test.erezept.client.usecases.ChargeItemGetCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxChargeItemSet;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.utils.ActorMockUtil;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GetChargeItemsTest extends ErpFhirBuildingTest {

  @BeforeAll
  static void setup() {
    CoverageReporter.getInstance().startTestcase("don't care");
  }

  @Test
  void shouldPerformCorrectCommand() {
    val patientMock = ActorMockUtil.createRandomPatient();
    val patientActor = patientMock.getLeft();
    val patientClient = patientMock.getRight();

    val resource = new ErxChargeItemSet();
    val response = ErpResponseUtil.createErpResponse(resource, ErxChargeItemSet.class, 200);

    when(patientClient.request(any(ChargeItemGetCommand.class))).thenReturn(response);
    val action =
        GetChargeItems.fromServerWith(
            new ChargeItemGetCommand(IQueryParameter.search().withCount(3).createParameter()));
    assertDoesNotThrow(() -> action.answeredBy(patientActor));
  }
}
