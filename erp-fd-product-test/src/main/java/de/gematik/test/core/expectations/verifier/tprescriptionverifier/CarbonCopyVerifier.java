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

package de.gematik.test.core.expectations.verifier.tprescriptionverifier;

import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.r4.erp.tprescription.ErpTPrescriptionCarbonCopy;
import java.util.List;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CarbonCopyVerifier {

  private static final String NO_MEDICATION_DISPENSE_AND_GEM_ERP_MEDICATION_PAIR =
      "No MedicationDispense and GemErpMedication pair";
  private static final String TO_VERIFY_CARBON_COPY = " found to verify CarbonCopy";

  public static VerificationStep<ErpTPrescriptionCarbonCopy> checkPznFromGemMedication(
      List<Pair<ErxMedicationDispense, GemErpMedication>> medicationDispensePair) {
    val pzn =
        medicationDispensePair.stream()
            .findFirst()
            .orElseThrow(
                () ->
                    new AssertionError(
                        "No MedicationDispense and GemErpMedication pair found to verify"
                            + " CarbonCopies PZN"))
            .getRight()
            .getPzn();
    Predicate<ErpTPrescriptionCarbonCopy> predicate =
        carbonCopy -> carbonCopy.getMedicationFromPrescription().getPzn().equals(pzn);
    return new VerificationStep.StepBuilder<ErpTPrescriptionCarbonCopy>(
            ErpAfos.A_27827.getRequirement(),
            "Die PZN in der CarbonCopy stimmmt mit der PZN der Medication überein")
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<ErpTPrescriptionCarbonCopy> checkMedicationName(
      List<Pair<ErxMedicationDispense, GemErpMedication>> medicationDispensePair) {
    val pznName =
        medicationDispensePair.stream()
            .findFirst()
            .orElseThrow(
                () ->
                    new AssertionError(
                        NO_MEDICATION_DISPENSE_AND_GEM_ERP_MEDICATION_PAIR + TO_VERIFY_CARBON_COPY))
            .getRight()
            .getNameFromCodeText();
    Predicate<ErpTPrescriptionCarbonCopy> predicate =
        carbonCopy ->
            carbonCopy
                .getMedicationFromPrescription()
                .getName()
                .orElse("false")
                .equals(
                    pznName.orElseThrow(
                        () ->
                            new AssertionError(
                                "No name in GemErpMedication" + TO_VERIFY_CARBON_COPY)));
    return new VerificationStep.StepBuilder<ErpTPrescriptionCarbonCopy>(
            ErpAfos.A_27827.getRequirement(),
            "Der MedicationName in der CarbonCopy stimmt mit der PZN der Medication überein")
        .predicate(predicate)
        .accept();
  }

  // MedicationCategory tbd ... no Category found in CarbonCopy

  public static VerificationStep<ErpTPrescriptionCarbonCopy> checkPrescriptionId(
      List<Pair<ErxMedicationDispense, GemErpMedication>> medicationDispensePair) {
    val gemDispPrescriptionId =
        medicationDispensePair.stream()
            .findFirst()
            .orElseThrow(
                () ->
                    new AssertionError(
                        NO_MEDICATION_DISPENSE_AND_GEM_ERP_MEDICATION_PAIR + TO_VERIFY_CARBON_COPY))
            .getLeft()
            .getPrescriptionId();
    Predicate<ErpTPrescriptionCarbonCopy> predicate =
        carbonCopy -> carbonCopy.getPrescriptionId().equals(gemDispPrescriptionId);
    return new VerificationStep.StepBuilder<ErpTPrescriptionCarbonCopy>(
            ErpAfos.A_27827.getRequirement(),
            "Die PrescriptionID in der CarbonCopy stimmt mit der PrescriptionID der Dispensation"
                + " überein")
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<ErpTPrescriptionCarbonCopy> checkDarreichungsformInPrescription(
      List<Pair<ErxMedicationDispense, GemErpMedication>> medicationDispensePair) {
    val gemPrescrDarreichungsform =
        medicationDispensePair.stream()
            .findFirst()
            .orElseThrow(
                () ->
                    new AssertionError(
                        NO_MEDICATION_DISPENSE_AND_GEM_ERP_MEDICATION_PAIR + TO_VERIFY_CARBON_COPY))
            .getRight()
            .getDarreichungsform()
            .orElseThrow(
                () ->
                    new AssertionError(
                        "No Darreichungsform in GemErpMedication " + TO_VERIFY_CARBON_COPY));
    Predicate<ErpTPrescriptionCarbonCopy> predicate =
        carbonCopy ->
            carbonCopy
                .getMedicationFromPrescription()
                .getDarreichungsform()
                .equals(gemPrescrDarreichungsform);
    return new VerificationStep.StepBuilder<ErpTPrescriptionCarbonCopy>(
            ErpAfos.A_27827.getRequirement(),
            "Die Darreichungsform in der CarbonCopy_RxPrescription stimmt mit der Darreichungsform"
                + " der Medication überein")
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<ErpTPrescriptionCarbonCopy> checkDarreichungsformInDispensation(
      List<Pair<ErxMedicationDispense, GemErpMedication>> medicationDispensePair) {
    val gemPrescrDarreichungsform =
        medicationDispensePair.stream()
            .findFirst()
            .orElseThrow(
                () ->
                    new AssertionError(
                        NO_MEDICATION_DISPENSE_AND_GEM_ERP_MEDICATION_PAIR + TO_VERIFY_CARBON_COPY))
            .getRight()
            .getDarreichungsform()
            .orElseThrow(
                () ->
                    new AssertionError(
                        "No Darreichungsform in GemErpMedication" + TO_VERIFY_CARBON_COPY));
    Predicate<ErpTPrescriptionCarbonCopy> predicate =
        carbonCopy ->
            carbonCopy
                .getMedicationFromDispensation()
                .getDarreichungsform()
                .equals(gemPrescrDarreichungsform);
    return new VerificationStep.StepBuilder<ErpTPrescriptionCarbonCopy>(
            ErpAfos.A_27827.getRequirement(),
            "Die Darreichungsform in der CarbonCopy-RxDispensation stimmt mit der Darreichungsform"
                + " der Medication überein")
        .predicate(predicate)
        .accept();
  }
}
