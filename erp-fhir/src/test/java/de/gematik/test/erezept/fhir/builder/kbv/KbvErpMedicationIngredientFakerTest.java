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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import lombok.val;
import org.junit.jupiter.api.Test;

class KbvErpMedicationIngredientFakerTest extends ErpFhirParsingTest {
  @Test
  void buildFakerKbvErpMedicationIngredientWithVaccine() {
    val ingredient = KbvErpMedicationIngredientFaker.builder().withVaccine(fakerBool()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, ingredient);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakerKbvErpMedicationIngredientWithVersion() {
    val ingredient =
        KbvErpMedicationIngredientFaker.builder()
            .withVersion(KbvItaErpVersion.getDefaultVersion())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, ingredient);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakerKbvErpMedicationIngredientWithCategory() {
    val ingredient =
        KbvErpMedicationIngredientFaker.builder()
            .withCategory(fakerValueSet(MedicationCategory.class))
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, ingredient);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakerKbvErpMedicationIngredientWithAmount() {
    String numerator = "num";
    val ingredient = KbvErpMedicationIngredientFaker.builder().withAmount(numerator).fake();
    val ingredient2 = KbvErpMedicationIngredientFaker.builder().withAmount(numerator, "Stk").fake();
    val ingredient3 =
        KbvErpMedicationIngredientFaker.builder().withAmount(numerator, 1, "Stk").fake();
    val result = ValidatorUtil.encodeAndValidate(parser, ingredient);
    val result2 = ValidatorUtil.encodeAndValidate(parser, ingredient2);
    val result3 = ValidatorUtil.encodeAndValidate(parser, ingredient3);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
    assertTrue(result3.isSuccessful());
  }

  @Test
  void buildFakerKbvErpMedicationIngredientWithIngredient() {
    val ingredient =
        KbvErpMedicationIngredientFaker.builder().withIngredientComponent("Fake Unit").fake();
    val ingredient2 =
        KbvErpMedicationIngredientFaker.builder().withIngredientComponent(0, 1, "Fake Unit").fake();
    val result = ValidatorUtil.encodeAndValidate(parser, ingredient);
    val result2 = ValidatorUtil.encodeAndValidate(parser, ingredient2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  void buildFakerKbvErpMedicationIngredientWithStandardSize() {
    val ingredient =
        KbvErpMedicationIngredientFaker.builder()
            .withStandardSize(fakerValueSet(StandardSize.class))
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, ingredient);
    assertTrue(result.isSuccessful());
  }
}
