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

package de.gematik.test.core.expectations.verifier;

import static de.gematik.test.core.expectations.verifier.CommunicationBundleVerifier.containsOnlyIdentifierWith;
import static de.gematik.test.core.expectations.verifier.CommunicationBundleVerifier.receivedDateIsEqualTo;
import static de.gematik.test.core.expectations.verifier.CommunicationBundleVerifier.sentDateIsEqual;
import static de.gematik.test.core.expectations.verifier.CommunicationBundleVerifier.verifyReceivedDateWithPredicate;
import static de.gematik.test.core.expectations.verifier.CommunicationBundleVerifier.verifySentDateIsAfter;
import static de.gematik.test.core.expectations.verifier.CommunicationBundleVerifier.verifySentDateIsBefore;
import static de.gematik.test.core.expectations.verifier.CommunicationBundleVerifier.verifySentDateIsSortedAscend;
import static de.gematik.test.core.expectations.verifier.CommunicationBundleVerifier.verifySentDateWithPredicate;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunicationBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.TelematikID;
import java.time.LocalDate;
import java.time.Month;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class CommunicationBundleVerifierTest extends ErpFhirParsingTest {

  private final ErxCommunicationBundle dipRequest =
      parser.decode(
          ErxCommunicationBundle.class,
          ResourceLoader.readFileFromResource(
              "fhir/valid/erp/1.2.0/communicationbundle/2c565050-eefb-4d14-b00d-640a6e0cc677.xml"));

  private final ErxCommunicationBundle replyBundle =
      parser.decode(
          ErxCommunicationBundle.class,
          ResourceLoader.readFileFromResource(
              "fhir/valid/erp/1.2.0/communicationbundle/b8031217-57ca-4ab8-8283-dadf28421e1f.json"));

  private final ErxCommunicationBundle shortValidComBundle =
      parser.decode(
          ErxCommunicationBundle.class,
          ResourceLoader.readFileFromResource(
              "fhir/valid/erp/1.2.0/communicationbundle/2c565050-eefb-4d14-b00d-640a6e0cc677.xml"));

  @BeforeEach
  void init() {
    CoverageReporter.getInstance().startTestcase("not needed");
  }

  @Test
  void shouldNotInstantiateUtilityClass() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(CommunicationBundleVerifier.class));
  }

  @Test
  void shouldFindId() {
    val step =
        CommunicationBundleVerifier.containsCommunicationWithId(
            "01ebbe1d-b718-ec48-f02d-73a051be9e23", ErpAfos.A_19520);
    step.apply(dipRequest);
  }

  @Test
  void shouldThrowWhileMissingId() {
    val step =
        CommunicationBundleVerifier.containsCommunicationWithId(
            "01ebbe1d-b718-ec48-f02d-73a051be0000", ErpAfos.A_19520);
    assertThrows(AssertionError.class, () -> step.apply(dipRequest));
  }

  @Test
  void shouldFindNoId() {
    val step =
        CommunicationBundleVerifier.containsNoCommunicationWithId(
            "01ebbe1d-b718-ec48-f02d-73a051b0000", ErpAfos.A_19520);
    step.apply(dipRequest);
  }

  @Test
  void shouldThrowWhileMissingNoId() {
    val step =
        CommunicationBundleVerifier.containsNoCommunicationWithId(
            "01ebbe1d-b718-ec48-f02d-73a051be9e23", ErpAfos.A_19520);
    assertThrows(AssertionError.class, () -> step.apply(dipRequest));
  }

  @Test
  void shouldCountCorrect() {
    val step = CommunicationBundleVerifier.containsCountOfCommunication(4, ErpAfos.A_19521);
    step.apply(dipRequest);
  }

  @Test
  void shoulThrowWhileCounting() {
    val step = CommunicationBundleVerifier.containsCountOfCommunication(5, ErpAfos.A_19521);
    assertThrows(AssertionError.class, () -> step.apply(dipRequest));
  }

  @Test
  void shouldCompareRecipientCorrect() {
    val step =
        CommunicationBundleVerifier.containsOnlyRecipientWith(
            "5-2-KH-APO-Waldesrand-01", ErpAfos.A_19521);
    step.apply(dipRequest);
  }

  @Test
  void shoulThrowWhileCompareRecipient() {
    val step = CommunicationBundleVerifier.containsOnlyRecipientWith("X110499478", ErpAfos.A_19521);
    assertThrows(AssertionError.class, () -> step.apply(dipRequest));
  }

  @Test
  void shouldCompareSenderAsKvnrCorrect() {
    val step = CommunicationBundleVerifier.onlySenderWith(KVNR.from("X110499478"));
    step.apply(dipRequest);
  }

  @Test
  void shouldCompareSenderAsTelematikIdCorrect() {
    val step =
        CommunicationBundleVerifier.onlySenderWith(TelematikID.from("5-2-KH-APO-Waldesrand-01"));
    step.apply(replyBundle);
  }

  @Test
  void shouldCompareSenderCorrect() {
    val step = CommunicationBundleVerifier.onlySenderWith("X110499478", ErpAfos.A_19521);
    step.apply(dipRequest);
  }

  @Test
  void shouldThrowWhileCompareSender() {
    val step =
        CommunicationBundleVerifier.onlySenderWith("5-2-KH-APO-Waldesrand-01", ErpAfos.A_19521);
    assertThrows(AssertionError.class, () -> step.apply(dipRequest));
  }

  @Test
  void shouldVerifySenDateIsEqual() {
    val testDate = LocalDate.of(2024, Month.MARCH, 23);
    val step = sentDateIsEqual(testDate);
    assertDoesNotThrow(() -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldVerifySendDateIsNotEqualWithPredicateCorrect() {
    val testDate = LocalDate.of(2024, Month.JULY, 30);
    val step =
        verifySentDateWithPredicate(
            ld -> !ld.isEqual(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertDoesNotThrow(() -> step.apply(dipRequest));
  }

  @Test
  void shouldVerifySendDateEqualsWithPredicateCorrect() {
    val testDate = LocalDate.of(2024, Month.MARCH, 23);
    val step =
        verifySentDateWithPredicate(
            ld -> ld.isEqual(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertDoesNotThrow(() -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldVerifySendDateBeforeWithPredicateCorrect() {
    val testDate = LocalDate.of(2024, Month.MARCH, 24);
    val step =
        verifySentDateWithPredicate(
            ld -> ld.isBefore(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertDoesNotThrow(() -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldVerifySendDateIsAfterWithPredicateCorrect() {
    val testDate = LocalDate.of(2024, Month.MARCH, 21);
    val step =
        verifySentDateWithPredicate(
            ld -> ld.isAfter(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertDoesNotThrow(() -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldThrowWhileVerifyEqualsSendDateWithPredicate() {
    val testDate = LocalDate.of(2024, Month.MARCH, 24);
    val step =
        verifySentDateWithPredicate(
            ld -> ld.isEqual(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertThrows(AssertionError.class, () -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldThrowWhileVerifyEqualsSendDateWithPredicate2() {
    val testDate = LocalDate.of(2024, Month.MARCH, 21);
    val step =
        verifySentDateWithPredicate(
            ld -> ld.isEqual(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertThrows(AssertionError.class, () -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldThrowWhileVerifySendDateBeforeWithPredicate() {
    val testDate = LocalDate.of(2024, Month.MARCH, 22);
    val step =
        verifySentDateWithPredicate(
            ld -> ld.isBefore(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertThrows(AssertionError.class, () -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldThrowWhileVerifySendDateBeforeWithPredicate2() {
    val testDate = LocalDate.of(2024, Month.MARCH, 21);
    val step =
        verifySentDateWithPredicate(
            ld -> ld.isBefore(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertThrows(AssertionError.class, () -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldThrowWhileVerifySendDateIsAfterWithPredicate() {
    val testDate = LocalDate.of(2024, Month.MARCH, 23);
    val step =
        verifySentDateWithPredicate(
            ld -> ld.isAfter(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertThrows(AssertionError.class, () -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldThrowWhileVerifySendDateIsAfterWithPredicate2() {
    val testDate = LocalDate.of(2024, Month.MARCH, 23);
    val step =
        verifySentDateWithPredicate(
            ld -> ld.isAfter(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertThrows(AssertionError.class, () -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldVerifyReceivedDateEqualsWithPredicateCorrect() {
    val testDate = LocalDate.of(2024, Month.MARCH, 22);
    val step =
        verifyReceivedDateWithPredicate(
            ld -> ld.isEqual(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertDoesNotThrow(() -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldVerifyReceivedDateEqualsCorrect() {
    val testDate = LocalDate.of(2024, Month.MARCH, 22);
    val step = receivedDateIsEqualTo(testDate);
    assertDoesNotThrow(() -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldThrowWhileVerifyReceivedDateEqualsWithPredicate1() {
    val testDate = LocalDate.of(2024, Month.MARCH, 21);
    val step =
        verifyReceivedDateWithPredicate(
            ld -> ld.isEqual(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertThrows(AssertionError.class, () -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldThrowWhileVerifyReceivedDateEqualsWithPredicate2() {
    val testDate = LocalDate.of(2024, Month.MARCH, 23);
    val step =
        verifyReceivedDateWithPredicate(
            ld -> ld.isEqual(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertThrows(AssertionError.class, () -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldVerifyReceivedDateBeforeWithPredicateCorrect() {
    val testDate = LocalDate.of(2024, Month.MARCH, 23);
    val step =
        verifyReceivedDateWithPredicate(
            ld -> ld.isBefore(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertDoesNotThrow(() -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldThrowWhileVerifyReceivedDateBeforeWithPredicate1() {
    val testDate = LocalDate.of(2024, Month.MARCH, 22);
    val step =
        verifyReceivedDateWithPredicate(
            ld -> ld.isBefore(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertThrows(AssertionError.class, () -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldThrowWhileVerifyReceivedDateBeforeWithPredicate2() {
    val testDate = LocalDate.of(2024, Month.MARCH, 21);
    val step =
        verifyReceivedDateWithPredicate(
            ld -> ld.isBefore(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertThrows(AssertionError.class, () -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldVerifyReceivedDatAfterWithPredicateCorrect() {
    val testDate = LocalDate.of(2024, Month.MARCH, 21);
    val step =
        verifyReceivedDateWithPredicate(
            ld -> ld.isAfter(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertDoesNotThrow(() -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldThrowWhileVerifyReceivedDatAfterWithPredicate1() {
    val testDate = LocalDate.of(2024, Month.MARCH, 22);
    val step =
        verifyReceivedDateWithPredicate(
            ld -> ld.isAfter(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertThrows(AssertionError.class, () -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldThrowWhileVerifyReceivedDateAfterWithPredicate2() {
    val testDate = LocalDate.of(2024, Month.MARCH, 23);
    val step =
        verifyReceivedDateWithPredicate(
            ld -> ld.isAfter(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertThrows(AssertionError.class, () -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldVerifySendDateAscendSortedBundle() {
    val ascendSortedBundle =
        parser.decode(
            ErxCommunicationBundle.class,
            ResourceLoader.readFileFromResource(
                "fhir/valid/erp/1.2.0/communicationbundle/2c565050-eefb-4d14-b00d-640a6e0cc677AscendSendDate.xml"));
    val step = verifySentDateIsSortedAscend();
    assertDoesNotThrow(() -> step.apply(ascendSortedBundle));
  }

  @Test
  void shouldVerifySendDateAscendSortedBundle2() {
    val ascendSortedBundle =
        parser.decode(
            ErxCommunicationBundle.class,
            ResourceLoader.readFileFromResource(
                "fhir/valid/erp/1.2.0/communicationbundle/2c565050-eefb-4d14-b00d-640a6e0cc677WithManipulattdIdentifier.xml"));
    val step = containsOnlyIdentifierWith("169.000.000.098.463.41");
    assertDoesNotThrow(() -> step.apply(ascendSortedBundle));
  }

  @Test
  void shouldVerifySentDateIsAfterCorrect() {
    val testDate = LocalDate.of(1, Month.MARCH, 21);
    val step = verifySentDateIsAfter(testDate);
    assertDoesNotThrow(() -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldVerifySentDateIsBeforeCorrect() {
    val testDate = LocalDate.of(2025, Month.MARCH, 23);
    val step = verifySentDateIsBefore(testDate);
    assertDoesNotThrow(() -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldThrowWhileVerifySentDateIsBefore() {
    val testDate = LocalDate.of(2024, Month.MARCH, 21);
    val step = verifySentDateIsBefore(testDate);
    assertThrows(AssertionError.class, () -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldThrowWhileVerifySentDateIsAfter() {
    val testDate = LocalDate.of(2025, Month.MARCH, 23);
    val step = verifySentDateIsAfter(testDate);
    assertThrows(AssertionError.class, () -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldThrowWhileVerifySentDateIsAfter2() {
    val testDate = LocalDate.of(2024, Month.MARCH, 23);
    val step = verifySentDateIsAfter(testDate);
    assertThrows(AssertionError.class, () -> step.apply(shortValidComBundle));
  }
}
