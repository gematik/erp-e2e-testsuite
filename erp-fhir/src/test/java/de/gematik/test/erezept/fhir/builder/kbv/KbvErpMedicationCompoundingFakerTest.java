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

import de.gematik.test.erezept.fhir.extensions.kbv.ProductionInstruction;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import lombok.val;
import org.junit.jupiter.api.Test;

class KbvErpMedicationCompoundingFakerTest extends ParsingTest {
  @Test
  void buildFakeKbvErpMedicationCompoundingWithDosageForm() {
    val medication =
        KbvErpMedicationCompoundingFaker.builder()
            .withPackaging("fass")
            .withDosageForm("Zäpfchen, viel Spaß")
            .fake();
    val medication2 =
        KbvErpMedicationCompoundingFaker.builder()
            .withProductionInstruction("mischen")
            .withDosageForm(Darreichungsform.AEO)
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    val result2 = ValidatorUtil.encodeAndValidate(parser, medication2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  void buildFakeKbvErpMedicationCompoundingWithAmount() {
    val medication =
        KbvErpMedicationCompoundingFaker.builder()
            .withAmount(fakerAmount())
            .withPackaging("fass")
            .fake();
    val medication2 =
        KbvErpMedicationCompoundingFaker.builder()
            .withProductionInstruction("mischen")
            .withAmount(5, 1, "Stk")
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    val result2 = ValidatorUtil.encodeAndValidate(parser, medication2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  void buildFakeKbvErpMedicationCompoundingWithVaccine() {
    val medication =
        KbvErpMedicationCompoundingFaker.builder()
            .withProductionInstruction("mischen")
            .withVaccine(fakerBool())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeKbvErpMedicationCompoundingWithProductionInstruction() {
    val medication =
        KbvErpMedicationCompoundingFaker.builder()
            .withPackaging("fass")
            .withProductionInstruction(ProductionInstruction.asCompounding("freitext"))
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeKbvErpMedicationCompoundingWithMedicationIngredient() {
    val medication =
        KbvErpMedicationCompoundingFaker.builder()
            .withPackaging("Fassabfüllung")
            .withMedicationIngredient(PZN.random().getValue(), fakerDrugName())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
  }
}
