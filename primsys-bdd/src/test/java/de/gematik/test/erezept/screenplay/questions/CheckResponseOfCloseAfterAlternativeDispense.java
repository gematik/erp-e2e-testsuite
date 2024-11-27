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

import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.LinkedList;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

public class CheckResponseOfCloseAfterAlternativeDispense implements Question<Boolean> {

  private final DequeStrategy order;

  private final Actor patient;

  private CheckResponseOfCloseAfterAlternativeDispense(DequeStrategy order, Actor patient) {
    this.order = order;
    this.patient = patient;
  }

  public static Builder forPrescription(String order) {
    return forPrescription(DequeStrategy.fromString(order));
  }

  public static Builder forPrescription(DequeStrategy order) {
    return new Builder(order);
  }

  @Override
  public Boolean answeredBy(Actor actor) {
    val checkResults = new LinkedList<Boolean>();
    val prescriptionManager = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);

    val dispensedPrescription = order.chooseFrom(prescriptionManager.getDispensedPrescriptions());
    val dispensedMedicationId =
        dispensedPrescription.getMedicationDispenses().get(0).getErpMedicationFirstRep().getId();

    val patientsMedicationDispense =
        patient.asksFor(GetMedicationDispense.asPatient().forPrescription(order));

    // TODO: rather then checking the ID of the contained medication, check the prescriptionId!
    checkResults.add(
        patientsMedicationDispense.getMedicationDispenses().stream()
            .noneMatch(md -> dispensedMedicationId.equals(md.getErpMedicationFirstRep().getId())));

    // TODO: recap if we need those checks here at all
    val telematikId = actor.abilityTo(UseSMCB.class).getTelematikID();
    checkResults.add(
        patientsMedicationDispense.getMedicationDispenses().stream()
            .allMatch(md -> md.getPerformerIdFirstRep().equals(telematikId)));

    return !checkResults.contains(false);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {

    private final DequeStrategy order;

    public CheckResponseOfCloseAfterAlternativeDispense forThePatient(Actor patient) {
      return new CheckResponseOfCloseAfterAlternativeDispense(order, patient);
    }
  }
}
