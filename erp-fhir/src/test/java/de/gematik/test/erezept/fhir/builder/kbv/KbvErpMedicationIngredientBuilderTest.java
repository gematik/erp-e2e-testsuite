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

import de.gematik.test.erezept.fhir.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import java.math.BigDecimal;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class KbvErpMedicationIngredientBuilderTest extends ParsingTest {

  @Test
  void shouldBuildCorrect() {
    val mI =
        KbvErpMedicationIngredientBuilder.builder()
            .darreichungsform("aufs Auge")
            .ingredientComponent(3, 1, "wölkchen")
            .drugName("MonsterPille")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, mI);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldFakeCorrect() {
    val mI = KbvErpMedicationIngredientFaker.builder().fake();
    val result = ValidatorUtil.encodeAndValidate(parser, mI);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldFakeCorrectWithDrugName() {
    val drugName = "TestName";
    val mI = KbvErpMedicationIngredientFaker.builder().withDrugName(drugName).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, mI);
    assertEquals(
        drugName,
        mI.getIngredientFirstRep().getItem().getChildByName("text").getValues().get(0).toString());
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldFakeCorrectWithDrugNameAndConsumeDescription() {
    val drugName = "TestName";
    val darreichung = "darreichungsform";
    val mI =
        KbvErpMedicationIngredientFaker.builder()
            .withDosageForm(darreichung)
            .withDrugName(drugName)
            .fake();
    assertEquals(
        drugName,
        mI.getIngredientFirstRep().getItem().getChildByName("text").getValues().get(0).toString());
    assertEquals(darreichung, mI.getForm().getText());
    val result = ValidatorUtil.encodeAndValidate(parser, mI);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(name = "[{index}] -> Build KBV MedicationIngredient with KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaErpVersions")
  void shouldSetCorrectVersion(KbvItaErpVersion version) {
    val mI =
        KbvErpMedicationIngredientBuilder.builder()
            .version(version)
            .drugName("fancyName")
            .darreichungsform("10 Tropfen 1-0-1-0")
            .ingredientComponent("Tropfen")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, mI);
    assertTrue(result.isSuccessful());
    assertEquals(version, mI.getVersion());
    assertEquals("Tropfen", mI.getIngredientFirstRep().getStrength().getNumerator().getUnit());
  }

  @Test
  void shouldSetCorrectCategory() {
    val mI =
        KbvErpMedicationIngredientBuilder.builder()
            .drugName("fancyName")
            .darreichungsform("10 Tropfen 1-0-1-0")
            .ingredientComponent("Tropfen")
            .category(MedicationCategory.C_00)
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, mI);
    assertTrue(result.isSuccessful());
    assertEquals(MedicationCategory.C_00, mI.getCategoryFirstRep());
  }

  @Test
  void shouldSetCorrectVaccine() {
    val mI =
        KbvErpMedicationIngredientBuilder.builder()
            .drugName("fancyName")
            .darreichungsform("10 Tropfen 1-0-1-0")
            .ingredientComponent("Tropfen")
            .isVaccine(true)
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, mI);
    assertTrue(result.isSuccessful());
    assertTrue(mI.isVaccine());
  }

  @Test
  void shouldSetCorrectStandardSize() {
    val mI =
        KbvErpMedicationIngredientBuilder.builder()
            .drugName("fancyName")
            .darreichungsform("10 Tropfen 1-0-1-0")
            .ingredientComponent("Tropfen")
            .normGroesse(StandardSize.NB)
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, mI);
    assertTrue(result.isSuccessful());
    assertEquals(StandardSize.NB, mI.getStandardSize());
  }

  @Test
  void shouldSetCorrectAmount() {
    val mI =
        KbvErpMedicationIngredientBuilder.builder()
            .drugName("fancyName")
            .darreichungsform("10 Tropfen 1-0-1-0")
            .ingredientComponent("Tropfen")
            .amount("5")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, mI);
    assertTrue(result.isSuccessful());
    assertEquals("5", mI.getAmount().getNumerator().getExtensionFirstRep().getValue().toString());
  }

  @Test
  void shouldSetCorrectAmount2() {
    val mI =
        KbvErpMedicationIngredientBuilder.builder()
            .drugName("fancyName")
            .darreichungsform("10 Tropfen 1-0-1-0")
            .ingredientComponent("Tropfen")
            .amount("5", 1, "Schokotaler")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, mI);
    assertTrue(result.isSuccessful());
    assertEquals("5", mI.getAmount().getNumerator().getExtensionFirstRep().getValue().toString());
    assertEquals("Schokotaler", mI.getAmount().getNumerator().getUnit());
    assertEquals(1, mI.getAmount().getDenominator().getValue().intValue());
  }

  @Test
  void shouldSetCorrectAmount3() {
    val mI =
        KbvErpMedicationIngredientBuilder.builder()
            .drugName("fancyName")
            .darreichungsform("10 Tropfen 1-0-1-0")
            .ingredientComponent("Tropfen")
            .amount("5", "Schokotaler")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, mI);
    assertTrue(result.isSuccessful());
    assertEquals("5", mI.getAmount().getNumerator().getExtensionFirstRep().getValue().toString());
    assertEquals("Schokotaler", mI.getAmount().getNumerator().getUnit());
  }

  @Test
  void shouldCheckRequired() {
    var builder = KbvErpMedicationIngredientBuilder.builder();
    assertThrows(BuilderException.class, builder::build);
  }

  @Test
  void shouldSetCorrectDarreichung() {
    val mI =
        KbvErpMedicationIngredientBuilder.builder()
            .drugName("fancyName")
            .ingredientComponent("Tropfen")
            .darreichungsform("DR")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, mI);
    assertEquals("DR", mI.getForm().getText());
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldSetCorrectIngredient() {
    val mI =
        KbvErpMedicationIngredientBuilder.builder()
            .drugName("fancyName")
            .darreichungsform("10 Tropfen 1-0-1-0")
            .ingredientComponent(3L, 1, "bälle")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, mI);
    assertTrue(result.isSuccessful());
    assertEquals(
        BigDecimal.valueOf(3), mI.getIngredientFirstRep().getStrength().getNumerator().getValue());
  }
}
