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
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunicationBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import java.time.LocalDate;
import java.time.Month;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Slf4j
class CommunicationBundleVerifierTest extends ErpFhirParsingTest {

  private final ErxCommunicationBundle dispRequest =
      parser.decode(
          ErxCommunicationBundle.class,
          ResourceLoader.readFileFromResource(
              "fhir/valid/erp/1.4.0/communicationbundle/dispRequest_1_4.json"));

  private final ErxCommunicationBundle replyBundle =
      parser.decode(
          ErxCommunicationBundle.class,
          ResourceLoader.readFileFromResource(
              "fhir/valid/erp/1.4.0/communicationbundle/reply_1_4.xml"));

  private final ErxCommunicationBundle shortValidComBundle =
      parser.decode(
          ErxCommunicationBundle.class,
          ResourceLoader.readFileFromResource(
              "fhir/valid/erp/1.4.0/communicationbundle/shortDispRequestBundle.json"));

  private final LocalDate marchThe18thIn25 = LocalDate.of(2025, Month.MARCH, 18);

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
            "01ebda7b-83e2-9fc0-471f-12c70443747c", ErpAfos.A_19520);
    step.apply(dispRequest);
  }

  @Test
  void shouldThrowWhileMissingId() {
    val step =
        CommunicationBundleVerifier.containsCommunicationWithId(
            "01ebbe1d-b718-ec48-f02d-73a051be0000", ErpAfos.A_19520);
    assertThrows(AssertionError.class, () -> step.apply(dispRequest));
  }

  @Test
  void shouldFindNoId() {
    val step =
        CommunicationBundleVerifier.containsNoCommunicationWithId(
            "01ebbe1d-b718-ec48-f02d-73a051b0000", ErpAfos.A_19520);
    step.apply(dispRequest);
  }

  @Test
  void shouldThrowWhileMissingNoId() {
    val step =
        CommunicationBundleVerifier.containsNoCommunicationWithId(
            "01ebda7b-83e2-9fc0-471f-12c70443747c", ErpAfos.A_19520);
    assertThrows(AssertionError.class, () -> step.apply(dispRequest));
  }

  @Test
  void shouldCountCorrect() {
    val step = CommunicationBundleVerifier.containsCountOfCommunication(5, ErpAfos.A_19521);
    step.apply(dispRequest);
  }

  @Test
  void shoulThrowWhileCounting() {
    val step = CommunicationBundleVerifier.containsCountOfCommunication(4, ErpAfos.A_19521);
    assertThrows(AssertionError.class, () -> step.apply(dispRequest));
  }

  @Test
  void shouldCompareRecipientCorrect() {
    val step =
        CommunicationBundleVerifier.containsOnlyRecipientWith(
            "3-SMC-B-Testkarte--883110000163973", ErpAfos.A_19521);
    step.apply(dispRequest);
  }

  @Test
  void shoulThrowWhileCompareRecipient() {
    val step = CommunicationBundleVerifier.containsOnlyRecipientWith("X110499478", ErpAfos.A_19521);
    assertThrows(AssertionError.class, () -> step.apply(dispRequest));
  }

  @Test
  void shouldCompareSenderAsKvnrCorrect() {
    val step = CommunicationBundleVerifier.onlySenderWith(KVNR.from("X110670787"));
    step.apply(dispRequest);
  }

  @Test
  void shouldCompareSenderAsTelematikIdCorrect() {
    val step =
        CommunicationBundleVerifier.onlySenderWith(
            TelematikID.from("3-SMC-B-Testkarte--883110000163973"));
    step.apply(replyBundle);
  }

  @Test
  void shouldCompareSenderCorrect() {
    val step = CommunicationBundleVerifier.onlySenderWith("X110670787", ErpAfos.A_19521);
    step.apply(dispRequest);
  }

  @Test
  void shouldThrowWhileCompareSender() {
    val step =
        CommunicationBundleVerifier.onlySenderWith("5-2-KH-APO-Waldesrand-01", ErpAfos.A_19521);
    assertThrows(AssertionError.class, () -> step.apply(dispRequest));
  }

  @Test
  void shouldVerifySenDateIsEqual() {
    val testDate = LocalDate.of(2025, Month.MARCH, 18);
    val step = sentDateIsEqual(testDate);
    assertDoesNotThrow(() -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldVerifySendDateIsNotEqualWithPredicateCorrect() {
    val testDate = LocalDate.of(2024, Month.JULY, 30);
    val step =
        verifySentDateWithPredicate(
            ld -> !ld.isEqual(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertDoesNotThrow(() -> step.apply(dispRequest));
  }

  @Test
  void shouldVerifySendDateEqualsWithPredicateCorrect() {

    val step =
        verifySentDateWithPredicate(
            ld -> ld.isEqual(marchThe18thIn25),
            "auch .." + marchThe18thIn25 + " braucht nen Verifier...");
    assertDoesNotThrow(() -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldVerifySendDateBeforeWithPredicateCorrect() {
    val testDate = LocalDate.of(2026, Month.MARCH, 24);
    val step =
        verifySentDateWithPredicate(
            ld -> ld.isBefore(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertDoesNotThrow(() -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldVerifySendDateIsAfterWithPredicateCorrect() {
    val step =
        verifySentDateWithPredicate(
            ld -> ld.isAfter(marchThe18thIn25.minusDays(1)),
            "auch .." + marchThe18thIn25.minusDays(1) + " braucht nen Verifier...");
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
    val testDate = LocalDate.of(2025, Month.MARCH, 18);
    val step =
        verifySentDateWithPredicate(
            ld -> ld.isBefore(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertThrows(AssertionError.class, () -> step.apply(shortValidComBundle));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 5, 10})
  void shouldThrowWhileVerifySendDateIsAfterWithPredicate(int plusDays) {

    val step =
        verifySentDateWithPredicate(
            ld -> ld.isAfter(marchThe18thIn25.plusDays(plusDays)),
            "auch .." + marchThe18thIn25 + " braucht nen Verifier...");
    assertThrows(AssertionError.class, () -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldVerifyReceivedDateEqualsWithPredicateCorrect() {
    val step =
        verifyReceivedDateWithPredicate(
            ld -> ld.isEqual(marchThe18thIn25),
            "auch .." + marchThe18thIn25 + " braucht nen Verifier...");
    assertDoesNotThrow(() -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldVerifyReceivedDateEqualsCorrect() {
    val step = receivedDateIsEqualTo(marchThe18thIn25);
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
    val testDate = LocalDate.of(2026, Month.MARCH, 23);
    val step =
        verifyReceivedDateWithPredicate(
            ld -> ld.isBefore(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertDoesNotThrow(() -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldThrowWhileVerifyReceivedDateBeforeWithPredicate1() {
    val step =
        verifyReceivedDateWithPredicate(
            ld -> ld.isBefore(marchThe18thIn25.minusDays(2)),
            "auch .." + marchThe18thIn25.minusDays(2) + " braucht nen Verifier...");
    assertThrows(AssertionError.class, () -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldThrowWhileVerifyReceivedDateBeforeWithPredicate2() {
    val testDate = LocalDate.of(2025, Month.MARCH, 18);
    val step =
        verifyReceivedDateWithPredicate(
            ld -> ld.isBefore(testDate), "auch .." + testDate + " braucht nen Verifier...");
    assertThrows(AssertionError.class, () -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldVerifyReceivedDatAfterWithPredicateCorrect() {
    val step =
        verifyReceivedDateWithPredicate(
            ld -> ld.isAfter(marchThe18thIn25.minusDays(4)),
            "auch .." + marchThe18thIn25 + " braucht nen Verifier...");
    assertDoesNotThrow(() -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldThrowWhileVerifyReceivedDatAfterWithPredicate1() {
    val step =
        verifyReceivedDateWithPredicate(
            ld -> ld.isAfter(marchThe18thIn25),
            "auch .." + marchThe18thIn25 + " braucht nen Verifier...");
    assertThrows(AssertionError.class, () -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldThrowWhileVerifyReceivedDateAfterWithPredicate2() {
    val step =
        verifyReceivedDateWithPredicate(
            ld -> ld.isAfter(marchThe18thIn25.plusDays(1)),
            "auch .." + marchThe18thIn25 + " braucht nen Verifier...");
    assertThrows(AssertionError.class, () -> step.apply(shortValidComBundle));
  }

  @Test
  void shouldVerifySendDateAscendSortedBundle() {
    val step = verifySentDateIsSortedAscend();
    assertDoesNotThrow(() -> step.apply(replyBundle));
  }

  @Test
  void shouldVerifySendDateAscendSortedBundle2() {
    val ascendSortedBundle =
        parser.decode(
            ErxCommunicationBundle.class,
            ResourceLoader.readFileFromResource(
                "fhir/valid/erp/1.4.0/communicationbundle/dispReqWithIdentifier.xml"));
    val step = containsOnlyIdentifierWith("160.000.006.993.636.55");
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

    val step = verifySentDateIsAfter(marchThe18thIn25.plusDays(5));
    assertThrows(AssertionError.class, () -> step.apply(shortValidComBundle));
  }
}
