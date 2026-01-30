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

package de.gematik.test.erezept.fhir.builder.eu;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import de.gematik.test.erezept.fhir.r4.eu.EuOrganizationProfession;
import de.gematik.test.erezept.fhir.r4.eu.EuPractitioner;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import lombok.val;
import org.junit.jupiter.api.Test;

class EuPractitionerRoleBuilderTest extends ErpFhirParsingTest {

  private final EuPractitionerRoleBuilder builder =
      EuPractitionerRoleBuilder.builder()
          .euPractitioner((EuPractitioner) new EuPractitioner().setId("123"))
          .euOrganization(EuOrganizationFaker.faker().fake())
          .version(EuVersion.getDefaultVersion())
          .defaultProfessionOID();

  @Test
  void shouldGetPractitionerReferenceCorrect() {
    val role = builder.build();
    assertEquals("Practitioner/123", role.getPractitioner().getReference());
  }

  @Test
  void shouldGetOrganizationReferenceCorrect() {
    val role = builder.build();
    assertNotNull(role.getOrganization().getReference());
  }

  @Test
  void shouldGetCodeCorrect() {
    val role = builder.build();
    assertEquals("2262", role.getOrgenanizationProfession().getCode());
    assertEquals("Pharmacists", role.getOrgenanizationProfession().getDisplay());
  }

  @Test
  void shouldBeValid() {
    val role = builder.build();
    val res = ValidatorUtil.encodeAndValidate(parser, role);
    assertTrue(res.isSuccessful());
  }

  @Test
  void shouldSetOidManuallyWorks() {
    val role =
        builder
            .euOrganizationProfessionOID(
                new EuOrganizationProfession("urn:oid:System", "code", "display"))
            .build();
    assertEquals("urn:oid:System", role.getOrgenanizationProfession().getCanonicalUrl());
    assertEquals("code", role.getOrgenanizationProfession().getCode());
    assertEquals("display", role.getOrgenanizationProfession().getDisplay());
  }

  @Test
  void shouldBuildSimplePractRole() {
    val role = EuPractitionerRoleBuilder.getSimplePractitionerRole();
    val res = ValidatorUtil.encodeAndValidate(parser, role);
    assertTrue(res.isSuccessful());
  }
}
