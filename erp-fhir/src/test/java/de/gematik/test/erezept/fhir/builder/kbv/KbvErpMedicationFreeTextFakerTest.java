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

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerBool;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerValueSet;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import lombok.val;
import org.junit.jupiter.api.Test;

class KbvErpMedicationFreeTextFakerTest extends ErpFhirParsingTest {
  @Test
  void buildFakeMedicationFreeTextWithDosageForm() {
    val freetext =
        KbvErpMedicationFreeTextFaker.builder()
            .withDosageForm(fakerValueSet(Darreichungsform.class).getCode())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, freetext);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeMedicationFreeTextWithVaccine() {
    val freetext = KbvErpMedicationFreeTextFaker.builder().withVaccine(fakerBool()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, freetext);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFAkeMedicationFreeTextWithCategory() {
    val freetext =
        KbvErpMedicationFreeTextFaker.builder().withCategory(MedicationCategory.C_00).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, freetext);
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
}
