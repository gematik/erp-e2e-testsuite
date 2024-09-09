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

package de.gematik.test.erezept.screenplay.task;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
public class HandoverDataMatrixCode implements Task {

  private final DmcStack dmcStack;
  private final Actor pharmacy;
  private final DequeStrategy deque;

  private HandoverDataMatrixCode(DmcStack dmcStack, DequeStrategy deque, Actor pharmacy) {
    this.dmcStack = dmcStack;
    this.pharmacy = pharmacy;
    this.deque = deque;
  }

  @Override
  public <T extends Actor> void performAs(final T actor) {
    val ability = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val managedStack = ability.chooseStack(dmcStack);
    val dmc = deque.chooseFrom(managedStack);

    val prescriptionManager = SafeAbility.getAbility(pharmacy, ManagePharmacyPrescriptions.class);
    prescriptionManager.appendAssignedPrescription(dmc);
    log.info(
        format(
            "Patient {0} has physically assigned Prescription with TaskID {1} to Pharmacy {2}",
            actor.getName(), dmc.getTaskId(), pharmacy.getName()));
  }

  public static Builder fromStack(String stack) {
    return new Builder(DmcStack.fromString(stack));
  }

  public static Builder fromActiveStack() {
    return new Builder(DmcStack.ACTIVE);
  }

  public static Builder fromDeletedStack() {
    return new Builder(DmcStack.DELETED);
  }

  public static class Builder {
    private final DmcStack stack;
    private DequeStrategy dequeue;

    private Builder(DmcStack stack) {
      this.stack = stack;
    }

    public Builder with(String order) {
      return with(DequeStrategy.fromString(order));
    }

    public Builder with(DequeStrategy dequeue) {
      this.dequeue = dequeue;
      return this;
    }

    public HandoverDataMatrixCode to(Actor pharmacy) {
      return new HandoverDataMatrixCode(stack, dequeue, pharmacy);
    }
  }
}
