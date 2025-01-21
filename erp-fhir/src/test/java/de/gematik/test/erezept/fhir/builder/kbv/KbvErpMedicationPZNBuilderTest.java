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

package de.gematik.test.erezept.fhir.builder.kbv;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.parser.profiles.version.*;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.valuesets.*;
import lombok.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class KbvErpMedicationPZNBuilderTest extends ParsingTest {

  @ParameterizedTest(name = "[{index}] -> Build KBV Medication with KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaErpVersions")
  void shouldBuildMedicationWithFixedValues(KbvItaErpVersion version) {
    val medicationResourceId = "c1e7027e-3c5b-4e87-a10a-572676b92e22";
    val medication =
        KbvErpMedicationPZNBuilder.builder()
            .version(version)
            .setResourceId(medicationResourceId)
            .category(MedicationCategory.C_00) // default C_00
            .isVaccine(false) // default false
            .normgroesse(StandardSize.N1) // default NB (nicht betroffen)
            .darreichungsform(Darreichungsform.TKA) // default TAB
            .amount(5, "Stk") // default 10 {tbl}
            .pzn("04773414", "Doxycyclin AL 200 T, 10 Tabletten N1")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(name = "[{index}] -> Build KBV Medication with KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaErpVersions")
  void shouldBuildMedicationWithFixedValues2(KbvItaErpVersion version) {
    val medicationResourceId = "c1e7027e-3c5b-4e87-a10a-572676b92e22";
    val medication =
        KbvErpMedicationPZNBuilder.builder()
            .version(version)
            .setResourceId(medicationResourceId)
            .category(MedicationCategory.C_00) // default C_00
            .isVaccine(false) // default false
            .normgroesse(StandardSize.N1) // default NB (nicht betroffen)
            .darreichungsform(Darreichungsform.TKA) // default TAB
            .pzn(PZN.random(), "5 in 1 Medikament")
            .amount(4L)
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(name = "[{index}] -> Build KBV Medication with Faker for KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaErpVersions")
  void shouldBuildMedicationWithFakerAndValidValues(KbvItaErpVersion version) {
    val medicationResourceId = "e41d2c17-7632-486e-b24f-60c6c7a8b8d9";
    val randomPZN = PZN.random();
    val medication =
        KbvErpMedicationPZNBuilder.faker(
                randomPZN.getValue(), "Test Medikament", MedicationCategory.C_00)
            .version(version)
            .setResourceId(medicationResourceId)
            .build();

    assertNotNull(medication);
    assertEquals(medicationResourceId, medication.getId());
    assertNotNull(medication.getCode());
    assertEquals("Test Medikament", medication.getCode().getText());
    assertFalse(
        medication.getForm().getCoding().stream()
            .anyMatch(
                coding ->
                    coding.getCode().equals(Darreichungsform.PUE.getCode())
                        || coding.getCode().equals(Darreichungsform.LYE.getCode())),
        "Excluded Darreichungsformen PUE and LYE should not be used before April 2025");

    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
  }
}
