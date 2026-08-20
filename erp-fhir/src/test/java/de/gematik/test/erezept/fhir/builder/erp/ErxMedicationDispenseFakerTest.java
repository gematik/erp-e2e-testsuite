/*
 * Copyright (Change Date see Readme), gematik GmbH
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

import static de.gematik.test.erezept.eml.fhir.profile.UseFulCodeSystems.DOSIEREINHEIT;
import static de.gematik.test.erezept.fhir.parser.ProfileFhirParserFactory.ERP_FHIR_PROFILES_TOGGLE;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.ucum.UcumCodeSystem;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.DosageDgMPBuilder;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.TimingBuilder;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.UnitsOfTimeDE;
import de.gematik.test.erezept.eml.fhir.r4.dgmp.DosageDgMP;
import de.gematik.test.erezept.eml.fhir.valuesets.BmpDosiereinheit;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.math.BigDecimal;
import java.util.Date;
import lombok.val;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.Timing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;

@ClearSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE)
class ErxMedicationDispenseFakerTest extends ErpFhirParsingTest {

  @Test
  void buildFakeMedicationDispenseWithPrescriptionId() {
    val medDispense =
        ErxMedicationDispenseFaker.builder()
            .withPrescriptionId(PrescriptionId.random().getValue())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medDispense);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeMedicationDispense() {
    val medDispense = ErxMedicationDispenseFaker.builder().fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medDispense);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeMedicationDispenseWithStatus() {
    val medDispense =
        ErxMedicationDispenseFaker.builder()
            .withStatus(MedicationDispense.MedicationDispenseStatus.COMPLETED)
            .fake();
    val medDispense2 =
        ErxMedicationDispenseFaker.builder()
            .withStatus(MedicationDispense.MedicationDispenseStatus.COMPLETED.toCode())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medDispense);
    val result2 = ValidatorUtil.encodeAndValidate(parser, medDispense2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  void shouldFakeWithGivenGemErpMedication() {
    val medication = GemErpMedicationFaker.forPznMedication().fake();
    val medDispense = ErxMedicationDispenseFaker.builder().withMedication(medication).fake();
    assertTrue(parser.isValid(medDispense));
  }

  @Test
  void buildFakeMedicationDispenseWithHandedOverDate() {
    val medDispense = ErxMedicationDispenseFaker.builder().withHandedOverDate(new Date()).fake();
    assertTrue(parser.isValid(medDispense));
  }

  @Test
  void buildFakeMedicationDispenseWithPreparedDate() {
    val medDispense = ErxMedicationDispenseFaker.builder().withPreparedDate(new Date()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medDispense);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeMedicationDispenseWithBatch() {
    val batch = new Medication.MedicationBatchComponent();
    batch.setLotNumber("123");
    val medDispense = ErxMedicationDispenseFaker.builder().withBatch(batch).fake();
    val medDispense2 = ErxMedicationDispenseFaker.builder().withBatch("123", new Date()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medDispense);
    val result2 = ValidatorUtil.encodeAndValidate(parser, medDispense2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  void buildSimpleDosage() {

    DosageDgMP dosage =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(8, UnitsOfTimeDE.WOCHE)
                    .frequency(1)
                    .period(3, Timing.UnitsOfTime.D)
                    .when("EVE")
                    .build())
            .build();
    assertNotNull(dosage);
    assertEquals(1, dosage.getDoseAndRate().size());
    assertEquals(
        "v", ((org.hl7.fhir.r4.model.Quantity) dosage.getDoseAndRate().get(0).getDose()).getCode());
    assertEquals(
        "mg",
        ((org.hl7.fhir.r4.model.Quantity) dosage.getDoseAndRate().get(0).getDose()).getUnit());
    val medDisp =
        ErxMedicationDispenseFaker.builder(ErpWorkflowVersion.V1_6)
            .withPrescriptionId(PrescriptionId.random().getValue())
            .withDgmp(dosage)
            .fake();

    Assertions.assertTrue(
        ValidatorUtil.encodeAndValidate(ErpFhirParsingTest.parser, medDisp).isSuccessful());
  }

  @Test
  void buildSimpleDosageWithTiming() {

    DosageDgMP dosage =
        DosageDgMPBuilder.dosageBuilder(2, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3, Timing.UnitsOfTime.D)
                    .frequency(1)
                    .timeOfDay("08:00:00")
                    .build())
            .build();

    val medDisp =
        ErxMedicationDispenseFaker.builder(ErpWorkflowVersion.V1_6)
            .withPrescriptionId(PrescriptionId.random().getValue())
            .withDgmp(dosage)
            .fake();

    Assertions.assertTrue(
        ValidatorUtil.encodeAndValidate(ErpFhirParsingTest.parser, medDisp, EncodingType.XML)
            .isSuccessful());

    Assertions.assertEquals(
        BigDecimal.valueOf(2.0),
        medDisp
            .getDosageInstructionFirstRep()
            .getDoseAndRateFirstRep()
            .getDoseQuantity()
            .getValue());
    Assertions.assertEquals(
        "mg",
        medDisp
            .getDosageInstructionFirstRep()
            .getDoseAndRateFirstRep()
            .getDoseQuantity()
            .getUnit());
    Assertions.assertEquals(
        DOSIEREINHEIT.getCanonicalUrl(),
        medDisp
            .getDosageInstructionFirstRep()
            .getDoseAndRateFirstRep()
            .getDoseQuantity()
            .getSystem());
    Assertions.assertEquals(
        "v",
        medDisp
            .getDosageInstructionFirstRep()
            .getDoseAndRateFirstRep()
            .getDoseQuantity()
            .getCode());
  }

  @Test
  void shouldBuildWithSpecialSystem() {
    val value = 2;
    val unit = "Tablette";
    val system = UcumCodeSystem.UCUM_URL;
    val code = "1";
    DosageDgMP dosage =
        DosageDgMPBuilder.dosageBuilder(value, unit, code)
            .timing(
                TimingBuilder.forRepeatComp().period(3, Timing.UnitsOfTime.D).frequency(2).build())
            .build();
    val medDisp =
        ErxMedicationDispenseFaker.builder(ErpWorkflowVersion.V1_6)
            .withPrescriptionId(PrescriptionId.random().getValue())
            .withDgmp(dosage)
            .fake();

    Assertions.assertTrue(
        ValidatorUtil.encodeAndValidate(ErpFhirParsingTest.parser, medDisp).isSuccessful());
    Assertions.assertEquals(
        BigDecimal.valueOf(value),
        medDisp
            .getDosageInstructionFirstRep()
            .getDoseAndRateFirstRep()
            .getDoseQuantity()
            .getValue());
    Assertions.assertEquals(
        unit,
        medDisp
            .getDosageInstructionFirstRep()
            .getDoseAndRateFirstRep()
            .getDoseQuantity()
            .getUnit());
    Assertions.assertEquals(
        system,
        medDisp
            .getDosageInstructionFirstRep()
            .getDoseAndRateFirstRep()
            .getDoseQuantity()
            .getSystem());
    Assertions.assertEquals(
        code,
        medDisp
            .getDosageInstructionFirstRep()
            .getDoseAndRateFirstRep()
            .getDoseQuantity()
            .getCode());
  }

  @Test
  void shouldSetDosageInstructionCorrect() {
    val dosageText = "DosageText";
    val medDisp =
        ErxMedicationDispenseFaker.builder(ErpWorkflowVersion.V1_6)
            .withPrescriptionId(PrescriptionId.random().getValue())
            .withDosageInstruction(dosageText)
            .fake();
    assertEquals(dosageText, medDisp.getDosageInstructionText().get(0));
  }

  @Test
  void shouldSetPatientInstructionCorrect() {
    val patientInstr = "PatientInstructionText";
    val medDisp =
        ErxMedicationDispenseFaker.builder(ErpWorkflowVersion.V1_5)
            .withPrescriptionId(PrescriptionId.random().getValue())
            .withPatientInstruction(patientInstr)
            .fake();
    assertEquals(patientInstr, medDisp.getDosageInstruction().get(0).getPatientInstruction());
  }
}
