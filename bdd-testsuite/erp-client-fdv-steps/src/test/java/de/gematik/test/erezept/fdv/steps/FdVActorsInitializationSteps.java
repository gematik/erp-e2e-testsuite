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

package de.gematik.test.erezept.fdv.steps;

import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.PrimSysBddFactory;
import de.gematik.test.erezept.apimeasure.DumpingStopwatch;
import de.gematik.test.erezept.config.ConfigurationReader;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.de.Angenommen;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

@Slf4j
public class FdVActorsInitializationSteps {
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

  @Angenommen(
      "^(?:der|die) (GKV|PKV|BG|SEL|SOZ|GPV|PPV|BEI) Versicherte (.+) hat Zugriff auf"
          + " (?:seine|ihre) (?:digitale Identität|eGK)$")
  public void initPatient(String insuranceType, String patientName) {
    log.trace("Initialize Patient {} {}", insuranceType, patientName);
    val theActor = OnStage.theActorCalled(patientName);
    config.equipAsPatient(theActor, insuranceType);
  }

  @Angenommen(
      "^(?:der|die) (GKV|PKV|BG|SEL|SOZ|GPV|PPV|BEI) Versicherte (.+) hat eine (?:digitale"
          + " Identität|eGK) für die Abholung in der Apotheke$")
  public void initPatientForVsdm(String insuranceType, String patientName) {
    log.trace("Initialize Patient {} {} for VSDM", insuranceType, patientName);
    val theActor = OnStage.theActorCalled(patientName);
    config.equipAsPatientForVsdm(theActor, insuranceType);
  }
}
