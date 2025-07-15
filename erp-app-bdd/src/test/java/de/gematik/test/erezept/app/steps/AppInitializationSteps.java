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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.app.steps;

import static java.text.MessageFormat.format;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static net.serenitybdd.screenplay.GivenWhenThen.when;

import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.PrimSysBddFactory;
import de.gematik.test.erezept.app.abilities.HandleAppAuthentication;
import de.gematik.test.erezept.app.abilities.UseConfigurationData;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.cfg.AppiumDriverFactory;
import de.gematik.test.erezept.app.cfg.ErpAppConfiguration;
import de.gematik.test.erezept.app.mobile.Environment;
import de.gematik.test.erezept.app.task.ChangeTheEnvironment;
import de.gematik.test.erezept.app.task.NavigateThroughCardwall;
import de.gematik.test.erezept.app.task.NavigateThroughOnboarding;
import de.gematik.test.erezept.app.task.SetUpDevice;
import de.gematik.test.erezept.app.task.SetVirtualEgk;
import de.gematik.test.erezept.app.task.SkipOnboarding;
import de.gematik.test.erezept.app.task.UseInstalledApp;
import de.gematik.test.erezept.app.task.ios.NavigateThroughOnboardingOnIOS;
import de.gematik.test.erezept.config.ConfigurationReader;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.util.Optional;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;

public class AppInitializationSteps {

  private static SmartcardArchive smartcards;
  private static ErpAppConfiguration config;
  private static PrimSysBddFactory primsysConfig;

  private String scenarioName;
  /* the actor who is responsible for reporting the test result to the MDC */
  private Actor testReporter;

  @BeforeAll
  public static void init() {
    smartcards = SmartcardArchive.fromResources();
    config = ConfigurationReader.forAppConfiguration().wrappedBy(ErpAppConfiguration::fromDto);
    primsysConfig =
        ConfigurationReader.forPrimSysConfiguration()
            .wrappedBy(dto -> PrimSysBddFactory.fromDto(dto, smartcards));
  }

  @Before
  public void setUp(Scenario scenario) {
    scenarioName = scenario.getName();
    OnStage.setTheStage(Cast.ofStandardActors());
  }

  @After
  public void tearDown(Scenario scenario) {
    Optional.ofNullable(this.testReporter)
        .map(actor -> actor.abilityTo(UseTheApp.class))
        .ifPresent(driver -> driver.finish(scenario));

    OnStage.drawTheCurtain();
  }

  @Angenommen(
      "^(?:der|die) (GKV|PKV|BG|SEL|SOZ|GPV|PPV|BEI) Versicherte (.+) hat die E-Rezept App auf"
          + " (?:seinem|ihrem) Smartphone eingerichtet$")
  @Wenn(
      "^(?:der|die) (GKV|PKV|BG|SEL|SOZ|GPV|PPV|BEI) Versicherte (.+) die E-Rezept App auf"
          + " (?:seinem|ihrem) Smartphone eingerichtet hat$")
  public void initPatient(String insuranceType, String userName) {
    val useTheAppiumDriver = AppiumDriverFactory.forUser(scenarioName, userName, config);

    // assemble the screenplay
    val theAppUser = OnStage.theActorCalled(userName);

    // remember the app user for reporting the final test result to MDC
    this.testReporter = theAppUser;

    theAppUser.describedAs(format("Eine {0} App-Nutzer des E-Rezept", insuranceType));
    givenThat(theAppUser).can(UseConfigurationData.forUser(userName, config));
    givenThat(theAppUser).can(useTheAppiumDriver);

    val appPassword = config.getAppUserByName(userName).getAppPassword();
    givenThat(theAppUser).can(HandleAppAuthentication.withGivenPassword(appPassword));
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
      "^(?:der|die) Versicherte (.+) hat die E-Rezept App auf"
          + " (?:seinem|ihrem) Smartphone für die Nutzung ohne TI eingerichtet$")
  @Wenn(
      "^(?:der|die) Versicherte (.+) (?:sein|ihr) Smartphone für die Nutzung ohne TI eingerichtet"
          + " hat$")
  public void initPatientWithoutTi(String userName) {
    val useTheAppiumDriver = AppiumDriverFactory.forUser(scenarioName, userName, config);

    // assemble the screenplay
    val theAppUser = OnStage.theActorCalled(userName);
    // remember the app user for reporting the final test result to MDC
    this.testReporter = theAppUser;

    theAppUser.describedAs(format("Eine App-Nutzer des E-Rezept ohne eGK"));
    givenThat(theAppUser).can(UseConfigurationData.forUser(userName, config));
    givenThat(theAppUser).can(useTheAppiumDriver);

    givenThat(theAppUser).can(HandleAppAuthentication.withStrongPassword());

    // walk through onboarding
    givenThat(theAppUser).attemptsTo(NavigateThroughOnboardingOnIOS.entirely());
  }

  @Angenommen(
      "^(?:der|die) (GKV|PKV|BG|SEL|SOZ|GPV|PPV|BEI) Versicherte (.+) legt sich ein Profil in der"
          + " E-Rezept App von (.+) an$")
  public void initPatientForExistingApp(
      String insuranceType, String userName, String deviceOwnerUserName) {
    val deviceOwner = OnStage.theActorCalled(deviceOwnerUserName);
    val deviceName = SafeAbility.getAbility(deviceOwner, UseConfigurationData.class).getDevice();
    val theAppUser = OnStage.theActorCalled(userName);
    theAppUser.describedAs(
        format(
            "Eine {0} App-Nutzer der E-Rezept auf dem Gerät von {1} mitnutzt",
            insuranceType, deviceOwnerUserName));
    givenThat(theAppUser).can(UseConfigurationData.asCoUser(userName, deviceName, config));
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
    val userConfiguration = UseConfigurationData.forUser(userName, config);

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
    // remember the app user for reporting the final test result to MDC
    this.testReporter = theAppUser;

    theAppUser.describedAs(format("Eine {0} App-Nutzer des E-Rezept", insuranceType));
    givenThat(theAppUser).can(UseConfigurationData.forUser(userName, config));
    givenThat(theAppUser).can(useTheApp);

    givenThat(theAppUser).can(HandleAppAuthentication.withStrongPassword());
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

    val userConfiguration = SafeAbility.getAbility(theAppUser, UseConfigurationData.class);
    if (userConfiguration.useVirtualEgk()) {
      val egk = smartcards.getEgkByICCSN(userConfiguration.getEgkIccsn());
      when(theAppUser).attemptsTo(SetVirtualEgk.withEgk(egk));
    }
  }

  @Und(
      "^(?:der|die) (GKV|PKV|BG|SEL|SOZ|GPV|PPV|BEI) Versicherte (.+) hat die Cardwall erfolgreich"
          + " durchlaufen")
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
