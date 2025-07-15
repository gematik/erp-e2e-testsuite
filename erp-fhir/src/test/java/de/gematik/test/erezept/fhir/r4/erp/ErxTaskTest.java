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

package de.gematik.test.erezept.fhir.r4.erp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.valuesets.PerformerType;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.junit.jupiter.api.Test;

class ErxTaskTest extends ErpFhirParsingTest {

  private static final String BASE_PATH = "fhir/valid/erp/";
  private static final String BASE_PATH_1_4_0 = BASE_PATH + "1.4.0/task/";

  @Test
  void shouldEncodeSingleTask() {
    val expectedId = "9b48f82c-9c11-4a57-aa72-a805f9537a82";
    val fileName = expectedId + ".json";
    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4_0 + fileName);

    val task = parser.decode(ErxTask.class, content);
    assertNotNull(task, "Valid ErxTask must be parseable");

    assertEquals(expectedId, task.getTaskId().getValue());

    val expectedPrescriptionID = PrescriptionId.from("160.000.033.491.280.78");
    assertEquals(expectedPrescriptionID, task.getPrescriptionId());

    val expectedFlowType = PrescriptionFlowType.FLOW_TYPE_160;
    assertEquals(expectedFlowType, task.getFlowType());

    val expectedAccessCode =
        AccessCode.from("777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");
    assertTrue(task.hasAccessCode());
    assertEquals(expectedAccessCode, task.getAccessCode());

    assertFalse(task.hasSecret()); // does not have a secret in Task_01.xml

    val expectedStatus = TaskStatus.INPROGRESS;
    assertEquals(expectedStatus, task.getStatus());

    val kvnr = task.getForKvnr().get();
    assertEquals("X123456789", kvnr.getValue());

    val expectedPerformer = PerformerType.PUBLIC_PHARMACY;
    assertEquals(expectedPerformer, task.getPerformerFirstRep());

    val expectedIntent = Task.TaskIntent.ORDER;
    assertEquals(expectedIntent, task.getIntent());

    assertTrue(task.hasLastMedicationDispenseDate());

    // just for coverage, should never fail
    assertNotNull(task.toString());
  }

  @Test
  void shouldEncodeExpiryAndAcceptDates() {
    val expectedId = "9b48f82c-9c11-4a57-aa72-a805f9537a82";
    val fileName = expectedId + ".json";
    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4_0 + fileName);

    val task = parser.decode(ErxTask.class, content);
    assertNotNull(task, "Valid ErxTask must be parseable");

    val dc = DateConverter.getInstance();
    val expectedExpiry = dc.dateFromIso8601("2022-06-02");
    val expectedAccept = dc.dateFromIso8601("2022-04-02");

    assertEquals(expectedExpiry, task.getExpiryDate());
    assertEquals(expectedAccept, task.getAcceptDate());
  }

  @Test
  void shouldEncodeSingleTask1() {
    val fileName = "9b48f82c-9c11-4a57-aa72-a805f9537a82.json";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4_0 + fileName);
    val task = parser.decode(ErxTask.class, content);
    assertNotNull(task, "Valid ErxTask must be parseable");

    val expectedId = "9b48f82c-9c11-4a57-aa72-a805f9537a82";
    assertEquals(expectedId, task.getTaskId().getValue());

    val expectedPrescriptionID = PrescriptionId.from("160.000.033.491.280.78");
    assertEquals(expectedPrescriptionID, task.getPrescriptionId());

    val expectedFlowType = PrescriptionFlowType.FLOW_TYPE_160;
    assertEquals(expectedFlowType, task.getFlowType());

    val expectedAccessCode =
        AccessCode.from("777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");
    assertTrue(task.hasAccessCode());
    assertEquals(expectedAccessCode, task.getOptionalAccessCode().orElseThrow());

    assertFalse(task.hasSecret()); // resource does not have a secret

    val expectedStatus = TaskStatus.INPROGRESS;
    assertEquals(expectedStatus, task.getStatus());
    assertTrue(task.getForKvnr().isPresent());
    assertEquals("X123456789", task.getForKvnr().orElseThrow().getValue());

    val expectedPerformer = PerformerType.PUBLIC_PHARMACY;
    assertEquals(expectedPerformer, task.getPerformerFirstRep());

    val expectedIntent = Task.TaskIntent.ORDER;
    assertEquals(expectedIntent, task.getIntent());

    val dc = DateConverter.getInstance();
    val expectedExpiry = dc.dateFromIso8601("2022-06-02");
    val expectedAccept = dc.dateFromIso8601("2022-04-02");

    assertEquals(expectedExpiry, task.getExpiryDate());
    assertEquals(expectedAccept, task.getAcceptDate());

    // just for coverage, should never fail
    assertNotNull(task.toString());
  }

  @Test
  void shouldThrowMissingFieldExceptionWhenNoDatesGiven() {
    val task = new ErxTask();
    assertThrows(MissingFieldException.class, task::getExpiryDate);
    assertThrows(MissingFieldException.class, task::getAcceptDate);
  }

  @Test
  void shouldEncodeSingleDraftTask() {
    val fileName = "607255ed-ce41-47fc-aad3-cfce1c39963f.json";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4_0 + fileName);
    val task = parser.decode(ErxTask.class, content);
    assertNotNull(task, "Valid ErxTask must be parseable");

    val expectedId = "607255ed-ce41-47fc-aad3-cfce1c39963f";
    assertEquals(expectedId, task.getTaskId().getValue());

    val expectedPrescriptionID = PrescriptionId.from("160.000.033.491.280.78");
    assertEquals(expectedPrescriptionID, task.getPrescriptionId());

    val expectedFlowType = PrescriptionFlowType.FLOW_TYPE_160;
    assertEquals(expectedFlowType, task.getFlowType());

    val expectedAccessCode =
        AccessCode.from("777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");
    assertTrue(task.hasAccessCode());
    assertEquals(expectedAccessCode, task.getOptionalAccessCode().orElseThrow());

    assertFalse(task.hasSecret()); // resource does not have a secret

    val expectedStatus = TaskStatus.READY;
    assertEquals(expectedStatus, task.getStatus());
    assertFalse(task.getForKvnr().isEmpty());

    val expectedPerformer = PerformerType.PUBLIC_PHARMACY;
    assertEquals(expectedPerformer, task.getPerformerFirstRep());

    val expectedIntent = Task.TaskIntent.ORDER;
    assertEquals(expectedIntent, task.getIntent());

    // just for coverage, should never fail
    assertNotNull(task.toString());
  }

  @Test
  void shouldHaveSecret() {
    val expectedId = "f7942ab3-5d81-4a62-9f7d-d648661c79de";
    val fileName = expectedId + ".xml";
    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4_0 + fileName);

    val expectedSecret =
        Secret.from("77bd0d3fe7c2a4d0b7abca081ee92cd309eab76379c273b31efe624b4aaac239");
    val task = parser.decode(ErxTask.class, content);
    assertNotNull(task, "Valid ErxTask must be parseable");
    assertEquals(TaskStatus.INPROGRESS, task.getStatus());
    assertTrue(task.hasSecret());
    assertTrue(task.hasAccessCode());
    assertEquals(expectedSecret, task.getSecret().orElseThrow());
  }

  @Test
  void shouldHaveLastMedicationDispenseDate() throws ParseException {
    val fileName = "9b48f82c-9c11-4a57-aa72-a805f9537a82.json";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4_0 + fileName);
    val task = parser.decode(ErxTask.class, content);
    assertNotNull(task, "Valid ErxTask must be parseable");

    assertTrue(task.hasLastMedicationDispenseDate());
    val lmd = task.getLastMedicationDispenseDate().orElseThrow();

    val formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    val expectation = formatter.parse("2022-05-20T13:28:17+02:00").toInstant();
    assertEquals(expectation, lmd);
  }

  @Test
  void shouldThrowOnMissingFields() {
    val expectedId = "9b48f82c-9c11-4a57-aa72-a805f9537a82";
    val fileName = expectedId + ".json";
    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4_0 + fileName);

    val task = parser.decode(ErxTask.class, content);

    // remove all identifier to ensure there are no prescription IDs and access codes
    task.setIdentifier(List.of());
    // remove all extensions to ensure there are no prescription flow types
    task.setExtension(List.of());

    assertThrows(MissingFieldException.class, task::getPrescriptionId);
    assertThrows(MissingFieldException.class, task::getFlowType);
    assertThrows(MissingFieldException.class, task::getAccessCode);
  }

  @Test
  void shouldGenerateErxTaskFromResource() {
    val resource = new Task();
    val erxTask = ErxTask.fromTask(resource);
    assertNotNull(erxTask);
    assertEquals(ErxTask.class, erxTask.getClass());
  }

  @Test
  void shouldReturnEmptyWhenLastMedicationDispenseDateIsAbsent() {
    val expectedId = "607255ed-ce41-47fc-aad3-cfce1c39963f";
    val fileName = expectedId + ".json";
    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4_0 + fileName);

    val task = parser.decode(ErxTask.class, content);
    assertNotNull(task, "Valid ErxTask must be parseable");

    assertFalse(task.getLastMedicationDispenseDateElement().isPresent());
  }
}
