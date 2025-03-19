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
 */

package de.gematik.test.erezept.fhir.builder.erp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import lombok.val;
import org.junit.jupiter.api.Test;

class ErxMedicationDispenseDiGAFakerTest extends ErpFhirParsingTest {

  @Test
  void shouldFakeDiGAMedicationDispense() {
    val medDispense =
        ErxMedicationDispenseDiGAFaker.builder().withVersion(ErpWorkflowVersion.V1_4_0).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medDispense);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldFakeDiGAMedicationDispenseWithConcreteValues() {
    val pzn = "12345678";
    val digaName = "Gematico Diabetestherapie";
    val redeemCode = "DE12345678901234";
    val deepLink = "https://gematico.de?redeemCode=DE12345678901234";

    val medDispense =
        ErxMedicationDispenseDiGAFaker.builder()
            .withVersion(ErpWorkflowVersion.V1_4_0)
            .withPzn(pzn, digaName)
            .withRedeemCode(redeemCode)
            .withDeepLink(deepLink)
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medDispense);
    assertTrue(result.isSuccessful());

    assertTrue(medDispense.isDiGA());

    assertTrue(medDispense.getDeepLink().isPresent());
    assertEquals(deepLink, medDispense.getDeepLink().get().getValue());

    assertTrue(medDispense.getRedeemCode().isPresent());
    assertEquals(redeemCode, medDispense.getRedeemCode().get().getValue());

    assertEquals(pzn, medDispense.getPzn().getValue());
    assertEquals(digaName, medDispense.getDigaName());
  }
}
