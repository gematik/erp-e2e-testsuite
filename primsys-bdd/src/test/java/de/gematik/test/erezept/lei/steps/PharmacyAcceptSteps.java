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

import de.gematik.test.erezept.client.exceptions.UnexpectedResponseResourceError;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.screenplay.questions.*;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.task.*;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

/**
 * Testschritte die aus der Perspektive einer Apotheke und Apotheker bzw. Apothekerin ausgeführt
 * werden
 */
public class PharmacyAcceptSteps {

  @Wenn(
      "^die Apotheke (.+) das (letzte|erste) (?:zugewiesene|abgerufene) E-Rezept beim Fachdienst akzeptiert$")
  public void whenAcceptPrescription(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy).attemptsTo(AcceptPrescription.fromStack(order));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) zugewiesene E-Rezept beim Fachdienst akzeptieren$")
  public void thenAcceptPrescription(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy).attemptsTo(AcceptPrescription.fromStack(order));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) zugewiesene E-Rezept nicht beim Fachdienst akzeptieren$")
  @Und(
      "^die Apotheke (.+) kann das (letzte|erste) zugewiesen E-Rezept nicht beim Fachdienst akzeptieren$")
  public void thenForbiddenToAcceptPrescription(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            Negate.the(AcceptPrescription.fromStack(order))
                .with(UnexpectedResponseResourceError.class));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) zugewiesene E-Rezept nicht beim Fachdienst akzeptieren, weil es nicht mehr existiert$")
  public void thenForbiddenToAcceptPrescriptionWith410(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAcceptOperation.fromStack(order)).isEqualTo(410));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) zugewiesene E-Rezept nicht beim Fachdienst akzeptieren, weil es noch nicht gültig ist$")
  public void thenForbiddenToAcceptPrescriptionWith403(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAcceptOperation.fromStack(order)).isEqualTo(403));
  }

  /**
   * @param order
   */
  @Dann(
      "^kann die Apotheke das (letzte|erste) zugewiesene E-Rezept nicht beim Fachdienst akzeptieren, weil es nicht mehr existiert$")
  public void thenForbiddenToAcceptPrescriptionWith410(String order) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAcceptOperation.fromStack(order)).isEqualTo(410));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) zugewiesene E-Rezept nicht beim Fachdienst akzeptieren, weil es einen Konflikt gibt$")
  public void thenForbiddenToAcceptPrescriptionWith409(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAcceptOperation.fromStack(order)).isEqualTo(409));
  }

  @Dann(
      "^kann die Apotheke das (letzte|erste) zugewiesene E-Rezept nicht beim Fachdienst akzeptieren, weil es einen Konflikt gibt$")
  public void thenForbiddenToAcceptPrescriptionWith409(String order) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAcceptOperation.fromStack(order)).isEqualTo(409));
  }

  @Dann("^kann die Apotheke (.+) (?:noch)? keine E-Rezepte akzeptieren$")
  @Und("^die Apotheke (.+) kann (?:noch)? keine E-Rezepte akzeptieren$")
  public void thenCannotAcceptPrescription(String pharmName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            Negate.the(AcceptPrescription.fromStack(DequeStrategy.FIFO))
                .with(MissingPreconditionError.class));
  }
}
