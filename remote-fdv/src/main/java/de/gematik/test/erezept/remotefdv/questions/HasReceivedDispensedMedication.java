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

package de.gematik.test.erezept.remotefdv.questions;

import de.gematik.test.erezept.remotefdv.abilities.UseTheRemoteFdVClient;
import de.gematik.test.erezept.remotefdv.client.PatientRequests;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@RequiredArgsConstructor
@Slf4j
public class HasReceivedDispensedMedication implements Question<Boolean> {

  private final DequeStrategy deque;

  public static Question<Boolean> fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static Question<Boolean> fromStack(DequeStrategy deque) {
    return new HasReceivedDispensedMedication(deque);
  }

  @Override
  @Step("{0} überprüft ob Dispensierinformationen erhalten wurden")
  public Boolean answeredBy(Actor actor) {
    val client = SafeAbility.getAbility(actor, UseTheRemoteFdVClient.class);
    val receivedDrugs = SafeAbility.getAbility(actor, ReceiveDispensedDrugs.class);
    val dispensationInformation = deque.chooseFrom(receivedDrugs.getDispensedDrugsList());
    val prescriptionId = dispensationInformation.prescriptionId();

    val localDate =
        dispensationInformation.dispenseDate().atZone(ZoneId.systemDefault()).toLocalDate();
    val whenHandedOver = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    val response = client.sendRequest(PatientRequests.getMedicationDispense(whenHandedOver));
    val medDispenses = response.getResourcesListOptional().orElse(Collections.emptyList());

    return medDispenses.stream()
        .anyMatch(m -> m.getPrescriptionId().equals(prescriptionId.getValue()));
  }
}
