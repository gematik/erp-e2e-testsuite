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

import de.gematik.test.erezept.screenplay.questions.ResponseOfRejectOperation;
import de.gematik.test.erezept.screenplay.task.CheckTheReturnCode;
import de.gematik.test.erezept.screenplay.task.RejectPrescription;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

public class PharmacyRejectPrescriptionSteps {
  /**
   * TMD-1619
   *
   * @param pharmName ist der Name der Apotheke
   */
  @Wenn("^die Apotheke (.+) das (letzte|erste) akzeptierte Rezept zurückweist$")
  public void whenRejectPrescription(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy).attemptsTo(RejectPrescription.fromStack(order));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept nicht mit dem falschen"
          + " Secret (.+) zurückgeben$")
  @Und(
      "^die Apotheke (.+) kann das (letzte|erste) akzeptierte E-Rezept nicht mit dem falschen"
          + " Secret (.+) zurückgeben$")
  public void thenRejectMedicationWithWrongSecret(
      String pharmName, String order, String wrongSecret) {

    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfRejectOperation.withInvalidSecret(wrongSecret).fromStack(order))
                .isEqualTo(403));
  }
}
