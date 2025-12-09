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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
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
        GemErpMedicationFaker.forPznMedication().withPzn(pzn, "Gematico Diabetestherapie").fake();

    val medicationDispense =
        ErxMedicationDispenseDiGABuilder.forKvnr(kvnr)
            .version(ErpWorkflowVersion.V1_4)
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
    assertEquals(PrescriptionId.from(prescriptionId), medicationDispense.getPrescriptionId());
    assertTrue(medicationDispense.getDosageInstruction().isEmpty());
    assertTrue(medicationDispense.getNote().isEmpty());
  }

  @Test
  void shouldBuildDeclineEvdgaDispensation() {
    val md =
        ErxMedicationDispenseDiGABuilder.forKvnr(KVNR.random())
            .performerId(TelematikID.random().getValue())
            .note("Formfehler in der Schreibweise, daher wird die Verordnung zurückgewiesen")
            .prescriptionId(PrescriptionId.random())
            .build();
    val result = parser.isValid(md);
    assertTrue(result);
  }

  @Test
  void shouldBuildMinimalDispensationWithRedeem() {
    val medication =
        GemErpMedicationFaker.forPznMedication()
            .withPzn(PZN.random(), "Gematico Diabetestherapie")
            .fake();

    val md =
        ErxMedicationDispenseDiGABuilder.forKvnr(KVNR.random())
            .medication(medication)
            .performerId(TelematikID.random())
            .redeemCode("REDEEMCODE")
            .prescriptionId(PrescriptionId.random())
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, md);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldBuildMinimalDispensationWithDeepLink() {
    val medication =
        GemErpMedicationFaker.forPznMedication()
            .withPzn(PZN.random(), "Gematico Diabetestherapie")
            .fake();
    val deeplink = "https://gematico.de?redeemCode=DE12345678901234";
    val md =
        ErxMedicationDispenseDiGABuilder.forKvnr(KVNR.random())
            .medication(medication)
            .performerId(TelematikID.random())
            .deepLink(deeplink)
            .prescriptionId(PrescriptionId.random())
            .build();
    assertEquals(deeplink, md.getDeepLink().get().getValue());
    assertFalse(md.isDeclined());
  }

  @Test
  void shouldBuildMinimalDispensationWithoutDeepLinkAndRedeemCode() {
    val md =
        ErxMedicationDispenseDiGABuilder.forKvnr(KVNR.random())
            .performerId(TelematikID.random().getValue())
            .note("Formfehler in der Schreibweise, daher wird die Verordnung zurückgewiesen")
            .prescriptionId(PrescriptionId.random())
            .build();
    assertTrue(md.isDeclined());
    assertTrue(parser.isValid(md));
  }
}
