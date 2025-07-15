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

package de.gematik.test.erezept.actions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.actions.chargeitem.GetChargeItemById;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GetChargeItemByIdTest extends ErpFhirBuildingTest {

  private static PatientActor patient;

  @BeforeAll
  static void setup() {
    CoverageReporter.getInstance().startTestcase("don't care");
    // init pharmacy
    patient = new PatientActor("Sina Hüllmann");
    UseTheErpClient useErpClient = mock(UseTheErpClient.class);
    patient.can(useErpClient);
  }

  @Test
  void shouldPerformCorrectCommandWithoutAccessCode() {
    val action = GetChargeItemById.withPrescriptionId(PrescriptionId.random()).withoutAccessCode();
    assertDoesNotThrow(() -> action.answeredBy(patient));
  }

  @Test
  void shouldPerformCorrectCommandWithAccessCode() {
    val action =
        GetChargeItemById.withPrescriptionId(PrescriptionId.random())
            .withAccessCode(AccessCode.random());
    assertDoesNotThrow(() -> action.answeredBy(patient));
  }
}
