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

package de.gematik.test.erezept.remotefdv.steps;

import static java.text.MessageFormat.format;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.smartcards.DummyEgk;
import de.gematik.bbriccs.smartcards.cfg.SmartcardConfigDto;
import de.gematik.test.erezept.config.ConfigurationReader;
import de.gematik.test.erezept.remotefdv.cfg.ErpRemoteFdVConfiguration;
import de.gematik.test.erezept.remotefdv.cfg.RemoteFdVFactory;
import de.gematik.test.erezept.remotefdv.task.SetUpRemoteFdV;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import io.cucumber.java.Before;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;

public class FdVInitializationSteps {
  private ErpRemoteFdVConfiguration config;

  @Before
  public void setUp() {
    config =
        ConfigurationReader.forRemoteFdVConfiguration()
            .wrappedBy(ErpRemoteFdVConfiguration::fromDto);

    OnStage.setTheStage(Cast.ofStandardActors());
  }

  @Angenommen(
      "^(?:der|die) (GKV|PKV|BG|SEL|SOZ|GPV|PPV|BEI) Versicherte (.+) hat die Remote-FdV auf"
          + " (?:seinem|ihrem) Smartphone eingerichtet$")
  @Wenn(
      "^(?:der|die) (GKV|PKV|BG|SEL|SOZ|GPV|PPV|BEI) Versicherte (.+) die Remote-FdV auf"
          + " (?:seinem|ihrem) Smartphone eingerichtet hat$")
  public void initPatient(String insuranceType, String userName) {
    val useTheRemoteFdV = RemoteFdVFactory.forUser(userName, config);

    val theAppUser = OnStage.theActorCalled(userName);
    val kvnr = config.getAppUserByName(userName).getKvnr();

    theAppUser.describedAs(format("Eine {0} App-Nutzer des E-Rezept", insuranceType));
    givenThat(theAppUser).can(useTheRemoteFdV);

    val egkConfig = new SmartcardConfigDto();
    egkConfig.setIdentifier(kvnr);
    egkConfig.setOwnerName(userName);
    givenThat(theAppUser).can(ProvideEGK.sheOwns(DummyEgk.fromConfig(egkConfig)));
    givenThat(theAppUser)
        .can(ProvidePatientBaseData.forPatient(KVNR.from(kvnr), userName, insuranceType));
    givenThat(theAppUser).can(ManageDataMatrixCodes.sheGetsPrescribed());
    givenThat(theAppUser).can(ReceiveDispensedDrugs.forHimself());

    // walk through onboarding
    givenThat(theAppUser).attemptsTo(SetUpRemoteFdV.forUser(config.getAppUserByName(userName)));
  }
}
