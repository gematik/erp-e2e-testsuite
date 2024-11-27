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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class KbvErpMedicationFreeTextBuilderTest extends ParsingTest {

  @ParameterizedTest(name = "[{index}] -> Build KBV MedicationFreeText with KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaErpVersions")
  void shouldBuildWithKbvVersion(KbvItaErpVersion version) {
    val medicationFreeText =
        new KbvErpMedicationFreeTextBuilder()
            .version(version)
            .freeText("Nasenstöpsel während des Nase putzens entfernen.")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, medicationFreeText);
    assertTrue(result.isSuccessful());
    assertEquals(version, medicationFreeText.getVersion());
  }

  @Test
  void builderShouldWork() {
    val medicationFreeText =
        new KbvErpMedicationFreeTextBuilder()
            .category(MedicationCategory.C_00)
            .isVaccine(true)
            .darreichung("NasenStöpsel")
            .freeText("Nasenstöpsel während des Nase putzens entfernen.")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, medicationFreeText);
    assertTrue(result.isSuccessful());
  }

  @Test
  void builderShouldWorkWithoutDarreichungsform() {
    val medicationFreeText =
        new KbvErpMedicationFreeTextBuilder()
            .category(MedicationCategory.C_00)
            .isVaccine(true)
            .freeText("Nasenstöpsel während des Nase putzens entfernen.")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, medicationFreeText);
    assertTrue(result.isSuccessful());
  }

  @Test
  void fakerShouldWork() {
    val medFreeText = KbvErpMedicationFreeTextFaker.builder().fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medFreeText);
    assertTrue(result.isSuccessful());
  }

  @Test
  void fakerWithTextShouldWork() {
    val medFreeText =
        KbvErpMedicationFreeTextFaker.builder()
            .withFreeText("3 mal täglich einen lutscher lutschen und anschließend Zähnchen putzen")
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medFreeText);
    assertTrue(result.isSuccessful());
  }

  @Test
  void requiredEntriesShouldBeChecked() {
    val builder = KbvErpMedicationFreeTextBuilder.builder();
    assertThrows(
        BuilderException.class,
        () -> {
          val test1 = builder.build();
        });
  }
}
