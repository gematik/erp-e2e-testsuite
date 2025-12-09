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

package de.gematik.test.erezept.eml.fhir.r4;

import static de.gematik.test.erezept.eml.fhir.profile.UseFulCodeSystems.BFARM_CS_MED_REF;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.test.erezept.eml.fhir.profile.UseFulCodeSystems;
import de.gematik.test.erezept.eml.fhir.testutil.EpaFhirParsingTest;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EpaPharmaceuticalProductTest extends EpaFhirParsingTest {

  static EpaPharmaceuticalProduct epaMedPharmProduct;
  static String productKey = "123456";
  static String productDisplay = "Produktanzeige";
  static String productText = "codingText";
  static ASK askCode = ASK.from("ASK123");
  static ATC atcCode = ATC.from("ATC456");
  static String snomedCode = "SNOMED789";
  static String ingredientAtcCode = "INGATC001";
  static String ingredientDisplay = "IngredientDisplay";
  static double strengthNumeratorValue = 5.0;
  static String strengthNumeratorUnit = "mg";
  static double strengthDenominatorValue = 10.0;
  static String strengthDenominatorUnit = "ml";

  static Quantity numerator =
      new Quantity().setValue(strengthNumeratorValue).setUnit(strengthNumeratorUnit);

  static Quantity denominator =
      new Quantity().setValue(strengthDenominatorValue).setUnit(strengthDenominatorUnit);

  static Ratio strengthRatio = new Ratio().setNumerator(numerator).setDenominator(denominator);

  static Medication.MedicationIngredientComponent ingredientComponent =
      new Medication.MedicationIngredientComponent()
          .setItem(
              new org.hl7.fhir.r4.model.CodeableConcept()
                  .addCoding(
                      new org.hl7.fhir.r4.model.Coding()
                          .setSystem(DeBasisProfilCodeSystem.ATC.getCanonicalUrl())
                          .setCode(ingredientAtcCode)
                          .setDisplay(ingredientDisplay)))
          .setStrength(strengthRatio);

  @BeforeAll
  static void setup() {
    epaMedPharmProduct =
        EpaPharmaceuticalProdBuilder.builder()
            .productKey(productKey, productDisplay)
            .codingText(productText)
            .askCode(askCode)
            .atcCode(atcCode)
            .snomedCode(snomedCode)
            .ingredientComponent(ingredientComponent)
            .build();
  }

  @Test
  void shouldTestProductKey() {
    Optional<String> key = epaMedPharmProduct.getProductKey();
    assertTrue(key.isPresent());
    assertEquals(productKey, key.orElseThrow());
    assertTrue(epaFhir.validate(epaMedPharmProduct).isSuccessful());
  }

  @Test
  void shouldTestProductDisplay() {
    Optional<String> display = epaMedPharmProduct.getProductKeyDisplay();
    assertTrue(display.isPresent());
    assertEquals(productDisplay, display.get());
  }

  @Test
  void shouldTestProductText() {
    Optional<String> system =
        epaMedPharmProduct.getCode().getCoding().stream()
            .filter(c -> UseFulCodeSystems.BFARM_CS_MED_REF.matches(c.getSystem()))
            .map(Coding::getSystem)
            .findFirst();
    assertEquals(BFARM_CS_MED_REF.getCanonicalUrl(), system.orElseThrow());
  }

  @Test
  void shouldTestAskCode() {
    Optional<ASK> ask = epaMedPharmProduct.getAskCode();
    assertTrue(ask.isPresent());
    assertEquals(askCode, ask.get());
  }

  @Test
  void shouldGetOptionalEmptyCausedByMissingIngredientComponent() {
    val epaMedPharmProd =
        EpaPharmaceuticalProdBuilder.builder()
            .productKey(productKey, productDisplay)
            .codingText(productText)
            .askCode(askCode)
            .atcCode(atcCode)
            .snomedCode(snomedCode)
            .build();
    assertTrue(epaMedPharmProd.getFirstIngredient().isEmpty());
  }

  @Test
  void shouldDedectIngredientComponent() {
    assertTrue(epaMedPharmProduct.getFirstIngredient().isPresent());
  }

  @Test
  void shouldTestAtcCode() {
    Optional<ATC> atc = epaMedPharmProduct.getAtcCode();
    assertTrue(atc.isPresent());
    assertEquals(atcCode, atc.get());
  }

  @Test
  void shouldTestSnomedCode() {
    Optional<String> snomed = epaMedPharmProduct.getSnomedCode();
    assertTrue(snomed.isPresent());
    assertEquals(snomedCode, snomed.get());
  }

  @Test
  void testIngredientValues() {
    assertEquals(ingredientAtcCode, epaMedPharmProduct.getIngredientAtcCode().orElse(null));
    assertEquals(ingredientDisplay, epaMedPharmProduct.getIngredientAtcDisplay().orElse(null));
    assertEquals(
        strengthNumeratorValue,
        epaMedPharmProduct.getIngredientStrengthNumeratorValue().orElse(0.0));
    assertEquals(
        strengthNumeratorUnit,
        epaMedPharmProduct.getIngredientStrengthNumeratorUnit().orElse(null));
    assertEquals(
        strengthDenominatorValue,
        epaMedPharmProduct.getIngredientStrengthDenominatorValue().orElse(0.0));
    assertEquals(
        strengthDenominatorUnit,
        epaMedPharmProduct.getIngredientStrengthDenominatorUnit().orElse(null));
  }

  @Test
  void shouldValidateCorrect() {
    val result = epaFhir.validate(epaMedPharmProduct);
    assertTrue(result.isSuccessful(), result.toString());
  }

  @Test
  void shouldGetReadableStrength() {
    assertEquals("5 mg pro 10 ml", epaMedPharmProduct.getIngredientStrengthReadable());
  }
}
