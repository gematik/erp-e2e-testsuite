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

package de.gematik.test.core.expectations.verifier.emlverifier;

import de.gematik.test.core.expectations.requirements.EmlAfos;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpCancelPrescription;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EpaOpCancelPrescriptionVerifier {

  public static VerificationStep<EpaOpCancelPrescription> emlPrescriptionIdIsEqualTo(
      PrescriptionId prescriptionId) {
    Predicate<EpaOpCancelPrescription> predicate =
        prescription ->
            prescription.getEpaPrescriptionId().getValue().equals(prescriptionId.getValue());
    return new VerificationStep.StepBuilder<EpaOpCancelPrescription>(
            EmlAfos.A_25953.getRequirement(),
            "Die Prescription MUSS die PrescriptionID haben: " + prescriptionId.getValue())
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpCancelPrescription> emlAuthoredOnIsEqualTo(Date date) {
    Predicate<EpaOpCancelPrescription> predicate =
        prescription -> prescription.getEpaAuthoredOn().equals(date);
    return new VerificationStep.StepBuilder<EpaOpCancelPrescription>(
            EmlAfos.A_25931.getRequirement(),
            "Das AuthoredOn Datum entspricht dem erwarteten Datum")
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<List<EpaOpCancelPrescription>> emlDoesNotContainAnything() {
    Predicate<List<EpaOpCancelPrescription>> predicate = List::isEmpty;
    return new VerificationStep.StepBuilder<List<EpaOpCancelPrescription>>(
            EmlAfos.A_25951.getRequirement(), "Eml besitzt eine leere Liste")
        .predicate(predicate)
        .accept();
  }
}
