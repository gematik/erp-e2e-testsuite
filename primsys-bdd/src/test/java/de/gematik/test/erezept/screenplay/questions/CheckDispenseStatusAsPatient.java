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

import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.time.Instant;
import java.util.LinkedList;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Type;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CheckDispenseStatusAsPatient implements Question<Boolean> {

  private final DequeStrategy deque;
  @Nullable private final Actor pharmacy;

  public static Builder forThePatient() {
    return new Builder();
  }

  @Override
  public Boolean answeredBy(Actor actor) {
    val dispensed = SafeAbility.getAbility(actor, ReceiveDispensedDrugs.class);

    val prescriptionId = deque.chooseFrom(dispensed.getDispensedDrugsList());
    val response = actor.asksFor(DownloadTaskById.withId(TaskId.from(prescriptionId)));
    log.info(
        format(
            "Actor {0} asked for Task with Id {1} and received {2}",
            actor.getName(), prescriptionId.getValue(), response));

    val checkResults = new LinkedList<Boolean>();
    checkResults.add(
        response.getEntry().stream()
            .anyMatch(entry -> entry.getResource().getIdPart().equals(prescriptionId.getValue())));
    if (pharmacy != null) {
      val medDispTimeStampList =
          SafeAbility.getAbility(pharmacy, ManagePharmacyPrescriptions.class)
              .getDispenseTimestamps();
      val dispTime =
          medDispTimeStampList
              .getRawList()
              .get(
                  deque.equals(DequeStrategy.LIFO)
                      ? medDispTimeStampList.getRawList().size() - 1
                      : 0);

      val timeStampOfLastMedDispens =
          response.getTask().getExtension().stream()
              .filter(ext -> ext.getUrl().contains("GEM_ERP_EX_LastMedicationDispense"))
              .findFirst()
              .orElseThrow()
              .getValue();

      val timeStampEqualsDispensation = compareTimes(timeStampOfLastMedDispens, dispTime);
      checkResults.add(timeStampEqualsDispensation);
    }
    checkResults.add(response.getTask().getStatus().equals(Task.TaskStatus.INPROGRESS));
    return (!checkResults.contains(false));
  }

  private boolean compareTimes(Type timeOne, Instant timeTwo) { // todo ask for better Compare
    return timeOne.primitiveValue().substring(0, 19).equals(timeTwo.toString().substring(0, 19));
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private DequeStrategy deque;
    @Nullable private Actor pharmacy;

    public Builder andPrescription(String order) {
      return andPrescription(DequeStrategy.fromString(order));
    }

    public Builder andPrescription(DequeStrategy deque) {
      this.deque = deque;
      return this;
    }

    @Nullable
    public Builder byPharmacy(Actor pharmacy) {
      this.pharmacy = pharmacy;
      return this;
    }

    public CheckDispenseStatusAsPatient build() {
      return new CheckDispenseStatusAsPatient(deque, pharmacy);
    }
  }
}
