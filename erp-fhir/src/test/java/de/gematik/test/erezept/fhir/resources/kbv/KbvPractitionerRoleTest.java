/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.fhir.resources.kbv;

import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import lombok.val;
import org.junit.jupiter.api.Test;

class KbvPractitionerRoleTest extends ParsingTest {

  private static final String BASE_PATH_1_0_0 = "fhir/valid/kbv_evdga/1.0.0/";

  @Test
  void shouldThrowOnMissingTeamNumber01() {
    val expectedID = "EVDGA_Bundle";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_0_0 + fileName);
    val bundle = parser.decode(KbvEvdgaBundle.class, content);
    val practitionerRole = bundle.getPractitionerRole().orElseThrow();

    practitionerRole.setOrganization(null);
    assertThrows(MissingFieldException.class, practitionerRole::getTeamNumber);
  }

  @Test
  void shouldThrowOnMissingTeamNumber02() {
    val expectedID = "EVDGA_Bundle";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_0_0 + fileName);
    val bundle = parser.decode(KbvEvdgaBundle.class, content);
    val practitionerRole = bundle.getPractitionerRole().orElseThrow();

    practitionerRole.getOrganization().setIdentifier(null);
    assertThrows(MissingFieldException.class, practitionerRole::getTeamNumber);
  }
}
