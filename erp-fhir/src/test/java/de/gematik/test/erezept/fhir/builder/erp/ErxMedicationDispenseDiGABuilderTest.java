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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Date;
import lombok.val;
import org.junit.jupiter.api.Test;

class ErxMedicationDispenseDiGABuilderTest extends ErpFhirParsingTest {

  @Test
  void shouldBuildDiGAMedicationDispenseFixedValues() {
    val kvnr = KVNR.from("X234567890");
    val telematikId = "606358757";
    val prescriptionId = "162.000.033.491.280.69";
    val pzn = PZN.from("12345678");
    val medication =
        GemErpMedicationFaker.builder().withPzn(pzn, "Gematico Diabetestherapie").fake();

    val medicationDispense =
        ErxMedicationDispenseDiGABuilder.forKvnr(kvnr)
            .version(ErpWorkflowVersion.V1_4_0)
            .performerId(telematikId)
            .prescriptionId(prescriptionId)
            .medication(medication)
            .deepLink("https://gematico.de?redeemCode=DE12345678901234")
            .redeemCode("DE12345678901234")
            .status("completed") // default COMPLETED
            .whenHandedOver(new Date())
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationDispense);
    assertTrue(result.isSuccessful());

    assertNotNull(medicationDispense.getId());
    assertEquals(kvnr.getValue(), medicationDispense.getSubjectId().getValue());
    assertEquals(new PrescriptionId(prescriptionId), medicationDispense.getPrescriptionId());
    assertTrue(medicationDispense.getDosageInstruction().isEmpty());
    assertTrue(medicationDispense.getNote().isEmpty());
  }
}
