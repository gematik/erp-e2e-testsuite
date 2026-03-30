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

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerDrugName;
import static de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest.ERP_FHIR_PROFILES_TOGGLE;
import static org.junit.jupiter.api.Assertions.*;

import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.coding.WithSystem;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.erezept.eml.fhir.valuesets.EpaDrugCategory;
import de.gematik.test.erezept.fhir.builder.dgmp.DosageDgMPBuilder;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.dgmp.DosageDgMP;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.BmpDosiereinheit;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

@ClearSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE)
class ErxMedicationDispenseBuilderTest extends ErpFhirParsingTest {

  @ParameterizedTest(name = "[{index}] -> Build MedicationDispense with ErpWorkflowVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void buildMedicationDispenseFixedValues(ErpWorkflowVersion version) {
    val pzn = "06313728";
    val kvnr = KVNR.from("X234567890");
    val telematikId = "606358757";
    val prescriptionId = "160.100.000.000.011.09";
    val lotNumber = "123456";
    val medicationDispenseBuilder =
        ErxMedicationDispenseBuilder.forKvnr(kvnr)
            .version(version)
            .performerId(telematikId)
            .prescriptionId(prescriptionId)
            .status("completed") // default COMPLETED
            .whenPrepared(new Date())
            .whenHandedOver(new Date())
            .batch(lotNumber, new Date())
            .wasSubstituted(true);

    medicationDispenseBuilder.medication(
        GemErpMedicationFaker.forPznMedication().withPzn(PZN.from(pzn), fakerDrugName()).fake());

    val medicationDispense = medicationDispenseBuilder.build();

    assertTrue(getRes(medicationDispense).isSuccessful());

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

  @ParameterizedTest(name = "[{index}] -> Build MedicationDispense with ErpWorkflowVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void buildMedicationDispenseWithMultipleDosageInstructions(ErpWorkflowVersion version) {
    val pzn = "06313728";
    val medication =
        GemErpMedicationFaker.forPznMedication(version)
            .withPzn(PZN.from(pzn), fakerDrugName())
            .fake();
    DosageDgMP dosage =
        DosageDgMPBuilder.dosageBuilder("Tablette", BmpDosiereinheit.MIO_E)
            .text("1 Tablette morgens")
            .value(new BigDecimal(1))
            .build();

    val kvnr = KVNR.from("X234567890");
    val telematikId = "606358757";
    val prescriptionId = "160.100.000.000.011.09";
    val lotNumber = "123456";
    val medicationDispense =
        ErxMedicationDispenseBuilder.forKvnr(kvnr)
            .version(version)
            .performerId(telematikId)
            .prescriptionId(prescriptionId)
            .medication(medication)
            .whenPrepared(new Date())
            .whenHandedOver(new Date())
            .batch(lotNumber, new Date())
            .wasSubstituted(true)
            .dgmp(dosage)
            .note("in 7 Tagen Rücksprache mit dem Hausarzt halten")
            .build();

    assertNotNull(medicationDispense.getId());
    assertEquals(kvnr.getValue(), medicationDispense.getSubjectId().getValue());
    assertEquals(PrescriptionId.from(prescriptionId), medicationDispense.getPrescriptionId());
    if (version.isSmallerThanOrEqualTo(ErpWorkflowVersion.V1_5)) {

      assertEquals(1, medicationDispense.getDosageInstruction().size());
      assertEquals("1 Tablette morgens", medicationDispense.getDosageInstructionText().get(0));

    } else {
      assertTrue(
          medicationDispense.getDosageInstructionText().stream()
              .anyMatch(t -> t.equals("1 Tablette morgens")));
      assertEquals(2, medicationDispense.getExtension().size());
    }

    assertEquals(1, medicationDispense.getNote().size());
    assertEquals(
        "in 7 Tagen Rücksprache mit dem Hausarzt halten",
        medicationDispense.getNoteFirstRep().getText());
    assertTrue(ValidatorUtil.encodeAndValidate(parser, medicationDispense).isSuccessful());
    System.out.println();
  }

  @ParameterizedTest(name = "[{index}] -> Build MedicationDispense with ErpWorkflowVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void buildMedicationDispenseWitDgmpDosageInstruction(ErpWorkflowVersion version) {
    val pzn = "06313728";
    val medication =
        GemErpMedicationFaker.forPznMedication(version)
            .withPzn(PZN.from(pzn), fakerDrugName())
            .fake();

    val kvnr = KVNR.from("X234567890");
    val telematikId = "606358757";
    val prescriptionId = "160.100.000.000.011.09";
    val lotNumber = "123456";
    val medicationDispense =
        ErxMedicationDispenseBuilder.forKvnr(kvnr)
            .version(version)
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
    if (version.isSmallerThanOrEqualTo(ErpWorkflowVersion.V1_5)) {

      assertEquals(2, medicationDispense.getDosageInstruction().size());
      assertEquals("nur nach dem Essen", medicationDispense.getDosageInstructionText().get(0));
      assertEquals("nicht vor dem Schlafen", medicationDispense.getDosageInstructionText().get(1));
    } else {
      assertTrue(
          medicationDispense.getDosageInstructionText().stream()
              .anyMatch(t -> t.equals("nur nach dem Essen, nicht vor dem Schlafen")));
      assertEquals(2, medicationDispense.getExtension().size());
    }

    assertEquals(1, medicationDispense.getNote().size());
    assertEquals(
        "in 7 Tagen Rücksprache mit dem Hausarzt halten",
        medicationDispense.getNoteFirstRep().getText());
    assertTrue(
        ValidatorUtil.encodeAndValidate(parser, medicationDispense, EncodingType.XML, true, true)
            .isSuccessful());
    System.out.println();
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build MedicationDispense with faker and E-Rezept WF-Versions Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void buildMedicationDispenseWithFaker01(ErpWorkflowVersion erpWorkflowVersion) {
    val kvnr = KVNR.from("X234567890");
    val performerId = "01234567890";
    val prescriptionId = PrescriptionId.random();

    val medicationDispense =
        ErxMedicationDispenseFaker.builder(erpWorkflowVersion)
            .withKvnr(kvnr)
            .withPerformer(performerId)
            .withPrescriptionId(prescriptionId)
            .fake();
    val res = getRes(medicationDispense);
    assertTrue(res.isSuccessful());

    assertNotNull(medicationDispense.getId());
    assertEquals(prescriptionId, medicationDispense.getPrescriptionId());
    assertEquals(kvnr.getValue(), medicationDispense.getSubjectId().getValue());
    assertEquals(performerId, medicationDispense.getPerformerIdFirstRep());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build MedicationDispense with faker and E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void buildMedicationDispenseWithFaker02(ErpWorkflowVersion erpWorkflowVersion) {
    val kvnr = KVNR.asGkv("X234567890");
    val performerId = "01234567890";
    val prescriptionId = PrescriptionId.from("200.100.000.000.011.09");
    val medicationDispense =
        ErxMedicationDispenseFaker.builder(erpWorkflowVersion)
            .withKvnr(kvnr)
            .withPerformer(performerId)
            .withPrescriptionId(prescriptionId)
            .fake();

    assertTrue(getRes(medicationDispense).isSuccessful());

    assertNotNull(medicationDispense.getId());
    assertEquals(kvnr.getValue(), medicationDispense.getSubjectId().getValue());
    assertEquals(prescriptionId, medicationDispense.getPrescriptionId());
    assertEquals(performerId, medicationDispense.getPerformerIdFirstRep());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build MedicationDispense with faker and E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void throwExceptionWhileBuildMedicationDispenseWithFaker02(
      ErpWorkflowVersion erpWorkflowVersion) {
    val erxMedicationDispensebuilder = ErxMedicationDispenseBuilder.forKvnr(KVNR.random());
    val gemMedication = GemErpMedicationFaker.forPznMedication(erpWorkflowVersion).fake();
    erxMedicationDispensebuilder.medication(gemMedication);
    assertThrows(BuilderException.class, erxMedicationDispensebuilder::build);
  }

  @ParameterizedTest(
      name = "[{index}] -> Build MedicationDispense with faker and E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void buildMedicationDispenseWithFaker03(ErpWorkflowVersion erpWorkflowVersion) {
    val kvnr = KVNR.from("X234567890");
    val performerId = "01234567890";
    val prescriptionId = PrescriptionId.from("160.100.000.000.011.09");
    val medicationDispense =
        ErxMedicationDispenseFaker.builder(erpWorkflowVersion)
            .withKvnr(kvnr)
            .withPerformer(performerId)
            .withPrescriptionId(prescriptionId)
            .fake();

    assertTrue(getRes(medicationDispense).isSuccessful());

    assertNotNull(medicationDispense.getId());
    assertEquals(kvnr.getValue(), medicationDispense.getSubjectId().getValue());
    assertEquals(prescriptionId, medicationDispense.getPrescriptionId());
    assertEquals(performerId, medicationDispense.getPerformerIdFirstRep());
    assertTrue(medicationDispense.getContainedKbvMedication().isEmpty());
  }

  @Test
  void shouldThrowOnDispensingWrongMedicationForProfileVersion() {
    val medicationDispenseBuilder =
        ErxMedicationDispenseFaker.builder(ErpWorkflowVersion.V1_3).toBuilder();

    medicationDispenseBuilder.medication(
        GemErpMedicationFaker.forPznMedication(ErpWorkflowVersion.V1_3).fake());

    assertThrows(BuilderException.class, medicationDispenseBuilder::build);
  }

  @Test
  void shouldSetDosageDgmpCorrect() {
    DosageDgMP dosage =
        DosageDgMPBuilder.dosageBuilder("Tablette", BmpDosiereinheit.AUGENBADEWANNE)
            .text("1 Tablette morgens")
            .value(new BigDecimal(5))
            .build();

    val medDisp =
        ErxMedicationDispenseBuilder.forKvnr(KVNR.random())
            .medication(GemErpMedicationFaker.forPznMedication().fake())
            .version(ErpWorkflowVersion.V1_6)
            .dgmp(dosage)
            .performerId(TelematikID.random())
            .prescriptionId(PrescriptionId.random())
            .status("completed") // default COMPLETED
            .build();
    assertTrue(ValidatorUtil.encodeAndValidate(parser, medDisp).isSuccessful());
    assertEquals(
        Optional.of(5),
        medDisp.getDosageInstruction().stream()
            .flatMap(
                dI ->
                    dI.getDoseAndRate().stream()
                        .map(dAR -> dAR.getDoseQuantity().getValue().intValue()))
            .findFirst());
  }

  @Test
  void shouldSetDosageDgmpCorrectAsList() {
    DosageDgMP dosage =
        DosageDgMPBuilder.dosageBuilder("Tablette", BmpDosiereinheit.AUGENBADEWANNE)
            .text("1 Tablette morgens")
            .value(new BigDecimal(5))
            .build();

    val medDisp =
        ErxMedicationDispenseBuilder.forKvnr(KVNR.random())
            .version(ErpWorkflowVersion.V1_6)
            .medication(
                GemErpMedicationFaker.forPznMedication()
                    .withDrugCategory(EpaDrugCategory.C_02)
                    .fake())
            .dgmp(List.of(dosage))
            .performerId(TelematikID.random())
            .prescriptionId(PrescriptionId.random())
            .status("completed") // default COMPLETED
            .build();
    assertTrue(ValidatorUtil.encodeAndValidate(parser, medDisp).isSuccessful());
    assertEquals(
        Optional.of(5),
        medDisp.getDosageInstruction().stream()
            .map(
                dI ->
                    dI.getDoseAndRate().stream()
                        .map(dAR -> dAR.getDoseQuantity().getValue().intValue())
                        .findFirst()
                        .orElseThrow())
            .findFirst());
  }

  private static ValidationResult getRes(Resource medicationDispense) {
    return ValidatorUtil.encodeAndValidate(parser, medicationDispense, false);
  }
}
