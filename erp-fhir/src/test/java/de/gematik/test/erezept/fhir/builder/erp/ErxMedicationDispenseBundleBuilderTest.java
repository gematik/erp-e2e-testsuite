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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.stream.IntStream;
import lombok.val;
import org.junit.jupiter.api.Test;

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
                    ErxMedicationDispenseFaker.builder(ErpWorkflowVersion.getDefaultVersion())
                        .withKvnr(kvnr)
                        .withPerformer(performerId)
                        .withPrescriptionId(prescriptionId)
                        .fake()));

    val bundle = builder.build();

    assertTrue(ValidatorUtil.encodeAndValidate(parser, bundle).isSuccessful());
    assertEquals(3, bundle.getEntry().size());
  }

  @Test
  void buildFakedBundle() {
    val kvnr = KVNR.from("X110488614");
    val performerId = "3-SMC-B-Testkarte-883110000116873";
    val prescriptionId = PrescriptionId.from("160.000.006.741.854.62");
    val bundle =
        ErxMedicationDispenseBundleFaker.build(ErpWorkflowVersion.V1_4)
            .withAmount(2)
            .withKvnr(kvnr)
            .withPerformerId(performerId)
            .withPrescriptionId(prescriptionId)
            .fake();

    assertTrue(ValidatorUtil.encodeAndValidate(parser, bundle).isSuccessful());
    assertEquals(2, bundle.getEntry().size());
  }

  @Test
  void shouldSetCorrectVersion() {
    val bundle = ErxMedicationDispenseBundleFaker.build(ErpWorkflowVersion.V1_6).fake();
    assertTrue(
        bundle
            .getEntry()
            .get(0)
            .getResource()
            .getMeta()
            .getProfile()
            .get(0)
            .getValue()
            .endsWith("|1.6"));
  }
}
