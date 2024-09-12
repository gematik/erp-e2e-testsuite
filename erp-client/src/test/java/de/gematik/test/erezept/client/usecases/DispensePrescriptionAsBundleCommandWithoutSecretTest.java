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

package de.gematik.test.erezept.client.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.client.rest.HttpRequestMethod;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.values.TaskId;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;

class DispensePrescriptionAsBundleCommandWithoutSecretTest {

  @Test
  void shouldDispenseAsBundleCorrect() {
    val medDisp = new ErxMedicationDispense();
    val command =
        new DispensePrescriptionAsBundleCommandWithoutSecret(TaskId.from("testId"), medDisp);
    assertEquals("/Task/testId/$dispense", command.getRequestLocator());
    assertEquals(HttpRequestMethod.POST, command.getMethod());
  }

  @Test
  void shouldHaveRequestBody() {
    val testString = "testId";
    val medDisp = new ErxMedicationDispense();
    medDisp.setId(testString);
    val command =
        new DispensePrescriptionAsBundleCommandWithoutSecret(TaskId.from("testId"), medDisp);
    assertTrue(command.getRequestBody().isPresent());
    assertEquals(
        testString,
        (((Bundle.BundleEntryComponent)
                    ((java.util.ArrayList<?>) ((Bundle) command.getRequestBody().get()).getEntry())
                        .get(0))
                .getResource())
            .getId());
  }
}
