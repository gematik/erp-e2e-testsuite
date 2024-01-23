/*
 * Copyright 2023 gematik GmbH
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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNBuilder;
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
    val medication = KbvErpMedicationPZNBuilder.faker(pzn).version(kbvItaVersion).build();

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
            .build();

    assertNotNull(medicationDispense.getId());
    assertEquals(1, medicationDispense.getErpMedication().size());
    assertEquals(kvnr, medicationDispense.getSubjectId());
    assertEquals(new PrescriptionId(prescriptionId), medicationDispense.getPrescriptionId());
    assertEquals(
        lotNumber, medicationDispense.getErpMedicationFirstRep().getBatch().getLotNumber());
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
        ErxMedicationDispenseBuilder.faker(kvnr, performerId, prescriptionId).build();

    assertNotNull(medicationDispense.getId());
    assertEquals(prescriptionId, medicationDispense.getPrescriptionId());
    assertEquals(kvnr, medicationDispense.getSubjectId());
    assertEquals(performerId, medicationDispense.getPerformerIdFirstRep());
    assertNotNull(medicationDispense.getErpMedicationFirstRep().getBatch());

    val result = ValidatorUtil.encodeAndValidate(parser, medicationDispense);
    assertTrue(result.isSuccessful());
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
        ErxMedicationDispenseBuilder.faker(kvnr, performerId, prescriptionId).build();

    assertNotNull(medicationDispense.getId());
    assertEquals(kvnr, medicationDispense.getSubjectId());
    assertEquals(prescriptionId, medicationDispense.getPrescriptionId());
    assertEquals(performerId, medicationDispense.getPerformerIdFirstRep());

    val result = ValidatorUtil.encodeAndValidate(parser, medicationDispense);
    assertTrue(result.isSuccessful());
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
        ErxMedicationDispenseBuilder.faker(kvnr, performerId, pzn, prescriptionId).build();

    assertNotNull(medicationDispense.getId());
    assertEquals(kvnr, medicationDispense.getSubjectId());
    assertEquals(prescriptionId, medicationDispense.getPrescriptionId());
    assertEquals(pzn, medicationDispense.getErpMedicationFirstRep().getPznFirstRep());
    assertEquals(performerId, medicationDispense.getPerformerIdFirstRep());

    val result = ValidatorUtil.encodeAndValidate(parser, medicationDispense);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldBuildMedicationDispense120ByDefault() {
    val kvnr = KVNR.from("X234567890");
    val performerId = "01234567890";
    val prescriptionId = new PrescriptionId("160.100.000.000.011.09");
    val pzn = "00111222";
    val medicationDispense =
        ErxMedicationDispenseBuilder.faker(kvnr, performerId, pzn, prescriptionId).build();

    val profile = medicationDispense.getMeta().getProfile().get(0).asStringValue();
    val profileVersion = profile.split("\\|")[1];
    assertTrue(ErpWorkflowVersion.V1_2_0.isEqual(profileVersion));
  }

  @Test
  @SetSystemProperty(key = "erp.fhir.medicationdispense.default", value = "True")
  void shouldTurnOffDefaultMedicationDispense120() {
    val kvnr = KVNR.from("X234567890");
    val performerId = "01234567890";
    val prescriptionId = new PrescriptionId("160.100.000.000.011.09");
    val pzn = "00111222";
    val medicationDispense =
        ErxMedicationDispenseBuilder.faker(kvnr, performerId, pzn, prescriptionId).build();

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
        ErxMedicationDispenseBuilder.faker(kvnr, performerId, pzn, prescriptionId).build();

    val profile = medicationDispense.getMeta().getProfile().get(0).asStringValue();
    val profileVersion = profile.split("\\|")[1];
    assertTrue(ErpWorkflowVersion.V1_2_0.isEqual(profileVersion));
  }
}
