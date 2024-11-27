/*
 * Copyright 2024 gematik GmbH
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.parser.profiles.cfg.ParserConfigurations;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Date;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;

class ErxMedicationDispenseBuilderTest extends ParsingTest {

  @ParameterizedTest(name = "[{index}] -> Build MedicationDispense with ErpWorkflowVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void buildMedicationDispenseFixedValues(ErpWorkflowVersion version) {
    val pzn = "06313728";
    val kbvItaVersion =
        ParserConfigurations.getInstance().getAppropriateVersion(KbvItaErpVersion.class, version);
    val medication =
        KbvErpMedicationPZNFaker.builder()
            .withPznMedication(pzn, fakerDrugName())
            .withVersion(kbvItaVersion)
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
    assertEquals(kvnr, medicationDispense.getSubjectId());
    assertEquals(new PrescriptionId(prescriptionId), medicationDispense.getPrescriptionId());
    assertTrue(medicationDispense.getDosageInstruction().isEmpty());
    assertTrue(medicationDispense.getNote().isEmpty());
  }

  @ParameterizedTest(name = "[{index}] -> Build MedicationDispense with ErpWorkflowVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void buildMedicationDispenseWithMultipleDosageInstructions(ErpWorkflowVersion version) {
    val pzn = "06313728";
    val kbvItaVersion =
        ParserConfigurations.getInstance().getAppropriateVersion(KbvItaErpVersion.class, version);
    val medication =
        KbvErpMedicationPZNFaker.builder()
            .withPznMedication(pzn, fakerDrugName())
            .withVersion(kbvItaVersion)
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
    assertEquals(kvnr, medicationDispense.getSubjectId());
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
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpFhirProfileVersions")
  @ClearSystemProperty(key = "erp.fhir.profile")
  void buildMedicationDispenseWithFaker01(String erpFhirProfileVersion) {
    System.setProperty("erp.fhir.profile", erpFhirProfileVersion);
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
    assertEquals(kvnr, medicationDispense.getSubjectId());
    assertEquals(performerId, medicationDispense.getPerformerIdFirstRep());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build MedicationDispense with faker and E-Rezept FHIR Profiles {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpFhirProfileVersions")
  @ClearSystemProperty(key = "erp.fhir.profile")
  void buildMedicationDispenseWithFaker02(String erpFhirProfileVersion) {
    System.setProperty("erp.fhir.profile", erpFhirProfileVersion);
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
    assertEquals(kvnr, medicationDispense.getSubjectId());
    assertEquals(prescriptionId, medicationDispense.getPrescriptionId());
    assertEquals(performerId, medicationDispense.getPerformerIdFirstRep());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build MedicationDispense with faker and E-Rezept FHIR Profiles {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpFhirProfileVersions")
  @ClearSystemProperty(key = "erp.fhir.profile")
  void buildMedicationDispenseWithFaker03(String erpFhirProfileVersion) {
    System.setProperty("erp.fhir.profile", erpFhirProfileVersion);
    val kvnr = KVNR.from("X234567890");
    val performerId = "01234567890";
    val prescriptionId = new PrescriptionId("160.100.000.000.011.09");
    val pzn = "00111222";
    val medicationDispense =
        ErxMedicationDispenseFaker.builder()
            .withKvnr(kvnr)
            .withPerformer(performerId)
            .withPzn(pzn)
            .withPrescriptionId(prescriptionId)
            .fake();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationDispense);
    assertTrue(result.isSuccessful());

    assertNotNull(medicationDispense.getId());
    assertEquals(kvnr, medicationDispense.getSubjectId());
    assertEquals(prescriptionId, medicationDispense.getPrescriptionId());

    assertEquals(performerId, medicationDispense.getPerformerIdFirstRep());

    if (ErpWorkflowVersion.getDefaultVersion().compareTo(ErpWorkflowVersion.V1_4_0) < 0) {
      assertEquals(pzn, medicationDispense.getErpMedicationFirstRep().getPznFirstRep());
    } else {
      assertTrue(medicationDispense.getErpMedication().isEmpty());
    }
  }

  @Test
  void shouldBuildMedicationDispense120ByDefault() {
    val kvnr = KVNR.from("X234567890");
    val performerId = "01234567890";
    val prescriptionId = new PrescriptionId("160.100.000.000.011.09");
    val pzn = "00111222";
    val medicationDispense =
        ErxMedicationDispenseFaker.builder()
            .withKvnr(kvnr)
            .withPerformer(performerId)
            .withPzn(pzn)
            .withPrescriptionId(prescriptionId)
            .fake();

    val profile = medicationDispense.getMeta().getProfile().get(0).asStringValue();
    val profileVersion = profile.split("\\|")[1];
    assertTrue(ErpWorkflowVersion.getDefaultVersion().isEqual(profileVersion));
  }

  @Test
  @SetSystemProperty(key = "erp.fhir.medicationdispense.default", value = "True")
  void shouldTurnOffDefaultMedicationDispense120() {
    val kvnr = KVNR.from("X234567890");
    val performerId = "01234567890";
    val prescriptionId = new PrescriptionId("160.100.000.000.011.09");
    val pzn = "00111222";
    val medicationDispense =
        ErxMedicationDispenseFaker.builder()
            .withKvnr(kvnr)
            .withPerformer(performerId)
            .withPrescriptionId(prescriptionId)
            .withPzn(pzn)
            .fake();

    val profile = medicationDispense.getMeta().getProfile().get(0).asStringValue();
    val profileVersion = profile.split("\\|")[1];
    val defaultVersion = ErpWorkflowVersion.getDefaultVersion();
    assertTrue(defaultVersion.isEqual(profileVersion));
  }

  @Test
  @SetSystemProperty(key = "erp.fhir.medicationdispense.default", value = "False")
  void shouldTurnOnDefaultMedicationDispense120() {
    val kvnr = KVNR.from("X234567890");
    val performerId = "01234567890";
    val prescriptionId = new PrescriptionId("160.100.000.000.011.09");
    val pzn = "00111222";
    val medicationDispense =
        ErxMedicationDispenseFaker.builder()
            .withKvnr(kvnr)
            .withPerformer(performerId)
            .withPzn(pzn)
            .withPrescriptionId(prescriptionId)
            .fake();

    val profile = medicationDispense.getMeta().getProfile().get(0).asStringValue();
    val profileVersion = profile.split("\\|")[1];
    assertTrue(ErpWorkflowVersion.getDefaultVersion().isEqual(profileVersion));
  }
}
