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

package de.gematik.test.erezept.fhir.resources.erp;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.exceptions.*;
import de.gematik.test.erezept.fhir.parser.profiles.systems.*;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.util.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Task.*;
import org.junit.jupiter.api.*;

class ErxTaskTest extends ParsingTest {
  private final String BASE_PATH = "fhir/valid/erp/";
  private final String BASE_PATH_1_1_1 = BASE_PATH + "1.1.1/";
  private final String BASE_PATH_1_2_0 = BASE_PATH + "1.2.0/task/";

  @Test
  void shouldEncodeSingleTask111() {
    val fileName = "Task_01.xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_1_1 + fileName);
    val task = parser.decode(ErxTask.class, content);
    assertNotNull(task, "Valid ErxTask must be parseable");

    val expectedId = "16aaabf2-1de1-11b2-8057-a3c264e57515";
    assertEquals(expectedId, task.getUnqualifiedId());

    val expectedPrescriptionID = new PrescriptionId("160.002.266.580.400.82");
    assertEquals(expectedPrescriptionID, task.getPrescriptionId());

    val expectedFlowType = PrescriptionFlowType.FLOW_TYPE_160;
    assertEquals(expectedFlowType, task.getFlowType());

    val expectedAccessCode =
        new AccessCode("8d7d61c14e3e24c79d17dbe4ef0fc3cf9356cbfa07aecb593246314434606642");
    assertTrue(task.hasAccessCode());
    assertEquals(expectedAccessCode, task.getAccessCode());

    assertFalse(task.hasSecret()); // does not have a secret in Task_01.xml

    val expectedStatus = Task.TaskStatus.DRAFT;
    assertEquals(expectedStatus, task.getStatus());
    assertTrue(
        task.getForKvid().isEmpty()); // DRAFT-Task does not have a KVID for a specific Patient

    val expectedPerformer = PerformerType.PUBLIC_PHARMACY;
    assertEquals(expectedPerformer, task.getPerformerFirstRep());

    val expectedIntent = Task.TaskIntent.ORDER;
    assertEquals(expectedIntent, task.getIntent());

    // just for coverage, should never fail
    assertNotNull(task.toString());
  }

  @Test
  void shouldEncodeSingleTask120() {
    val fileName = "160_000_031_325_714_07.json";

    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_2_0 + fileName);
    val task = parser.decode(ErxTask.class, content);
    assertNotNull(task, "Valid ErxTask must be parseable");

    val expectedId = "160.000.031.325.714.07";
    assertEquals(expectedId, task.getUnqualifiedId());

    val expectedPrescriptionID = new PrescriptionId("160.000.031.325.714.07");
    assertEquals(expectedPrescriptionID, task.getPrescriptionId());

    val expectedFlowType = PrescriptionFlowType.FLOW_TYPE_160;
    assertEquals(expectedFlowType, task.getFlowType());

    val expectedAccessCode =
        new AccessCode("d9fb6f72947ae4d85e1e5c9c8a00502975cb3d5efe3321f079890c829eeeb7ae");
    assertTrue(task.hasAccessCode());
    assertEquals(expectedAccessCode, task.getOptionalAccessCode().orElseThrow());

    assertFalse(task.hasSecret()); // resource does not have a secret

    val expectedStatus = TaskStatus.READY;
    assertEquals(expectedStatus, task.getStatus());
    assertTrue(task.getForKvid().isPresent());
    assertEquals("X110498565", task.getForKvid().orElseThrow());

    val expectedPerformer = PerformerType.PUBLIC_PHARMACY;
    assertEquals(expectedPerformer, task.getPerformerFirstRep());

    val expectedIntent = Task.TaskIntent.ORDER;
    assertEquals(expectedIntent, task.getIntent());

    // just for coverage, should never fail
    assertNotNull(task.toString());
  }

  @Test
  void shouldEncodeSinglePkvDraftTask120() {
    val fileName = "209_000_000_000_035_71.xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_2_0 + fileName);
    val task = parser.decode(ErxTask.class, content);
    assertNotNull(task, "Valid ErxTask must be parseable");

    val expectedId = "209.000.000.000.035.71";
    assertEquals(expectedId, task.getUnqualifiedId());

    val expectedPrescriptionID = new PrescriptionId("209.000.000.000.035.71");
    assertEquals(expectedPrescriptionID, task.getPrescriptionId());

    val expectedFlowType = PrescriptionFlowType.FLOW_TYPE_209;
    assertEquals(expectedFlowType, task.getFlowType());

    val expectedAccessCode =
        new AccessCode("b7bc799dd564e3ae10e79b3558154daa720c190613809088b964d090ec9ed2c2");
    assertTrue(task.hasAccessCode());
    assertEquals(expectedAccessCode, task.getOptionalAccessCode().orElseThrow());

    assertFalse(task.hasSecret()); // resource does not have a secret

    val expectedStatus = TaskStatus.DRAFT;
    assertEquals(expectedStatus, task.getStatus());
    assertTrue(task.getForKvid().isEmpty());

    val expectedPerformer = PerformerType.PUBLIC_PHARMACY;
    assertEquals(expectedPerformer, task.getPerformerFirstRep());

    val expectedIntent = Task.TaskIntent.ORDER;
    assertEquals(expectedIntent, task.getIntent());

    // just for coverage, should never fail
    assertNotNull(task.toString());
  }

  @Test
  void shouldHaveSecret() {
    val fileName = "Task_03";
    val fileExtensions = List.of(".xml", ".json");

    fileExtensions.stream()
        .map(ext -> ResourceUtils.readFileFromResource(BASE_PATH_1_1_1 + fileName + ext))
        .forEach(
            content -> {
              val expectedSecret =
                  new Secret("c36ca26502892b371d252c99b496e31505ff449aca9bc69e231c58148f6233cf");
              val task = parser.decode(ErxTask.class, content);
              assertNotNull(task, "Valid ErxTask must be parseable");
              assertTrue(task.hasSecret());
              assertTrue(task.hasAccessCode());
              assertEquals(expectedSecret, task.getSecret().orElseThrow());
            });
  }

  @Test
  void shouldThrowOnMissingPrescriptionId() {
    val fileName = "Task_01.xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_1_1 + fileName);
    val task = parser.decode(ErxTask.class, content);

    // remove the prescription ID identifier
    val r =
        task.getIdentifier()
            .removeIf(
                identifier ->
                    ErpWorkflowNamingSystem.PRESCRIPTION_ID
                            .getCanonicalUrl()
                            .equals(identifier.getSystem())
                        || ErpWorkflowNamingSystem.PRESCRIPTION_ID_121
                            .getCanonicalUrl()
                            .equals(identifier.getSystem()));
    assertThrows(MissingFieldException.class, task::getPrescriptionId);
  }
}
