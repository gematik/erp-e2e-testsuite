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

package de.gematik.test.erezept.fhir.values;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.Identifier;
import org.junit.jupiter.api.Test;

class PrescriptionIdTest {

  @Test
  void checkValidPrescriptionId() {
    val id = "160.000.000.000.123.76";
    assertTrue(PrescriptionId.checkId(id));
  }

  @Test
  void checkInvalidPrescriptionId() {
    val id = "160.000.000.000.123.77";
    assertFalse(PrescriptionId.checkId(id));
  }

  @Test
  void shouldTranslateToTaskId() {
    val pid = PrescriptionId.random();
    val tid = pid.toTaskId();
    assertEquals(pid.getValue(), tid.getValue());
  }

  @Test
  void checkRandomPrescriptionId() {
    for (int i = 0; i < 10; i++) {
      // just run a few iterations to really make sure random IDs are okay!
      val r = PrescriptionId.random();
      assertTrue(PrescriptionId.checkId(r));
    }
  }

  @Test
  void shouldCreatePrescriptionIdFromString() {
    val p = PrescriptionId.from("123");
    assertFalse(p.check());
  }

  @Test
  void shouldDetectOldPrescriptionId() {
    val identifier = PrescriptionId.random().asIdentifier(ErpWorkflowNamingSystem.PRESCRIPTION_ID);
    assertTrue(PrescriptionId.isPrescriptionId(identifier));
  }

  @Test
  void shouldDetectNewPrescriptionId() {
    val identifier =
        PrescriptionId.random().asIdentifier(ErpWorkflowNamingSystem.PRESCRIPTION_ID_121);
    assertTrue(PrescriptionId.isPrescriptionId(identifier));
  }

  @Test
  void shouldGetFlowTypeFromPrescriptionId() {
    Stream.of(PrescriptionFlowType.values())
        .forEach(
            ft -> {
              val prescriptionId = PrescriptionId.random(ft);
              assertEquals(ft, prescriptionId.getFlowType());
            });
  }

  @Test
  void shouldThrowOnPrescriptionIdOfInvalidFlowType() {
    val prescriptionId = PrescriptionId.from("127.0.0.1");
    assertThrows(InvalidValueSetException.class, prescriptionId::getFlowType);
  }

  @Test
  void shouldCreateFromTaskId() {
    val taskId = TaskId.from("123");
    val prescriptionId = PrescriptionId.from(taskId);
    assertEquals("123", prescriptionId.getValue());
  }

  @Test
  void shouldNotMatchOnMissingSystem() {
    val identifier = new Identifier();
    identifier.setValue("160.000.006.403.515.71");
    assertFalse(PrescriptionId.isPrescriptionId(identifier));
  }
}
