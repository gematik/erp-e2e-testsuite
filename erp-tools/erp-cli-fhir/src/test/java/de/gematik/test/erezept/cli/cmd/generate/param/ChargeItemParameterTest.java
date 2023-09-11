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

package de.gematik.test.erezept.cli.cmd.generate.param;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import lombok.val;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import picocli.CommandLine.MutuallyExclusiveArgsException;

class ChargeItemParameterTest {

  @Test
  void shouldNotRequireAnyOptions() {
    val cip = new ChargeItemParameter();
    val cmdline = new CommandLine(cip);
    assertDoesNotThrow(() -> cmdline.parseArgs());

    val ci = cip.createChargeItem();
    assertNotNull(ci);
    assertEquals(
        PrescriptionFlowType.FLOW_TYPE_200, ci.getPrescriptionId().getFlowType()); // default 200
  }

  @Test
  void shouldSetPrescriptionId() {
    val cip = new ChargeItemParameter();
    val cmdline = new CommandLine(cip);
    assertDoesNotThrow(() -> cmdline.parseArgs("--prescriptionid", "160.0.0.1"));

    val ci = cip.createChargeItem();
    assertNotNull(ci);
    assertEquals(PrescriptionFlowType.FLOW_TYPE_160, ci.getPrescriptionId().getFlowType());
  }

  @Test
  void shouldSetPrescriptionIdByFlowType() {
    val cip = new ChargeItemParameter();
    val cmdline = new CommandLine(cip);
    assertDoesNotThrow(() -> cmdline.parseArgs("--flowtype", "169"));

    val ci = cip.createChargeItem();
    assertNotNull(ci);
    assertEquals(PrescriptionFlowType.FLOW_TYPE_169, ci.getPrescriptionId().getFlowType());
  }

  @Test
  void shouldHavePrescriptionIdOrFlowTypeExclusively() {
    val cip = new ChargeItemParameter();
    val cmdline = new CommandLine(cip);
    assertThrows(
        MutuallyExclusiveArgsException.class,
        () -> cmdline.parseArgs("--prescriptionid", "160.0.0.1", "--flowtype", "169"));
  }
}
