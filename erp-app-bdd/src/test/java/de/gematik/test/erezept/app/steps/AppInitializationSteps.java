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

package de.gematik.test.erezept.app.steps;

import static java.text.MessageFormat.*;
import static net.serenitybdd.screenplay.GivenWhenThen.*;

import de.gematik.test.erezept.PrimSysBddFactory;
import de.gematik.test.erezept.app.abilities.*;
import de.gematik.test.erezept.app.cfg.*;
import de.gematik.test.erezept.app.mobile.Environment;
import de.gematik.test.erezept.app.task.*;
import de.gematik.test.erezept.config.ConfigurationReader;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.smartcard.*;
import io.cucumber.java.*;
import io.cucumber.java.de.*;
import lombok.*;
import net.serenitybdd.screenplay.actors.*;

public class AppInitializationSteps {

  private SmartcardArchive smartcards;
  private ErpAppConfiguration config;

  private PrimSysBddFactory primsysConfig;
  private String scenarioName;

  @Before
  public void setUp(Scenario scenario) {
    scenarioName = scenario.getName();
    smartcards = SmartcardFactory.getArchive();
    config = ConfigurationReader.forAppConfiguration().wrappedBy(ErpAppConfiguration::fromDto);
    primsysConfig =
        ConfigurationReader.forPrimSysConfiguration()
            .wrappedBy(dto -> PrimSysBddFactory.fromDto(dto, smartcards));
    OnStage.setTheStage(Cast.ofStandardActors());
  }

  @After
  public void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Angenommen(
      "^(?:der|die) (GKV|PKV|BG|SEL|SOZ|GPV|PPV|BEI) Versicherte (.+) hat die E-Rezept App auf (?:seinem|ihrem) Smartphone eingerichtet$")
  public void initPatient(String insuranceType, String userName) {
    val useTheAppiumDriver = AppiumDriverFactory.forUser(scenarioName, userName, config);

    // assemble the screenplay
    val theAppUser = OnStage.theActorCalled(userName);
    theAppUser.describedAs(format("Eine {0} App-Nutzer des E-Rezept", insuranceType));
    givenThat(theAppUser).can(UseAppUserConfiguration.forUser(userName, config));
    givenThat(theAppUser).can(useTheAppiumDriver);

    givenThat(theAppUser).can(HandleAppAuthentication.withStrongPassword());
    givenThat(theAppUser).can(ManageDataMatrixCodes.sheGetsPrescribed());
    givenThat(theAppUser).can(ReceiveDispensedDrugs.forHimself());

    // walk through onboarding
    givenThat(theAppUser)
        .attemptsTo(
            SetUpDevice.forEnvironment(primsysConfig.getActiveEnvironment())
                .withInsuranceType(insuranceType)
                .byMappingVirtualEgkFrom(smartcards));
  }

  @Angenommen(
      "^(?:der|die) (GKV|PKV|BG|SEL|SOZ|GPV|PPV|BEI) Versicherte (.+) legt sich ein Profil in der E-Rezept App von (.+) an$")
  public void initPatientForExistingApp(
      String insuranceType, String userName, String deviceOwnerUserName) {
    val deviceOwner = OnStage.theActorCalled(deviceOwnerUserName);
    val theAppUser = OnStage.theActorCalled(userName);
    theAppUser.describedAs(
        format(
            "Eine {0} App-Nutzer der E-Rezept auf dem Gerät von {1} mitnutzt",
            insuranceType, deviceOwnerUserName));
    givenThat(theAppUser).can(UseAppUserConfiguration.forUser(userName, config));
    givenThat(theAppUser).can(ManageDataMatrixCodes.sheGetsPrescribed());
    givenThat(theAppUser).can(ReceiveDispensedDrugs.forHimself());

    givenThat(theAppUser)
        .attemptsTo(
            UseInstalledApp.ownedBy(deviceOwner)
                .forEnvironment(primsysConfig.getActiveEnvironment())
                .withInsuranceType(insuranceType)
                .byMappingVirtualEgkFrom(smartcards));
  }

  @Angenommen(
      "^(?:der|die) (GKV|PKV|BG|SEL|SOZ|GPV|PPV|BEI) Versicherte (.+) überspringt das Onboarding")
  public void initPatientWithoutOnboarding(String insuranceType, String userName) {
    val useTheAppiumDriver = AppiumDriverFactory.forUser(scenarioName, userName, config);
    val userConfiguration = UseAppUserConfiguration.forUser(userName, config);

    // assemble the screenplay
    val theAppUser = OnStage.theActorCalled(userName);
    theAppUser.describedAs(format("Eine {0} App-Nutzer des E-Rezept", insuranceType));
    givenThat(theAppUser).can(userConfiguration);
    givenThat(theAppUser).can(useTheAppiumDriver);

    if (userConfiguration.useVirtualEgk()) {
      val egk = smartcards.getEgkByICCSN(userConfiguration.getEgkIccsn());
      givenThat(theAppUser).can(ProvideEGK.sheOwns(egk));
    }

    givenThat(theAppUser).attemptsTo(SkipOnboarding.directly());
  }

  @Angenommen(
      "^(?:der|die) (GKV|PKV|BG|SEL|SOZ|GPV|PPV|BEI) Versicherte (.+) öffnet das Onboarding$")
  public void initPatientOpenOnboarding(String insuranceType, String userName) {
    val theAppUser = OnStage.theActorCalled(userName);
    val useTheApp = AppiumDriverFactory.forUser(scenarioName, userName, config);

    theAppUser.describedAs(format("Eine {0} App-Nutzer des E-Rezept", insuranceType));
    givenThat(theAppUser).can(UseAppUserConfiguration.forUser(userName, config));
    givenThat(theAppUser).can(useTheApp);

    givenThat(theAppUser).can(HandleAppAuthentication.withStrongPassword());
    givenThat(theAppUser).can(ManageDataMatrixCodes.sheGetsPrescribed());
  }

  @Dann("^kann (?:der|die) Versicherte (.+) das Onboarding erfolgreich durchlaufen$")
  @Und("^(?:der|die) Versicherte (.+) kann das Onboarding erfolgreich durchlaufen$")
  @Wenn("^(?:der|die) Versicherte (.+) das Onboarding erfolgreich durchläuft$")
  public void userCanFinishTheOnboardingSuccessfully(String userName) {
    val theAppUser = OnStage.theActorCalled(userName);
    when(theAppUser)
        .attemptsTo(NavigateThroughOnboarding.byFinishingTheEntireOnboardingSuccessfully());

    when(theAppUser)
        .attemptsTo(
            ChangeTheEnvironment.bySwitchInTheDebugMenuTo(
                Environment.fromString(primsysConfig.getActiveEnvironment().getName())));

    val userConfiguration = SafeAbility.getAbility(theAppUser, UseAppUserConfiguration.class);
    if (userConfiguration.useVirtualEgk()) {
      val egk = smartcards.getEgkByICCSN(userConfiguration.getEgkIccsn());
      when(theAppUser).attemptsTo(SetVirtualEgk.withEgk(egk));
    }
  }

  @Und(
      "^(?:der|die) (GKV|PKV|BG|SEL|SOZ|GPV|PPV|BEI) Versicherte (.+) hat die Cardwall erfolgreich durchlaufen")
  public void userCanFinishCardwallSuccessfully(String insuranceType, String user) {
    val theAppUser = OnStage.theActorCalled(user);
    givenThat(theAppUser).can(ReceiveDispensedDrugs.forHimself());

    when(theAppUser)
        .attemptsTo(
            NavigateThroughCardwall.forEnvironment(primsysConfig.getActiveEnvironment())
                .byMappingVirtualEgkFrom(smartcards));

    val egk = SafeAbility.getAbility(theAppUser, ProvideEGK.class);
    givenThat(theAppUser)
        .can(ProvidePatientBaseData.forPatient(egk.getKvnr(), theAppUser.getName(), insuranceType));
  }
}
