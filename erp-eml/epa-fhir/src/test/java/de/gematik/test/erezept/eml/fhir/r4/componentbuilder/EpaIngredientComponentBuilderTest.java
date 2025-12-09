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

package de.gematik.test.erezept.eml.fhir.r4.componentbuilder;

import static de.gematik.test.erezept.eml.fhir.profile.EpaMedicationStructDef.INGREDIENT_DARREICHUNGSFORM;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.eml.fhir.testutil.EpaFhirParsingTest;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Quantity;
import org.junit.jupiter.api.Test;

class GemEpaIngredientComponentBuilderTest extends EpaFhirParsingTest {

  @Test
  void shouldBuildMinimalCorrectIngredientCode() {
    val ingredientCode =
        GemEpaIngredientComponentBuilder.builder()
            .ingredientStrength(new Quantity(3), new Quantity(1))
            .ask(ASK.from("code"))
            .atc(ATC.from("code"))
            // .strengthAmountText("kbvDdarreichungsform")
            .snomed("snomed")
            .ask(ASK.from("code2"))
            .atc(ATC.from("code2"))
            .darreichungsform("kbvDdarreichungsform")
            .strengthAmountText("StrengthFreiText")
            .snomed("snomed2")
            .build();
    assertEquals(6, ingredientCode.getItemCodeableConcept().getCoding().size());
    assertEquals(1, ingredientCode.getExtension().size());
  }

  @Test
  void shouldBuildCorrectIngredientCode() {
    val ingredientCode =
        GemEpaIngredientComponentBuilder.builder()
            .ingredientStrength(3, "ml", 1, "mg")
            .strengthNumSystem()
            .ask(ASK.from("askCode"))
            .atc(ATC.from("AtcCode"))
            .strengthAmountText("kbvDdarreichungsform")
            .snomed("snomed")
            .build();

    assertEquals(
        String.valueOf(3), String.valueOf(ingredientCode.getStrength().getNumerator().getValue()));
    assertEquals(
        String.valueOf(1),
        String.valueOf(ingredientCode.getStrength().getDenominator().getValue()));
    assertEquals(
        String.valueOf(3),
        String.valueOf(ingredientCode.getItemCodeableConcept().getCoding().size()));
    assertEquals(
        "AtcCode",
        ingredientCode.getItemCodeableConcept().getCoding().stream()
            .filter(c -> c.getSystem().contains("atc"))
            .findFirst()
            .map(Coding::getCode)
            .orElseThrow());
    assertEquals(
        "askCode",
        ingredientCode.getItemCodeableConcept().getCoding().stream()
            .filter(c -> c.getSystem().contains("ask"))
            .findFirst()
            .map(Coding::getCode)
            .orElseThrow());
    assertEquals(
        "snomed",
        ingredientCode.getItemCodeableConcept().getCoding().stream()
            .filter(c -> c.getSystem().contains("snomed"))
            .findFirst()
            .map(Coding::getCode)
            .orElseThrow());
  }

  @Test
  void shouldSetTextInCodindingCorrect() {
    val txt = "code-Text-Test";
    val ingredientCode =
        GemEpaIngredientComponentBuilder.builder()
            .ingredientStrength(null, null)
            .ask(ASK.from("askCode"))
            .ingredientCodingText(txt)
            .build();
    assertEquals(txt, ingredientCode.getItemCodeableConcept().getText());
  }

  @Test
  void shouldNotFillMissingIngredientStrengthAttributes() {
    val ingredientCode =
        GemEpaIngredientComponentBuilder.builder().ask(ASK.from("askCode")).build();
    assertFalse(ingredientCode.hasStrength());
  }

  @Test
  void shouldSetIngredientCodeText() {
    val testText = "ingredient-code-text";
    val ingredientCode =
        GemEpaIngredientComponentBuilder.builder()
            .ask(ASK.from("askCode"))
            .ingredientCodingText(testText)
            .build();
    assertEquals(testText, ingredientCode.getItemCodeableConcept().getText());
  }

  @Test
  void shouldNotFillMissingIngredientStrengthWhileIsComplete() {
    val ingredientCode =
        GemEpaIngredientComponentBuilder.builder()
            .ask(ASK.from("askCode"))
            .strengthNumSystem()
            .ingredientStrength(new Quantity(3), new Quantity(1), "mg")
            .build();
    val refNumQuantity = Quantity.fromUcum("3", "mg");
    val refDenomQuantity = Quantity.fromUcum("1", "mg");
    assertEquals(refNumQuantity.getValue(), ingredientCode.getStrength().getNumerator().getValue());
    assertEquals(refNumQuantity.getCode(), ingredientCode.getStrength().getNumerator().getCode());
    assertEquals(
        refNumQuantity.getSystem(), ingredientCode.getStrength().getNumerator().getSystem());
    assertEquals(
        refDenomQuantity.getValue(), ingredientCode.getStrength().getDenominator().getValue());
    assertEquals(
        refDenomQuantity.getCode(), ingredientCode.getStrength().getDenominator().getCode());
    assertEquals(
        refDenomQuantity.getSystem(), ingredientCode.getStrength().getDenominator().getSystem());
  }

  @Test
  void shouldSetKbvStrengthAmountTextFreitextExtension() {
    val darreichungsformFreitext = "Freitext";
    var builder =
        GemEpaIngredientComponentBuilder.builder()
            .pzn(PZN.random())
            .darreichungsform(darreichungsformFreitext);

    var ingredient = builder.build();

    boolean hasExtension =
        ingredient.getExtension().stream().anyMatch(INGREDIENT_DARREICHUNGSFORM::matches);

    assertTrue(hasExtension, "Die KBV Darreichungsform Freitext Extension sollte gesetzt sein.");
    assertEquals(
        darreichungsformFreitext,
        String.valueOf(
            ingredient.getExtension().stream()
                .filter(INGREDIENT_DARREICHUNGSFORM::matches)
                .map(Extension::getValue)
                .findFirst()
                .orElseThrow()),
        "Die KBV Darreichungsform Freitext Extension sollte den korrekten Wert haben.");
    assertDoesNotThrow(
        () ->
            ((CodeableConcept) ingredient.getItem())
                .getCoding().stream().map(Coding::getCode).findFirst().orElseThrow());
  }
}
