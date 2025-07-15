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

package de.gematik.test.erezept.screenplay.questions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBase;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.screenplay.strategy.ActorRole;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MedicationDispenseContains implements Question<Boolean> {

  private final GetMedicationDispense question;
  private final Predicate<ErxMedicationDispenseBundle> predicate;

  @Override
  @Step("{0} ruft eine MedicationDispense am FD ab und überprüft den Inhalt")
  public Boolean answeredBy(Actor actor) {
    val response = actor.asksFor(question);
    log.info(
        format(
            "Actor {0} asked for MedicationDispenses and received {1} elements",
            actor.getName(), response.getMedicationDispenses().size()));
    return predicate.test(response);
  }

  public static Builder forThePatient() {
    return forActorRole(ActorRole.PATIENT);
  }

  public static Builder forThePharmacy() {
    return forActorRole(ActorRole.PHARMACY);
  }

  public static Builder forActorRole(ActorRole role) {
    return new Builder(role);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final ActorRole role;
    private DequeStrategy deque;

    public Builder andPrescription(String order) {
      return andPrescription(DequeStrategy.fromString(order));
    }

    public Builder andPrescription(DequeStrategy deque) {
      this.deque = deque;
      return this;
    }

    public MedicationDispenseContains numberOfMedicationDispenses(long num) {
      val q = GetMedicationDispense.as(role).forPrescription(deque);
      Predicate<ErxMedicationDispenseBundle> p =
          (actual) -> actual.getMedicationDispenses().size() == num;
      return new MedicationDispenseContains(q, p);
    }

    public MedicationDispenseContains hasRedeemCode() {
      val q = GetMedicationDispense.as(role).forPrescription(deque);
      Predicate<ErxMedicationDispenseBundle> p =
          (actual) ->
              actual.getMedicationDispenses().stream()
                  .filter(ErxMedicationDispenseBase::isDiGA)
                  .map(ErxMedicationDispense::getRedeemCode)
                  .map(Optional::isPresent)
                  .findFirst()
                  .orElse(false);
      return new MedicationDispenseContains(q, p);
    }

    public MedicationDispenseContains hasNoRedeemCode() {
      val q = GetMedicationDispense.as(role).forPrescription(deque);
      Predicate<ErxMedicationDispenseBundle> p =
          (actual) ->
              actual.getMedicationDispenses().stream()
                  .filter(ErxMedicationDispenseBase::isDiGA)
                  .map(ErxMedicationDispense::getRedeemCode)
                  .toList()
                  .isEmpty();

      return new MedicationDispenseContains(q, p);
    }
  }
}
