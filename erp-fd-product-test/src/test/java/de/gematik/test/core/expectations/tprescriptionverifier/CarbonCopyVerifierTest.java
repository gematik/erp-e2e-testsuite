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

package de.gematik.test.core.expectations.tprescriptionverifier;

import static de.gematik.test.core.expectations.verifier.tprescriptionverifier.CarbonCopyVerifier.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBuilder;
import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationFaker;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.r4.erp.tprescription.ErpTPrescriptionCarbonCopy;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import java.util.Date;
import java.util.List;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CarbonCopyVerifierTest extends ErpFhirParsingTest {

  private static ErpTPrescriptionCarbonCopy erpTPrescriptionCarbonCopy =
      parser.decode(
          ErpTPrescriptionCarbonCopy.class,
          ResourceLoader.readFileFromResource("TPrescription/Parameters-TRP-Carbon-Copy.json"));
  private List<Pair<ErxMedicationDispense, GemErpMedication>> correctMedicationDispenseBundle;
  private List<Pair<ErxMedicationDispense, GemErpMedication>> incorrectMedicationDispenseBundle;

  @BeforeEach
  void setupTestData() {
    CoverageReporter.getInstance().startTestcase("not needed");

    val correctMedication =
        GemErpMedicationFaker.forPznMedication()
            .withPzn(PZN.from("19201712"), "Pomalidomid Accord 1 mg 21 x 1 Hartkapseln")
            .withDarreichungsform(Darreichungsform.HKP)
            .fake();
    val correctMedicationDispense =
        ErxMedicationDispenseBuilder.forKvnr(KVNR.from(""))
            .version(ErpWorkflowVersion.V1_6)
            .performerId(TelematikID.random())
            .prescriptionId(PrescriptionId.from("166.100.000.000.001.39"))
            .status("completed") // default COMPLETED
            .whenPrepared(new Date())
            .whenHandedOver(new Date())
            .batch("123456", new Date())
            .wasSubstituted(true)
            .medication(correctMedication)
            .build();

    correctMedicationDispenseBundle =
        List.of(Pair.of(correctMedicationDispense, correctMedication));

    val incorrectMedication =
        GemErpMedicationFaker.forPznMedication()
            .withPzn(PZN.from("11111111"), "Fruchtgummies die extra weichen")
            .withDarreichungsform(Darreichungsform.KDA)
            .fake();
    val incorrectMedicationDispense =
        ErxMedicationDispenseBuilder.forKvnr(KVNR.from(""))
            .version(ErpWorkflowVersion.V1_6)
            .performerId(TelematikID.random())
            .prescriptionId(PrescriptionId.from("166.100.111.000.111.11"))
            .status("completed") // default COMPLETED
            .whenPrepared(new Date())
            .whenHandedOver(new Date())
            .batch("123456", new Date())
            .wasSubstituted(true)
            .medication(incorrectMedication)
            .build();
    incorrectMedicationDispenseBundle =
        List.of(Pair.of(incorrectMedicationDispense, incorrectMedication));
  }

  @Test
  void shouldVerifyPznCorrect() {
    val step = checkPznFromGemMedication(correctMedicationDispenseBundle);
    assertDoesNotThrow(() -> step.apply(erpTPrescriptionCarbonCopy));
  }

  @Test
  void shouldThrowWhileVerifiingPzn() {
    val step = checkPznFromGemMedication(incorrectMedicationDispenseBundle);
    assertThrows(AssertionError.class, () -> step.apply(erpTPrescriptionCarbonCopy));
  }

  @Test
  void shouldVerifyMedicationNameCorrect() {
    val step = checkMedicationName(correctMedicationDispenseBundle);
    assertDoesNotThrow(() -> step.apply(erpTPrescriptionCarbonCopy));
  }

  @Test
  void shouldThrowWhileVerifyMedicationName() {
    val step = checkMedicationName(incorrectMedicationDispenseBundle);
    assertThrows(AssertionError.class, () -> step.apply(erpTPrescriptionCarbonCopy));
  }

  @Test
  void shouldVerifyPrescriptionIdCorrect() {
    val step = checkPrescriptionId(correctMedicationDispenseBundle);
    assertDoesNotThrow(() -> step.apply(erpTPrescriptionCarbonCopy));
  }

  @Test
  void shouldThrowWhileVerifyPrescriptionId() {
    val step = checkPrescriptionId(incorrectMedicationDispenseBundle);
    assertThrows(AssertionError.class, () -> step.apply(erpTPrescriptionCarbonCopy));
  }

  @Test
  void shouldVerifyDarreichungsformInPrescriptionCorrect() {
    val step = checkDarreichungsformInPrescription(correctMedicationDispenseBundle);
    assertDoesNotThrow(() -> step.apply(erpTPrescriptionCarbonCopy));
  }

  @Test
  void shouldThrowWhileVerifyDarreichungsformInPrescription() {
    val step = checkDarreichungsformInPrescription(incorrectMedicationDispenseBundle);
    assertThrows(AssertionError.class, () -> step.apply(erpTPrescriptionCarbonCopy));
  }

  @Test
  void shouldVerifyDarreichungsformInDispensationCorrect() {
    val step = checkDarreichungsformInDispensation(correctMedicationDispenseBundle);
    assertDoesNotThrow(() -> step.apply(erpTPrescriptionCarbonCopy));
  }

  @Test
  void shouldThrowWhileVerifyDarreichungsformInDispensation() {
    val step = checkDarreichungsformInDispensation(incorrectMedicationDispenseBundle);
    assertThrows(AssertionError.class, () -> step.apply(erpTPrescriptionCarbonCopy));
  }
}
