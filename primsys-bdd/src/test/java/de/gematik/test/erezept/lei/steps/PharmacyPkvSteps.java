/*
 * Copyright 2023 gematik GmbH
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

import static net.serenitybdd.screenplay.GivenWhenThen.then;
import static net.serenitybdd.screenplay.GivenWhenThen.when;

import de.gematik.test.erezept.screenplay.questions.HasChargeItemBundle;
import de.gematik.test.erezept.screenplay.questions.ResponseOfGetChargeItemBundle;
import de.gematik.test.erezept.screenplay.questions.ResponseOfPostChargeItem;
import de.gematik.test.erezept.screenplay.questions.ResponseOfPutChargeItem;
import de.gematik.test.erezept.screenplay.task.CheckTheReturnCode;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.ensure.Ensure;

public class PharmacyPkvSteps {

  /**
   * In diesem Step wird für das letzte dispensierte Rezept des Versicherten ein PKV-Abgabedatensatz
   * erstellt. Der PKV-Abgabendatensatz wird mit der SMC-B der Apotheke signiert und per POST
   * /chargeItem beim Fachdienst hinterlegt.
   *
   * @param pharmName ist der Name der Apotheke, die den PKV-Abrechnungsinformationen erstellen soll
   * @param order gibt an, ob für das letzte oder das erste dispensierte E-Rezept der
   *     Abrechnungsdatensatz erstellt werden soll
   */
  @Wenn(
      "^die Apotheke (.+) für das (letzte|erste) dispensierte E-Rezept die"
          + " PKV-Abrechnungsinformationen bereitstellt$")
  public void whenPharmacySignsWithSmcbAndPostsChargeItem(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfPostChargeItem.fromDispensed(order).and().signedByPharmacy())
                .isEqualTo(201));
  }

  @Dann(
      "^kann die Apotheke (.+) für das (letzte|erste) dispensierte E-Rezept keine"
          + " PKV-Abrechnungsinformationen bereitstellen, weil keine Einwilligung vorliegt$")
  public void thenCannotPostChargeItem403(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfPostChargeItem.fromDispensed(order).and().signedByPharmacy())
                .isEqualTo(403));
  }

  @Dann(
      "^kann die Apotheke (.+) für das (letzte|erste) dispensierte E-Rezept keine"
          + " PKV-Abrechnungsinformationen bereitstellen, weil es kein PKV-Rezept ist$")
  public void thenCannotPostChargeItem400(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfPostChargeItem.fromDispensed(order).and().signedByPharmacy())
                .isEqualTo(400));
  }

  @Dann(
      "^kann die Apotheke (.+) für das (letzte|erste) dispensierte E-Rezept keine"
          + " PKV-Abrechnungsinformationen bereitstellen, weil der Task nicht mehr existiert$")
  public void thenCannotPostChargeItemFromDispensed409(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);

    when(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfPostChargeItem.fromDispensed(order).and().signedByPharmacy())
                .isEqualTo(409));
  }

  @Dann(
      "^kann die Apotheke (.+) für das (letzte|erste) akzeptierte E-Rezept (?:noch) keine"
          + " PKV-Abrechnungsinformationen bereitstellen, weil der Task noch nicht quittiert ist$")
  public void thenCannotPostChargeItemForAccepted409(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);

    when(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfPostChargeItem.fromAccepted(order).and().signedByPharmacy())
                .isEqualTo(409));
  }

  @Dann(
      "^kann die Apotheke (.+) den (letzten|ersten) autorisierten PKV-Abgabedatensatz für das"
          + " dispensierte E-Rezept (?:erstmalig ändern|ändern)$")
  @Und(
      "^die Apotheke (.+) den (letzten|ersten) autorisierten PKV-Abgabedatensatz für das"
          + " dispensierte E-Rezept (?:erstmalig ändert|ändert)$")
  public void thenChargeItemCanBeChanged(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfPutChargeItem.fromDispensed(order).and().signedByPharmacy())
                .isEqualTo(200));
  }

  @Dann(
      "^kann die Apotheke (.+) den (letzten|ersten) autorisierten PKV-Abgabedatensatz für das"
          + " dispensierte E-Rezept nicht (?:erneut ändern|ändern), (?:weil sie kein Recht dazu"
          + " hat|weil der Datensatz bereits geändert wurde)$")
  @Und(
      "^die Apotheke (.+) kann den (letzten|ersten) autorisierten PKV-Abgabedatensatz für das"
          + " dispensierte E-Rezept nicht (?:erneut ändern|ändern), (?:weil sie kein Recht dazu"
          + " hat|weil der Datensatz bereits geändert wurde)$")
  public void andThenCannotChangeChargeItem403(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfPutChargeItem.fromDispensed(order).and().signedByPharmacy())
                .isInBetween(403, 404)); // see B_FD-499
  }

  @Dann(
      "^kann die Apotheke (.+) die (letzten|ersten) berechtigten PKV-Abrechnungsinformationen vom"
          + " Fachdienst abrufen$")
  @Wenn(
      "^die Apotheke (.+) die (letzten|ersten) berechtigten PKV-Abrechnungsinformationen vom"
          + " Fachdienst abruft$")
  public void andWhenThenPharmacyGetsChargeItemBundle(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(Ensure.that(HasChargeItemBundle.forPrescription(order).asPharmacy()).isTrue());
  }

  @Dann(
      "^kann die Apotheke (.+) für das (letzte|erste) dispensierte E-Rezept den"
          + " PKV-Abrechnungsinformationen nicht mit einem falschen AccessCode vom Fachdienst"
          + " abrufen$")
  public void thenPharmacyCannotGetChargeItemWithWrongAccessCode(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfGetChargeItemBundle.forPrescription(order)
                        .withRandomAccessCode()
                        .asPharmacy())
                .isEqualTo(403));
  }

  @Dann(
      "^kann die Apotheke (.+) für das (letzte|erste) dispensierte E-Rezept den"
          + " PKV-Abrechnungsinformationen nicht mit dem falschen AccessCode (.+) vom Fachdienst"
          + " abrufen$")
  public void thenPharmacyCannotGetChargeItemWithWrongAccessCode(
      String pharmName, String order, String accessCode) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfGetChargeItemBundle.forPrescription(order)
                        .withAccessCode(accessCode)
                        .asPharmacy())
                .isEqualTo(403));
  }

  @Dann(
      "^kann die Apotheke (.+) für das (letzte|erste) dispensierte E-Rezept keine"
          + " PKV-Abrechnungsinformationen mit dem falschen Secret (.+) bereitstellen$")
  public void thenCannotPostChargeItemWithWrongSecret(
      String pharmName, String order, String wrongSecret) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfPostChargeItem.fromDispensed(order)
                        .withCustomSecret(wrongSecret)
                        .and()
                        .signedByPharmacy())
                .isEqualTo(403));
  }
}
