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

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.stream.IntStream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.junitpioneer.jupiter.*;

class ErxMedicationDispenseBundleBuilderTest extends ParsingTest {

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationInfoReq with E-Rezept FHIR Profiles {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpFhirProfileVersions")
  @ClearSystemProperty.ClearSystemProperties(
      value = {
        @ClearSystemProperty(key = "erp.fhir.profile"),
        @ClearSystemProperty(key = "erp.fhir.medicationdispense.default")
      })
  void buildFakedBundle(String erpFhirProfileVersion) {
    System.setProperty("erp.fhir.profile", erpFhirProfileVersion);
    System.setProperty(
        "erp.fhir.medicationdispense.default",
        "true"); // required to build old medication dispenses!
    val kvnr = KVNR.random();
    val performerId = GemFaker.fakerTelematikId();
    val prescriptionId = PrescriptionId.random();
    val bundle =
        ErxMedicationDispenseBundleBuilder.faker(3, kvnr, performerId, prescriptionId).build();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertEquals(3, bundle.getEntry().size());
  }

  @ParameterizedTest(name = "[{index}] -> Build MedicationDispense with ErpWorkflowVersion {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpFhirProfileVersions")
  @ClearSystemProperty.ClearSystemProperties(
      value = {
        @ClearSystemProperty(key = "erp.fhir.profile"),
        @ClearSystemProperty(key = "erp.fhir.medicationdispense.default")
      })
  void buildEmptyWithAddedFakedDispenses(String erpFhirProfileVersion) {
    System.setProperty("erp.fhir.profile", erpFhirProfileVersion);
    System.setProperty(
        "erp.fhir.medicationdispense.default",
        "true"); // required to build old medication dispenses!
    val kvnr = KVNR.random();
    val performerId = GemFaker.fakerTelematikId();
    val prescriptionId = PrescriptionId.random();
    val builder = ErxMedicationDispenseBundleBuilder.empty();

    IntStream.range(0, 3)
        .forEach(
            idx ->
                builder.add(
                    ErxMedicationDispenseBuilder.faker(kvnr, performerId, prescriptionId).build()));

    val bundle = builder.build();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle, false);
    assertTrue(result.isSuccessful());
    assertEquals(3, bundle.getEntry().size());
  }

  @Test
  void shouldNotHaveAnyProfileForOldProfiles() {
    val erpWorkflowVersion = ErpWorkflowVersion.V1_1_1;
    val kbvItaErpVersion = KbvItaErpVersion.V1_0_2;
    val kvnr = KVNR.random();
    val performerId = GemFaker.fakerTelematikId();
    val prescriptionId = PrescriptionId.random();
    val builder = ErxMedicationDispenseBundleBuilder.empty().version(erpWorkflowVersion);

    IntStream.range(0, 3)
        .forEach(
            idx ->
                builder.add(
                    ErxMedicationDispenseBuilder.faker(kvnr, performerId, prescriptionId)
                        .version(erpWorkflowVersion)
                        .medication(
                            KbvErpMedicationBuilder.faker().version(kbvItaErpVersion).build())
                        .build()));

    val bundle = builder.build();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertTrue(bundle.getMeta().getProfile().isEmpty());
  }

  @Test
  void shouldHaveProfileForNewerProfiles() {
    val erpWorkflowVersion = ErpWorkflowVersion.getDefaultVersion();
    val kbvItaErpVersion = KbvItaErpVersion.getDefaultVersion();
    val kvnr = KVNR.random();
    val performerId = GemFaker.fakerTelematikId();
    val prescriptionId = PrescriptionId.random();
    val builder = ErxMedicationDispenseBundleBuilder.empty().version(erpWorkflowVersion);

    IntStream.range(0, 3)
        .forEach(
            idx ->
                builder.add(
                    ErxMedicationDispenseBuilder.faker(kvnr, performerId, prescriptionId)
                        .version(erpWorkflowVersion)
                        .medication(
                            KbvErpMedicationBuilder.faker().version(kbvItaErpVersion).build())
                        .build()));

    val bundle = builder.build();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertFalse(bundle.getMeta().getProfile().isEmpty());
    assertTrue(ErpWorkflowStructDef.CLOSE_OPERATION_BUNDLE.match(bundle.getMeta()));
  }

  @Test
  void buildFakedBundleWithAddedFakedDispenses() {
    val kvnr = KVNR.random();
    val performerId = GemFaker.fakerTelematikId();
    val prescriptionId = PrescriptionId.random();
    val builder = ErxMedicationDispenseBundleBuilder.faker(3, kvnr, performerId, prescriptionId);

    IntStream.range(0, 3)
        .forEach(
            idx ->
                builder.add(
                    ErxMedicationDispenseBuilder.faker(kvnr, performerId, prescriptionId).build()));

    val bundle = builder.build();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertEquals(6, bundle.getEntry().size());
  }
}
