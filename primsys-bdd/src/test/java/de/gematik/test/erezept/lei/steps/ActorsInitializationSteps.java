/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.lei.steps;

import static net.serenitybdd.screenplay.GivenWhenThen.when;

import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.common.PrimSysBddFactory;
import de.gematik.test.erezept.common.apimeasure.DumpingStopwatch;
import de.gematik.test.erezept.common.screenplay.task.ConnectSubscriptionService;
import de.gematik.test.erezept.config.ConfigurationReader;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Wenn;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;

@Slf4j
public class ActorsInitializationSteps {

  private static SmartcardArchive smartcards;
  static PrimSysBddFactory config;
  private static DumpingStopwatch stopwatch;

  @BeforeAll
  public static void init() {
    smartcards = SmartcardArchive.fromResources();
    config =
        ConfigurationReader.forPrimSysConfiguration()
            .wrappedBy(dto -> PrimSysBddFactory.fromDto(dto, smartcards));
    stopwatch = new DumpingStopwatch("e2e_testsuite");
  }

  @Before
  public void setUp() {
    OnStage.setTheStage(Cast.ofStandardActors());
  }

  @After
  public void teardown() {
    OnStage.drawTheCurtain();
  }

  @AfterAll
  public static void teardownStopwatch() {
    stopwatch.close();
  }

  /**
   * Initialisiere einen Apotheker mit einer SMC-B <br>
   * <b>Notiz:</b> der Name im ersten Parameter entspricht nicht zwangsläufig dem echten Namen, der
   * auf der Smartcard hinterlegt ist. Dieser Name wird lediglich für das Screenplay-Pattern
   * benötigt
   *
   * @param pharmName der Name der Apotheke, der innerhalb des Szenarios verwendet wird, um diesen
   *     zu identifizieren
   */
  @Angenommen("^die Apotheke (.+) hat Zugriff auf ihre SMC-B$")
  public void initPharmacy(String pharmName) {
    log.trace("Initialize Pharmacy {}", pharmName);
    val theActor = OnStage.theActorCalled(pharmName);
    config.equipAsPharmacy(theActor);
  }

  @Angenommen("^(?:der Apotheker|die Apothekerin) (.+) hat Zugriff auf (?:seinen|ihren) HBA$")
  public void initApothecary(String apothecaryName) {
    log.trace("Initialize Apothecary {}", apothecaryName);
    val theActor = OnStage.theActorCalled(apothecaryName);
    config.equipAsApothecary(theActor);
  }

  /**
   * Initialisiere einen Arzt mit den beiden Smartcards SMC-B und HBA
   *
   * <p><b>Notiz:</b> Der Name im ersten Parameter entspricht nicht dem echten Namen der auf HBA
   * hinterlegt ist. Dieser Name wird lediglich für das Screenplay-Pattern benötigt
   *
   * @param docName der Name des Arztes, der innerhalb des Szenarios verwendet wird, um diesen zu
   *     identifizieren
   */
  @Angenommen(
      "^(?:der|die) (?:Arzt|Ärztin|Psychotherapeut|Psychotherapeutin|Psychologischer"
          + " Psychotherapeut|Kinderpsychotherapeut) (.+) hat Zugriff auf (?:seinen|ihren) HBA"
          + " und auf die SMC-B der Praxis$")
  public void initDoctor(String docName) {
    log.trace("Initialize Doctor {}", docName);
    val theActor = OnStage.theActorCalled(docName);
    config.equipAsDoctor(theActor);
  }

  @Angenommen("^der Kostenträger (.+) hat Zugriff auf seine SMC-B KTR$")
  public void initKtr(String insuranceName) {
    log.trace("Initialize HealthInsurance {}", insuranceName);
    val theActor = OnStage.theActorCalled(insuranceName);
    config.equipAsKtr(theActor);
  }

  @Wenn("^(?:die Apotheke|der Kostenträger) (.+) sich mit dem Subscription Service verbindet$")
  public void whenConnectedToSubscriptionService(String pharmName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy)
        .attemptsTo(
            ConnectSubscriptionService.connect(
                config.getActiveEnvironment().getTi().getSubscriptionServiceUrl()));
  }
}
