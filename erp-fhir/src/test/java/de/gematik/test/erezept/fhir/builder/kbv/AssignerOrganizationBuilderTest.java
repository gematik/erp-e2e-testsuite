/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.fhir.builder.kbv;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.fhir.exceptions.*;
import de.gematik.test.erezept.fhir.parser.profiles.version.*;
import de.gematik.test.erezept.fhir.references.kbv.*;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.*;
import lombok.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class AssignerOrganizationBuilderTest extends ParsingTest {

  @ParameterizedTest(name = "[{index}] -> Build KBV GKV Patient with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildAssignerOrganizationWithFixedValues(KbvItaForVersion version) {
    val organizationResourceId = "d55c6c01-057b-483d-a1eb-2bd1e182551f";
    val organization =
        AssignerOrganizationBuilder.builder()
            .version(version)
            .setResourceId(organizationResourceId)
            .name("Arztpraxis Meyer")
            .iknr(IKNR.from("757299999"))
            .phone("+490309876543")
            .email("info@praxis.de")
            .address("Berlin", "10623", "Wegelystraße 3")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(name = "[{index}] -> Build KBV GKV Patient with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildAssignerOrganizationWithFaker01(KbvItaForVersion version) {
    val orgRef = new OrganizationReference("id");
    val organization = AssignerOrganizationBuilder.faker(orgRef, "AOK").version(version).build();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(name = "[{index}] -> Build KBV GKV Patient with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildAssignerOrganizationWithFaker02(KbvItaForVersion version) {
    val organization = AssignerOrganizationBuilder.faker().version(version).build();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(name = "[{index}] -> Build KBV GKV Patient with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void shouldFailOnEmptyAssignerOrganizationBuilder(KbvItaForVersion version) {
    val ob = AssignerOrganizationBuilder.builder().version(version);
    assertThrows(BuilderException.class, ob::build);
  }

  @ParameterizedTest(name = "[{index}] -> Build KBV GKV Patient with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void shouldFailOnFakerWithGkvPatient(KbvItaForVersion version) {
    val patient = PatientBuilder.faker(VersicherungsArtDeBasis.GKV).version(version).build();
    assertThrows(BuilderException.class, () -> AssignerOrganizationBuilder.faker(patient));
  }

  @Test
  void shouldFailOnFakerWithInvalidPkvPatient01() {
    val mockPatient = mock(KbvPatient.class);
    when(mockPatient.hasPkvKvnr()).thenReturn(true);
    when(mockPatient.getPkvAssigner()).thenReturn(Optional.empty());
    assertThrows(BuilderException.class, () -> AssignerOrganizationBuilder.faker(mockPatient));
  }

  @Test
  void shouldFailOnFakerWithInvalidPkvPatient02() {
    val patient = PatientBuilder.faker(VersicherungsArtDeBasis.PKV).build();
    val mockPatient = mock(KbvPatient.class);
    when(mockPatient.hasPkvKvnr()).thenReturn(true);
    when(mockPatient.getPkvAssigner()).thenReturn(patient.getPkvAssigner());
    when(mockPatient.getPkvAssignerName()).thenReturn(Optional.empty());
    assertThrows(BuilderException.class, () -> AssignerOrganizationBuilder.faker(mockPatient));
  }

  @Test
  void shouldFailOnNullIknr() {
    val ob = AssignerOrganizationBuilder.builder();
    String iknrString = null;
    assertThrows(
        NullPointerException.class, () -> ob.iknr(iknrString)); // NOSONAR iknr is null by intention

    IKNR iknrObject = null;
    assertThrows(
        NullPointerException.class, () -> ob.iknr(iknrObject)); // NOSONAR iknr is null by intention
  }
}
