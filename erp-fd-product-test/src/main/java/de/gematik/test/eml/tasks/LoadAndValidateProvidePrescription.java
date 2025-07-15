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

import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.erezept.abilities.UseTheEpaMockClient;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvidePrescription;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
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
public class LoadAndValidateProvidePrescription implements Task {

  private final List<VerificationStep<EpaOpProvidePrescription>> verifiersList;
  private final PrescriptionId prescriptionId;

  public static LoadAndValidateProvidePrescriptionBuilder withValidator(
      List<VerificationStep<EpaOpProvidePrescription>> validatorList) {
    return new LoadAndValidateProvidePrescriptionBuilder(validatorList);
  }

  @Override
  @Step("{0} läd die ProvidePrescription vom EpaMock und validiert den Inhalt")
  public <T extends Actor> void performAs(T actor) {

    val client = SafeAbility.getAbility(actor, UseTheEpaMockClient.class);

    val requests = client.downloadProvidePrescriptionBy(prescriptionId);

    if (requests.isEmpty()) {
      throw new AssertionError(
          "No EpaOpProvidePrescription found for prescriptionId: " + prescriptionId.getValue());
    }

    requests.forEach(r -> verifiersList.forEach(v -> v.apply(r)));
  }

  @AllArgsConstructor
  public static class LoadAndValidateProvidePrescriptionBuilder {
    private final List<VerificationStep<EpaOpProvidePrescription>> verifiersList;

    public LoadAndValidateProvidePrescription forPrescription(PrescriptionId prescriptionId) {
      return new LoadAndValidateProvidePrescription(verifiersList, prescriptionId);
    }
  }
}
