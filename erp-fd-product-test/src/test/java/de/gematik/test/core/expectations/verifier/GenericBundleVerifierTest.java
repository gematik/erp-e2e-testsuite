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

import static de.gematik.test.core.expectations.verifier.AuditEventVerifier.hasAuditEventAtPosition;
import static de.gematik.test.core.expectations.verifier.GenericBundleVerifier.*;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.fhir.r4.erp.ErxAuditEvent;
import de.gematik.test.erezept.fhir.r4.erp.ErxAuditEventBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GenericBundleVerifierTest extends ErpFhirParsingTest {
  private ErxAuditEventBundle secondErxAuditEventBundle;
  private ErxAuditEventBundle firstErxAuditEventBundle;

  @BeforeEach
  void setupReporter() {
    CoverageReporter.getInstance().startTestcase("not needed");
    StopwatchProvider.init();
    firstErxAuditEventBundle =
        getDecodedFromPath(
            ErxAuditEventBundle.class,
            "fhir/valid/erp/1.4.0/auditeventbundle/f9c1d812-1ccb-4e4e-b62c-4d51a67ea4b7.json");
    secondErxAuditEventBundle =
        getDecodedFromPath(
            ErxAuditEventBundle.class,
            "fhir/valid/erp/1.4.0/auditeventbundle/f9c1d812-1ccb-4e4e-b62c-4d51a67ea4b7_manipulated.json");
  }

  @Test
  void shouldNotInstantiateUtilityClass() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(GenericBundleVerifier.class));
  }

  @Test
  void shouldDetect50EventsCorrect() {
    val step = containsEntriesOfCount(50);
    assertDoesNotThrow(() -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldThrowWileDetectingCountOfContainedElements() {
    val step = minimumCountOfEntriesOf(70);
    assertThrows(AssertionError.class, () -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldDetectMin50EventsCorrect() {
    val step = minimumCountOfEntriesOf(50);
    assertDoesNotThrow(() -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldThrowWileDetectingMinimalCountOfContainedElements() {
    val step = containsEntriesOfCount(30);
    assertThrows(AssertionError.class, () -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldDetectAll5LinkRelations() {
    val step = containsAll5Links();
    assertDoesNotThrow(() -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldDetectTreeOf3LinkRelations() {
    val step = containsCountOfGivenLinks(List.of("next", "prev", "self", "test"), 3L);
    assertDoesNotThrow(() -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldDetectTreeOf4LinkRelations() {
    val step = containsCountOfGivenLinks(List.of("next", "prev", "self"), 3L);
    assertDoesNotThrow(() -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldDetectOneOf1LinkRelations() {
    val step = containsCountOfGivenLinks(List.of("self"), 1L);
    assertDoesNotThrow(() -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldThrowsWhileDetectTwoOf1LinkRelations() {
    val step = containsCountOfGivenLinks(List.of("self"), 2L);
    assertThrows(AssertionError.class, () -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldDetectTotalCountCorrect() {
    val step = containsTotalCountOf(0);
    assertDoesNotThrow(() -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldThrowWhileDetectTotalCount() {
    val step = containsTotalCountOf(10);
    assertThrows(AssertionError.class, () -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldDetectQueryParamsCorrect() {
    val step = expectedParamsIn("next", "_count", "50");
    assertDoesNotThrow(() -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldThrowWileDetectQueryParams() {
    val step = expectedParamsIn("next", "_count", "40");
    assertThrows(AssertionError.class, () -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldDetectQueryParamsCorrect2() {
    val step = expectedParamsIn("prev", "__offset", "20");
    assertDoesNotThrow(() -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldThrowWileDetectQueryParams2() {
    val step = expectedParamsIn("prev", "__offset", "30");
    assertThrows(AssertionError.class, () -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldDetectQueryParamsCorrect3() {
    val step = expectedParamsIn("last", "_id", "gt01eb9373-789e-7db8-0000-000000000000");
    assertDoesNotThrow(() -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldThrowWileDetectQueryParams3() {
    val step = expectedParamsIn("last", "_id", "-789e-7db8-0000-000000000000");
    assertThrows(AssertionError.class, () -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldThrowWileDetectMissingQueryParams() {
    val step = expectedParamsIn("last", "_missing", "-789e-7db8-0000-000000000000");
    assertThrows(AssertionError.class, () -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldCompareTwoAuditEventBundlesAndThrows() {
    val step = hasSameEntryIds(firstErxAuditEventBundle, ErpAfos.A_24442);
    assertThrows(AssertionError.class, () -> step.apply(secondErxAuditEventBundle));
  }

  @Test
  void shouldCompareTwoEventBundles() {
    val step = hasSameEntryIds(firstErxAuditEventBundle, ErpAfos.A_24442);
    assertDoesNotThrow(() -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldDetectDifferentLength() {
    val step = hasSameEntryIds(firstErxAuditEventBundle, ErpAfos.A_24442);

    val newBundle = new ErxAuditEventBundle();
    newBundle.getEntry().addAll(firstErxAuditEventBundle.getEntry());
    newBundle.addEntry(new Bundle.BundleEntryComponent().setResource(new ErxAuditEvent()));

    assertNotEquals(
        newBundle.getAuditEvents().size(), firstErxAuditEventBundle.getAuditEvents().size());
    val erxBundle = new ErxAuditEventBundle();
    assertThrows(AssertionError.class, () -> step.apply(erxBundle));
  }

  @Test
  void shouldCompareBundleEntryAtPositionCorrect() {
    val bundleComponent = firstErxAuditEventBundle.getAuditEvents().get(2);
    val step = hasElementAtPosition(bundleComponent, 2);
    assertDoesNotThrow(() -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldThrowWhileCompareAuditEventAtPositionCorrect() {
    val auditEventNo2 = firstErxAuditEventBundle.getAuditEvents().get(2);
    val step = hasAuditEventAtPosition(auditEventNo2, 3);
    assertThrows(AssertionError.class, () -> step.apply(firstErxAuditEventBundle));
  }
}
