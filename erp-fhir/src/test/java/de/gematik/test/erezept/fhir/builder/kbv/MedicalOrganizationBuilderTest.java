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

import de.gematik.test.erezept.fhir.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.BSNR;
import de.gematik.test.erezept.fhir.valuesets.Country;
import lombok.val;
import org.junit.Test;

public class MedicalOrganizationBuilderTest extends ParsingTest {

  @Test
  public void buildMedicalOrganizationWithFixedValues() {
    val organizationResourceId = "d55c6c01-057b-483d-a1eb-2bd1e182551f";
    val organization =
        MedicalOrganizationBuilder.builder()
            .setResourceId(organizationResourceId)
            .name("Arztpraxis Meyer")
            .bsnr(BSNR.from("757299999"))
            .phone("+490309876543")
            .email("info@praxis.de")
            .address(Country.D, "Berlin", "10623", "Wegelystraße 3")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
  }

  @Test
  public void buildMedicalOrganizationWithFaker() {
    val organization = MedicalOrganizationBuilder.faker().build();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
  }

  @Test
  public void shouldFailOnEmptyMedicalOrganizationBuilder() {
    val ob = MedicalOrganizationBuilder.builder();
    assertThrows(BuilderException.class, ob::build);
  }
}
