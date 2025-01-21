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

import static net.serenitybdd.screenplay.GivenWhenThen.then;

import de.gematik.test.erezept.client.exceptions.UnexpectedResponseResourceError;
import de.gematik.test.erezept.screenplay.task.ClosePrescription;
import de.gematik.test.erezept.screenplay.task.Negate;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.de.Dann;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

public class PharmacyCloseSteps {

  @Dann(
      "^kann die Apotheke (.+) für das (erste|letzte) dispensierte E-Rezept den Workflow"
          + " abschliessen$")
  public void thenThePharmacyCanCloseAndStoreAcceptation(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);

    then(thePharmacy)
        .attemptsTo(
            ClosePrescription.fromStack(order)
                .withoutMedicationDispense()
                .andStoreAcceptInformationForLaterStep());
  }

  @Dann(
      "^kann die Apotheke (.+) das (erste|letzte) E-Rezept für (.+) beim Abschluss des Workflows"
          + " die Dispensierinformationen ändern in:$")
  public void thenThePharmacyCanChangeDispensInformation(
      String pharmName, String order, String patientName, DataTable medications) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);

    then(thePharmacy)
        .attemptsTo(
            ClosePrescription.fromStack(order)
                .withAlternativeMedications(medications)
                .forThePatient(thePatient));
  }

  @Dann(
      "^kann die Apotheke (.+) für das (erste|letzte) dispensierte E-Rezept den Workflow nicht"
          + " abschliessen$")
  public void thenThePharmacyCantCloseWithoutDispense(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            Negate.the(ClosePrescription.fromStack(order).withoutMedicationDispense())
                .with(UnexpectedResponseResourceError.class));
  }
}
