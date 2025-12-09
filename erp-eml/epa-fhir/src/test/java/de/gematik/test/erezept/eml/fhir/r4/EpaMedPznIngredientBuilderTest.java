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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.eml.fhir.profile.EpaMedicationStructDef;
import de.gematik.test.erezept.eml.fhir.profile.EpaMedicationVersion;
import de.gematik.test.erezept.eml.fhir.testutil.EpaFhirParsingTest;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.junit.jupiter.api.Test;

class EpaMedPznIngredientBuilderTest extends EpaFhirParsingTest {

  @Test
  void shouldBuildEpaMedPznIngredient() {
    val epaPznIngr =
        EpaMedPznIngredientBuilder.builder()
            .pzn(PZN.from("123456789"), "displayText")
            .codingText("textInCoding")
            .build();

    val result = encodeAndValidate((epaPznIngr));
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldBuildMinimalEpaMedPznIngredWithCode() {
    val cC = new CodeableConcept().addCoding(PZN.random().asCoding());
    cC.getCoding().get(0).setDisplay("display");

    val epaPznIngr = EpaMedPznIngredientBuilder.builder().pzn(cC).build();

    val result = encodeAndValidate((epaPznIngr));
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldSetCorrectVersion() {
    val epaPznIngr =
        EpaMedPznIngredientBuilder.builder()
            .version(EpaMedicationVersion.V1_0_3)
            .pzn(PZN.from("123"))
            .build();

    val result = encodeAndValidate((epaPznIngr));
    assertTrue(result.isSuccessful());
    assertTrue(
        epaPznIngr.getMeta().getProfile().stream()
            .findFirst()
            .orElseThrow()
            .getValue()
            .endsWith("|1.0.3"));
  }

  @Test
  void shouldBuildMinimalEpaMedPznIngredPzn() {
    val epaPznIngr = EpaMedPznIngredientBuilder.builder().pzn(PZN.from("123")).build();

    val result = encodeAndValidate((epaPznIngr));
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldBuildMinimalEpaMedPznIngredPznWithoutVersion() {
    val epaPznIngr =
        EpaMedPznIngredientBuilder.builder().withoutVersion().pzn(PZN.from("123")).build();

    val result = encodeAndValidate((epaPznIngr));
    assertTrue(result.isSuccessful());
    assertEquals(
        EpaMedicationStructDef.MEDICATION_PZN_INGREDIENT.getCanonicalUrl(),
        epaPznIngr.getMeta().getProfile().stream().findFirst().orElseThrow().getValue());
  }

  @Test
  void shouldBuildMinimalEpaMedPznIngredPznWithDarreichungsform() {
    val testText = "darreichungsform";
    val epaPznIngr =
        EpaMedPznIngredientBuilder.builder()
            .darreichungsform(testText, "ingredItemText")
            .pzn(PZN.from("123"))
            .build();
    val result = encodeAndValidate((epaPznIngr));
    assertTrue(result.isSuccessful());
    assertEquals(testText, epaPznIngr.getDarreichungsform().orElseThrow());
  }

  @Test
  void shouldThrowCausedByMissingValues() {
    val epaPznIngrBuilder = EpaMedPznIngredientBuilder.builder();
    assertThrows(BuilderException.class, epaPznIngrBuilder::build);
  }

  @Test
  void shouldThrowCausedByToManyValues() {
    val cC = new CodeableConcept().addCoding(PZN.random().asCoding());
    cC.getCoding().get(0).setDisplay("display");
    val epaPznIngrBuilder = EpaMedPznIngredientBuilder.builder();
    epaPznIngrBuilder.pzn(PZN.random()).pzn(cC);
    assertThrows(BuilderException.class, epaPznIngrBuilder::build);
  }
}
