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

package de.gematik.test.erezept.screenplay.task;

import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

public class LoosePresciption implements Task {

  private final DequeStrategy deque;

  public LoosePresciption(DequeStrategy deque) {
    this.deque = deque;
  }

  public static LoosePresciption fromStack(String deque) {
    return fromStack(DequeStrategy.fromString(deque));
  }

  public static LoosePresciption fromStack(DequeStrategy deque) {
    return new LoosePresciption(deque);
  }

  @Override
  public <T extends Actor> void performAs(final T actor) {
    val prescriptionManager = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    val prescription = deque.chooseFrom(prescriptionManager.getAcceptedPrescriptions());
    prescriptionManager.getAcceptedPrescriptions().getRawList().remove(prescription);
  }
}
