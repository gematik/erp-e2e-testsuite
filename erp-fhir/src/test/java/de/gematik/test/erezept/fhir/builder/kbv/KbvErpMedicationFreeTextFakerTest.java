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

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerBool;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerValueSet;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import lombok.val;
import org.junit.jupiter.api.Test;

class KbvErpMedicationFreeTextFakerTest extends ParsingTest {
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
}
