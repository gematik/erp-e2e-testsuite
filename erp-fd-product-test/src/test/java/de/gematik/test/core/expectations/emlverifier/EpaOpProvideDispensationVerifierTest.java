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

package de.gematik.test.core.expectations.emlverifier;

import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvideDispensationVerifier.emlDispensationIdIsEqualTo;
import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvideDispensationVerifier.emlHandedOverIsEqualTo;
import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvideDispensationVerifier.emlMedicationDispenseMapsTo;
import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvideDispensationVerifier.emlMedicationMapsTo;
import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvideDispensationVerifier.emlOrganisationHasSMCBTelematikId;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvideDispensationVerifier;
import de.gematik.test.erezept.eml.fhir.EpaFhirFactory;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvideDispensation;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBuilder;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.time.LocalDate;
import java.time.Month;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EpaOpProvideDispensationVerifierTest extends ErpFhirBuildingTest {
  private static final Date testDate_22_01_2025 =
      DateConverter.getInstance().localDateToDate(LocalDate.of(2025, Month.JANUARY, 22));
  private static EpaOpProvideDispensation validEpaOpProvideDispensation;

  @BeforeAll
  static void setup() {
    CoverageReporter.getInstance().startTestcase("not needed");
    val fhir = EpaFhirFactory.create();
    validEpaOpProvideDispensation =
        fhir.decode(
            EpaOpProvideDispensation.class,
            ResourceLoader.readFileFromResource(
                "fhir/valid/parameters/Parameters-example-epa-op-provide-dispensation-erp-input-parameters-1.json"));
  }

  private static GemErpMedication getMedication() {
    val medication = new GemErpMedication();
    medication
        .getCode()
        .getCoding()
        .add(
            DeBasisProfilCodeSystem.PZN
                .asCoding("10019621")
                .setDisplay("IBU-ratiopharm 400mg akut Schmerztabletten"));
    medication
        .getCode()
        .getCoding()
        .add(new Coding(DeBasisProfilCodeSystem.ATC.getCanonicalUrl(), "M01AE01", "Ibuprofen"));
    return medication;
  }

  @Test
  void shouldVerifyPrescriptionIdInEmlDispensationCorrect() {
    val step = emlDispensationIdIsEqualTo(PrescriptionId.from("160.153.303.257.459"));
    assertDoesNotThrow(() -> step.apply(validEpaOpProvideDispensation));
  }

  @Test
  void shouldThrowWhileVerifyPrescriptionIdInEmlDispensation() {
    val step = emlDispensationIdIsEqualTo(PrescriptionId.from("123-FailID"));
    assertThrows(AssertionError.class, () -> step.apply(validEpaOpProvideDispensation));
  }

  @Test
  void shouldVerifyWhenHandedOverInEmlDispensationCorrect() {
    val step = emlHandedOverIsEqualTo(testDate_22_01_2025);
    assertDoesNotThrow(() -> step.apply(validEpaOpProvideDispensation));
  }

  @Test
  void shouldThrowWhileVerifyWhenHandedOverInEmlDispensation() {
    val testDate = DateConverter.getInstance().localDateToDate(LocalDate.of(2020, Month.JULY, 31));
    val step = emlHandedOverIsEqualTo(testDate);
    assertThrows(AssertionError.class, () -> step.apply(validEpaOpProvideDispensation));
  }

  @Test
  void shouldVerifyMedicationInEmlDispensationCorrect() {
    val medication = new KbvErpMedication();
    medication
        .getCode()
        .getCoding()
        .add(
            DeBasisProfilCodeSystem.PZN
                .asCoding("10019621")
                .setDisplay("IBU-ratiopharm 400mg akut Schmerztabletten"));
    medication
        .getCode()
        .getCoding()
        .add(new Coding(DeBasisProfilCodeSystem.ATC.getCanonicalUrl(), "M01AE01", "Ibuprofen"));
    val step = emlMedicationMapsTo(medication);
    assertDoesNotThrow(() -> step.apply(validEpaOpProvideDispensation));
  }

  @Test
  void shouldThrowWhileVerifyMedicationInEmlDispensationCausedByWrongPznValue() {
    val medication = new KbvErpMedication();
    medication.getCode().getCoding().add(DeBasisProfilCodeSystem.PZN.asCoding("1111111"));

    medication
        .getCode()
        .getCoding()
        .add(new Coding(DeBasisProfilCodeSystem.ATC.getCanonicalUrl(), "M01AE01", "Ibuprofen"));
    val step = emlMedicationMapsTo(medication);
    assertThrows(AssertionError.class, () -> step.apply(validEpaOpProvideDispensation));
  }

  @Test
  void shouldThrowWhileVerifyMedicationInEmlDispensationCausedByWrongAtcValue() {
    val medication = new KbvErpMedication();
    medication
        .getCode()
        .getCoding()
        .add(
            DeBasisProfilCodeSystem.PZN
                .asCoding("10019621")
                .setDisplay("IBU-ratiopharm 400mg akut Schmerztabletten"));
    medication
        .getCode()
        .getCoding()
        .add(new Coding(DeBasisProfilCodeSystem.ATC.getCanonicalUrl(), "M1234567", "Ibuprofen"));
    val step = emlMedicationMapsTo(medication);
    assertThrows(AssertionError.class, () -> step.apply(validEpaOpProvideDispensation));
  }

  @Test
  void shouldThrowWhileVerifyMedicationInEmlDispensationCausedByWrongAskValue() {
    val medication = new KbvErpMedication();
    medication
        .getCode()
        .getCoding()
        .add(
            DeBasisProfilCodeSystem.PZN
                .asCoding("10019621")
                .setDisplay("IBU-ratiopharm 400mg akut Schmerztabletten"));
    medication.getCode().getCoding().add(DeBasisProfilCodeSystem.ASK.asCoding("M1234567"));
    val step = emlMedicationMapsTo(medication);
    assertThrows(AssertionError.class, () -> step.apply(validEpaOpProvideDispensation));
  }

  @Test
  void shouldThrowWhileVerifyMedicationInEmlDispensationCausedByWrongPznSystem() {
    val medication = new KbvErpMedication();
    val system = DeBasisProfilCodeSystem.PZN.getCanonicalUrl().replaceFirst("fhir", "FooBar");
    medication.getCode().getCoding().add(new Coding(system, "123456789", "Ibuprofen"));
    val step = emlMedicationMapsTo(medication);

    assertThrows(AssertionError.class, () -> step.apply(validEpaOpProvideDispensation));
  }

  @Test
  void shouldThrowWhileVerifyMedicationInEmlDispensationCausedByWrongAtcSystem() {
    val medication = new KbvErpMedication();
    val system = DeBasisProfilCodeSystem.ATC.getCanonicalUrl().replaceFirst("fhir", "FooBar");
    medication
        .getCode()
        .getCoding()
        .add(
            DeBasisProfilCodeSystem.PZN
                .asCoding("10019621")
                .setDisplay("IBU-ratiopharm 400mg akut Schmerztabletten"));
    medication.getCode().getCoding().add(new Coding(system, "123456789", "Ibuprofen"));
    val step = emlMedicationMapsTo(medication);
    assertThrows(AssertionError.class, () -> step.apply(validEpaOpProvideDispensation));
  }

  @Test
  void shouldThrowWhileVerifyMedicationInEmlDispensationCausedByWrongAskSystem() {
    val medication = new KbvErpMedication();
    val system = DeBasisProfilCodeSystem.ASK.getCanonicalUrl().replaceFirst("fhir", "FooBar");
    medication
        .getCode()
        .getCoding()
        .add(
            DeBasisProfilCodeSystem.PZN
                .asCoding("10019621")
                .setDisplay("IBU-ratiopharm 400mg akut Schmerztabletten"));
    medication.getCode().getCoding().add(new Coding(system, "M01AE21", "Ibuprofen"));
    val step = emlMedicationMapsTo(medication);
    assertThrows(AssertionError.class, () -> step.apply(validEpaOpProvideDispensation));
  }

  @Test
  void shouldThrowWhileVerifyMedicationInEmlDispensationCausedByTooManyMedications() {
    val medication = new KbvErpMedication();
    medication
        .getCode()
        .getCoding()
        .add(
            DeBasisProfilCodeSystem.PZN
                .asCoding("10019621")
                .setDisplay("IBU-ratiopharm 400mg akut Schmerztabletten"));
    medication
        .getCode()
        .getCoding()
        .add(
            DeBasisProfilCodeSystem.ASK
                .asCoding("10019621")
                .setDisplay("IBU-ratiopharm 400mg akut Schmerztabletten"));
    medication
        .getCode()
        .getCoding()
        .add(new Coding(DeBasisProfilCodeSystem.ATC.getCanonicalUrl(), "M01AE01", "Ibuprofen"));
    val step = emlMedicationMapsTo(medication);
    assertThrows(AssertionError.class, () -> step.apply(validEpaOpProvideDispensation));
  }

  @Test
  void shouldThrowWhileVerifyMedicationInEmlDispensationCausedByNotEnoughMedications() {
    val medication = new KbvErpMedication();
    medication
        .getCode()
        .getCoding()
        .add(new Coding(DeBasisProfilCodeSystem.ATC.getCanonicalUrl(), "M01AE01", "Ibuprofen"));
    val step = emlMedicationMapsTo(medication);
    assertThrows(AssertionError.class, () -> step.apply(validEpaOpProvideDispensation));
  }

  @Test
  void shouldVerifyMedicationDispenseInEmlDispensationCorrect() {
    val medicDsp =
        ErxMedicationDispenseBuilder.forKvnr(KVNR.from("X110411319"))
            .dosageInstruction("1-0-0-0")
            .whenHandedOver(testDate_22_01_2025)
            .wasSubstituted(false)
            .medication(getMedication())
            .performerId("urn:uuid:151f1697-7512-4e21-9466-1b75207475d8")
            .prescriptionId("160.153.303.257.459")
            .status(MedicationDispense.MedicationDispenseStatus.COMPLETED)
            .medication(getMedication())
            .build();

    val step = emlMedicationDispenseMapsTo(medicDsp);
    assertDoesNotThrow(() -> step.apply(validEpaOpProvideDispensation));
  }

  @Test
  void shouldThrowWhileVerifyMedicationDispenseInEmlDispensationWithWrongKVNR() {
    val medicDsp =
        ErxMedicationDispenseBuilder.forKvnr(KVNR.from("X1111111111"))
            .dosageInstruction("1-0-0-0")
            .whenHandedOver(testDate_22_01_2025)
            .wasSubstituted(false)
            .status(MedicationDispense.MedicationDispenseStatus.COMPLETED)
            .medication(getMedication())
            .performerId("urn:uuid:151f1697-7512-4e21-9466-1b75207475d8")
            .prescriptionId("160.153.303.257.459")
            .build();

    val step = emlMedicationDispenseMapsTo(medicDsp);
    assertThrows(AssertionError.class, () -> step.apply(validEpaOpProvideDispensation));
  }

  @Test
  void shouldThrowWhileVerifyMedicationDispenseInEmlDispensationWithWrongDosageInstruction() {
    val medicDsp =
        ErxMedicationDispenseBuilder.forKvnr(KVNR.from("X110411319"))
            .dosageInstruction("1-0-1-6")
            .whenHandedOver(testDate_22_01_2025)
            .wasSubstituted(false)
            .status(MedicationDispense.MedicationDispenseStatus.COMPLETED)
            .medication(getMedication())
            .performerId("urn:uuid:151f1697-7512-4e21-9466-1b75207475d8")
            .prescriptionId("160.153.303.257.459")
            .build();

    val step = emlMedicationDispenseMapsTo(medicDsp);
    assertThrows(AssertionError.class, () -> step.apply(validEpaOpProvideDispensation));
  }

  @Test
  void shouldThrowWhileVerifyMedicationDispenseInEmlDispensationWithWrongHandedOver() {
    val medicDsp =
        ErxMedicationDispenseBuilder.forKvnr(KVNR.from("X110411319"))
            .dosageInstruction("1-0-0-0")
            .whenHandedOver(new Date(1))
            .wasSubstituted(false)
            .status(MedicationDispense.MedicationDispenseStatus.COMPLETED)
            .medication(getMedication())
            .performerId("urn:uuid:151f1697-7512-4e21-9466-1b75207475d8")
            .prescriptionId("160.153.303.257.459")
            .build();

    val step = emlMedicationDispenseMapsTo(medicDsp);
    assertThrows(AssertionError.class, () -> step.apply(validEpaOpProvideDispensation));
  }

  @Test
  void shouldThrowWhileVerifyMedicationDispenseInEmlDispensationWithWrongSubstitution() {
    val medicDsp =
        ErxMedicationDispenseBuilder.forKvnr(KVNR.from("X110411319"))
            .dosageInstruction("1-0-0-0")
            .whenHandedOver(testDate_22_01_2025)
            .wasSubstituted(true)
            .status(MedicationDispense.MedicationDispenseStatus.COMPLETED)
            .medication(getMedication())
            .performerId("urn:uuid:151f1697-7512-4e21-9466-1b75207475d8")
            .prescriptionId("160.153.303.257.459")
            .build();

    val step = emlMedicationDispenseMapsTo(medicDsp);
    assertThrows(AssertionError.class, () -> step.apply(validEpaOpProvideDispensation));
  }

  @Test
  void shouldThrowWhileVerifyMedicationDispenseInEmlDispensationWithWrongStatus() {
    val medicDsp =
        ErxMedicationDispenseBuilder.forKvnr(KVNR.from("X110411319"))
            .dosageInstruction("1-0-0-0")
            .whenHandedOver(testDate_22_01_2025)
            .wasSubstituted(false)
            .status(MedicationDispense.MedicationDispenseStatus.PREPARATION)
            .medication(getMedication())
            .performerId("urn:uuid:151f1697-7512-4e21-9466-1b75207475d8")
            .prescriptionId("160.153.303.257.459")
            .build();

    val step = emlMedicationDispenseMapsTo(medicDsp);
    assertThrows(AssertionError.class, () -> step.apply(validEpaOpProvideDispensation));
  }

  @Test
  void shouldVerifyOrganisationSmcbIdInEmlDispensationCorrect() {
    val step = emlOrganisationHasSMCBTelematikId(TelematikID.from("9-2.58.00000040"));
    assertDoesNotThrow(() -> step.apply(validEpaOpProvideDispensation));
  }

  @Test
  void shouldThrowWhileVerifyOrganisationSmcbIdInEmlDispensation() {
    val step = emlOrganisationHasSMCBTelematikId(TelematikID.from("91-2.123.123456789"));
    assertThrows(AssertionError.class, () -> step.apply(validEpaOpProvideDispensation));
  }

  @Test
  void EmlDoesNotContainAnythingWithEmptyList() {
    List<EpaOpProvideDispensation> emptyList = List.of();

    VerificationStep<List<EpaOpProvideDispensation>> verificationStep =
        EpaOpProvideDispensationVerifier.emlDoesNotContainAnything();

    Predicate<List<EpaOpProvideDispensation>> predicate = verificationStep.getPredicate();

    assertTrue(predicate.test(emptyList), "Expected verification step to pass for an empty list");
  }

  @Test
  void EmlDoesNotContainAnythingWithNonEmptyList() {
    List<EpaOpProvideDispensation> nonEmptyList =
        List.of(new EpaOpProvideDispensation(), new EpaOpProvideDispensation());

    VerificationStep<List<EpaOpProvideDispensation>> verificationStep =
        EpaOpProvideDispensationVerifier.emlDoesNotContainAnything();

    Predicate<List<EpaOpProvideDispensation>> predicate = verificationStep.getPredicate();

    assertFalse(
        predicate.test(nonEmptyList), "Expected verification step to fail for a non-empty list");
  }
}
