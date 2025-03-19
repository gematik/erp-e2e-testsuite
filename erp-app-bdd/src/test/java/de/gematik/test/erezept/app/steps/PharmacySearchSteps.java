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
 */

package de.gematik.test.erezept.app.steps;

import static net.serenitybdd.screenplay.GivenWhenThen.then;
import static net.serenitybdd.screenplay.GivenWhenThen.when;

import de.gematik.test.erezept.app.questions.ThePharmacyProvidesDeliveryOptions;
import de.gematik.test.erezept.app.task.ios.OpenPharmacyViaSearchOnIos;
import de.gematik.test.erezept.app.task.ios.WalkThroughMultiplePharmacies;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.ensure.Ensure;

public class PharmacySearchSteps {

  @Dann(
      "^kann (?:der|die) Versicherte (.+) die folgenden Apotheken mit den Belieferungsoptionen in"
          + " der Apothekensuche finden:")
  public void thenFindMultiplePharmacies(String userName, DataTable dataTable) {
    val theAppUser = OnStage.theActorCalled(userName);
    then(theAppUser).attemptsTo(WalkThroughMultiplePharmacies.givenFrom(dataTable.asMaps()));
  }

  @Wenn(
      "^(?:der|die) Versicherte (.+) die Apotheke mit dem Namen (.+) in der Apothekensuche sucht$")
  @Deprecated
  public void whenSearchPharmacy(String userName, String pharmacyName) {
    val theAppUser = OnStage.theActorCalled(userName);
    when(theAppUser).attemptsTo(OpenPharmacyViaSearchOnIos.named(pharmacyName).fromMainscreen());
  }

  @Dann(
      "^wird (?:der|dem) Versicherten die Apotheken mit den folgenden Belieferungsoptionen"
          + " angezeigt:")
  @Deprecated
  public void thenShowPharmacy(DataTable deliveryOptions) {
    val theAppUser = OnStage.theActorInTheSpotlight();
    then(theAppUser)
        .attemptsTo(
            Ensure.that(ThePharmacyProvidesDeliveryOptions.givenFrom(deliveryOptions.asMaps()))
                .isTrue());
  }
}
