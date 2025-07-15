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

package de.gematik.test.core.expectations.verifier.emlverifier;

import static de.gematik.test.core.Helper.findBySystem;
import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.core.expectations.requirements.EmlAfos;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvideDispensation;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.jetbrains.annotations.NotNull;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EpaOpProvideDispensationVerifier {

  public static VerificationStep<EpaOpProvideDispensation> emlDispensationIdIsEqualTo(
      PrescriptionId prescriptionId) {
    Predicate<EpaOpProvideDispensation> predicate =
        dispensation ->
            dispensation.getEpaPrescriptionId().getValue().equals(prescriptionId.getValue());
    return new VerificationStep.StepBuilder<EpaOpProvideDispensation>(
            EmlAfos.A_25952.getRequirement(),
            format(
                "Die Dispensation muss die PrescriptionID: {0} haben, daher ist {1} nicht ewrfüllt",
                prescriptionId.getValue(), EmlAfos.A_25952))
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpProvideDispensation> emlHandedOverIsEqualTo(Date date) {
    Predicate<EpaOpProvideDispensation> predicate =
        dispensation -> dispensation.getEpaWhenHandedOver().equals(date);
    return new VerificationStep.StepBuilder<EpaOpProvideDispensation>(
            EmlAfos.A_25946.getRequirement(),
            "Das AuthoredOn Datum entspricht nicht dem erwarteten Datum: " + date.toString())
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpProvideDispensation> emlMedicationMapsTo(
      KbvErpMedication expectedMedication) {
    Predicate<EpaOpProvideDispensation> predicate =
        getDispensationPredicate(expectedMedication.getCode());

    return new VerificationStep.StepBuilder<EpaOpProvideDispensation>(
            EmlAfos.A_25946.getRequirement(),
            "Die / Das enthaltene/n Coding/s (PZN / ASK / ATC) in der Epa Medication stimmt/-en"
                + " nicht mit der KbvMedication überein übereinstimmen")
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpProvideDispensation> emlMedicationMapsTo(
      GemErpMedication expectedMedication) {
    Predicate<EpaOpProvideDispensation> predicate =
        getDispensationPredicate(expectedMedication.getCode());

    return new VerificationStep.StepBuilder<EpaOpProvideDispensation>(
            EmlAfos.A_25946.getRequirement(),
            "Die / Das enthaltene/n Coding/s (PZN / ASK / ATC) in der Epa Medication stimmt/-en"
                + " nicht mit der KbvMedication überein übereinstimmen")
        .predicate(predicate)
        .accept();
  }

  @NotNull
  private static Predicate<EpaOpProvideDispensation> getDispensationPredicate(
      CodeableConcept expectedMedication) {
    return dispensation -> {
      val epaCodings = dispensation.getEpaMedication().getCode().getCoding();
      val expectedMedCodings = expectedMedication.getCoding();
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
  }

  public static VerificationStep<EpaOpProvideDispensation> emlMedicationDispenseMapsTo(
      ErxMedicationDispense medicationDispense) {
    Predicate<EpaOpProvideDispensation> predicate =
        dispensation -> {
          val epaDispensation = dispensation.getEpaMedicationDispense();

          if (!medicationDispense.getStatus().equals(epaDispensation.getStatus())) return false;
          if (!Objects.equals(
              epaDispensation.getSubject().getIdentifier().getValue(),
              medicationDispense.getSubjectId().getValue())) return false;
          if (!Objects.equals(
              epaDispensation.getDosageInstructionFirstRep().getText(),
              medicationDispense.getDosageInstructionTextFirstRep())) return false;
          if (!epaDispensation.getWhenHandedOver().equals(medicationDispense.getWhenHandedOver()))
            return false;

          return (epaDispensation.getSubstitution().getWasSubstituted()
              == medicationDispense.getSubstitution().getWasSubstituted());
        };
    return new VerificationStep.StepBuilder<EpaOpProvideDispensation>(
            EmlAfos.A_25946.getRequirement(),
            "Die Werte der EML_MedicationDispense im Bereich Performer, Status, Subject,"
                + " Substituted, DosageInstruction und WhenHandedOver müssen mit den Werten der"
                + " MedicationDispense übereinstimmen")
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpProvideDispensation> emlOrganisationHasSMCBTelematikId(
      TelematikID sMCBTelematikId) {

    Predicate<EpaOpProvideDispensation> predicate =
        prescription ->
            prescription
                .getEpaOrganisation()
                .getTelematikId()
                .getValue()
                .equals(sMCBTelematikId.getValue());
    return new VerificationStep.StepBuilder<EpaOpProvideDispensation>(
            EmlAfos.A_25949.getRequirement(),
            format("Die EpaOrganisation muss die TelematikId {0} enthalten", sMCBTelematikId))
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<List<EpaOpProvideDispensation>> emlDoesNotContainAnything() {
    Predicate<List<EpaOpProvideDispensation>> predicate = List::isEmpty;
    return new VerificationStep.StepBuilder<List<EpaOpProvideDispensation>>(
            EmlAfos.A_25951.getRequirement(), "Eml besitzt eine leere Liste")
        .predicate(predicate)
        .accept();
  }
}
