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

package de.gematik.test.erezept.fhir.resources.kbv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationCompoundingBuilder;
import de.gematik.test.erezept.fhir.exceptions.FhirValidatorException;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.values.PZN;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class KbvErpMedicationTest {

  @Test
  void shouldDetectVersionCorrectly() {
    List.of(KbvItaErpVersion.V1_0_2, KbvItaErpVersion.V1_1_0)
        .forEach(
            version -> {
              val medication = KbvErpMedicationBuilder.faker().version(version).build();
              assertEquals(version, medication.getVersion());
            });
  }

  @Test
  void shouldThrowIfMedicationDoesNotContainAProfile() {
    val medication = new KbvErpMedication();
    assertThrows(FhirValidatorException.class, medication::getVersion);
  }

  @Test
  void shouldProvideDefaultVersionIfMissing() {
    val medication = KbvErpMedicationBuilder.faker().build();

    // remove version from profile
    medication
        .getMeta()
        .getProfile()
        .forEach(
            profile -> {
              val unversioned = profile.asStringValue().split("\\|")[0];
              profile.setValueAsString(unversioned);
            });
    assertEquals(KbvItaErpVersion.getDefaultVersion(), medication.getVersion());
  }

  @Test
  void getPznOptionalShouldWorkForMedicationCompound() {
    PZN pzn = PZN.random();
    val medComp = KbvErpMedicationCompoundingBuilder.faker(pzn, "", "").build();
    assertEquals(pzn.getValue(), medComp.getPznOptional().get().getValue());
  }

  @Test
  void getPznOptionalShouldWorkForMedicationPZN() {
    String pzn = PZN.random().getValue();
    val medComp = KbvErpMedicationBuilder.faker(pzn, "Useless Medicine").build();
    assertEquals(pzn, medComp.getPznOptional().get().getValue());
  }
}
