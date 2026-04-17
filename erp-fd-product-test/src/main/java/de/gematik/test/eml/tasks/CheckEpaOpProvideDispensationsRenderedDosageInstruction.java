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

package de.gematik.test.eml.tasks;

import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvideDispensationVerifier.emlMedDispenseHasEqualGeneratedDosageInstrWith;
import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvideDispensationVerifier.provDispensationHasCorrectDosageComponent;

import de.gematik.test.core.expectations.requirements.EmlAfos;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.erezept.abilities.UseTheEpaMockClient;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvideDispensation;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CheckEpaOpProvideDispensationsRenderedDosageInstruction implements Task {
  private final ErxMedicationDispenseBundle erxMedicationDispenseBundle;
  private final PrescriptionId prescriptionId;
  private final List<VerificationStep<EpaOpProvideDispensation>> additionalVerificationSteps;

  public static CheckEpaOpProvideDispensationsRenderedDosageInstruction forDispensation(
      ErxMedicationDispenseBundle erxMedicationDispenseBundle, PrescriptionId prescriptionId) {
    return forDispensationWithAdditionalVerifier(
        erxMedicationDispenseBundle, prescriptionId, List.of());
  }

  public static CheckEpaOpProvideDispensationsRenderedDosageInstruction
      forDispensationWithAdditionalVerifier(
          ErxMedicationDispenseBundle erxMedicationDispenseBundle,
          PrescriptionId prescriptionId,
          List<VerificationStep<EpaOpProvideDispensation>> additionalVerificationSteps) {
    return new CheckEpaOpProvideDispensationsRenderedDosageInstruction(
        erxMedicationDispenseBundle, prescriptionId, additionalVerificationSteps);
  }

  @Override
  @Step("{0} läd sich die ProvideDispensation vom EpaMock und validiert die Werte des DosageDgmp")
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

    List<VerificationStep<EpaOpProvideDispensation>> verifiers =
        new ArrayList<>(additionalVerificationSteps);
    val mdPair = erxMedicationDispenseBundle.getDispensePairBy(prescriptionId).get(0);
    verifiers.addAll(
        List.of(
            emlMedDispenseHasEqualGeneratedDosageInstrWith(mdPair.getLeft()),
            provDispensationHasCorrectDosageComponent(mdPair.getLeft())));

    request.forEach(r -> verifiers.forEach(v -> v.apply(r)));
  }
}
