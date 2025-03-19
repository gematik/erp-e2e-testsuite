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

package de.gematik.test.erezept.actions;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.actions.chargeitem.GetChargeItems;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.usecases.ChargeItemGetCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxChargeItemSet;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;

class GetChargeItemsTest extends ErpFhirBuildingTest {

  @Test
  void shouldPerformCorrectCommand() {
    PatientActor patient;
    CoverageReporter.getInstance().startTestcase("don't care");
    // init pharmacy
    patient = new PatientActor("Sina Hüllmann");
    UseTheErpClient useErpClient = mock(UseTheErpClient.class);
    patient.can(useErpClient);
    val resource = new ErxChargeItemSet();
    val response =
        ErpResponse.forPayload(resource, ErxChargeItemSet.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(ChargeItemGetCommand.class))).thenReturn(response);
    val action =
        GetChargeItems.fromServerWith(
            new ChargeItemGetCommand(IQueryParameter.search().withCount(3).createParameter()));
    assertDoesNotThrow(() -> action.answeredBy(patient));
  }
}
