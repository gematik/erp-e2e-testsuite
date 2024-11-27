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

import static de.gematik.test.core.Helper.findBySystem;
import static java.text.MessageFormat.format;

import de.gematik.test.core.expectations.requirements.EmlAfos;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvidePrescription;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TelematikID;
import java.util.Date;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EpaOpProvidePrescriptionVerifier {

  public static VerificationStep<EpaOpProvidePrescription> emlPrescriptionIdIsEqualTo(
      PrescriptionId prescriptionId) {
    Predicate<EpaOpProvidePrescription> predicate =
        prescription ->
            prescription.getEpaPrescriptionId().getValue().equals(prescriptionId.getValue());
    return new VerificationStep.StepBuilder<EpaOpProvidePrescription>(
            EmlAfos.A_25952.getRequirement(),
            format(
                "Die Dispensation muss die PrescriptionID: {0} haben, daher ist {1} nicht erfüllt",
                prescriptionId.getValue(), EmlAfos.A_25952))
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpProvidePrescription> emlAuthoredOnIsEqualTo(Date date) {
    Predicate<EpaOpProvidePrescription> predicate =
        prescription -> prescription.getEpaAuthoredOn().equals(date);
    return new VerificationStep.StepBuilder<EpaOpProvidePrescription>(
            EmlAfos.A_25946.getRequirement(),
            "Das AuthoredOn Datum entspricht nicht dem erwarteten Datum")
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpProvidePrescription> emlMedicationMapsTo(
      KbvErpMedication expectedMedication) {
    Predicate<EpaOpProvidePrescription> predicate =
        prescription -> {
          val epaCodings = prescription.getEpaMedication().getCode().getCoding();
          val expectedMedCodings = expectedMedication.getCode().getCoding();
          if (epaCodings.size() != expectedMedCodings.size()) return false;

          return epaCodings.stream()
              .map(
                  epaCoding -> {
                    val expectedCoding = findBySystem(epaCoding, expectedMedCodings);
                    return Pair.of(epaCoding, expectedCoding);
                  })
              .allMatch(
                  pair ->
                      pair.getRight()
                          .map(
                              expectedCoding ->
                                  expectedCoding.getCode().equals(pair.getLeft().getCode()))
                          .orElse(false));
        };

    return new VerificationStep.StepBuilder<EpaOpProvidePrescription>(
            EmlAfos.A_25946.getRequirement(),
            "Die / Das enthaltene/n Coding/s (PZN / ASK / ATC) in der Epa Medication stimmt/-en"
                + " nicht mit der KbvMedication überein übereinstimmen")
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpProvidePrescription> emlMedicationRequestMapsTo(
      KbvErpMedicationRequest medicationRequest) {
    Predicate<EpaOpProvidePrescription> predicate =
        prescription -> {
          if (medicationRequest.getAuthoredOn().getTime()
              != prescription.getEpaMedicationRequest().getAuthoredOn().getTime()) return false;
          if (!medicationRequest
              .getStatus()
              .equals(prescription.getEpaMedicationRequest().getStatus())) return false;

          val medReqQuan = medicationRequest.getDispenseRequest().getQuantity();
          val prescrQuant =
              prescription.getEpaMedicationRequest().getDispenseRequest().getQuantity();
          return medReqQuan.getValue().equals(prescrQuant.getValue())
              && medReqQuan.getSystem().equals(prescrQuant.getSystem());
        };
    return new VerificationStep.StepBuilder<EpaOpProvidePrescription>(
            EmlAfos.A_25946.getRequirement(),
            "Die Werte in MedicationRequest für AuthoredOn, DispenseRequestQuantity und Status"
                + " müssen übereinstimmen")
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpProvidePrescription> emlOrganisationHasSmcbTelematikId(
      TelematikID telematikId) {

    Predicate<EpaOpProvidePrescription> predicate =
        prescription ->
            prescription
                .getEpaOrganisation()
                .getTelematikId()
                .getValue()
                .equals(telematikId.getValue());
    return new VerificationStep.StepBuilder<EpaOpProvidePrescription>(
            EmlAfos.A_25949.getRequirement(),
            format("Die EpaOrganisation muss die TelematikId {0} enthalten", telematikId))
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpProvidePrescription> emlPractitionerHasHbaTelematikId(
      TelematikID hbaId) {
    Predicate<EpaOpProvidePrescription> predicate =
        prescription ->
            prescription.getEpaPractitioner().getTelematikId().getValue().equals(hbaId.getValue());
    return new VerificationStep.StepBuilder<EpaOpProvidePrescription>(
            EmlAfos.A_25949.getRequirement(),
            "Der EpaPractitioner muss die TelematikId: {0} besitzen")
        .predicate(predicate)
        .accept();
  }
}
