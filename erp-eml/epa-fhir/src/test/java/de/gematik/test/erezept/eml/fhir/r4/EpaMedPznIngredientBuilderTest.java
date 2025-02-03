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

package de.gematik.test.erezept.eml.fhir.r4;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.eml.fhir.EpaFhirFactory;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.junit.jupiter.api.Test;

class EpaMedPznIngredientBuilderTest {

  @Test
  void shouldBuildEpaMedPznIngred() {
    val epaPznIngr =
        EpaMedPznIngredientBuilder.builder()
            .withPzn(PZN.from("123456789"), "displayText")
            .withCodingText("textInCoding")
            .build();
    val fhir = EpaFhirFactory.create();
    val pznIngreAsString = fhir.encode(epaPznIngr, EncodingType.XML, true);
    val isValid = fhir.isValid(pznIngreAsString);
    assertTrue(isValid);
  }

  @Test
  void shouldBuildMinimalEpaMedPznIngredWithCode() {
    val cC = new CodeableConcept().addCoding(PZN.random().asCoding());
    cC.getCoding().get(0).setDisplay("display");

    val epaPznIngr = EpaMedPznIngredientBuilder.builder().withPzn(cC).build();
    val fhir = EpaFhirFactory.create();
    val pznIngreAsString = fhir.encode(epaPznIngr, EncodingType.XML, true);
    val isValid = fhir.isValid(pznIngreAsString);
    assertTrue(isValid);
  }

  @Test
  void shouldBuildMinimalEpaMedPznIngredWithPZN() {
    val epaPznIngr = EpaMedPznIngredientBuilder.builder().withPzn(PZN.from("123")).build();
    val fhir = EpaFhirFactory.create();
    val pznIngreAsString = fhir.encode(epaPznIngr, EncodingType.XML, true);
    val isValid = fhir.isValid(pznIngreAsString);
    assertTrue(isValid);
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
    epaPznIngrBuilder.withPzn(PZN.random()).withPzn(cC);
    assertThrows(BuilderException.class, epaPznIngrBuilder::build);
  }
}
