/*
 * Copyright (Change Date see Readme), gematik GmbH
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

import static java.text.MessageFormat.format;

import de.gematik.erezept.remotefdv.api.model.MedicationDispense;
import de.gematik.test.erezept.common.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.common.screenplay.util.SafeAbility;
import de.gematik.test.erezept.fdv.abilities.UseTheRemoteFdVClient;
import de.gematik.test.erezept.remotefdv.client.PatientRequests;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
  private final DequeStrategy deque;
  private final Predicate<List<MedicationDispense>> predicate;

  @Override
  @Step("{0} ruft eine MedicationDispense am FD ab und überprüft den Inhalt")
  public Boolean answeredBy(Actor actor) {
    val client = SafeAbility.getAbility(actor, UseTheRemoteFdVClient.class);
    val receivedDrugs = SafeAbility.getAbility(actor, ReceiveDispensedDrugs.class);
    val dispensationInformation = deque.chooseFrom(receivedDrugs.getDispensedDrugsList());
    val prescriptionId = dispensationInformation.prescriptionId();

    val localDate =
        dispensationInformation.dispenseDate().atZone(ZoneId.systemDefault()).toLocalDate();
    val whenHandedOver = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    val response = client.sendRequest(PatientRequests.getMedicationDispense(whenHandedOver));
    val medDispenses =
        response.getExpectedResourcesList().stream()
            .filter(md -> md.getPrescriptionId().contains(prescriptionId.getValue()))
            .toList();

    log.info(
        format(
            "Actor {0} asked for MedicationDispenses and received {1} elements",
            actor.getName(), response.getExpectedResourcesList().size()));
    return predicate.test(medDispenses);
  }

  public static Builder forPatient() {
    return new Builder();
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private DequeStrategy deque;

    public Builder andPrescription(DequeStrategy deque) {
      this.deque = deque;
      return this;
    }

    public Builder andPrescription(String order) {
      return andPrescription(DequeStrategy.fromString(order));
    }

    public MedicationDispenseContains numberOfMedicationDispenses(long num) {
      Predicate<List<MedicationDispense>> p = (actual) -> actual.size() == num;
      return new MedicationDispenseContains(deque, p);
    }
  }
}
