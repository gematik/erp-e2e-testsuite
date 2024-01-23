/*
 * Copyright 2023 gematik GmbH
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

import static org.mockito.Mockito.mock;

import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GetPrescriptionByIdTest {

  private static PharmacyActor pharmacy;

  @BeforeAll
  static void setup() {
    CoverageReporter.getInstance().startTestcase("don't care");
    // init pharmacy
    pharmacy = new PharmacyActor("Am Flughafen");
    UseTheErpClient useErpClient = mock(UseTheErpClient.class);
    pharmacy.can(useErpClient);
  }

  @Test
  void shouldPerformCorrectCommandWithoutAuthentication() {
    val action = GetPrescriptionById.withTaskId(TaskId.from("123")).withoutAuthentication();
    Assertions.assertDoesNotThrow(() -> action.answeredBy(pharmacy));
  }

  @Test
  void shouldPerformCorrectCommandWithAccessCode() {
    val action =
        GetPrescriptionById.withTaskId(TaskId.from("123"))
            .withAccessCode(AccessCode.fromString("321"));
    Assertions.assertDoesNotThrow(() -> action.answeredBy(pharmacy));
  }

  @Test
  void shouldPerformCorrectCommandWithSecret() {
    val action =
        GetPrescriptionById.withTaskId(TaskId.from("123")).withSecret(Secret.fromString("secret"));
    Assertions.assertDoesNotThrow(() -> action.answeredBy(pharmacy));
  }
}
