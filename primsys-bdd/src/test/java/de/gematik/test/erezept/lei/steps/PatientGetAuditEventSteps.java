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

package de.gematik.test.erezept.lei.steps;

import static net.serenitybdd.screenplay.GivenWhenThen.and;

import de.gematik.test.erezept.screenplay.task.CheckDispenseAuditEvent;
import io.cucumber.java.de.Und;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

/** Testschritte zum Abruf der AuditEvents als Patient */
public class PatientGetAuditEventSteps {

  @Und(
      "^(?:der|die) Versicherte (.+) kann im Protokoll für ihr (erstes|letztes) E-Rezept einsehen,"
          + " dass es von der Apotheke (.+) abgegeben wurde$")
  public void patientCheckDispensationInAuditEvents(
      String patientName, String order, String pharmacyName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmacyName);
    and(thePatient)
        .attemptsTo(CheckDispenseAuditEvent.forPrescription(order).dispensedBy(thePharmacy));
  }
}
