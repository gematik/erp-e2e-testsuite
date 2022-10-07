/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.app.questions.android;

import static java.text.MessageFormat.format;
import static org.junit.Assert.assertEquals;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.locators.LocatorStrategy;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.SneakyThrows;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import org.openqa.selenium.By;

public class HasReceivedPrescriptionOnAndroid implements Question<Boolean> {

  @Override
  @SneakyThrows
  public Boolean answeredBy(Actor actor) {
    val app = SafeAbility.getAbility(actor, UseTheApp.class);
    val dmcs = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);

    // check the precondition by counting existing DMCs for this actor
    //        if(dmcs.getDmcList().size() == 0) {
    //            throw new MissingPreconditionException("No Prescriptions via DMC available");
    //        }

    // check the number of visible prescriptions on the screen
    val numOfPrescriptions =
        app.getWebElementListLen("List of Prescriptions") - 1; // subtract Archive ComposeNode
    if (numOfPrescriptions == 0) {
      // could not find any prescriptions on the screen
      return false;
    }

    /*
    Note: this construct is pretty hacky because the "List of Prescriptions" does not have proper Identifiers
    here, we will fetch each single element by instrumenting the original XPath
    */
    val platformLocator = app.getPlatformLocator("List of Prescriptions");
    assertEquals(
        "Locating single Prescriptions works only with XPath for now",
        LocatorStrategy.XPATH,
        platformLocator.getStrategy());

    boolean foundPrescription = false;
    for (int prescriptionIdx = 1; prescriptionIdx <= numOfPrescriptions; prescriptionIdx++) {
      val xpath = format("{0}[{1}]", platformLocator.getLocatorId(), prescriptionIdx);
      val prescriptionLocator = By.xpath(xpath);

      /*
      Well, nasty hack but required as long as espresso-driver cannot wait for an element to be clickable.
      This sleep bridges the gap while the app is loading the prescriptions again: elements are available but not yet clickable
      TODO: what about using Pause extends org.openqa.selenium.interactions.Interaction
       */
      Thread.sleep(2000);
      app.tap(prescriptionLocator);
      // TODO: check the prescription details here!
      foundPrescription = false;
      app.tap("Leave Prescription Details");
    }

    return foundPrescription;
  }
}
