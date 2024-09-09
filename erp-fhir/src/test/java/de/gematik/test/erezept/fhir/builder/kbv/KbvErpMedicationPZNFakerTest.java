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

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.valuesets.BaseMedicationType;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import lombok.val;
import org.junit.jupiter.api.Test;

class KbvErpMedicationPZNFakerTest extends ParsingTest {
  @Test
  void buildFakerKbvErpMedicationPZNWithType() {
    val medication =
        KbvErpMedicationPZNFaker.builder().withType(BaseMedicationType.MEDICAL_PRODUCT).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakerKbvErpMedicationPZNWithVaccine() {
    val medication = KbvErpMedicationPZNFaker.builder().withVaccine(fakerBool()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakerKbvErpMedicationPZNWithStandardSize() {
    val medication =
        KbvErpMedicationPZNFaker.builder()
            .withStandardSize(fakerValueSet(StandardSize.class))
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldFakeKbvErpMedicationPznWithSupplyForm() {
    val medication = KbvErpMedicationPZNFaker.builder().withSupplyForm(Darreichungsform.SCH).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakerKbvErpMedicationPZNWithPZNMedicationName() {
    val medication =
        KbvErpMedicationPZNFaker.builder().withPznMedication(PZN.random(), fakerDrugName()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakerKbvErpMedicationPZNWithAmount() {
    val medication = KbvErpMedicationPZNFaker.builder().withAmount(fakerAmount()).fake();
    val medication2 = KbvErpMedicationPZNFaker.builder().withAmount(fakerAmount(), "Stk").fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    val result2 = ValidatorUtil.encodeAndValidate(parser, medication2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }
}
