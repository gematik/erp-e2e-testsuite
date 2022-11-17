/*
 * Copyright (c) 2022 gematik GmbH
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

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.fhir.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.references.kbv.OrganizationReference;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.IKNR;
import de.gematik.test.erezept.fhir.valuesets.IdentifierTypeDe;
import java.util.Optional;
import lombok.val;
import org.junit.Test;

public class AssignerOrganizationBuilderTest extends ParsingTest {

  @Test
  public void buildAssignerOrganizationWithFixedValues() {
    val organizationResourceId = "d55c6c01-057b-483d-a1eb-2bd1e182551f";
    val organization =
        AssignerOrganizationBuilder.builder()
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

  @Test
  public void buildAssignerOrganizationWithFaker01() {
    val orgRef = new OrganizationReference("id");
    val organization = AssignerOrganizationBuilder.faker(orgRef, "AOK").build();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
  }

  @Test
  public void buildAssignerOrganizationWithFaker02() {
    val organization = AssignerOrganizationBuilder.faker().build();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
  }

  @Test
  public void shouldFailOnEmptyAssignerOrganizationBuilder() {
    val ob = AssignerOrganizationBuilder.builder();
    assertThrows(BuilderException.class, ob::build);
  }

  @Test
  public void shouldFailOnFakerWithGkvPatient() {
    val patient = PatientBuilder.faker(IdentifierTypeDe.GKV).build();
    assertThrows(BuilderException.class, () -> AssignerOrganizationBuilder.faker(patient));
  }

  @Test
  public void shouldFailOnFakerWithInvalidPkvPatient01() {
    val mockPatient = mock(KbvPatient.class);
    when(mockPatient.hasPkvId()).thenReturn(true);
    when(mockPatient.getPkvAssigner()).thenReturn(Optional.empty());
    assertThrows(BuilderException.class, () -> AssignerOrganizationBuilder.faker(mockPatient));
  }

  @Test
  public void shouldFailOnFakerWithInvalidPkvPatient02() {
    val patient = PatientBuilder.faker(IdentifierTypeDe.PKV).build();
    val mockPatient = mock(KbvPatient.class);
    when(mockPatient.hasPkvId()).thenReturn(true);
    when(mockPatient.getPkvAssigner()).thenReturn(patient.getPkvAssigner());
    when(mockPatient.getPkvAssignerName()).thenReturn(Optional.empty());
    assertThrows(BuilderException.class, () -> AssignerOrganizationBuilder.faker(mockPatient));
  }

  @Test
  public void shouldFailOnNullIknr() {
    val ob = AssignerOrganizationBuilder.builder();
    String iknrString = null;
    assertThrows(
        NullPointerException.class, () -> ob.iknr(iknrString)); // NOSONAR iknr is null by intention

    IKNR iknrObject = null;
    assertThrows(
        NullPointerException.class, () -> ob.iknr(iknrObject)); // NOSONAR iknr is null by intention
  }
}
