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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ErxMedicationDispenseBundleBuilderTest extends ErpFhirParsingTest {

  @Test
  void buildEmptyWithAddedFakedDispenses() {
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
                        .withVersion(ErpWorkflowVersion.V1_4)
                        .withPerformer(performerId)
                        .withPrescriptionId(prescriptionId)
                        .fake()));

    val bundle = builder.build();

    assertTrue(parser.isValid(bundle));
    assertEquals(3, bundle.getEntry().size());
  }

  @Test
  void buildFakedBundle() {
    val kvnr = KVNR.from("X110488614");
    val performerId = "3-SMC-B-Testkarte-883110000116873";
    val prescriptionId = PrescriptionId.from("160.000.006.741.854.62");
    val bundle =
        ErxMedicationDispenseBundleFaker.build()
            .withAmount(2)
            .version(ErpWorkflowVersion.V1_4)
            .withKvnr(kvnr)
            .withPerformerId(performerId)
            .withPrescriptionId(prescriptionId)
            .fake();

    assertTrue(parser.isValid(bundle));
    assertEquals(2, bundle.getEntry().size());
  }

  @ParameterizedTest
  @MethodSource
  void shouldHaveProfileForOldProfiles(
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
    assertTrue(parser.isValid(bundle));
    assertFalse(bundle.getMeta().getProfile().isEmpty());
    assertTrue(ErpWorkflowStructDef.CLOSE_OPERATION_BUNDLE.matches(bundle.getMeta()));
  }

  static Stream<Arguments> shouldHaveProfileForOldProfiles() {
    return Stream.of(Arguments.of(ErpWorkflowVersion.V1_3, KbvItaErpVersion.V1_1_0));
  }
}
