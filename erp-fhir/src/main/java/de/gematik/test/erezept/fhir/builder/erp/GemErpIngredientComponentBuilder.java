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

import static de.gematik.test.erezept.fhir.profiles.systems.CommonCodeSystem.UCUM;

import de.gematik.test.erezept.eml.fhir.r4.componentbuilder.GematikIngredientComponentBuilder;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.GemErpIngredientComponent;
import lombok.val;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Ratio;

public class GemErpIngredientComponentBuilder
    extends GematikIngredientComponentBuilder<
        GemErpIngredientComponent, GemErpIngredientComponentBuilder> {

  public GemErpIngredientComponentBuilder() {
    super(new GemErpIngredientComponent());
  }

  public static GemErpIngredientComponentBuilder builder() {
    return new GemErpIngredientComponentBuilder();
  }

  @Override
  public GemErpIngredientComponentBuilder ingredientStrength(
      Quantity numerator, Quantity demoninator) {
    val cachedStrength = new Ratio().setNumerator(numerator).setDenominator(demoninator);
    // old behavior needs System and code
    if (ErpWorkflowVersion.getDefaultVersion().isSmallerThanOrEqualTo(ErpWorkflowVersion.V1_4)) {
      cachedStrength.getNumerator().setSystem(UCUM.getCanonicalUrl()).setCode("mg");
      cachedStrength.getDenominator().setSystem(UCUM.getCanonicalUrl()).setCode("ml");
    }
    this.strength = cachedStrength;
    return self();
  }
}
