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

package de.gematik.test.erezept.fhir.r4.kbv;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.valuesets.DmpKennzeichen;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;

class KbvCoverageTest {

  @Test
  void shouldGetFromResource() {
    val resource = new Coverage();
    val kbvCoverage = KbvCoverage.fromCoverage((Resource) resource);
    assertNotNull(kbvCoverage);
    assertNotEquals(resource, kbvCoverage);
  }

  @Test
  void shouldNotCast() {
    val resource = new Coverage();
    val kbvCoverage = KbvCoverage.fromCoverage(resource);
    val kbvCoverage2 = KbvCoverage.fromCoverage(kbvCoverage);
    assertEquals(kbvCoverage, kbvCoverage2);
  }

  @Test
  void shouldThrowOnMissingInsuranceKind() {
    val coverage = mock(KbvCoverage.class);
    when(coverage.getInsuranceKindOptional()).thenReturn(Optional.empty());
    when(coverage.getInsuranceKind()).thenCallRealMethod();
    assertThrows(MissingFieldException.class, coverage::getInsuranceKind);
  }

  @Test
  void shouldThrowOnMissingInsurantState() {
    val coverage = mock(KbvCoverage.class);
    when(coverage.getInsurantStateOptional()).thenReturn(Optional.empty());
    when(coverage.getInsurantState()).thenCallRealMethod();
    assertThrows(MissingFieldException.class, coverage::getInsurantState);
  }

  @Test
  void shouldThrowOnMissingPersonGroup() {
    val coverage = mock(KbvCoverage.class);
    when(coverage.getPersonGroupOptional()).thenReturn(Optional.empty());
    when(coverage.getPersonGroup()).thenCallRealMethod();
    assertThrows(MissingFieldException.class, coverage::getPersonGroup);
  }

  @Test
  void shouldReturnDmpKennzeichen() {
    val coverage = mock(KbvCoverage.class);
    when(coverage.getDmpKennzeichenOptional()).thenReturn(Optional.of(DmpKennzeichen.DM1));
    when(coverage.getDmpKennzeichen()).thenCallRealMethod();
    assertDoesNotThrow(coverage::getDmpKennzeichen);

    val coverage2 = new KbvCoverage();
    assertEquals(Optional.empty(), coverage2.getDmpKennzeichenOptional());
  }

  @Test
  void shouldThrowOnMissingDmpKennzeichen() {
    val coverage = mock(KbvCoverage.class);
    when(coverage.getDmpKennzeichenOptional()).thenReturn(Optional.empty());
    when(coverage.getDmpKennzeichen()).thenCallRealMethod();
    assertThrows(MissingFieldException.class, coverage::getDmpKennzeichen);
  }
}
