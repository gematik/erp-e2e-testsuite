/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.client.usecases;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBuilder;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.val;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DispenseMedicationCommandTest {

  private FhirParser parser;

  @BeforeEach
  void setup() {
    parser = new FhirParser();
  }

  @Test
  void getRequestLocator() {
    val md =
        ErxMedicationDispenseBuilder.faker(KVNR.random(), "performerid", PrescriptionId.random()).build();
    val taskId = TaskId.from("123456");
    val secret = "7890123";
    val cmd = new DispenseMedicationCommand(taskId, new Secret(secret), md);

    val expected = format("/Task/{0}/$close?secret={1}", taskId, secret);
    val actual = cmd.getRequestLocator();
    assertEquals(expected, actual);
  }

  @Test
  void createSingleDispense() {
    val md =
        ErxMedicationDispenseBuilder.faker(
                KVNR.random(), GemFaker.fakerTelematikId(), PrescriptionId.random())
            .build();
    val taskId = "123456";
    val secret = "7890123";
    val cmd = new DispenseMedicationCommand(TaskId.from(taskId), Secret.fromString(secret), md);

    val expectedEndpoint = format("/Task/{0}/$close?secret={1}", taskId, secret);
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
                    ErxMedicationDispenseBuilder.faker(kvnr, performerId, prescriptionId).build()));

    val taskId = "123456";
    val secret = "7890123";
    val cmd = new DispenseMedicationCommand(TaskId.from(taskId), Secret.fromString(secret), mds);

    val expectedEndpoint = format("/Task/{0}/$close?secret={1}", taskId, secret);
    assertEquals(expectedEndpoint, cmd.getRequestLocator());

    val optBody = cmd.getRequestBody();
    assertTrue(optBody.isPresent());
    val body = optBody.orElseThrow();
    assertEquals(ResourceType.Bundle, body.getResourceType());
    val rawBody = parser.encode(body, EncodingType.XML);

    val result = parser.validate(rawBody);

    if (!result.isSuccessful()) {

      // give me some hints if the encoded result is invalid
      val r =
          result.getMessages().stream()
              .map(m -> "(" + m.getLocationString() + ") " + m.getMessage())
              .collect(Collectors.joining("\n"));
      System.out.println(format("Errors: {0}\n{1}", result.getMessages().size(), r));
    }

    assertTrue(result.isSuccessful());
  }
}
