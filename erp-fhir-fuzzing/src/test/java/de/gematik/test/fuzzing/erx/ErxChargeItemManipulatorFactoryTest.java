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

package de.gematik.test.fuzzing.erx;

import static de.gematik.test.fuzzing.erx.ErxChargeItemManipulatorFactory.binaryVersionManipulator;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemFaker;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import lombok.val;
import org.junit.jupiter.api.Test;

class ErxChargeItemManipulatorFactoryTest {

  @Test
  void shouldManipulateChargeItem() {
    val cI = ErxChargeItemFaker.builder().fake();
    val orgProfile = cI.getContained().get(0).getMeta().getProfile();
    val manipulators = binaryVersionManipulator();
    manipulators.forEach(m -> m.getParameter().accept(cI));
    assertNotEquals(orgProfile, cI.getContained().get(0).getMeta().getProfile());
    assertTrue(
        cI.getContained()
            .get(0)
            .getMeta()
            .getProfile()
            .get(0)
            .getValue()
            .contains(ErpWorkflowStructDef.BINARY_12.getCanonicalUrl()));
  }

  @Test
  void shouldDetectChargeItemBinaryVersionAfterManipulation() {
    val cI = ErxChargeItemFaker.builder().fake();
    assertFalse(cI.getContained().get(0).getMeta().hasProfile());

    val manipulators = binaryVersionManipulator();
    manipulators.forEach(m -> m.getParameter().accept(cI));
    assertTrue(
        cI.getContained()
            .get(0)
            .getMeta()
            .getProfile()
            .get(0)
            .getValue()
            .contains(ErpWorkflowStructDef.BINARY_12.getCanonicalUrl()));
  }
}
