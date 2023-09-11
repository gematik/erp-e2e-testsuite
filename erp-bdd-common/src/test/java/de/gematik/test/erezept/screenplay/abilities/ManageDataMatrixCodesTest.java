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

package de.gematik.test.erezept.screenplay.abilities;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import lombok.val;
import org.junit.jupiter.api.Test;

class ManageDataMatrixCodesTest {

  @Test
  void shouldAddAndConsumeDmcs() {
    val manageDmcs = ManageDataMatrixCodes.sheGetsPrescribed();

    val first = TaskId.from("taskId");
    val second = TaskId.from("taskId2");
    manageDmcs.appendDmc(DmcPrescription.ownerDmc(first, AccessCode.random()));
    manageDmcs.appendDmc(DmcPrescription.ownerDmc(second, AccessCode.random()));
    assertEquals(2, manageDmcs.getDmcs().getRawList().size());

    var dmc = manageDmcs.getFirstDmc();
    assertEquals(first, dmc.getTaskId());
    assertEquals(2, manageDmcs.getDmcs().getRawList().size());

    dmc = manageDmcs.getLastDmc();
    assertEquals(second, dmc.getTaskId());
    assertEquals(2, manageDmcs.getDmcs().getRawList().size());

    dmc = manageDmcs.consumeFirstDmc();
    assertEquals(first, dmc.getTaskId());
    assertEquals(1, manageDmcs.getDmcs().getRawList().size());

    dmc = manageDmcs.consumeLastDmc();
    assertEquals(second, dmc.getTaskId());
    assertEquals(0, manageDmcs.getDmcs().getRawList().size());
  }

  @Test
  void shouldChooseStacks() {
    val manageDmcs = ManageDataMatrixCodes.sheGetsPrescribed();

    manageDmcs.appendDmc(DmcPrescription.ownerDmc(TaskId.from("taskId"), AccessCode.random()));
    manageDmcs.appendDmc(DmcPrescription.ownerDmc(TaskId.from("taskId2"), AccessCode.random()));

    val activeDmcs = manageDmcs.chooseStack(DmcStack.ACTIVE);
    assertEquals(2, activeDmcs.getRawList().size());

    val deletedDmcs = manageDmcs.chooseStack(DmcStack.DELETED);
    assertTrue(deletedDmcs.isEmpty());
  }

  @Test
  void shouldThrowOnEmptyStacks() {
    val manageDmcs = ManageDataMatrixCodes.sheGetsPrescribed();
    assertEquals(0, manageDmcs.getDmcs().getRawList().size());

    assertThrows(MissingPreconditionError.class, manageDmcs::getFirstDmc);
    assertThrows(MissingPreconditionError.class, manageDmcs::getLastDmc);
  }
  
  @Test
  void shouldHaveToString() {
    assertTrue(DmcStack.ACTIVE.toString().contains("ausgestellt"));
    assertTrue(DmcStack.DELETED.toString().contains("gelöscht"));
  }
}
