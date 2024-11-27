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

import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvidePrescriptionVerifier.*;

import de.gematik.test.core.expectations.requirements.EmlAfos;
import de.gematik.test.erezept.abilities.UseTheEpaMockClient;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.TelematikID;
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
public class CheckEpaOpProvidePrescriptionWithTask implements Task {

  private final KbvErpBundle kbvErpBundle;
  private final TelematikID tidSmcb;
  private final TelematikID tidHba;

  /**
   * Check the EpaOpProvidedPrescription Opject with the given bundle and telematik ids
   *
   * @param bundle is an KbvErpBundle with the Input Data
   * @param tidSmcb is the TelematikID of the SMC-B and is setting in the
   *     EmlEpaOpProvidedPrescription by the E-Prescription Backend
   * @param tidHba is the TelematikID of the HBA and is setting in the EmlEpaOpProvidedPrescription
   *     by the E-Prescription Backend
   * @return CheckEpaOpProvidePrescriptionWithTask Object that will be performed by serenity
   *     screenplay
   */
  public static CheckEpaOpProvidePrescriptionWithTask forPrescription(
      KbvErpBundle bundle, TelematikID tidSmcb, TelematikID tidHba) {
    return new CheckEpaOpProvidePrescriptionWithTask(bundle, tidSmcb, tidHba);
  }

  @Override
  public <T extends Actor> void performAs(T actor) {
    val client = SafeAbility.getAbility(actor, UseTheEpaMockClient.class);

    val request = client.downloadProvidePrescriptionBy(kbvErpBundle.getPrescriptionId());

    if (request.isEmpty()) {
      throw new AssertionError(
          "No EpaOpProvidePrescription found for prescriptionId: "
              + kbvErpBundle.getPrescriptionId().getValue());
    }
    log.info(
        "A_25948 {} wird implizit mit getestet", EmlAfos.A_25948.getRequirement().getDescription());
    val verifiers =
        List.of(
            emlPrescriptionIdIsEqualTo(kbvErpBundle.getPrescriptionId()),
            emlAuthoredOnIsEqualTo(kbvErpBundle.getAuthoredOn()),
            emlMedicationMapsTo(kbvErpBundle.getMedication()),
            emlMedicationRequestMapsTo(kbvErpBundle.getMedicationRequest()),
            emlOrganisationHasSmcbTelematikId(tidSmcb),
            emlPractitionerHasHbaTelematikId(tidHba));

    request.forEach(r -> verifiers.forEach(v -> v.apply(r)));
  }
}
