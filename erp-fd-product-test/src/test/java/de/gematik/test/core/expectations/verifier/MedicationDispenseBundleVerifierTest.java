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

package de.gematik.test.core.expectations.verifier;

import static de.gematik.test.core.expectations.verifier.MedicationDispenseBundleVerifier.*;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseFaker;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.values.TelematikID;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MedicationDispenseBundleVerifierTest extends ParsingTest {

  ErxMedicationDispenseBundle validMedDisp =
      parser.decode(
          ErxMedicationDispenseBundle.class,
          ResourceLoader.readFileFromResource(
              "fhir/valid/erp/1.2.0/medicationdispensebundle/be2a759d-a37e-4355-9600-91df7208b66e_EqualWhenPrepared.json"));

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
    val testDate = LocalDate.of(2024, Month.JULY, 31);
    val step =
        verifyWhenHandedOverWithPredicate(
            ld -> !ld.isEqual(testDate), "is halt so..." + testDate + "enthalten");
    assertDoesNotThrow(() -> step.apply(validMedDisp));
  }

  @Test
  void shouldVerifyWhenHandedOverCorrect2() {
    val testDate = LocalDate.of(2024, Month.JULY, 30);
    val step =
        verifyWhenHandedOverWithPredicate(
            ld -> ld.isEqual(testDate), "is halt so..." + testDate + "enthalten");
    assertDoesNotThrow(() -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileVerifyWhenHandedOver() {
    val testDate = LocalDate.of(2024, Month.JULY, 30);
    val step =
        verifyWhenHandedOverWithPredicate(
            ld -> !ld.isEqual(testDate), "is halt so..." + testDate + "enthalten");
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldVerifyWhenPreparedIsNotEqualCorrect() {
    val testDate = LocalDate.of(2024, Month.JULY, 30);
    val step =
        verifyWhenPreparedWithPredicate(
            ld -> !ld.isEqual(testDate), "is halt so..." + testDate + "enthalten");
    assertDoesNotThrow(() -> step.apply(validMedDisp));
  }

  @Test
  void shouldVerifyWhenPreparedIsNotEqualsCorrect() {
    val testDate = LocalDate.of(2024, Month.JULY, 31);
    val step =
        verifyWhenPreparedWithPredicate(
            ld -> !ld.isEqual(testDate), "is halt so..." + testDate + "enthalten");
    assertDoesNotThrow(() -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileVerifyWhenPreparedIsNotEquals() {
    val testDate = LocalDate.of(2024, Month.JULY, 29);
    val step =
        verifyWhenPreparedWithPredicate(
            ld -> !ld.isEqual(testDate), "is halt so..." + testDate + "enthalten");
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileVerifyWhenPreparedIsEquals() {
    val testDate = LocalDate.of(2024, Month.JULY, 31);
    val step =
        verifyWhenPreparedWithPredicate(
            ld -> ld.isEqual(testDate), "is halt so..." + testDate + "enthalten");
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldVerifyWhenPreparedIsBeforeCorrect() {
    val testDate = LocalDate.of(2024, Month.JULY, 30);
    val step = verifyWhenPreparedIsBefore(testDate);
    assertDoesNotThrow(() -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileVerifyWhenPreparedIsBeforeMatchingTheDate() {
    val testDate = LocalDate.of(2024, Month.JULY, 29);
    val step = verifyWhenPreparedIsBefore(testDate);
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileVerifyWhenPreparedIsBefore() {
    val testDate = LocalDate.of(2024, Month.JULY, 22);
    val step = verifyWhenPreparedIsBefore(testDate);
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldVerifyPerformerIdsCorrect() {
    val step = verifyAllPerformerIdsAre(TelematikID.from("5-2-KH-APO-Waldesrand-01"));
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
                    "fhir/valid/erp/1.2.0/medicationdispensebundle/9145d0d0-7b77-483f-ad89-cd9d34fc1f08.xml"));
    val step = verifyWhenHandedOverIsSortedSerialAscend();
    assertDoesNotThrow(() -> step.apply(medDispBundle));
  }

  @Test
  void shouldThrowsWhileVerifyHandedOverIsSerialAscendSorted() {
    val medDispBundle =
        validMedDisp =
            parser.decode(
                ErxMedicationDispenseBundle.class,
                ResourceLoader.readFileFromResource(
                    "fhir/valid/erp/1.2.0/medicationdispensebundle/9145d0d0-7b77-483f-ad89-cd9d34fc1f08.json"));
    val step = verifyWhenHandedOverIsSortedSerialAscend();
    assertThrows(AssertionError.class, () -> step.apply(medDispBundle));
  }

  @Test
  void shouldVerifyWhenHandedOverIsBeforeCorrect() {
    val testDate = LocalDate.of(2024, Month.JULY, 31);
    val step = verifyWhenHandedOverIsBefore(testDate);
    assertDoesNotThrow(() -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileVerifyWhenHandedOverIsBefore() {
    val testDate = LocalDate.of(2024, Month.JULY, 30);
    val step = verifyWhenHandedOverIsBefore(testDate);
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileVerifyWhenHandedOverIsBefore2() {
    val testDate = LocalDate.of(2024, Month.JULY, 29);
    val step = verifyWhenHandedOverIsBefore(testDate);
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldVerifyWhenHandedOverIsEqualCorrect() {
    val testDate = LocalDate.of(2024, Month.JULY, 30);
    val step = verifyWhenHandedOverIsEqual(testDate);
    assertDoesNotThrow(() -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileVerifyWhenHandedOverIsEqual() {
    val testDate = LocalDate.of(2024, Month.JULY, 31);
    val step = verifyWhenHandedOverIsEqual(testDate);
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileVerifyWhenHandedOverIsEqual2() {
    val testDate = LocalDate.of(2024, Month.JULY, 29);
    val step = verifyWhenHandedOverIsEqual(testDate);
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldVerifyWhenHandedOverIsAfterCorrect() {
    val testDate = LocalDate.of(2024, Month.JULY, 20);
    val step = verifyWhenHandedOverIsAfter(testDate);
    assertDoesNotThrow(() -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileVerifyWhenHandedOverIsAfter() {
    val testDate = LocalDate.of(2024, Month.JULY, 30);
    val step = verifyWhenHandedOverIsAfter(testDate);
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileVerifyWhenHandedOverIsAfter2() {
    val testDate = LocalDate.of(2024, Month.JULY, 31);
    val step = verifyWhenHandedOverIsAfter(testDate);
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldDetectCorrectCountOfContainedMedicationDispenses() {
    val step = verifyCountOfContainedMedication(5);
    assertDoesNotThrow(() -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileDetectingCorrectCountOfContainedMedicationDispenses() {
    val step = verifyCountOfContainedMedication(3);
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldDetectCorrectContainedMedicationDispenses() {
    val step = verifyContainedMedicationDispensePZNs(validMedDisp.getMedicationDispenses());
    assertDoesNotThrow(() -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileDetectingCorrectContainedMedicationDispenses() {
    val partOfMedDisp = validMedDisp.getMedicationDispenses().subList(0, 4);
    val step = verifyContainedMedicationDispensePZNs(partOfMedDisp);
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }

  @Test
  void shouldThrowWhileDetectingAMissingContainedMedicationDispenses() {
    val medDispList = List.of(ErxMedicationDispenseFaker.builder().withPzn("123456789").fake());

    val step = verifyContainedMedicationDispensePZNs(medDispList);
    assertThrows(AssertionError.class, () -> step.apply(validMedDisp));
  }
}
