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

package de.gematik.test.core.expectations.verifier.emlverifier;

import de.gematik.test.core.expectations.requirements.EmlAfos;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpCancelDispensation;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Date;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EpaOpCancelDispensationVerifier {

  public static VerificationStep<EpaOpCancelDispensation> emlPrescriptionIdIsEqualTo(
      PrescriptionId prescriptionId) {
    Predicate<EpaOpCancelDispensation> predicate =
        dispensation ->
            dispensation.getEpaPrescriptionId().getValue().equals(prescriptionId.getValue());
    return new VerificationStep.StepBuilder<EpaOpCancelDispensation>(
            EmlAfos.A_25955.getRequirement(),
            "Die Prescription MUSS die PrescriptionID haben: " + prescriptionId.getValue())
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpCancelDispensation> emlAuthoredOnIsEqualTo(Date date) {
    Predicate<EpaOpCancelDispensation> predicate =
        dispensation -> dispensation.getEpaAuthoredOn().equals(date);
    return new VerificationStep.StepBuilder<EpaOpCancelDispensation>(
            EmlAfos.A_25930.getRequirement(),
            "Das AuthoredOn Datum entspricht nicht dem erwarteten Datum")
        .predicate(predicate)
        .accept();
  }
}
