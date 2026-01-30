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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.extensions.kbv.ProductionInstruction;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import lombok.val;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Ratio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class KbvErpMedicationCompoundingBuilderTest extends ErpFhirParsingTest {

  @ParameterizedTest(
      name = "[{index}] -> Build KBV MedicationCompounding with KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaErpVersions")
  void shouldBuildWithKbvVersion(KbvItaErpVersion version) {
    val medicationCompounding =
        KbvErpMedicationCompoundingBuilder.builder()
            .productionInstruction(
                ProductionInstruction.asCompounding("freitext Z.B. gerührt nicht geschüttelt"))
            .medicationIngredient("11260676", "Fancy Salbe", "freitextInPzn")
            .version(version)
            .amount(123456L)
            .ingredientStrengthText("eine Hand voll")
            .darreichungsform(Darreichungsform.AEO)
            .build();

    assertTrue(
        medicationCompounding.getExtension().stream()
            .anyMatch(KbvItaErpStructDef.COMPOUNDING_INSTRUCTION::matches));

    assertTrue(ValidatorUtil.encodeAndValidate(parser, medicationCompounding).isSuccessful());
  }

  @Test
  void fakerShouldWork() {
    val medComp = KbvErpMedicationCompoundingFaker.builder().fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medComp);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest()
  @ValueSource(strings = {"Comp", "Package", "null"})
  void shouldBuildAsCompoundingAndPackage(String bool) {
    ProductionInstruction productionInstruction = null;
    if (bool.equals("Comp")) {
      productionInstruction =
          ProductionInstruction.asCompounding("freitext Z.B.: gerührt nicht geschüttelt");
    } else {
      if (bool.equals("Package")) {
        productionInstruction =
            ProductionInstruction.asPackaging("freitext Z.B.: Achtung! jetzt kommt ein Karton!!!");
      }
    }
    val medicationCompounding =
        KbvErpMedicationCompoundingBuilder.builder()
            .productionInstruction(productionInstruction)
            .darreichungsform("Nasentropfen")
            .ingredientStrengthText("eine Hand voll")
            .medicationIngredient("11260676", "Fancy Salbe")
            .amount(1);

    if (productionInstruction != null) {
      val result = ValidatorUtil.encodeAndValidate(parser, medicationCompounding.build());
      assertTrue(result.isSuccessful());
    } else {
      assertThrows(BuilderException.class, medicationCompounding::build);
    }
  }

  @Test
  void builderWithoutRequiredShouldThrowException1() {
    val builder = KbvErpMedicationCompoundingBuilder.builder();
    assertThrows(BuilderException.class, builder::build);
  }

  @Test
  void builderWithoutRequiredShouldThrowException2() {
    val builder2 =
        KbvErpMedicationCompoundingBuilder.builder()
            .productionInstruction(ProductionInstruction.random())
            .ingredientStrength(
                new Ratio()
                    .setNumerator(new Quantity().setValue(5).setUnit(" mg"))
                    .setDenominator(new Quantity().setUnit("g").setValue(1)));
    assertThrows(BuilderException.class, builder2::build);
  }

  @Test
  void fakerWithoutRequiredShouldThrowException3() {
    val builder3 =
        KbvErpMedicationCompoundingBuilder.builder()
            .productionInstruction(ProductionInstruction.random());

    assertThrows(BuilderException.class, builder3::build);
  }

  @Test
  void fakerWithoutRequiredShouldThrowException4() {
    val builder4 = KbvErpMedicationCompoundingBuilder.builder().medicationIngredient("p", "pp");

    assertThrows(BuilderException.class, builder4::build);
  }

  @Test
  void fakerWithoutRequiredShouldThrowException() {
    val builder5 = KbvErpMedicationCompoundingBuilder.builder().darreichungsform("Zäpfchen");
    assertThrows(BuilderException.class, builder5::build);
  }

  @Test
  void fakerWithoutRequiredShouldThrowException5() {
    val builder5 =
        KbvErpMedicationCompoundingBuilder.builder()
            .darreichungsform("Zäpfchen")
            .medicationIngredient(PZN.random().getValue(), "testText");
    assertThrows(BuilderException.class, builder5::build);
  }

  @Test
  void builderWithoutRequiredShouldThrowExceptionWhilePackagingAndProductionInstruction() {
    val builder5 =
        KbvErpMedicationCompoundingBuilder.builder()
            .darreichungsform("Zäpfchen")
            .medicationIngredient(PZN.random().getValue(), "testText")
            .productionInstruction(ProductionInstruction.random())
            .packaging("packung");
    assertThrows(BuilderException.class, builder5::build);
  }

  @Test
  void
      builderWithoutRequiredShouldThrowCausedByIngredientStrengthAndIngredientStrengthFreeTextIsMissing() {
    val builder =
        KbvErpMedicationCompoundingBuilder.builder()
            .darreichungsform("Zäpfchen")
            .ingredientItemText("text in Medication.ingredient.item[x]:itemCodeableConcept.text")
            .packaging("packung");
    assertThrows(BuilderException.class, builder::build);
  }

  @Test
  void builderShouldThrowExceptionIfIngredientItemTextIsNull() {
    val builder =
        KbvErpMedicationCompoundingBuilder.builder()
            .ingredientStrengthText("ingredStrengthText")
            .darreichungsform("bitte löffeln")
            .packaging("packung");
    assertThrows(BuilderException.class, builder::build);
  }

  @Test
  void shouldThrowExceptionIfIngredientStrengthAndFreeTextAreMissing() {
    var builder =
        KbvErpMedicationCompoundingBuilder.builder()
            .productionInstruction(ProductionInstruction.asCompounding("Herstellungshinweis"))
            .darreichungsform("Tablette")
            .medicationIngredient("12345678", "Testmedikation", "Freitext");

    assertThrows(
        BuilderException.class,
        builder::build,
        "Es muss eine BuilderException geworfen werden, wenn ingredientStrength und"
            + " ingredientStrengthFreeText fehlen.");
  }
}
