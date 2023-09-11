/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.lei.steps;

import de.gematik.test.erezept.screenplay.questions.ResponseOfAbortOperation;
import de.gematik.test.erezept.screenplay.questions.ResponseOfAbortUnaccepted;
import de.gematik.test.erezept.screenplay.task.AbortPrescription;
import de.gematik.test.erezept.screenplay.task.CheckTheReturnCode;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

import static net.serenitybdd.screenplay.GivenWhenThen.then;
import static net.serenitybdd.screenplay.GivenWhenThen.when;

public class PharmacyDeletePrescriptionSteps {

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept nicht mit dem falschen Secret (.+) löschen$")
  @Und(
      "^die Apotheke (.+) kann das (letzte|erste) akzeptierte E-Rezept nicht mit dem falschen Secret (.+) löschen$")
  public void thenDeleteMedicationWithWrongSecret(
      String pharmName, String order, String wrongSecret) {
    val thePharmacy = OnStage.theActorCalled(pharmName);

    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfAbortOperation.asPharmacy()
                        .withInvalidSecret(wrongSecret)
                        .fromStack(order))
                .isEqualTo(403));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept nicht mit einem falschen Secret löschen$")
  @Und(
      "^die Apotheke (.+) kann das (letzte|erste) akzeptierte E-Rezept nicht mit einem falschen Secret löschen$")
  public void thenDeleteMedicationWithWrongSecret(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);

    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfAbortOperation.asPharmacy().withInvalidSecret().fromStack(order))
                .isEqualTo(403));
  }

  /**
   * TMD-1621
   *
   * @param pharmName ist der Name der Apotheke
   */
  @Wenn("^die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept löscht$")
  public void whenAbortPrescription(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy).attemptsTo(AbortPrescription.asPharmacy().fromStack(order));
  }

  @Wenn("^die Apotheke das (letzte|erste) akzeptierte E-Rezept löscht$")
  public void whenAbortPrescription(String order) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    when(thePharmacy).attemptsTo(AbortPrescription.asPharmacy().fromStack(order));
  }

  /**
   * Bevor eine Apotheke ein zugewiesenes E-Rezept nicht akzeptiert, hat diese kein zugehöriges
   * Secret für diesen Task. Ohne diesen Secret kann die Apotheke auch folglich das E-Rezept nicht
   * löschen.
   *
   * @param pharmName ist der Name der Apotheke, die versuchen soll ein zugewiesenes E-Rezept, ohne
   *     Secret zu löschen
   * @param order ist die Reihenfolge, in der das zugewiesene E-Rezept vom Accepted-Stack geholt
   *     wird
   */
  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) zugewiesene E-Rezept ohne (?:Secret|zu akzeptieren) nicht löschen$")
  public void thenPharmacyCannotAbortPrescription(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortUnaccepted.asPharmacy().fromStack(order))
                .isEqualTo(403));
  }
}
