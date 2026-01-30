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

package de.gematik.test.erezept.fhir.builder.eu;

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerDrugName;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.coding.WithSystem;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Date;
import lombok.val;
import org.junit.jupiter.api.Test;

class EuMedicationDispenseBuilderTest extends ErpFhirParsingTest {

  @Test
  void buildMedicationDispenseFixedValues() {
    val pzn = "06313728";
    val kvnr = KVNR.from("X234567890");
    val telematikId = "606358757";
    val prescriptionId = "160.100.000.000.011.09";
    val lotNumber = "123456";
    val medicationDispenseBuilder =
        EuMedicationDispenseBuilder.forKvnr(kvnr)
            .performerId(telematikId)
            .prescriptionId(prescriptionId)
            .status("completed") // default COMPLETED
            .whenPrepared(new Date())
            .whenHandedOver(new Date())
            .batch(lotNumber, new Date())
            .wasSubstituted(true);

    medicationDispenseBuilder.medication(
        EuMedicationPZNFaker.faker().withPzn(PZN.from(pzn), fakerDrugName()).fake());

    val medicationDispense = medicationDispenseBuilder.build();

    assertTrue(parser.isValid(medicationDispense));

    assertNotNull(medicationDispense.getId());

    assertTrue(
        WithSystem.anyOf(
                DeBasisProfilNamingSystem.KVID_PKV_SID, DeBasisProfilNamingSystem.KVID_GKV_SID)
            .matchesReferenceIdentifier(medicationDispense.getSubject()));
    assertEquals(kvnr.getValue(), medicationDispense.getSubjectId().getValue());
    assertEquals(PrescriptionId.from(prescriptionId), medicationDispense.getPrescriptionId());
    assertTrue(medicationDispense.getDosageInstruction().isEmpty());
    assertTrue(medicationDispense.getNote().isEmpty());
  }

  @Test
  void buildMedicationDispenseWithMultipleDosageInstructions() {
    val pzn = "06313728";
    val medication =
        EuMedicationPZNFaker.faker()
            .withPzn(PZN.from(pzn), fakerDrugName())
            //            .withVersion(kbvItaVersion)
            .fake();

    val kvnr = KVNR.from("X234567890");
    val telematikId = "606358757";
    val prescriptionId = "160.100.000.000.011.09";
    val lotNumber = "123456";
    val medicationDispense =
        EuMedicationDispenseBuilder.forKvnr(kvnr)
            .performerId(telematikId)
            .prescriptionId(prescriptionId)
            .medication(medication)
            .whenPrepared(new Date())
            .whenHandedOver(new Date())
            .batch(lotNumber, new Date())
            .wasSubstituted(true)
            .dosageInstruction("nur nach dem Essen")
            .dosageInstruction("nicht vor dem Schlafen")
            .note("in 7 Tagen Rücksprache mit dem Hausarzt halten")
            .build();

    assertNotNull(medicationDispense.getId());
    assertEquals(kvnr.getValue(), medicationDispense.getSubjectId().getValue());
    assertEquals(PrescriptionId.from(prescriptionId), medicationDispense.getPrescriptionId());
    assertEquals(2, medicationDispense.getDosageInstruction().size());
    assertEquals("nur nach dem Essen", medicationDispense.getDosageInstructionText().get(0));
    assertEquals("nicht vor dem Schlafen", medicationDispense.getDosageInstructionText().get(1));
    assertEquals(1, medicationDispense.getNote().size());
    assertEquals(
        "in 7 Tagen Rücksprache mit dem Hausarzt halten",
        medicationDispense.getNoteFirstRep().getText());
    assertTrue(parser.isValid(medicationDispense));
  }

  @Test
  void buildMedicationDispenseWithFaker01() {
    val kvnr = KVNR.from("X234567890");
    val performerId = "01234567890";
    val prescriptionId = PrescriptionId.random();

    val medicationDispense =
        EuMedicationDispenseFaker.builder()
            .withKvnr(kvnr)
            .withPerformer(performerId)
            .withPrescriptionId(prescriptionId)
            .fake();

    assertTrue(ValidatorUtil.encodeAndValidate(parser, medicationDispense).isSuccessful());

    assertNotNull(medicationDispense.getId());
    assertEquals(prescriptionId, medicationDispense.getPrescriptionId());
    assertEquals(kvnr.getValue(), medicationDispense.getSubjectId().getValue());
    assertEquals(performerId, medicationDispense.getPerformerIdFirstRep());
  }

  @Test
  void buildMedicationDispenseWithFaker02() {
    val kvnr = KVNR.from("X234567890");
    val performerId = "01234567890";
    val prescriptionId = PrescriptionId.from("200.100.000.000.011.09");
    val medicationDispense =
        EuMedicationDispenseFaker.builder()
            .withKvnr(kvnr)
            .withPerformer(performerId)
            .withPrescriptionId(prescriptionId)
            .fake();

    assertTrue(ValidatorUtil.encodeAndValidate(parser, medicationDispense).isSuccessful());

    assertNotNull(medicationDispense.getId());
    assertEquals(kvnr.getValue(), medicationDispense.getSubjectId().getValue());
    assertEquals(prescriptionId, medicationDispense.getPrescriptionId());
    assertEquals(performerId, medicationDispense.getPerformerIdFirstRep());
  }

  @Test
  void throwExceptionWhileBuildMedicationDispenseWithFaker02() {
    val erxMedicationDispensebuilder = EuMedicationDispenseBuilder.forKvnr(KVNR.random());
    val euMedication = EuMedicationBuilder.builder().build();
    erxMedicationDispensebuilder.medication(euMedication);
    assertThrows(BuilderException.class, erxMedicationDispensebuilder::build);
  }
}
