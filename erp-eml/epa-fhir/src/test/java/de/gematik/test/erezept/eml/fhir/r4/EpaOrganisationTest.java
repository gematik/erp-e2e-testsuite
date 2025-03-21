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

package de.gematik.test.erezept.eml.fhir.r4;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class EpaOrganisationTest {

  @Test
  void shouldGetCorrectTelematikId() {
    val epaOrganisation = new EpaOrganisation();

    epaOrganisation.setIdentifier(
        List.of(DeBasisProfilNamingSystem.TELEMATIK_ID_SID.asIdentifier("123.asd")));
    assertEquals(TelematikID.from("123.asd"), epaOrganisation.getTelematikId());
  }
}
