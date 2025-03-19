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
 */

package de.gematik.test.eml.tasks;

import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvideDispensationVerifier.*;

import de.gematik.test.core.expectations.requirements.EmlAfos;
import de.gematik.test.erezept.abilities.UseTheEpaMockClient;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvideDispensation;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TelematikID;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.ArrayList;
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
  private final ErxMedicationDispenseBundle erxMedicationDispenseBundle;
  private final TelematikID tiIdSMCB;
  private final PrescriptionId prescriptionId;

  public static CheckEpaOpProvideDispensation forDispensation(
      ErxMedicationDispenseBundle erxMedicationDispenseBundle,
      TelematikID tiIdSmcb,
      PrescriptionId prescriptionId) {
    return new CheckEpaOpProvideDispensation(erxMedicationDispenseBundle, tiIdSmcb, prescriptionId);
  }

  @Override
  public <T extends Actor> void performAs(T actor) {
    val client = SafeAbility.getAbility(actor, UseTheEpaMockClient.class);
    List<EpaOpProvideDispensation> request;

    request = client.downloadProvideDispensationBy(prescriptionId);

    if (request.isEmpty()) {
      throw new AssertionError(
          "No EpaOpProvideDispensation found for prescriptionId: " + prescriptionId.getValue());
    }
    log.info(
        "A_25948 {} wird implizit mit getestet", EmlAfos.A_25948.getRequirement().getDescription());

    val verifiers =
        new ArrayList<>(
            List.of(
                emlDispensationIdIsEqualTo(prescriptionId),
                emlOrganisationHasSMCBTelematikId(tiIdSMCB)));

    if (ErpWorkflowVersion.getDefaultVersion().compareTo(ErpWorkflowVersion.V1_3_0) <= 0) {
      val mdPair = erxMedicationDispenseBundle.unpackDispensePairBy(prescriptionId).get(0);
      verifiers.addAll(
          List.of(
              emlHandedOverIsEqualTo(mdPair.getLeft().getWhenHandedOver()),
              emlMedicationMapsTo(mdPair.getRight()),
              emlMedicationDispenseMapsTo(mdPair.getLeft())));

      request.forEach(r -> verifiers.forEach(v -> v.apply(r)));

    } else {
      val mdPair = erxMedicationDispenseBundle.getDispensePairBy(prescriptionId).get(0);
      verifiers.addAll(
          List.of(
              emlHandedOverIsEqualTo(mdPair.getLeft().getWhenHandedOver()),
              emlMedicationMapsTo(mdPair.getRight()),
              emlMedicationDispenseMapsTo(mdPair.getLeft())));

      request.forEach(r -> verifiers.forEach(v -> v.apply(r)));
    }
  }
}
