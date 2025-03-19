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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

class ErxMedicationDispenseBundleBuilderTest extends ErpFhirParsingTest {

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationInfoReq with E-Rezept FHIR Profiles {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#oldErpFhirProfileVersions")
  @ClearSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE)
  void buildFakedBundle(String erpFhirProfileVersion) {
    System.setProperty(ERP_FHIR_PROFILES_TOGGLE, erpFhirProfileVersion);
    val kvnr = KVNR.from("X110488614");
    val performerId = "3-SMC-B-Testkarte-883110000116873";
    val prescriptionId = PrescriptionId.from("160.000.006.741.854.62");
    val bundle =
        ErxMedicationDispenseBundleFaker.build()
            .withAmount(2)
            .withKvnr(kvnr)
            .withPerformerId(performerId)
            .withPrescriptionId(prescriptionId)
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertEquals(2, bundle.getEntry().size());
  }

  @ParameterizedTest(name = "[{index}] -> Build MedicationDispense with ErpWorkflowVersion {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#oldErpFhirProfileVersions")
  @ClearSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE)
  void buildEmptyWithAddedFakedDispenses(String erpFhirProfileVersion) {
    System.setProperty(ERP_FHIR_PROFILES_TOGGLE, erpFhirProfileVersion);
    val kvnr = KVNR.random();
    val performerId = GemFaker.fakerTelematikId();
    val prescriptionId = PrescriptionId.random();
    val builder = ErxMedicationDispenseBundleBuilder.empty();

    IntStream.range(0, 3)
        .forEach(
            idx ->
                builder.add(
                    ErxMedicationDispenseFaker.builder()
                        .withKvnr(kvnr)
                        .withPerformer(performerId)
                        .withPrescriptionId(prescriptionId)
                        .fake()));

    val bundle = builder.build();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
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
                    ErxMedicationDispenseFaker.builder()
                        .withKvnr(kvnr)
                        .withPerformer(performerId)
                        .withPrescriptionId(prescriptionId)
                        .withVersion(erpWorkflowVersion)
                        .withMedication(
                            KbvErpMedicationPZNFaker.builder().withVersion(kbvItaErpVersion).fake())
                        .fake()));

    val bundle = builder.build();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertTrue(bundle.getMeta().getProfile().isEmpty());
  }

  @ParameterizedTest
  @MethodSource
  void shouldHaveProfileForNewerProfiles(
      ErpWorkflowVersion erpWorkflowVersion, KbvItaErpVersion kbvItaErpVersion) {
    val kvnr = KVNR.random();
    val performerId = GemFaker.fakerTelematikId();
    val prescriptionId = PrescriptionId.random();
    val builder = ErxMedicationDispenseBundleBuilder.empty().version(erpWorkflowVersion);

    IntStream.range(0, 3)
        .forEach(
            idx ->
                builder.add(
                    ErxMedicationDispenseFaker.builder()
                        .withKvnr(kvnr)
                        .withPerformer(performerId)
                        .withPrescriptionId(prescriptionId)
                        .withVersion(erpWorkflowVersion)
                        .withMedication(
                            KbvErpMedicationPZNFaker.builder().withVersion(kbvItaErpVersion).fake())
                        .fake()));

    val bundle = builder.build();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertFalse(bundle.getMeta().getProfile().isEmpty());
    assertTrue(ErpWorkflowStructDef.CLOSE_OPERATION_BUNDLE.matches(bundle.getMeta()));
  }

  static Stream<Arguments> shouldHaveProfileForNewerProfiles() {
    return Stream.of(Arguments.of(ErpWorkflowVersion.V1_3_0, KbvItaErpVersion.V1_1_0));
  }
}
