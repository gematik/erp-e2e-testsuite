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

import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpCancelPrescriptionVerifier.*;

import de.gematik.test.core.expectations.requirements.EmlAfos;
import de.gematik.test.erezept.abilities.UseTheEpaMockClient;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
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
public class CheckEpaOpCancelPrescriptionWithTask implements Task {

  private final KbvErpBundle kbvErpBundle;

  /**
   * Create a CheckEpaOpCancelPrescriptionWithTask for verifying the cancellation of a prescription.
   *
   * @param bundle is an KbvErpBundle with the Input Data
   * @return CheckEpaOpCancelPrescriptionWithTask object for use in Serenity Screenplay
   */
  public static CheckEpaOpCancelPrescriptionWithTask forCancelPrescription(KbvErpBundle bundle) {
    return new CheckEpaOpCancelPrescriptionWithTask(bundle);
  }

  @Override
  public <T extends Actor> void performAs(T actor) {
    val client = SafeAbility.getAbility(actor, UseTheEpaMockClient.class);
    val prescriptionID = kbvErpBundle.getPrescriptionId();
    val authoredOn = kbvErpBundle.getAuthoredOn();
    val request = client.downloadCancelPrescriptionBy(prescriptionID);

    if (request.isEmpty()) {
      throw new AssertionError(
          "No EpaOpCancelPrescription found for prescriptionId: " + prescriptionID);
    }

    log.info(
        "A_25953 is being implicitly tested for prescriptionId: {}",
        EmlAfos.A_25953.getRequirement().getDescription());

    val verifiers =
        List.of(emlPrescriptionIdIsEqualTo(prescriptionID), emlAuthoredOnIsEqualTo(authoredOn));

    request.forEach(r -> verifiers.forEach(v -> v.apply(r)));
  }
}
