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

package de.gematik.test.eml.tasks;

import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvidePrescriptionVerifier.provPrescriptionHasCorrectDosageComponent;
import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvidePrescriptionVerifier.provPrescriptionHasCorrectGeneratedDosageExtension;

import de.gematik.test.core.expectations.requirements.EmlAfos;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.erezept.abilities.UseTheEpaMockClient;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvidePrescription;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
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
public class CheckEpaOpProvidePrescriptionsRenderedDosageInstructionValues implements Task {

  private final KbvErpBundle kbvErpBundle;
  private final List<VerificationStep<EpaOpProvidePrescription>> additionalVerificationSteps;

  public static CheckEpaOpProvidePrescriptionsRenderedDosageInstructionValues forPrescription(
      KbvErpBundle bundle) {
    return forPrescription(bundle, List.of());
  }

  @SafeVarargs
  public static CheckEpaOpProvidePrescriptionsRenderedDosageInstructionValues forPrescription(
      KbvErpBundle bundle,
      VerificationStep<EpaOpProvidePrescription>... additionalVerificationStep) {
    List<VerificationStep<EpaOpProvidePrescription>> l = List.of(additionalVerificationStep);
    return new CheckEpaOpProvidePrescriptionsRenderedDosageInstructionValues(bundle, l);
  }

  public static CheckEpaOpProvidePrescriptionsRenderedDosageInstructionValues forPrescription(
      KbvErpBundle bundle,
      List<VerificationStep<EpaOpProvidePrescription>> additionalVerificationStep) {
    return new CheckEpaOpProvidePrescriptionsRenderedDosageInstructionValues(
        bundle, additionalVerificationStep);
  }

  @Override
  @Step(
      "{0} läd sich die ProvidePrescription vom EpaMock und validiert die Werte der"
          + " DosageInstruction")
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
            provPrescriptionHasCorrectGeneratedDosageExtension(kbvErpBundle.getMedicationRequest()),
            provPrescriptionHasCorrectDosageComponent(kbvErpBundle.getMedicationRequest()));

    request.forEach(r -> verifiers.forEach(v -> v.apply(r)));
    request.forEach(r -> additionalVerificationSteps.forEach(v -> v.apply(r)));
  }
}
