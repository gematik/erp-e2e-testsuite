/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.eml.tasks;

import de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvideDispensationVerifier;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.abilities.UseTheEpaMockClient;
import de.gematik.test.erezept.eml.PollingTimeoutException;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvideDispensation;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CheckErpDoesNotProvideDispensationToEpa implements Task {

  private final PrescriptionId prescriptionID;

  /**
   * Check the EpaOpProvidedPrescription Opject with the given bundle and telematik ids
   *
   * @return CheckEpaOpProvidePrescriptionWithTask Object that will be performed by serenity
   *     screenplay
   */
  public static CheckErpDoesNotProvideDispensationToEpa forPrescription(
      PrescriptionId prescriptionID) {
    return new CheckErpDoesNotProvideDispensationToEpa(prescriptionID);
  }

  public static CheckErpDoesNotProvideDispensationToEpa forDispensation(
      ErpInteraction<ErxMedicationDispenseBundle> erpInteraction) {
    return forPrescription(
        erpInteraction.getExpectedResponse().getMedicationDispenses().get(0).getPrescriptionId());
  }

  @Override
  public <T extends Actor> void performAs(T actor) {
    val client = SafeAbility.getAbility(actor, UseTheEpaMockClient.class);
    List<EpaOpProvideDispensation> epaOpProvideDispensationList = List.of();

    try {
      epaOpProvideDispensationList = client.downloadProvideDispensationBy(prescriptionID);
    } catch (PollingTimeoutException ignored) {
      /*
       *
       * Das Polling in downloadProvidePrescriptionBy() wird benötigt
       * Einerseits: wenn wir nur einmal abfragen und der Fachdienst später etwas sendet, verpassen wir die Anfrage,
       * Andererseits: wenn wir einen positiven Testfall wie haben, dann können wir ihn mit Ausnahmebehandlung lösen
       *
       */
    }
    EpaOpProvideDispensationVerifier.emlDoesNotContainAnything()
        .apply(epaOpProvideDispensationList);
  }
}
