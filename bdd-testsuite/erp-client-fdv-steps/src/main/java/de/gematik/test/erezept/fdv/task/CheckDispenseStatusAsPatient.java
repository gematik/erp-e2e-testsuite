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

package de.gematik.test.erezept.fdv.task;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import de.gematik.test.erezept.screenplay.questions.DownloadTaskById;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import org.hl7.fhir.r4.model.Task.TaskStatus;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CheckDispenseStatusAsPatient implements Task {

  private final DequeStrategy deque;

  public static CheckDispenseStatusAsPatient fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static CheckDispenseStatusAsPatient fromStack(DequeStrategy deque) {
    return new CheckDispenseStatusAsPatient(deque);
  }

  @Override
  public <T extends Actor> void performAs(final T actor) {
    val dispensed = SafeAbility.getAbility(actor, ReceiveDispensedDrugs.class);

    val dispensationInformation = deque.chooseFrom(dispensed.getDispensedDrugsList());
    val prescriptionId = dispensationInformation.prescriptionId();
    val dispTime = dispensationInformation.dispenseDate();
    val response = actor.asksFor(DownloadTaskById.withId(TaskId.from(prescriptionId)));
    log.info(
        format(
            "Actor {0} asked for Task with Id {1} and received {2}",
            actor.getName(), prescriptionId.getValue(), response));

    val lastMedDispDate =
        response
            .getTask()
            .getLastMedicationDispenseDate()
            .orElseThrow(() -> new AssertionError("Last MedicationDispensed Extension is missing"));

    // we don't care about the milliseconds in the comparison
    assertEquals(
        dispTime.truncatedTo(ChronoUnit.SECONDS),
        lastMedDispDate.truncatedTo(ChronoUnit.SECONDS),
        format(
            "Prescription {0} has an unexpected 'Last Medication Dispense Date'",
            prescriptionId.getValue()));

    assertEquals(
        TaskStatus.INPROGRESS,
        response.getTask().getStatus(),
        format("Prescription {0} has unexpected status", prescriptionId.getValue()));
  }
}
