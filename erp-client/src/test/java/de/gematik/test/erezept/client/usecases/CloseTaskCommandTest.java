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

package de.gematik.test.erezept.client.usecases;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseFaker;
import de.gematik.test.erezept.fhir.builder.erp.GemOperationInputParameterBuilder;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.GemCloseOperationParameters;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import java.util.ArrayList;
import java.util.stream.IntStream;
import lombok.val;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.jupiter.api.Test;

class CloseTaskCommandTest extends ErpFhirParsingTest {

  @Test
  void getRequestLocator() {
    val taskId = TaskId.from("123456");
    val secret = Secret.from("7890123");
    val cmd = new CloseTaskCommand(taskId, secret);

    val expected = format("/Task/{0}/$close?secret={1}", taskId, secret.getValue());
    val actual = cmd.getRequestLocator();
    assertEquals(expected, actual);
  }

  @Test
  void createSingleDispense() {
    val md = ErxMedicationDispenseFaker.builder().fake();
    val taskId = TaskId.from("123456");
    val secret = Secret.from("7890123");
    val cmd = new CloseTaskCommand(taskId, secret, md);

    val expectedEndpoint = format("/Task/{0}/$close?secret={1}", taskId, secret.getValue());
    assertEquals(expectedEndpoint, cmd.getRequestLocator());

    val optBody = cmd.getRequestBody();
    assertTrue(optBody.isPresent());
    val body = optBody.orElseThrow();
    assertEquals(ResourceType.MedicationDispense, body.getResourceType());
    val rawBody = parser.encode(body, EncodingType.XML);
    assertTrue(parser.isValid(rawBody));
  }

  @Test
  void createMultipleDispenses() {
    val mds = new ArrayList<ErxMedicationDispense>();
    val kvnr = KVNR.random();
    val performerId = GemFaker.fakerTelematikId();
    val prescriptionId = PrescriptionId.random();
    IntStream.range(0, 3)
        .forEach(
            idx ->
                mds.add(
                    ErxMedicationDispenseFaker.builder()
                        .withKvnr(kvnr)
                        .withPerformer(performerId)
                        .withPrescriptionId(prescriptionId)
                        .fake()));

    val taskId = TaskId.from("123456");
    val secret = Secret.from("7890123");
    val cmd = new CloseTaskCommand(taskId, secret, mds);

    val expectedEndpoint = format("/Task/{0}/$close?secret={1}", taskId, secret.getValue());
    assertEquals(expectedEndpoint, cmd.getRequestLocator());

    val optBody = cmd.getRequestBody();
    assertTrue(optBody.isPresent());
    val body = optBody.orElseThrow();
    assertEquals(ResourceType.Bundle, body.getResourceType());
    val rawBody = parser.encode(body, EncodingType.XML);

    val result = parser.validate(rawBody);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldCloseWithoutMedicationDispense() {
    val taskId = TaskId.from("123456");
    val secret = Secret.from("7890123");
    val cmd = new CloseTaskCommand(taskId, secret);

    val optBody = cmd.getRequestBody();
    assertTrue(optBody.isEmpty());
  }

  @Test
  void shouldCloseWithParametersStructure() {
    val taskId = TaskId.from("123456");
    val secret = Secret.from("7890123");
    val closeParameters = GemOperationInputParameterBuilder.forClosingPharmaceuticals().build();
    val cmd = new CloseTaskCommand(taskId, secret, closeParameters);

    val optBody = cmd.getRequestBody();
    assertTrue(optBody.isPresent());
    assertInstanceOf(GemCloseOperationParameters.class, optBody.get());
  }
}
