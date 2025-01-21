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

import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvideDispensationVerifier.*;

import de.gematik.test.core.expectations.requirements.EmlAfos;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.erezept.abilities.UseTheEpaMockClient;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvideDispensation;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
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
public class CheckEpaOpProvideDispensation implements Task {

  private final ErxMedicationDispense medicationDispense;

  private final TelematikID tidSmcb;

  public static CheckEpaOpProvideDispensation forDispensation(
      ErxMedicationDispense medicationDispense, TelematikID tidSmcb) {
    return new CheckEpaOpProvideDispensation(medicationDispense, tidSmcb);
  }

  @Override
  public <T extends Actor> void performAs(T actor) {
    val client = SafeAbility.getAbility(actor, UseTheEpaMockClient.class);
    List<EpaOpProvideDispensation> request;

    request = client.downloadProvideDispensationBy(medicationDispense.getPrescriptionId());

    if (request.isEmpty()) {
      throw new AssertionError(
          "No EpaOpProvideDispensation found for prescriptionId: "
              + medicationDispense.getPrescriptionId().getValue());
    }
    log.info(
        "A_25948 {} wird implizit mit getestet", EmlAfos.A_25948.getRequirement().getDescription());
    List<VerificationStep<EpaOpProvideDispensation>> verifiers =
        List.of(
            emlDispensationIdIsEqualTo(medicationDispense.getPrescriptionId()),
            emlHandedOverIsEqualTo(medicationDispense.getWhenHandedOver()),
            emlMedicationMapsTo(medicationDispense.getContainedKbvMedicationFirstRep()),
            emlMedicationDispenseMapsTo(medicationDispense),
            emlOrganisationHasSmcbTelematikId(tidSmcb));

    request.forEach(r -> verifiers.forEach(v -> v.apply(r)));
  }
}
