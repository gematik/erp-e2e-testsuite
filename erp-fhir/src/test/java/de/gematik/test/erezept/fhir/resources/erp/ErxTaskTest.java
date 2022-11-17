/*
 * Copyright (c) 2022 gematik GmbH
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

import static org.junit.Assert.*;

import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.valuesets.PerformerType;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Task;
import org.junit.Before;
import org.junit.Test;

public class ErxTaskTest {
  private final String BASE_PATH = "fhir/valid/erp/1.1.1/";

  private FhirParser parser;

  @Before
  public void setUp() {
    this.parser = new FhirParser();
  }

  @Test
  public void shouldEncodeSingleTask() {
    val fileName = "Task_01.xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val task = parser.decode(ErxTask.class, content);
    assertNotNull("Valid ErxTask must be parseable", task);

    val expectedId = "16aaabf2-1de1-11b2-8057-a3c264e57515";
    assertEquals(expectedId, task.getUnqualifiedId());

    val expectedPrescriptionID = new PrescriptionId("160.002.266.580.400.82");
    assertEquals(expectedPrescriptionID, task.getPrescriptionId());

    val expectedFlowType = PrescriptionFlowType.FLOW_TYPE_160;
    assertEquals(expectedFlowType, task.getFlowType());

    val expectedAccessCode =
        new AccessCode("8d7d61c14e3e24c79d17dbe4ef0fc3cf9356cbfa07aecb593246314434606642");
    assertTrue(task.hasAccessCode());
    assertEquals(expectedAccessCode, task.getOptionalAccessCode().orElseThrow());

    assertFalse(task.hasSecret()); // does not have a secret in Task_01.xml

    val expectedStatus = Task.TaskStatus.DRAFT;
    assertEquals(expectedStatus, task.getStatus());
    assertTrue(
        task.getForKvid().isEmpty()); // DRAFT-Task does not have a KVID for a specific Patient

    val expectedPerformer = PerformerType.PUBLIC_PHARMACY;
    assertEquals(expectedPerformer, task.getPerformerFirstRep());

    val expectedIntent = Task.TaskIntent.ORDER;
    assertEquals(expectedIntent, task.getIntent());
  }

  @Test
  public void shouldHaveSecret() {
    val fileName = "Task_03";
    val fileExtensions = List.of(".xml", ".json");

    fileExtensions.stream()
        .map(ext -> ResourceUtils.readFileFromResource(BASE_PATH + fileName + ext))
        .forEach(
            content -> {
              val expectedSecret =
                  new Secret("c36ca26502892b371d252c99b496e31505ff449aca9bc69e231c58148f6233cf");
              val task = parser.decode(ErxTask.class, content);
              assertNotNull("Valid ErxTask must be parseable", task);
              assertTrue(task.hasSecret());
              assertTrue(task.hasAccessCode());
              assertEquals(expectedSecret, task.getSecret().orElseThrow());
            });
  }
}