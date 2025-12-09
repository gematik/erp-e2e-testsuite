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

package de.gematik.test.erezept.fhir.builder.erp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.builder.kbv.KbvIngredientComponentBuilder;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import lombok.val;
import org.hl7.fhir.r4.model.Quantity;
import org.junit.jupiter.api.Test;

class KbvIngredientComponentBuilderTest extends ErpFhirParsingTest {

  @Test
  void shouldBuildMultipleCorrectIngredientCode() {
    val ingredientCode =
        KbvIngredientComponentBuilder.builder()
            .ingredientStrength(new Quantity(3), new Quantity(1))
            .ask(ASK.from("code"))
            .atc(ATC.from("code"))
            .darreichungsform("kbvDdarreichungsform")
            .snomed("snomed")
            .ask(ASK.from("code2"))
            .atc(ATC.from("code2"))
            .pzn(PZN.from("pzn-code"))
            .darreichungsform("kbvDdarreichungsform")
            .snomed("snomed2")
            .build();
    assertEquals(7, ingredientCode.getItemCodeableConcept().getCoding().size());
    assertEquals(1, ingredientCode.getExtension().size());
  }

  @Test
  void shouldBuildMinimalCorrectIngredientCode() {
    val ingredientCode =
        KbvIngredientComponentBuilder.builder()
            .ingredientStrength(3, "l", 1, "qm")
            .darreichungsform("kbvDdarreichungsform")
            .snomed("snomed2")
            .build();
    assertEquals(1, ingredientCode.getItemCodeableConcept().getCoding().size());
    assertEquals(1, ingredientCode.getExtension().size());
  }

  @Test
  void shouldBuildCorrectIngredientCode() {
    val ingredientCode =
        KbvIngredientComponentBuilder.builder()
            .ingredientStrength(new Quantity(3), new Quantity(1), "mg")
            .withStrengthNumSystem()
            .ask(ASK.from("askCode"))
            .atc(ATC.from("AtcCode"))
            .pzn(PZN.from("pzn-code"))
            .darreichungsform("kbvDdarreichungsform")
            .snomed("snomed")
            .build();
    assertEquals(
        "pzn-code",
        ingredientCode.getItemCodeableConcept().getCoding().stream()
            .filter(DeBasisProfilCodeSystem.PZN::matches)
            .findFirst()
            .get()
            .getCode());
    assertEquals(
        String.valueOf(3), String.valueOf(ingredientCode.getStrength().getNumerator().getValue()));
    assertEquals(
        String.valueOf(1),
        String.valueOf(ingredientCode.getStrength().getDenominator().getValue()));
    assertEquals(
        String.valueOf(4),
        String.valueOf(ingredientCode.getItemCodeableConcept().getCoding().size()));
    assertEquals(
        "AtcCode",
        ingredientCode.getItemCodeableConcept().getCoding().stream()
            .filter(c -> c.getSystem().contains("atc"))
            .findFirst()
            .get()
            .getCode());
    assertEquals(
        "askCode",
        ingredientCode.getItemCodeableConcept().getCoding().stream()
            .filter(c -> c.getSystem().contains("ask"))
            .findFirst()
            .get()
            .getCode());
    assertEquals(
        "snomed",
        ingredientCode.getItemCodeableConcept().getCoding().stream()
            .filter(c -> c.getSystem().contains("snomed"))
            .findFirst()
            .get()
            .getCode());
  }

  @Test
  void shouldFillMissingIngredientStrengthAttributes() {
    val ingredientCode =
        KbvIngredientComponentBuilder.builder()
            .ingredientStrength(null, null)
            .ask(ASK.from("askCode"))
            .build();
    assertTrue(ingredientCode.getStrength().hasDenominator());
    assertTrue(ingredientCode.getStrength().hasNumerator());
  }

  @Test
  void shouldNotFillMissingIngredientStrengthAttributes() {
    val ingredientCode =
        KbvIngredientComponentBuilder.builder()
            .dontFillMissingIngredientStrength()
            .ask(ASK.from("askCode"))
            .build();
    assertFalse(ingredientCode.hasStrength());
  }

  @Test
  void shouldNotFillMissingIngredientStrengthWhileIsComplete() {
    val ingredientCode =
        KbvIngredientComponentBuilder.builder()
            .ask(ASK.from("askCode"))
            .withStrengthNumSystem()
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
  void shouldSetKbvDarreichungsformFreitextExtension() {
    val darreichungsformFreitext = "Freitext";
    var builder =
        KbvIngredientComponentBuilder.builder()
            .withKbvDarreichungsform(
                KbvItaErpStructDef.MEDICATION_INGREDIENT_AMOUNT, darreichungsformFreitext);

    var ingredient = builder.build();

    boolean hasExtension =
        ingredient.getExtension().stream()
            .anyMatch(
                ext ->
                    darreichungsformFreitext.equals(ext.getValueAsPrimitive().getValueAsString()));

    assertTrue(hasExtension, "Die KBV Darreichungsform Freitext Extension sollte gesetzt sein.");
    assertEquals(
        darreichungsformFreitext,
        ingredient.getExtension().get(0).getValueAsPrimitive().getValueAsString(),
        "Die KBV Darreichungsform Freitext Extension sollte den korrekten Wert haben.");
  }
}
