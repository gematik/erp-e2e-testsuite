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

package de.gematik.test.erezept.common.steps;

import static net.serenitybdd.screenplay.GivenWhenThen.when;

import de.gematik.test.erezept.common.screenplay.task.HandoverDataMatrixCode;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

public class DataMatrixCodeAssignmentSteps {
  /**
   * Dieser Schritt bildet den Vorgang in einer realen Apotheke nach. Der Versicherte mit dem Namen
   * aus {@code patientName} ruft zunächst alle seine E-Rezepte beim Fachdienst ab und wählt das
   * gewünschte E-Rezept anhand von {@code order} aus. Im Anschluss erfolgt die Zuweisung über den
   * Data Matrix Code an die Apotheke mit dem Namen {@code pharmacyName}.
   *
   * <p>Mit der Zuweisung über den DMC (Data * Matrix Code) findet keine weitere Interaktion durch
   * den Versicherten mit dem Fachdienst statt. Stattdessen wird dem Apotheker der DMC vorgezeigt.
   *
   * @param patientName ist der Name des Versicherten, der ein E-Rezept bei einer Apotheke einlösen
   *     möchte
   * @param order ist die Reihenfolge, mit der das gewünschte E-Rezept ausgewählt wird
   * @param pharmacyName ist die Apotheke, welche den DMC zugewiesen bekommt
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) (ausgestelltes|gelöschtes)"
          + " E-Rezept der Apotheke (.+) via Data Matrix Code zuweist$")
  @Wenn(
      "^(?:der|die) GKV Versicherte (.+) (?:sein|ihr) (letztes|erstes) (ausgestelltes|gelöschtes)"
          + " E-Rezept der Apotheke (.+) via Data Matrix Code zuweist$")
  @Und(
      "^(?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) (ausgestelltes|gelöschtes)"
          + " E-Rezept dem Kostenträger (.+) via Data Matrix Code zuweist$")
  public void whenAssignDataMatrixCodeFromStack(
      String patientName, String order, String dmcStack, String pharmacyName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmacyName);

    when(thePatient)
        .attemptsTo(HandoverDataMatrixCode.fromStack(dmcStack).with(order).to(thePharmacy));
  }
}
