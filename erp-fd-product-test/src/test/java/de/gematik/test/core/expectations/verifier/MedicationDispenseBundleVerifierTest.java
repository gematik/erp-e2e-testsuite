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

package de.gematik.test.core.expectations.verifier;

import static de.gematik.test.core.expectations.verifier.MedicationDispenseBundleVerifier.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.fhir.builder.eu.EuMedicationDispenseFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvPractitionerRoleBuilder;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.time.LocalDate;
import java.time.Month;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MedicationDispenseBundleVerifierTest extends ErpFhirParsingTest {

  private final LocalDate testDate = LocalDate.of(2025, Month.SEPTEMBER, 6);
  ErxMedicationDispenseBundle validMedDisp =
      parser.decode(
          ErxMedicationDispenseBundle.class,
          ResourceLoader.readFileFromResource(
              "fhir/valid/erp/1.4.0/medicationdispensebundle/Bundle-MultipleMedicationDispenseBundle.json"));
  ErxMedicationDispenseBundle simpleEuMedDispBundle =
      parser.decode(
          ErxMedicationDispenseBundle.class,
          ResourceLoader.readFileFromResource(
              "fhir/valid/eu/09_response_get_multiple_medication_dispense.json"));

  @BeforeEach
  void init() {
    CoverageReporter.getInstance().startTestcase("not needed");
  }

  @Test
  void shouldNotInstantiateUtilityClass() {
    assertTrue(
        PrivateConstructorsUtil.isUtilityConstructor(MedicationDispenseBundleVerifier.class));
  }

  @Test
  void shouldVerifyWhenHandedOverCorrect() {
    val step =
        verifyWhenHandedOverWithPredicate(
            ld -> !ld.isEqual(testDate.minusMonths(3)), "is halt so..." + testDate + "enthalten");
    assertDoesNotThrow(() -> step.apply(validMedDisp));
  }

  @Test
  void shouldVerifyWhenHandedOverCorrect2() {
    val step =
        verifyWhenHandedOverWithPredicate(
            ld -> ld.isEqual(testDate), "is halt so..." + testDate + "enthalten");
    assertDoesNotThrow(() -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileVerifyWhenHandedOver() {
    val step =
        verifyWhenHandedOverWithPredicate(
            ld -> !ld.isEqual(testDate), "is halt so..." + testDate + "enthalten");
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldVerifyWhenPreparedIsNotEqualCorrect() {
    val step =
        verifyWhenPreparedWithPredicate(
            ld -> !ld.isEqual(testDate.minusDays(2)), "is halt so..." + testDate + "enthalten");
    assertDoesNotThrow(() -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileVerifyWhenPreparedIsNotEquals() {
    val step =
        verifyWhenPreparedWithPredicate(
            ld -> !ld.isEqual(testDate), "is halt so..." + testDate + "enthalten");
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileVerifyWhenPreparedIsEquals() {
    val step =
        verifyWhenPreparedWithPredicate(
            ld -> ld.isEqual(testDate.plusDays(2)), "is halt so..." + testDate + "enthalten");
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldVerifyWhenPreparedIsBeforeCorrect() {
    val step = verifyWhenPreparedIsBefore(testDate.plusDays(2));
    assertDoesNotThrow(() -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileVerifyWhenPreparedIsBeforeMatchingTheDate() {
    val step = verifyWhenPreparedIsBefore(testDate);
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldVerifyPerformerIdsCorrect() {
    val step = verifyAllPerformerIdsAre(TelematikID.from("3-SMC-B-Testkarte-883110000095957"));
    assertDoesNotThrow(() -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileVerifyPerformerIds() {
    val step = verifyAllPerformerIdsAre(TelematikID.from("Völlig_falsche-Id"));
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldVerifyHandedOverIsSerialAscendSorted() {
    val medDispBundle =
        validMedDisp =
            parser.decode(
                ErxMedicationDispenseBundle.class,
                ResourceLoader.readFileFromResource(
                    "fhir/valid/erp/1.4.0/medicationdispensebundle/Bundle-MultipleMedicationDispenseBundle_HandedOverDateAscending.json"));
    val step = verifyWhenHandedOverIsSortedSerialAscend();
    assertDoesNotThrow(() -> step.apply(medDispBundle));
  }

  @Test
  void shouldThrowsWhileVerifyHandedOverIsSerialAscendSorted() {
    val medDispBundle =
        parser.decode(
            ErxMedicationDispenseBundle.class,
            ResourceLoader.readFileFromResource(
                "fhir/valid/erp/1.4.0/medicationdispensebundle/Bundle-MultipleMedicationDispenseBundle_HandedOverDateDescending.json"));
    val step = verifyWhenHandedOverIsSortedSerialAscend();
    assertThrows(AssertionError.class, () -> step.apply(medDispBundle));
  }

  @Test
  void shouldPassWhileVerifyingEuMedDispense() {
    val singleBundle =
        parser.decode(
            ErxMedicationDispenseBundle.class,
            ResourceLoader.readFileFromResource(
                "fhir/valid/eu/08_response_get_single_medicationdispense.json"));
    val step =
        containsEuPrescriptionAndDispensation(
            PrescriptionId.from("160.000.000.000.000.01"), ErpAfos.A_27070);
    assertDoesNotThrow(() -> step.apply(singleBundle));
  }

  @Test
  void shouldThrowWhileVerifyNonEuDispensation() {
    val step = containsEuPrescriptionAndDispensation(PrescriptionId.random(), ErpAfos.A_10406);
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldVerifyWhenHandedOverIsBeforeCorrect() {
    val step = verifyWhenHandedOverIsBefore(testDate.plusDays(2));
    assertDoesNotThrow(() -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileVerifyWhenHandedOverIsBefore() {
    val step = verifyWhenHandedOverIsBefore(testDate);
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileVerifyWhenHandedOverIsBefore2() {
    val step = verifyWhenHandedOverIsBefore(testDate.minusDays(2));
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldVerifyWhenHandedOverIsEqualCorrect() {
    val step = verifyWhenHandedOverIsEqual(testDate);
    assertDoesNotThrow(() -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileVerifyWhenHandedOverIsEqual() {
    val step = verifyWhenHandedOverIsEqual(testDate.plusDays(1));
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileVerifyWhenHandedOverIsEqual2() {
    val step = verifyWhenHandedOverIsEqual(testDate.minusDays(1));
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldVerifyWhenHandedOverIsAfterCorrect() {
    val step = verifyWhenHandedOverIsAfter(testDate.minusDays(2));
    assertDoesNotThrow(() -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileVerifyWhenHandedOverIsAfter() {
    val step = verifyWhenHandedOverIsAfter(testDate);
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileVerifyWhenHandedOverIsAfter2() {
    val step = verifyWhenHandedOverIsAfter(testDate.plusDays(1));
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldDetectCorrectCountOfContainedMedicationDispenses() {
    val step = verifyCountOfContainedMedication(2);
    assertDoesNotThrow(() -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileDetectingCorrectCountOfContainedMedicationDispenses() {
    val step = verifyCountOfContainedMedication(3);
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  private ErxMedicationDispenseBundle simpleMedDispBundle =
      parser.decode(
          ErxMedicationDispenseBundle.class,
          ResourceLoader.readFileFromResource(
              "fhir/valid/erp/1.4.0/medicationdispensebundle/Bundle-SimpleMedicationDispenseBundle.json"));

  @Test
  void shouldDetectCorrectContainedMedicationDispensesForNewProfiles() {

    val step = containsAllPZNsForNewProfiles(simpleMedDispBundle.getMedications());
    assertDoesNotThrow(() -> step.apply(simpleMedDispBundle));
  }

  @Test
  void shouldDetectContainedEuPractitionerData() {
    val step = containsEuPractitionerData(PrescriptionId.from("160.000.000.000.000.01"));
    assertDoesNotThrow(() -> step.apply(simpleEuMedDispBundle));
  }

  @Test
  void shouldThrowWhileDetectContainedEuPractitionerData() {
    val step = containsEuPractitionerData(PrescriptionId.from("000.000.000.000.000.01"));
    assertThrows(AssertionError.class, () -> step.apply(simpleEuMedDispBundle));
  }

  @Test
  void shouldDetectContainedEuPractitionerRole() {
    val medDsp =
        simpleEuMedDispBundle
            .getEuMedicationDispenseBy(PrescriptionId.from("160.000.000.000.000.01"))
            .stream()
            .findFirst()
            .orElseThrow();
    val step = containsEuPractitionerRoleRelateTo(medDsp);
    assertDoesNotThrow(() -> step.apply(simpleEuMedDispBundle));
  }

  @Test
  void shouldThrowWhileDetectContainedEuPractitionerRole() {
    val medDsp =
        EuMedicationDispenseFaker.builder()
            .withPrescriptionId(PrescriptionId.from("000.000.000.000.000.01"))
            .fake();
    medDsp.getPerformer().stream()
        .findFirst()
        .orElseThrow()
        .getActor()
        .setReference("PractitionerRole/0000000-276b-436d-a9ea-9dd5e042637b");
    val step = containsEuPractitionerRoleRelateTo(medDsp);
    assertThrows(AssertionError.class, () -> step.apply(simpleEuMedDispBundle));
  }

  @Test
  void shouldDetectEuOrganization() {
    val praRole =
        new KbvPractitionerRoleBuilder().setId("ebe39d92-276b-436d-a9ea-9dd5e042637b").build();
    val referedDispensation = EuMedicationDispenseFaker.builder().withPerformer(praRole).fake();

    val step = containsEuOrganisationDataRelatesTo(referedDispensation);
    assertDoesNotThrow(() -> step.apply(simpleEuMedDispBundle));
  }

  @Test
  void shouldThrowWhileDetectEuOrganization() {
    val praRole =
        new KbvPractitionerRoleBuilder().setId("ebe39d92-276b-436d-a9ea-00000000").build();

    val referedDispensation = EuMedicationDispenseFaker.builder().withPerformer(praRole).fake();

    val step = containsEuOrganisationDataRelatesTo(referedDispensation);
    assertThrows(AssertionError.class, () -> step.apply(simpleEuMedDispBundle));
  }

  @Test
  void shouldDetectContainedErxMedicationAndGemDispense() {
    val step =
        containsErxMedicationAndGemMedDispense(PrescriptionId.from("160.000.000.000.000.01"));
    assertDoesNotThrow(() -> step.apply(simpleMedDispBundle));
  }

  @Test
  void shoulThrowWhiledDetectContainedErxMedicationAndGemDispense() {
    val step =
        containsErxMedicationAndGemMedDispense(PrescriptionId.from("000.000.000.000.000.01"));
    assertThrows(AssertionError.class, () -> step.apply(simpleMedDispBundle));
  }
}
