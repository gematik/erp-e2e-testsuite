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

import static net.serenitybdd.screenplay.GivenWhenThen.when;

import de.gematik.test.erezept.screenplay.questions.ResponseOfPostChargeItem;
import de.gematik.test.erezept.screenplay.task.CheckTheReturnCode;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

public class ApothecarySteps {

  /**
   * In diesem Step wird für das letzte dispensierte Rezept des Versicherten ein PKV-Abgabedatensatz
   * erstellt. Der PKV-Abgabendatensatz wird mit dem HBA des agierenden Apothekers signiert und per
   * POST /chargeItem beim Fachdienst hinterlegt.
   *
   * @param apothecaryName ist der Name des Apothekers der den PKV-Abrechsnungsdatensatz mit seinem
   *     HBA signiert
   * @param pharmName ist der Name der Apotheke, die den PKV-Abrechnungsdatensatz erstellen soll
   * @param order gibt an, ob für das letzte oder das erste dispensierte E-Rezept der
   *     Abrechnungsdatensatz erstellt werden soll
   */
  @Wenn(
      "^(?:der Apotheker|die Apothekerin) (.+) als Angestellte(?:r)? der Apotheke (.+) für das (letzte|erste) dispensierte E-Rezept einen PKV-Abrechnungsdatensatz erstellt$")
  public void whenPharmacySignsWithHbaAndPostsChargeItem(
      String apothecaryName, String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val theApothecary = OnStage.theActorCalled(apothecaryName);
    when(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfPostChargeItem.fromStack(order).signedByApothecary(theApothecary))
                .isEqualTo(201));
  }
}
