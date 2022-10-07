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

package de.gematik.test.erezept.app.steps;

import static java.text.MessageFormat.format;
import static net.serenitybdd.screenplay.GivenWhenThen.*;
import static org.junit.Assert.assertTrue;

import de.gematik.test.erezept.app.abilities.HandleAppAuthentication;
import de.gematik.test.erezept.app.cfg.AppiumDriverFactory;
import de.gematik.test.erezept.app.questions.HasReceivedPrescription;
import de.gematik.test.erezept.app.task.RefreshPrescriptions;
import de.gematik.test.erezept.app.task.SetUpDevice;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;

public class AppUserSteps {

  @Before
  public void setUp() {
    OnStage.setTheStage(new OnlineCast());
  }

  @After
  public void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Angenommen(
      "^(?:der|die) (GKV|PKV|BG|SEL|SOZ|GPV|PPV|BEI) Versicherte (.+) hat die E-Rezept App auf (?:seinem|ihrem) Smartphone eingerichtet$")
  public void initPatient(String insuranceType, String userName) {
    val useTheAppiumDriver = AppiumDriverFactory.forUser(userName);

    // assemble the screenplay
    val theAppUser = OnStage.theActorCalled(userName);
    theAppUser.describedAs(format("Eine {0} App-Nutzer des E-Rezept", insuranceType));
    givenThat(theAppUser).can(useTheAppiumDriver);
    //
    // givenThat(theAppUser).can(HandleAppAuthentication.withGivenPassword("easy_password_123456"));
    givenThat(theAppUser).can(HandleAppAuthentication.withStrongPassword());
    givenThat(theAppUser).can(ManageDataMatrixCodes.sheGetsPrescribed());
    givenThat(theAppUser).can(ReceiveDispensedDrugs.forHimself());

    // walk through onboarding
    givenThat(theAppUser).attemptsTo(SetUpDevice.withDefaultValues());

    // set the correct Versicherungsart after set up
    SafeAbility.getAbility(theAppUser, ProvidePatientBaseData.class)
        .setVersicherungsArt(VersicherungsArtDeBasis.fromCode(insuranceType));
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
}
