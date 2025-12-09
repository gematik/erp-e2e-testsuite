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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import lombok.val;
import org.hl7.fhir.r4.model.Quantity;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;

class GemErpIngredientComponentBuilderTest extends ErpFhirParsingTest {

  @ClearSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE)
  @Test
  void shouldSetIngredientNumSystemAndSoElseAutomaticForOldVersions() {
    // only to find testcase while restructure versions:
    val onlyToFindVersionByCompiler = ErpWorkflowVersion.V1_4; // NOSONAR
    System.setProperty(ERP_FHIR_PROFILES_TOGGLE, "1.4.0");

    val ingrdComponent =
        new GemErpIngredientComponentBuilder()
            .ask(ASK.from("123456"))
            .ingredientStrength(new Quantity(3), new Quantity(2))
            .darreichungsform("halt irgendwie rein damit")
            .build();
    assertNotNull(ingrdComponent);
    assertNotNull(ingrdComponent.getStrength().getNumerator().getCode());
    assertNotNull(ingrdComponent.getStrength().getNumerator().getSystem());
    assertNotNull(ingrdComponent.getStrength().getNumerator().getValue());
    assertNotNull(ingrdComponent.getStrength().getDenominator().getCode());
    assertNotNull(ingrdComponent.getStrength().getDenominator().getSystem());
    assertNotNull(ingrdComponent.getStrength().getDenominator().getValue());
  }
}
