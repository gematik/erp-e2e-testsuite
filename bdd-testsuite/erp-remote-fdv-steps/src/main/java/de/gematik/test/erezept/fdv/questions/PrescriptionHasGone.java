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

package de.gematik.test.erezept.fdv.questions;

import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@RequiredArgsConstructor
public class PrescriptionHasGone implements Question<Boolean> {
  private final DequeStrategy deque;
  private final DmcStack stack;

  @Override
  @Step("{0} vergewissert, dass die Prescription nicht mehr vorhanden ist")
  public Boolean answeredBy(Actor actor) {
    val dmcAbility = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmc = deque.chooseFrom(dmcAbility.chooseStack(stack));
    val response = actor.asksFor(ReadPrescription.withTaskId(dmc.getTaskId()));
    return response.getResourceOptional().isEmpty();
  }

  public static Builder fromStack(String stack) {
    return fromStack(DmcStack.fromString(stack));
  }

  public static Builder fromStack(DmcStack stack) {
    return new Builder(stack);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final DmcStack stack;

    public PrescriptionHasGone withDeque(String order) {
      return withDeque(DequeStrategy.fromString(order));
    }

    public PrescriptionHasGone withDeque(DequeStrategy deque) {
      return Instrumented.instanceOf(PrescriptionHasGone.class).withProperties(deque, stack);
    }
  }
}
