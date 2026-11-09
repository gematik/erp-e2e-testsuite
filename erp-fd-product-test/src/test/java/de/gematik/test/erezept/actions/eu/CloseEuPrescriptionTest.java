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

package de.gematik.test.erezept.actions.eu;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.abilities.ProvidePharmacyBaseData;
import de.gematik.test.erezept.client.testutils.ErpResponseUtil;
import de.gematik.test.erezept.client.usecases.eu.EuCloseInputPostCommand;
import de.gematik.test.erezept.common.screenplay.util.SafeAbility;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.fuzzing.eu.EuCloseOperationManipulatorFactory;
import de.gematik.test.utils.ActorMockUtil;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CloseEuPrescriptionTest extends ErpFhirBuildingTest {

  @BeforeAll
  static void setup() {
    CoverageReporter.getInstance().startTestcase("don't care");
  }

  @Test
  void shouldBuildActionCorrect() {
    val pharmacyMock = ActorMockUtil.createRandomPharmacy();
    val pharmacyActor = pharmacyMock.getLeft();
    val pharmacyClient = pharmacyMock.getRight();

    val response = ErpResponseUtil.createErpResponse(null, EmptyResource.class, 200);
    when(pharmacyClient.request(any(EuCloseInputPostCommand.class))).thenReturn(response);

    SafeAbility.getAbility(pharmacyActor, ProvidePharmacyBaseData.class);
    pharmacyActor.abilityTo(ProvidePharmacyBaseData.class);
    val action =
        CloseEuPrescription.with(EuAccessCode.random(), KVNR.random())
            .withResourceManipulator(
                EuCloseOperationManipulatorFactory.getParametersManipulator().stream()
                    .findFirst()
                    .orElseThrow())
            .withAccepted(KbvErpBundleFaker.builder().fake());
    assertDoesNotThrow(() -> pharmacyActor.performs(action));
  }
}
