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

package de.gematik.test.erezept.screenplay.questions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.LinkedList;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MedicationDispenseContainsMedication implements Question<Boolean> {
  private final DequeStrategy medDspOrder;

  private final GetMedicationDispense question;

  private final Actor pharmacy;

  public static Builder withMedicationDispense(String order) {
    return withMedicationDispense(DequeStrategy.fromString(order));
  }

  public static Builder withMedicationDispense(DequeStrategy deque) {
    return new Builder(deque);
  }

  @Override
  public Boolean answeredBy(Actor actor) {
    val checkResults = new LinkedList<Boolean>();
    val prescriptionManager = SafeAbility.getAbility(pharmacy, ManagePharmacyPrescriptions.class);
    val dispenseBundle = medDspOrder.chooseFrom(prescriptionManager.getDispensedPrescriptions());

    val response = actor.asksFor(question);
    log.info(
        format(
            "Actor {0} asked for MedicationDispenses and received {1} elements",
            actor.getName(), response.getMedicationDispenses().size()));
    checkResults.add(response.getMedicationDispenses().size() == 1);

    val medDspFromStack = medDspOrder.chooseFrom(dispenseBundle.getMedicationDispenses());
    val medDspFromFD = response.getMedicationDispenses().get(0);
    checkResults.add(
        medDspFromFD
            .getErpMedicationFirstRep()
            .getId()
            .equals(medDspFromStack.getErpMedicationFirstRep().getId()));
    checkResults.add(
        medDspFromFD.getPerformerIdFirstRep().equals(medDspFromStack.getPerformerIdFirstRep()));
    checkResults.add(
        medDspFromFD
            .getErpMedicationFirstRep()
            .getPznFirstRep()
            .equals(medDspFromStack.getErpMedicationFirstRep().getPznFirstRep()));

    return (!checkResults.contains(false));
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final DequeStrategy medDspOrder;
    private DequeStrategy prescOrder;

    public Builder andPrescription(String order) {
      return andPrescription(DequeStrategy.fromString(order));
    }

    public Builder andPrescription(DequeStrategy deque) {
      this.prescOrder = deque;
      return this;
    }

    public MedicationDispenseContainsMedication ofPharmacy(Actor thePharmacy) {
      return new MedicationDispenseContainsMedication(
          medDspOrder, GetMedicationDispense.asPatient().forPrescription(prescOrder), thePharmacy);
    }
  }
}