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

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerDrugName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.coding.WithSystem;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Date;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;

class ErxMedicationDispenseBuilderTest extends ErpFhirParsingTest {

  @ParameterizedTest(name = "[{index}] -> Build MedicationDispense with ErpWorkflowVersion {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#oldErpWorkflowVersions")
  void buildMedicationDispenseFixedValuesForOldVersions(ErpWorkflowVersion version) {
    val pzn = "06313728";
    // TODO: provide the whole versionset instead of just the ErpWorkflowVersion?
    //    val kbvItaVersion =
    //        ParserConfigurations.getInstance().getAppropriateVersion(KbvItaErpVersion.class,
    // version);
    val medication =
        KbvErpMedicationPZNFaker.builder()
            .withPznMedication(pzn, fakerDrugName())
            //            .withVersion(kbvItaVersion)
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
            .status("completed") // default COMPLETED
            .whenPrepared(new Date())
            .whenHandedOver(new Date())
            .batch(lotNumber, new Date())
            .wasSubstituted(true)
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationDispense);
    assertTrue(result.isSuccessful());

    assertNotNull(medicationDispense.getId());
    assertEquals(kvnr.getValue(), medicationDispense.getSubjectId().getValue());
    assertEquals(new PrescriptionId(prescriptionId), medicationDispense.getPrescriptionId());
    assertTrue(medicationDispense.getDosageInstruction().isEmpty());
    assertTrue(medicationDispense.getNote().isEmpty());
  }

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

    if (version.compareTo(ErpWorkflowVersion.V1_3_0) <= 0) {
      medicationDispenseBuilder.medication(
          KbvErpMedicationPZNFaker.builder().withPznMedication(pzn, fakerDrugName()).fake());
    } else {
      medicationDispenseBuilder.medication(
          GemErpMedicationFaker.builder().withPzn(PZN.from(pzn), fakerDrugName()).fake());
    }

    val medicationDispense = medicationDispenseBuilder.build();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationDispense);
    assertTrue(result.isSuccessful());

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
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#oldErpWorkflowVersions")
  void buildMedicationDispenseWithMultipleDosageInstructions(ErpWorkflowVersion version) {
    val pzn = "06313728";
    // TODO: provide the whole versionset instead of just the ErpWorkflowVersion?
    //    val kbvItaVersion =
    //        ParserConfigurations.getInstance().getAppropriateVersion(KbvItaErpVersion.class,
    // version);
    val medication =
        KbvErpMedicationPZNFaker.builder()
            .withPznMedication(pzn, fakerDrugName())
            //            .withVersion(kbvItaVersion)
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
    assertEquals(new PrescriptionId(prescriptionId), medicationDispense.getPrescriptionId());
    assertEquals(2, medicationDispense.getDosageInstruction().size());
    assertEquals("nur nach dem Essen", medicationDispense.getDosageInstructionText().get(0));
    assertEquals("nicht vor dem Schlafen", medicationDispense.getDosageInstructionText().get(1));
    assertEquals(1, medicationDispense.getNote().size());
    assertEquals(
        "in 7 Tagen Rücksprache mit dem Hausarzt halten",
        medicationDispense.getNoteFirstRep().getText());
    val result = ValidatorUtil.encodeAndValidate(parser, medicationDispense);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build MedicationDispense with faker and E-Rezept FHIR Profiles {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#oldErpFhirProfileVersions")
  @ClearSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE)
  void buildMedicationDispenseWithFaker01(String erpFhirProfileVersion) {
    System.setProperty(ERP_FHIR_PROFILES_TOGGLE, erpFhirProfileVersion);
    val kvnr = KVNR.from("X234567890");
    val performerId = "01234567890";
    val prescriptionId = PrescriptionId.random();

    val medicationDispense =
        ErxMedicationDispenseFaker.builder()
            .withKvnr(kvnr)
            .withPerformer(performerId)
            .withPrescriptionId(prescriptionId)
            .fake();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationDispense);
    assertTrue(result.isSuccessful());

    assertNotNull(medicationDispense.getId());
    assertEquals(prescriptionId, medicationDispense.getPrescriptionId());
    assertEquals(kvnr.getValue(), medicationDispense.getSubjectId().getValue());
    assertEquals(performerId, medicationDispense.getPerformerIdFirstRep());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build MedicationDispense with faker and E-Rezept FHIR Profiles {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#oldErpFhirProfileVersions")
  @ClearSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE)
  void buildMedicationDispenseWithFaker02(String erpFhirProfileVersion) {
    System.setProperty(ERP_FHIR_PROFILES_TOGGLE, erpFhirProfileVersion);
    val kvnr = KVNR.from("X234567890");
    val performerId = "01234567890";
    val prescriptionId = new PrescriptionId("200.100.000.000.011.09");
    val medicationDispense =
        ErxMedicationDispenseFaker.builder()
            .withKvnr(kvnr)
            .withPerformer(performerId)
            .withPrescriptionId(prescriptionId)
            .fake();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationDispense);
    assertTrue(result.isSuccessful());

    assertNotNull(medicationDispense.getId());
    assertEquals(kvnr.getValue(), medicationDispense.getSubjectId().getValue());
    assertEquals(prescriptionId, medicationDispense.getPrescriptionId());
    assertEquals(performerId, medicationDispense.getPerformerIdFirstRep());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build MedicationDispense with faker and E-Rezept FHIR Profiles {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpFhirProfileVersions")
  @ClearSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE)
  void throwExceptionWhileBuildMedicationDispenseWithFaker02(String erpFhirProfileVersion) {
    System.setProperty(ERP_FHIR_PROFILES_TOGGLE, erpFhirProfileVersion);
    val erxMedicationDispensebuilder = ErxMedicationDispenseBuilder.forKvnr(KVNR.random());
    val medication = KbvErpMedicationPZNFaker.builder().fake();

    if (erpFhirProfileVersion.compareTo(ErpWorkflowVersion.V1_3_0.getVersion()) <= 0) {
      val gemMedication = GemErpMedication.fromMedication(medication);
      erxMedicationDispensebuilder.medication(gemMedication);
    } else {
      erxMedicationDispensebuilder.medication(medication);
    }
    assertThrows(BuilderException.class, erxMedicationDispensebuilder::build);
  }

  @ParameterizedTest(
      name = "[{index}] -> Build MedicationDispense with faker and E-Rezept FHIR Profiles {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#oldErpFhirProfileVersions")
  @ClearSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE)
  void buildMedicationDispenseWithFaker03(String erpFhirProfileVersion) {
    System.setProperty(ERP_FHIR_PROFILES_TOGGLE, erpFhirProfileVersion);
    val kvnr = KVNR.from("X234567890");
    val performerId = "01234567890";
    val prescriptionId = new PrescriptionId("160.100.000.000.011.09");
    val medicationDispense =
        ErxMedicationDispenseFaker.builder()
            .withKvnr(kvnr)
            .withPerformer(performerId)
            .withPrescriptionId(prescriptionId)
            .fake();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationDispense);
    assertTrue(result.isSuccessful());

    assertNotNull(medicationDispense.getId());
    assertEquals(kvnr.getValue(), medicationDispense.getSubjectId().getValue());
    assertEquals(prescriptionId, medicationDispense.getPrescriptionId());

    assertEquals(performerId, medicationDispense.getPerformerIdFirstRep());

    if (ErpWorkflowVersion.getDefaultVersion().compareTo(ErpWorkflowVersion.V1_3_0) <= 0) {
      assertFalse(medicationDispense.getContainedKbvMedication().isEmpty());
    } else {
      assertTrue(medicationDispense.getContainedKbvMedication().isEmpty());
    }
  }

  @SetSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE, value = "1.3.0")
  @Test
  void shouldBuildMedicationDispenseWithDefaultVersion() {
    val kvnr = KVNR.from("X234567890");
    val performerId = "01234567890";
    val prescriptionId = new PrescriptionId("160.100.000.000.011.09");
    val medicationDispense =
        ErxMedicationDispenseFaker.builder()
            .withKvnr(kvnr)
            .withPerformer(performerId)
            .withPrescriptionId(prescriptionId)
            .fake();

    val profile = medicationDispense.getMeta().getProfile().get(0).asStringValue();
    val profileVersion = profile.split("\\|")[1];
    assertTrue(ErpWorkflowVersion.getDefaultVersion().isEqual(profileVersion));
  }

  @ParameterizedTest
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void shouldThrowOnDispensingWrongMedicationForProfileVersion(ErpWorkflowVersion version) {
    val medicationDispenseBuilder =
        ErxMedicationDispenseFaker.builder().withVersion(version).toBuilder();

    if (version.compareTo(ErpWorkflowVersion.V1_3_0) <= 0) {
      // v1.3.0 and below using GemErpMedication which is only available from v1.4.0
      medicationDispenseBuilder.medication(GemErpMedicationFaker.builder().fake());
    } else {
      // v1.4.0 and above a GemErpMedication must be used instead
      medicationDispenseBuilder.medication(KbvErpMedicationPZNFaker.builder().fake());
    }

    assertThrows(BuilderException.class, medicationDispenseBuilder::build);
  }
}
