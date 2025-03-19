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

package de.gematik.test.erezept.client.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseFaker;
import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationFaker;
import de.gematik.test.erezept.fhir.builder.erp.GemOperationInputParameterBuilder;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.GemDispenseOperationParameters;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;

class DispensePrescriptionAsBundleCommandTest extends ErpFhirParsingTest {

  @Test
  void shouldDispenseAsBundleCorrect() {
    val medDisp = new ErxMedicationDispense();
    val command =
        new DispensePrescriptionAsBundleCommand(
            TaskId.from("testId"), Secret.fromString("testSecret"), medDisp);
    assertEquals("/Task/testId/$dispense?secret=testSecret", command.getRequestLocator());
    assertEquals(HttpRequestMethod.POST, command.getMethod());
  }

  @Test
  void shouldHaveRequestBody() {
    val testString = "testId";
    val medDisp = new ErxMedicationDispense();
    medDisp.setId(testString);
    val command =
        new DispensePrescriptionAsBundleCommand(
            TaskId.from("testId"), Secret.fromString("testSecret"), medDisp);
    assertTrue(command.getRequestBody().isPresent());
    val body = assertInstanceOf(Bundle.class, command.getRequestBody().get());
    assertEquals(1, body.getEntry().size());
    assertEquals(testString, body.getEntryFirstRep().getResource().getId());
  }

  @Test
  void shouldDispenseWithParametersStructure() {
    val taskId = "123456";
    val secret = "7890123";
    val dispenseParameters =
        GemOperationInputParameterBuilder.forDispensingPharmaceuticals()
            .with(
                ErxMedicationDispenseFaker.builder().fake(), GemErpMedicationFaker.builder().fake())
            .build();
    val cmd =
        new DispensePrescriptionAsBundleCommand(
            TaskId.from(taskId), Secret.fromString(secret), dispenseParameters);

    val optBody = cmd.getRequestBody();
    assertTrue(optBody.isPresent());
    assertInstanceOf(GemDispenseOperationParameters.class, optBody.get());
  }
}
