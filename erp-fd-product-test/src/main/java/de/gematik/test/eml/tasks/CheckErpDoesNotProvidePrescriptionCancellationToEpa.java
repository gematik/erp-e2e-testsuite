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

import de.gematik.test.core.expectations.verifier.emlverifier.EpaOpCancelPrescriptionVerifier;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.abilities.UseTheEpaMockClient;
import de.gematik.test.erezept.eml.PollingTimeoutException;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpCancelPrescription;
import de.gematik.test.erezept.fhir.resources.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
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
public class CheckErpDoesNotProvidePrescriptionCancellationToEpa implements Task {

  private final KbvErpBundle kbvErpBundle;

  /**
   * Check the EpaOpProvidedPrescription Opject with the given bundle and telematik ids
   *
   * @param bundle is an KbvErpBundle with the Input Data
   * @return CheckEpaOpProvidePrescriptionWithTask Object that will be performed by serenity
   *     screenplay
   */
  public static CheckErpDoesNotProvidePrescriptionCancellationToEpa forCancelPrescription(
      KbvErpBundle bundle) {
    return new CheckErpDoesNotProvidePrescriptionCancellationToEpa(bundle);
  }

  public static CheckErpDoesNotProvidePrescriptionCancellationToEpa forCancelPrescription(
      ErpInteraction<ErxPrescriptionBundle> erpInteraction) {
    return forCancelPrescription(erpInteraction.getExpectedResponse().getKbvBundle().orElseThrow());
  }

  @Override
  public <T extends Actor> void performAs(T actor) {
    val client = SafeAbility.getAbility(actor, UseTheEpaMockClient.class);
    List<EpaOpCancelPrescription> epaOpCancelPrescriptionList = List.of();

    try {
      epaOpCancelPrescriptionList =
          client.downloadCancelPrescriptionBy(kbvErpBundle.getPrescriptionId());
    } catch (PollingTimeoutException ignored) {
      /*
       *
       * Das Polling in downloadProvidePrescriptionBy() wird benötigt
       * Einerseits: wenn wir nur einmal abfragen und der Fachdienst später etwas sendet, verpassen wir die Anfrage,
       * Andererseits: wenn wir einen positiven Testfall wie haben, dann können wir ihn mit Ausnahmebehandlung lösen
       *
       */
    }
    EpaOpCancelPrescriptionVerifier.emlDoesNotContainAnything().apply(epaOpCancelPrescriptionList);
  }
}
