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

package de.gematik.test.erezept.fhir.builder.kbv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class KbvAssignerOrganizationBuilderTest extends ErpFhirParsingTest {

  @ParameterizedTest(name = "[{index}] -> Build KBV GKV Patient with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildAssignerOrganizationWithFixedValues(KbvItaForVersion version) {
    val organizationResourceId = "d55c6c01-057b-483d-a1eb-2bd1e182551f";
    val organization =
        KbvAssignerOrganizationBuilder.builder()
            .version(version)
            .setId(organizationResourceId)
            .name("Arztpraxis Meyer")
            .iknr(IKNR.asSidIknr("757299999")) // TODO: Weiche required??
            .phone("+490309876543")
            .email("info@praxis.de")
            .address("Berlin", "10623", "Wegelystraße 3")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(name = "[{index}] -> Build KBV GKV Patient with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildAssignerOrganizationWithFaker02(KbvItaForVersion version) {
    val organization = KbvAssignerOrganizationFaker.builder().withVersion(version).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(name = "[{index}] -> Build KBV GKV Patient with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void shouldFailOnEmptyAssignerOrganizationBuilder(KbvItaForVersion version) {
    val ob = KbvAssignerOrganizationBuilder.builder().version(version);
    assertThrows(BuilderException.class, ob::build);
  }

  @ParameterizedTest(name = "[{index}] -> Build KBV GKV Patient with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void shouldFailOnFakerWithGkvPatient(KbvItaForVersion version) {
    val patient =
        KbvPatientFaker.builder()
            .withKvnrAndInsuranceType(KVNR.random(), InsuranceTypeDe.GKV)
            .withVersion(version)
            .fake();
    try {
      KbvAssignerOrganizationFaker.builder().forPatient(patient).fake();
    } catch (RuntimeException e) {
      assertEquals(BuilderException.class, e.getClass());
    }
  }

  @Test
  void shouldFailOnFakerWithInvalidPkvPatient01() {
    val mockPatient = mock(KbvPatient.class);
    when(mockPatient.hasPkvKvnr()).thenReturn(true);
    when(mockPatient.getPkvAssigner()).thenReturn(Optional.empty());
    try {
      KbvAssignerOrganizationFaker.builder().forPatient(mockPatient).fake();
    } catch (RuntimeException e) {
      assertEquals(BuilderException.class, e.getClass());
    }
  }

  @Test
  void shouldFailOnFakerWithInvalidPkvPatient02() {
    val patient =
        KbvPatientFaker.builder()
            .withKvnrAndInsuranceType(KVNR.random(), InsuranceTypeDe.PKV)
            .fake();
    val mockPatient = mock(KbvPatient.class);
    when(mockPatient.hasPkvKvnr()).thenReturn(true);
    when(mockPatient.getPkvAssigner()).thenReturn(patient.getPkvAssigner());
    when(mockPatient.getPkvAssignerName()).thenReturn(Optional.empty());
    try {
      KbvAssignerOrganizationFaker.builder().forPatient(mockPatient).fake();
    } catch (RuntimeException e) {
      assertEquals(BuilderException.class, e.getClass());
    }
  }
}
