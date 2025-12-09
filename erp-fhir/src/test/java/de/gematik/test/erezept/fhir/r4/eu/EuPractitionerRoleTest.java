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

package de.gematik.test.erezept.fhir.r4.eu;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.builder.eu.EuOrganizationFaker;
import de.gematik.test.erezept.fhir.builder.eu.EuPractitionerRoleBuilder;
import lombok.val;
import org.junit.jupiter.api.Test;

class EuPractitionerRoleTest {
  EuPractitionerRoleBuilder eprBuilder =
      new EuPractitionerRoleBuilder()
          .euPractitioner((EuPractitioner) new EuPractitioner().setId("123"))
          .euOrganization(EuOrganizationFaker.faker().fake());

  @Test
  void shouldGetOIDCorrect() {
    val epr = eprBuilder.defaultProfessionOID().build();
    assertNotNull(epr.getOrgenanizationProfession());
    assertEquals("2262", epr.getOrgenanizationProfession().getCode());
    assertEquals("Pharmacists", epr.getOrgenanizationProfession().getDisplay());
  }

  @Test
  void shouldFailWhileGettingOid() {
    val epr = eprBuilder.build();
    assertThrows(MissingFieldException.class, () -> epr.getOrgenanizationProfession());
  }

  @Test
  void shouldGetFromEuPractitionerRole() {
    val org = eprBuilder.build();
    val fromEpr = EuPractitionerRole.fromPractitionerRole(org);
    assertEquals(org.getId(), fromEpr.getId());
    assertEquals(org.getPractitioner().getReference(), fromEpr.getPractitioner().getReference());
  }
}
