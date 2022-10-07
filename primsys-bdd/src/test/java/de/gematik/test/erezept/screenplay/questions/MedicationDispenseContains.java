/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.screenplay.questions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.screenplay.strategy.ActorRole;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategyEnum;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MedicationDispenseContains implements Question<Boolean> {

  private final GetMedicationDispense question;
  private final Predicate<ErxMedicationDispenseBundle> predicate;

  @Override
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
    private DequeStrategyEnum deque;

    public Builder andPrescription(String order) {
      return andPrescription(DequeStrategyEnum.fromString(order));
    }

    public Builder andPrescription(DequeStrategyEnum deque) {
      this.deque = deque;
      return this;
    }

    public MedicationDispenseContains numberOfMedicationDispenses(long num) {
      val q = GetMedicationDispense.as(role).forPrescription(deque);
      Predicate<ErxMedicationDispenseBundle> p =
          (actual) -> actual.getMedicationDispenses().size() == num;
      return new MedicationDispenseContains(q, p);
    }
  }
}
