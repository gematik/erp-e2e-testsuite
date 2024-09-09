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

import static net.serenitybdd.screenplay.GivenWhenThen.when;

import de.gematik.test.erezept.screenplay.questions.ResponseOfGetTaskById;
import de.gematik.test.erezept.screenplay.task.CheckTheReturnCode;
import de.gematik.test.erezept.screenplay.task.GetPrescriptionById;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

/**
 * Testschritte die aus der Perspektive einer Apotheke und Apotheker bzw. Apothekerin ausgeführt
 * werden
 */
public class PharmacyGetTaskSteps {

  @Wenn(
      "^die Apotheke (.+) das (letzte|erste) zugewiesene E-Rezept beim Fachdienst(?: erneut)?"
          + " abruft$")
  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) zugewiesene E-Rezept beim Fachdienst(?: erneut)?"
          + " abrufen$")
  public void getTaskById(String pharmaName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmaName);
    when(thePharmacy).attemptsTo(GetPrescriptionById.asPharmacy(order));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) zugewiesene E-Rezept nicht beim Fachdienst"
          + " abrufen, weil das E-Rezept noch nicht akzeptiert ist$")
  public void thenCanNotGetTaskById(String pharmaName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmaName);
    when(thePharmacy)
        .attemptsTo(CheckTheReturnCode.of(ResponseOfGetTaskById.asPharmacy(order)).isEqualTo(412));
  }
}
