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

package de.gematik.test.core.expectations.requirements;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PrescriptionServiceVersionTest {

  @Nested
  @DisplayName("isBetween")
  class IsBetweenTests {

    @Test
    void shouldReturnTrueWhenVersionIsBetweenBounds() {
      assertTrue(
          PrescriptionServiceVersion.V_1_20_0.isBetween(
              PrescriptionServiceVersion.V_1_19_0, PrescriptionServiceVersion.V_1_21_0));
    }

    @Test
    void shouldReturnTrueWhenVersionEqualsLowerBound() {
      assertTrue(
          PrescriptionServiceVersion.V_1_19_0.isBetween(
              PrescriptionServiceVersion.V_1_19_0, PrescriptionServiceVersion.V_1_21_0));
    }

    @Test
    void shouldReturnTrueWhenVersionEqualsUpperBound() {
      assertTrue(
          PrescriptionServiceVersion.V_1_21_0.isBetween(
              PrescriptionServiceVersion.V_1_19_0, PrescriptionServiceVersion.V_1_21_0));
    }

    @Test
    void shouldReturnFalseWhenVersionIsBelowLowerBound() {
      assertFalse(
          PrescriptionServiceVersion.V_1_19_0.isBetween(
              PrescriptionServiceVersion.V_1_20_0, PrescriptionServiceVersion.V_1_21_0));
    }

    @Test
    void shouldReturnFalseWhenVersionIsAboveUpperBound() {
      assertFalse(
          PrescriptionServiceVersion.V_1_21_0.isBetween(
              PrescriptionServiceVersion.V_1_19_0, PrescriptionServiceVersion.V_1_20_0));
    }
  }

  @Nested
  @DisplayName("isAtLeast")
  class IsAtLeastTests {

    @Test
    void shouldReturnTrueWhenVersionIsGreater() {
      assertTrue(
          PrescriptionServiceVersion.V_1_21_0.isAtLeast(PrescriptionServiceVersion.V_1_20_0));
    }

    @Test
    void shouldReturnTrueWhenVersionIsEqual() {
      assertTrue(
          PrescriptionServiceVersion.V_1_20_0.isAtLeast(PrescriptionServiceVersion.V_1_20_0));
    }

    @Test
    void shouldReturnFalseWhenVersionIsSmaller() {
      assertFalse(
          PrescriptionServiceVersion.V_1_19_0.isAtLeast(PrescriptionServiceVersion.V_1_20_0));
    }
  }

  @Nested
  @DisplayName("isLessThan")
  class IsLessThanTests {

    @Test
    void shouldReturnTrueWhenVersionIsSmaller() {
      assertTrue(
          PrescriptionServiceVersion.V_1_19_0.isLessThan(PrescriptionServiceVersion.V_1_20_0));
    }

    @Test
    void shouldReturnFalseWhenVersionIsEqual() {
      assertFalse(
          PrescriptionServiceVersion.V_1_20_0.isLessThan(PrescriptionServiceVersion.V_1_20_0));
    }

    @Test
    void shouldReturnFalseWhenVersionIsGreater() {
      assertFalse(
          PrescriptionServiceVersion.V_1_21_0.isLessThan(PrescriptionServiceVersion.V_1_20_0));
    }
  }

  @Nested
  @DisplayName("UNKNOWN Version")
  class UnknownVersionTests {

    @Test
    void unknownShouldBeLessThanValidVersions() {
      assertTrue(
          PrescriptionServiceVersion.UNKNOWN.isLessThan(PrescriptionServiceVersion.V_1_19_0));
    }

    @Test
    void validVersionShouldBeAtLeastUnknown() {
      assertTrue(PrescriptionServiceVersion.V_1_19_0.isAtLeast(PrescriptionServiceVersion.UNKNOWN));
    }

    @Test
    void unknownIsOnlyBetweenItself() {
      assertTrue(
          PrescriptionServiceVersion.UNKNOWN.isBetween(
              PrescriptionServiceVersion.UNKNOWN, PrescriptionServiceVersion.UNKNOWN));
    }
  }

  @Nested
  @DisplayName("from")
  class FromMethodTests {

    @Test
    void shouldResolveExactVersion() {
      assertEquals(PrescriptionServiceVersion.V_1_19_0, PrescriptionServiceVersion.from("1.19.0"));
    }

    @Test
    void shouldResolveVersionWithoutPatchSegment() {
      assertEquals(PrescriptionServiceVersion.V_1_19_0, PrescriptionServiceVersion.from("1.19"));
    }
  }
}
