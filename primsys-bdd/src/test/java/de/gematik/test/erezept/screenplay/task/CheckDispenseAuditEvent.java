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

package de.gematik.test.erezept.screenplay.task;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CheckDispenseAuditEvent implements Task {

  private final DequeStrategy deque;
  private final Actor pharmacy;

  public static Builder forPrescription(DequeStrategy order) {
    return new Builder(order);
  }

  public static Builder forPrescription(String order) {
    return forPrescription(DequeStrategy.fromString(order));
  }

  @Override
  public <T extends Actor> void performAs(final T actor) {
    val dispensed = SafeAbility.getAbility(actor, ReceiveDispensedDrugs.class);

    val dispensationInformation = deque.chooseFrom(dispensed.getDispensedDrugsList());
    val prescriptionId = dispensationInformation.prescriptionId();
    val dispTime = dispensationInformation.dispenseDate();
    val auditEventResponse =
        actor.asksFor(
            GetAuditEventBundle.forPatient(actor).forPrescription(prescriptionId).build());
    val auditEventList = auditEventResponse.getExpectedResource().getAuditEvents();
    val specificAuditEvents =
        auditEventList.stream()
            .filter(auditEvent -> auditEvent.getFirstText().contains("dispensed a prescription"))
            .filter(auditEvent -> auditEvent.getFirstText().contains(prescriptionId.getValue()))
            .toList();

    assertFalse(
        specificAuditEvents.isEmpty(),
        format(
            "No AuditEvent found that logs the dispense action for prescription {0}",
            prescriptionId.getValue()));

    // we don't care about the milliseconds in the comparison
    specificAuditEvents.forEach(
        auditEvent -> {
          val timeDiff =
              dispTime.getEpochSecond() - auditEvent.getRecorded().toInstant().getEpochSecond();
          val maxDiff = 60 * 2;
          assertTrue(
              timeDiff <= maxDiff,
              format("Time difference between Dispense and AuditEvent is too big: {0}s", timeDiff));
        });

    specificAuditEvents.forEach(
        auditEvent ->
            assertEquals(
                auditEvent.getAgentId(),
                SafeAbility.getAbility(pharmacy, UseSMCB.class).getTelematikID(),
                format(
                    "AuditEvent does not contain the pharmacies TelematikId: {0}",
                    SafeAbility.getAbility(pharmacy, UseSMCB.class).getTelematikID())));
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {

    private final DequeStrategy deque;

    public CheckDispenseAuditEvent dispensedBy(Actor pharmacy) {
      return new CheckDispenseAuditEvent(deque, pharmacy);
    }
  }
}
