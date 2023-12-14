/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.fhir.parser.profiles.version;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.fhir.exceptions.*;
import de.gematik.test.erezept.fhir.parser.profiles.*;
import java.time.*;
import java.util.*;
import lombok.*;
import org.junit.jupiter.api.*;

class ProfileVersionTest {

  @Test
  void shouldParseDeBasisProfilVersion() {
    val sources0913 =
        List.of(
            "profiles/de.basisprofil.r4-0.9.13/package/Profile",
            "0.9.13",
            "profiles/de.basisprofil.r4-0.9.13/package/ValueSet-1.2.276.0.76.11.39--20180713132816.json"); // containing multiple matches, where only the first one is of interest!

    sources0913.forEach(
        input -> {
          val version = ProfileVersion.fromString(DeBasisVersion.class, input);
          assertEquals(DeBasisVersion.V0_9_13, version);
        });

    val sources132 = List.of("profiles/de.basisprofil.r4-1.3.2/package/Profile", "1.3.2");

    sources132.forEach(
        input -> {
          val version = ProfileVersion.fromString(DeBasisVersion.class, input);
          assertEquals(DeBasisVersion.V1_3_2, version);
        });
  }

  @Test
  void shouldMatchFullEqualVersion() {
    val version = DeBasisVersion.V0_9_13;
    assertTrue(version.isEqual("0.9.13"));
  }

  @Test
  void shouldMatchNonSemantikVersion() {
    val version = PatientenrechnungVersion.V1_0_0;
    assertTrue(version.isEqual("1.0"));
  }

  @Test
  void shouldCompareEqualsVersions() {
    val left = ErpWorkflowVersion.V1_2_0;
    val right = ErpWorkflowVersion.V1_2_0;
    assertEquals(0, left.compareTo(right));
  }

  @Test
  void shouldCompareNonSemantikVersionEqual() {
    val version = ErpWorkflowVersion.V1_2_0;
    assertEquals(0, version.compareTo("1.2"));
  }

  @Test
  void shouldNotMatchNonSemantikVersion() {
    val version = DeBasisVersion.V1_3_2;
    assertFalse(version.isEqual("1.3"));
  }

  @Test
  void shouldCompareNonSemantikVersion() {
    val version = ErpWorkflowVersion.V1_1_1;
    assertEquals(1, version.compareTo("1.1"));
  }

  @Test
  void shouldThrowOnInvalidSemanticVersioningStructure() {
    assertThrows(FhirValidatorException.class, () -> ProfileVersion.parseVersion("0.0.a"));
    assertThrows(FhirValidatorException.class, () -> ProfileVersion.parseVersion("0.0"));
  }

  @Test
  void shouldThrowOnVersionMismatch() {
    assertThrows(
        FhirValidatorException.class,
        () -> ProfileVersion.fromString(ErpWorkflowVersion.class, "1.3.0"));
  }

  @Test
  void shouldThrowOnVersionMismatch2() {
    // on ProfileVersion.fromString() the versions as string must be given as full SemVer
    assertThrows(
        FhirValidatorException.class,
        () -> ProfileVersion.fromString(ErpWorkflowVersion.class, "1.2"));
  }

  @Test
  void shouldThrowOnProfileVersionTypeMismatch() {
    assertThrows(
        FhirValidatorException.class,
        () ->
            ProfileVersion.getDefaultVersion(ErpWorkflowVersion.class, CustomProfiles.KBV_ITA_ERP));
  }

  @Test
  void shouldNotThrowOnProfileVersionTypeMatch() {
    assertDoesNotThrow(
        () ->
            ProfileVersion.getDefaultVersion(
                ErpWorkflowVersion.class, CustomProfiles.GEM_ERP_WORKFLOW));
  }

  @Test
  void shouldThrowOnNoDefaultVersion() {
    val m = mock(CustomProfiles.class);
    doReturn(TestingVersion.class).when(m).getVersionClass();
    doReturn("testing.profile").when(m).getName();

    assertThrows(
        RuntimeException.class, () -> ProfileVersion.getDefaultVersion(TestingVersion.class, m));
  }

  @Getter
  @RequiredArgsConstructor
  enum TestingVersion implements ProfileVersion<TestingVersion> {
    V_0("0.0.0", LocalDate.of(1970, Month.JANUARY, 1), LocalDate.of(1980, Month.JULY, 31)),
    V_1("0.0.0", LocalDate.of(1970, Month.JANUARY, 1), LocalDate.of(1980, Month.JULY, 31));

    private final String version;
    private final LocalDate validFromDate;
    private final LocalDate validUntilDate;
    private final CustomProfiles customProfile = null;
  }
}
