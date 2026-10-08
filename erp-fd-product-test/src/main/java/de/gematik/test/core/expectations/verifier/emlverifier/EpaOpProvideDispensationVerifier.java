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

package de.gematik.test.core.expectations.verifier.emlverifier;

import static de.gematik.test.erezept.eml.fhir.profile.EpaMedicationStructDef.DRUG_CATEGORY_EXT;
import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.core.expectations.requirements.EmlAfos;
import de.gematik.test.core.expectations.requirements.EmlBfd;
import de.gematik.test.core.expectations.requirements.ErpBfd;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvideDispensation;
import de.gematik.test.erezept.fhir.profiles.definitions.DgMPStructDef;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.jetbrains.annotations.NotNull;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EpaOpProvideDispensationVerifier {

  public static final String NO_MATCHING_CODING =
      "Die / Das enthaltene/n Coding/s (PZN / ASK / ATC) in der Epa Medication stimmt/-en"
          + " nicht mit der Dispensation überein";

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
    Predicate<EpaOpProvideDispensation> predicate = evaluateCodings(expectedMedication.getCode());

    return new VerificationStep.StepBuilder<EpaOpProvideDispensation>(
            EmlAfos.A_25946.getRequirement(), NO_MATCHING_CODING)
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpProvideDispensation> emlMedicationHasCategory(
      MedicationCategory category) {
    Predicate<EpaOpProvideDispensation> predicate =
        dispensation ->
            dispensation.getEpaMedication().getExtension().stream()
                .filter(DRUG_CATEGORY_EXT::matches)
                .map(ex -> ((Coding) ex.getValue()).getCode().equals(category.getCode()))
                .findAny()
                .equals(Optional.of(true));

    return new VerificationStep.StepBuilder<EpaOpProvideDispensation>(
            EmlAfos.A_25946.getRequirement(),
            "Die EpaMedication muss die MedicationCategory {0} enthalten")
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpProvideDispensation> emlMedicationCodingsMapsTo(
      GemErpMedication expectedMedication) {
    Predicate<EpaOpProvideDispensation> predicate = evaluateCodings(expectedMedication.getCode());

    return new VerificationStep.StepBuilder<EpaOpProvideDispensation>(
            EmlAfos.A_25946.getRequirement(), NO_MATCHING_CODING)
        .predicate(predicate)
        .accept();
  }

  @NotNull
  private static Predicate<EpaOpProvideDispensation> evaluateCodings(
      CodeableConcept expectedMedication) {
    return dispensation -> {
      val epaCodings = dispensation.getEpaMedication().getCode().getCoding();
      val expectedMedCodings = expectedMedication.getCoding();
      if (epaCodings.size() != expectedMedCodings.size()) return false;

      return epaCodings.stream()
              .allMatch(
                  eC ->
                      expectedMedCodings.stream()
                          .anyMatch(expC -> expC.getCode().equals(eC.getCode())))
          && expectedMedCodings.stream()
              .allMatch(
                  eC -> epaCodings.stream().anyMatch(expC -> expC.getCode().equals(eC.getCode())));
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
              medicationDispense.getDosageOrPatientInstruction())) {
            if (!Objects.equals( // NOSONAR
                epaDispensation.getDosageInstructionFirstRep().getText(),
                medicationDispense.getDosageInstructionFirstRep().getText()
                    + "; "
                    + medicationDispense.getDosageInstructionFirstRep().getPatientInstruction()))
              return false;
          }
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

  public static VerificationStep<EpaOpProvideDispensation>
      emlMedDispenseHasEqualGeneratedDosageInstrWith(ErxMedicationDispense medicationDispense) {
    val gemDispensationRenderedDosage = medicationDispense.getRenderedDosageInstructionOptional();

    Predicate<EpaOpProvideDispensation> predicate =
        epaOpDispensation ->
            epaOpDispensation.getEpaMedicationDispense().getExtension().stream()
                .filter(DgMPStructDef.MD_RENDERED_DOSAGE_INSTRUCTION::matches)
                .map(ext -> ext.getValue().primitiveValue())
                .findFirst()
                .equals(gemDispensationRenderedDosage);

    return new VerificationStep.StepBuilder<EpaOpProvideDispensation>(
            EmlAfos.A_25949.getRequirement(),
            format(
                "Die EpaMedicationDispense muss die errechnete Dosierinformation {0} enthalten",
                gemDispensationRenderedDosage))
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpProvideDispensation>
      provDispensationHasCorrectDosageDgMPComponent(ErxMedicationDispense erxMedicationDispense) {

    Predicate<EpaOpProvideDispensation> dgMPContentValidation =
        dispensation ->
            dispensation
                .getEpaMedicationDispense()
                .getDosageInstructionDgMPs()
                .equals(erxMedicationDispense.getDosageInstructionDgMPs());

    return new VerificationStep.StepBuilder<EpaOpProvideDispensation>(
            EmlAfos.A_25949.getRequirement(),
            "Die EpaMedicationDispense muss die Dosierinformation der DosageDgMP enthalten")
        .predicate(dgMPContentValidation)
        .accept();
  }

  public static VerificationStep<EpaOpProvideDispensation>
      provDispensationContainsDosageInstruction(String dosageInstruction) {
    Predicate<EpaOpProvideDispensation> predicate =
        dispensation ->
            dispensation.getEpaMedicationDispense().getDosageInstruction().stream()
                .filter(it -> it.getText() != null)
                .map(it -> it.getText().equals(dosageInstruction))
                .findFirst()
                .orElse(Boolean.FALSE);

    return new VerificationStep.StepBuilder<EpaOpProvideDispensation>(
            EmlBfd.B_FD_1571.getRequirement(),
            format("Dispensation does not contains dosage instruction. {0}", dosageInstruction))
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpProvideDispensation>
      medicationInProvDispensationContainsAtcCodingWithVersion() {
    Predicate<EpaOpProvideDispensation> predicate =
        dispensation ->
            dispensation.getEpaMedication().getCode().getCoding().stream()
                .filter(DeBasisProfilCodeSystem.ATC::matches)
                .filter(coding -> !coding.hasVersion())
                .toList()
                .isEmpty();

    return new VerificationStep.StepBuilder<EpaOpProvideDispensation>(
            EmlBfd.B_FD_1571.getRequirement(),
            "Medication in Dispensation contains Coding with Version.")
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpProvideDispensation> emlOrganizationCountryCodeMapsTo(
      Coding expectedCountryCode) {
    Predicate<EpaOpProvideDispensation> predicate =
        r -> {
          val actualCoding = r.getEpaOrganisation().getCountryCode();
          return actualCoding != null && actualCoding.equalsDeep(expectedCountryCode);
        };
    return new VerificationStep.StepBuilder<EpaOpProvideDispensation>(
            EmlAfos.A_29379.getRequirement(),
            "Organization.extension[ncpehCountryEx].valueCoding sollte dem übermittelten"
                + " countryCode entsprechen")
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpProvideDispensation>
      emlMedicationDispenseStatusIsCompleted() {
    Predicate<EpaOpProvideDispensation> predicate =
        r ->
            MedicationDispense.MedicationDispenseStatus.COMPLETED.equals(
                r.getEpaMedicationDispense().getStatus());
    return new VerificationStep.StepBuilder<EpaOpProvideDispensation>(
            EmlAfos.A_29378.getRequirement(),
            "MedicationDispense sollte mit Status COMPLETED an den ePA Medication Service"
                + " übermittelt werden")
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpProvideDispensation>
      emlMedicationDispenseCodeCodingVersionContains(String version) {
    Predicate<EpaOpProvideDispensation> predicate =
        dispensation ->
            dispensation.getEpaMedication().getCode().getCoding().stream()
                .filter(DeBasisProfilCodeSystem.ATC::matches)
                .allMatch(co -> Objects.equals(co.getVersion(), version));
    return new VerificationStep.StepBuilder<EpaOpProvideDispensation>(
            ErpBfd.B_FD_1698.getRequirement(),
            format("MedicationCodeCodingATC sollte als Version {0} enthalten", version))
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpProvideDispensation>
      emlMedicationIngredientAtcCodingVersionContains(int version) {
    Predicate<EpaOpProvideDispensation> predicate =
        dispensation -> {
          val atcCodings = dispensation.getEpaMedication().getIngredientAtcList();

          return !atcCodings.isEmpty()
              && atcCodings.stream()
                  .allMatch(
                      co ->
                          Optional.ofNullable(co)
                              .map(ATC::getVersion)
                              .flatMap(optionalVersion -> optionalVersion)
                              .map(String.valueOf(version)::equals)
                              .orElse(false));
        };

    return new VerificationStep.StepBuilder<EpaOpProvideDispensation>(
            ErpBfd.B_FD_1698.getRequirement(),
            format("MedicationIngredientCodingATC sollte als Version {0} enthalten", version))
        .predicate(predicate)
        .accept();
  }
}
