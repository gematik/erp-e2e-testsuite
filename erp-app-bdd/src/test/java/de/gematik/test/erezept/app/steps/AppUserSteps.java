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

package de.gematik.test.erezept.app.steps;

import static java.text.MessageFormat.*;
import static net.serenitybdd.screenplay.GivenWhenThen.*;
import static org.junit.Assert.*;

import de.gematik.test.erezept.app.abilities.*;
import de.gematik.test.erezept.app.cfg.*;
import de.gematik.test.erezept.app.questions.*;
import de.gematik.test.erezept.app.task.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.smartcard.*;
import io.cucumber.java.*;
import io.cucumber.java.de.*;
import lombok.*;
import net.serenitybdd.screenplay.actors.*;

public class AppUserSteps {

  private SmartcardArchive smartcards;
  private ErpAppConfiguration config;

  @Before
  public void setUp() {
    smartcards = SmartcardFactory.getArchive();
    config = ErpAppConfiguration.getInstance();
    OnStage.setTheStage(new OnlineCast());
  }

  @After
  public void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Angenommen(
      "^(?:der|die) (GKV|PKV|BG|SEL|SOZ|GPV|PPV|BEI) Versicherte (.+) hat die E-Rezept App auf (?:seinem|ihrem) Smartphone eingerichtet$")
  public void initPatient(String insuranceType, String userName) {
    val useTheAppiumDriver = AppiumDriverFactory.forUser(userName, config);

    // assemble the screenplay
    val theAppUser = OnStage.theActorCalled(userName);
    theAppUser.describedAs(format("Eine {0} App-Nutzer des E-Rezept", insuranceType));
    givenThat(theAppUser).can(useTheAppiumDriver);
    //
    // givenThat(theAppUser).can(HandleAppAuthentication.withGivenPassword("easy_password_123456"));
    givenThat(theAppUser).can(HandleAppAuthentication.withStrongPassword());
    givenThat(theAppUser).can(ManageDataMatrixCodes.sheGetsPrescribed());
    givenThat(theAppUser).can(ReceiveDispensedDrugs.forHimself());

    if (config.getAppConfigurationForUser(userName).isUseVirtualeGK()) {
      val egk =
          smartcards.getEgkByICCSN("80276883110000113311"); // TODO: use a default eGK for now!
      givenThat(theAppUser).can(ProvideEGK.sheOwns(egk));
    }

    // walk through onboarding
    givenThat(theAppUser).attemptsTo(SetUpDevice.withInsuranceType(insuranceType));
  }

  @Angenommen(
      "^(?:der|die) (GKV|PKV|BG|SEL|SOZ|GPV|PPV|BEI) Versicherte (.+) überspringt das Onboarding")
  public void initPatientWithoutOnboarding(String insuranceType, String userName) {
    val useTheAppiumDriver = AppiumDriverFactory.forUser(userName, config);

    // assemble the screenplay
    val theAppUser = OnStage.theActorCalled(userName);
    theAppUser.describedAs(format("Eine {0} App-Nutzer des E-Rezept", insuranceType));
    givenThat(theAppUser).can(useTheAppiumDriver);
    //
    //    givenThat(theAppUser).can(HandleAppAuthentication.withStrongPassword());
    //    givenThat(theAppUser).can(ManageDataMatrixCodes.sheGetsPrescribed());
    //    givenThat(theAppUser).can(ReceiveDispensedDrugs.forHimself());

    if (config.getAppConfigurationForUser(userName).isUseVirtualeGK()) {
      val egk =
          smartcards.getEgkByICCSN("80276883110000113311"); // TODO: use a default eGK for now!
      givenThat(theAppUser).can(ProvideEGK.sheOwns(egk));
    }
    givenThat(theAppUser).attemptsTo(SkipOnboarding.directly());
  }

  @Wenn("^(?:der|die) Versicherte (.+) (?:seine|ihre) E-Rezepte abruft")
  public void whenRefreshPrescriptions(String userName) {
    val theAppUser = OnStage.theActorCalled(userName);
    when(theAppUser).attemptsTo(RefreshPrescriptions.byTap());
  }

  @Wenn("^(?:der|die) Versicherte (?:seine|ihre) E-Rezepte abruft")
  public void whenRefreshPrescriptions() {
    val theAppUser = OnStage.theActorInTheSpotlight();
    when(theAppUser).attemptsTo(RefreshPrescriptions.byTap());
  }

  @Dann("^hat (?:der|die) Versicherte (.+) das letzte E-Rezept elektronisch erhalten")
  public void thenReceivedPrescription(String userName) {
    val theAppUser = OnStage.theActorCalled(userName);
    assertTrue(
        "E-Rezept elektronisch nicht erhalten",
        then(theAppUser).asksFor(HasReceivedPrescription.withSomeStrategy()));
  }

  @Dann("^hat (?:der|die) Versicherte das letzte E-Rezept elektronisch erhalten")
  public void thenReceivedPrescription() {
    val theAppUser = OnStage.theActorInTheSpotlight();
    assertTrue(
        "E-Rezept elektronisch nicht erhalten",
        then(theAppUser).asksFor(HasReceivedPrescription.withSomeStrategy()));
  }

  @Dann("sieht der User den Mainscreen")
  public void thenUserCanSeeTheMainscreen() {
    val theAppUser = OnStage.theActorInTheSpotlight();
    assertTrue(
        "Onboarding wurde erfolgreich durchlaufen und wir befinden uns auf dem Mainscreen der App",
        then(theAppUser).asksFor(IsElementAvailable.withName(null))); // TODO
  }
}
