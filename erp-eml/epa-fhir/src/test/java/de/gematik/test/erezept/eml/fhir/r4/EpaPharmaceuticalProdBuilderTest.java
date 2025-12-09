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

import static de.gematik.test.erezept.eml.fhir.profile.EpaMedicationStructDef.PHARMACEUTICAL_PROD;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.test.erezept.eml.fhir.profile.EpaMedicationVersion;
import de.gematik.test.erezept.eml.fhir.profile.UseFulCodeSystems;
import de.gematik.test.erezept.eml.fhir.r4.componentbuilder.GemEpaIngredientComponentBuilder;
import de.gematik.test.erezept.eml.fhir.testutil.EpaFhirParsingTest;
import lombok.val;
import org.junit.jupiter.api.Test;

class EpaPharmaceuticalProdBuilderTest extends EpaFhirParsingTest {

  @Test
  void shouldBuildEpaMedPharmaceuticalProd() {
    val pharmaceuticalProd =
        EpaPharmaceuticalProdBuilder.builder().productKey("123_Codfe", "displayText").build();

    val result = encodeAndValidate((pharmaceuticalProd));

    assertTrue(result.isSuccessful());

    assertEquals("123_Codfe", pharmaceuticalProd.getProductKey().orElseThrow());
  }

  @Test
  void shouldBuildEpaMedPharmaceuticalProdWithoutVersion() {
    val pharmaceuticalProd =
        EpaPharmaceuticalProdBuilder.builder()
            .withoutVersion()
            .productKey("123_Codfe", "displayText")
            .build();

    val result = encodeAndValidate((pharmaceuticalProd));
    assertTrue(result.isSuccessful());
    assertEquals(
        PHARMACEUTICAL_PROD.getCanonicalUrl(),
        pharmaceuticalProd.getMeta().getProfile().stream().findFirst().orElseThrow().getValue());
  }

  @Test
  void shouldGetCodingText() {
    var builder =
        EpaPharmaceuticalProdBuilder.builder().codingText("TestText").productKey("123", "Display");
    var prod = builder.build();

    assertEquals("TestText", prod.getCode().getText());
    val result = encodeAndValidate((prod));
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldGetNoCodingTex() {
    var builder = EpaPharmaceuticalProdBuilder.builder().productKey("123", "Display");
    var prod = builder.build();
    assertNull(prod.getCode().getText());
    assertNotEquals("123", prod.getCode().getText());

    val result = encodeAndValidate((prod));
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldGetProductKey() {
    var builder = EpaPharmaceuticalProdBuilder.builder().productKey("456", "Display456");
    var prod = builder.build();
    assertTrue(
        prod.getCode().getCoding().stream()
            .anyMatch(c -> c.getCode().equals("456") && c.getDisplay().equals("Display456")));

    val result = encodeAndValidate((prod));
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldGetAskCode() {
    val testValue = ASK.from("ASK_1");
    var builder =
        EpaPharmaceuticalProdBuilder.builder().productKey("123", "Display").askCode(testValue);
    var prod = builder.build();
    assertEquals(testValue, prod.getAskCode().orElseThrow());
    val result = encodeAndValidate((prod));
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldGetAtcCode() {
    val testValue = ATC.from("ATC1");
    var builder =
        EpaPharmaceuticalProdBuilder.builder().productKey("123", "Display").atcCode(testValue);
    var prod = builder.build();
    assertEquals(testValue, prod.getAtcCode().orElseThrow());
    val result = encodeAndValidate((prod));
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldGetSnomedCode() {
    var builder =
        EpaPharmaceuticalProdBuilder.builder().productKey("123", "Display").snomedCode("987654");
    var prod = builder.build();
    assertTrue(
        prod.getCode().getCoding().stream()
            .anyMatch(
                c ->
                    c.getSystem().equals(UseFulCodeSystems.SNOMED_SCT.getCanonicalUrl())
                        && c.getCode().equals("987654")));
    val result = encodeAndValidate((prod));
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldGetIngredientComponent() {
    val ingred = GemEpaIngredientComponentBuilder.builder().atc(ATC.from("A01AB")).build();
    var prod =
        EpaPharmaceuticalProdBuilder.builder()
            .ingredientComponent(ingred)
            .productKey("123", "Display")
            .build();

    val result = encodeAndValidate((prod));
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldGetNoIngredientComponent() {
    var builder = EpaPharmaceuticalProdBuilder.builder().productKey("123", "Display");
    var prod = builder.build();
    assertTrue(prod.getIngredient().isEmpty());

    val result = encodeAndValidate((prod));
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldSetVersionCorrect() {
    val version = EpaMedicationVersion.V1_0_3;
    val med =
        EpaPharmaceuticalProdBuilder.builder().version(version).productKey("132", "Dsp").build();
    val result = encodeAndValidate((med));
    assertTrue(result.isSuccessful());
    assertTrue(
        med.getMeta().getProfile().stream()
            .findFirst()
            .orElseThrow()
            .getValue()
            .endsWith("|1.0.3"));
  }
}
