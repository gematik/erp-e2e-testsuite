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

package de.gematik.test.erezept.fhir.builder.kbv;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.AsvTeamNumber;
import lombok.val;
import org.junit.jupiter.api.Test;

class KbvPractitionerRoleBuilderTest extends ErpFhirParsingTest {

  @Test
  void shouldBuildCorrectPractitionerRole() {
    val practitioner = KbvPractitionerFaker.builder().fake();
    val asvTeamNumber = AsvTeamNumber.random();
    val practitionerRole =
        KbvPractitionerRoleBuilder.builder()
            .practitioner(practitioner)
            .teamNumber(asvTeamNumber)
            .version(KbvItaForVersion.V1_2_0)
            .build();
    assertTrue(parser.isValid(practitionerRole));
    assertTrue(practitionerRole.getPractitioner().getReference().endsWith(practitioner.getId()));
    assertEquals(
        asvTeamNumber.getValue(), practitionerRole.getOrganization().getIdentifier().getValue());
    assertTrue(practitionerRole.getMeta().getProfile().get(0).getValue().endsWith("1.2"));
  }

  @Test
  void shouldSetVersionCorrect() {
    val practitionerRole =
        KbvPractitionerRoleBuilder.builder()
            .practitioner(KbvPractitionerFaker.builder().fake())
            .teamNumber(AsvTeamNumber.random())
            .version(KbvItaForVersion.V1_1_0)
            .build();
    assertTrue(parser.isValid(practitionerRole));
    assertTrue(practitionerRole.getMeta().getProfile().get(0).getValue().endsWith("1.1.0"));
  }
}
