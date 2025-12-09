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

package de.gematik.test.erezept.lei.steps;

import static net.serenitybdd.screenplay.GivenWhenThen.then;
import static net.serenitybdd.screenplay.GivenWhenThen.when;

import de.gematik.test.erezept.screenplay.questions.ResponseOfAcceptOperation;
import de.gematik.test.erezept.screenplay.task.AcceptPrescription;
import de.gematik.test.erezept.screenplay.task.CheckTheReturnCode;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

/**
 * Testschritte die aus der Perspektive einer Apotheke und Apotheker bzw. Apothekerin ausgeführt
 * werden
 */
public class PharmacyAcceptSteps {
  @Wenn(
      "^die Apotheke (.+) das (letzte|erste) (?:zugewiesene|abgerufene) E-Rezept beim Fachdienst"
          + " akzeptiert$")
  public void whenAcceptPrescription(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy).attemptsTo(AcceptPrescription.fromStack(order));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) zugewiesene E-Rezept nicht beim Fachdienst"
          + " akzeptieren, weil es nicht mehr existiert$")
  public void thenForbiddenToAcceptPrescriptionWith410(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAcceptOperation.fromStack(order)).isEqualTo(410));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) zugewiesene E-Rezept nicht beim Fachdienst"
          + " akzeptieren, weil es einen Konflikt gibt$")
  public void thenForbiddenToAcceptPrescriptionWith409(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAcceptOperation.fromStack(order)).isEqualTo(409));
  }
}
