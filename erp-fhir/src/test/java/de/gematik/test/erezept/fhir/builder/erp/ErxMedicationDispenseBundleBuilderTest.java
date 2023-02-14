/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.fhir.builder.erp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
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
  @ClearSystemProperty(key = "erp.fhir.profile")
  void buildFakedBundle(String erpFhirProfileVersion) {
    System.setProperty("erp.fhir.profile", erpFhirProfileVersion);
    val kvid = GemFaker.fakerKvid();
    val performerId = GemFaker.fakerTelematikId();
    val prescriptionId = PrescriptionId.random();
    val bundle =
        ErxMedicationDispenseBundleBuilder.faker(3, kvid, performerId, prescriptionId).build();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertEquals(3, bundle.getEntry().size());
  }

  @Test
  void buildEmptyWithAddedFakedDispenses() {
    val kvid = GemFaker.fakerKvid();
    val performerId = GemFaker.fakerTelematikId();
    val prescriptionId = PrescriptionId.random();
    val builder = ErxMedicationDispenseBundleBuilder.empty();

    IntStream.range(0, 3)
        .forEach(
            idx ->
                builder.add(
                    ErxMedicationDispenseBuilder.faker(kvid, performerId, prescriptionId).build()));

    val bundle = builder.build();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertEquals(3, bundle.getEntry().size());
  }

  @Test
  void buildFakedBundleWithAddedFakedDispenses() {
    val kvid = GemFaker.fakerKvid();
    val performerId = GemFaker.fakerTelematikId();
    val prescriptionId = PrescriptionId.random();
    val builder = ErxMedicationDispenseBundleBuilder.faker(3, kvid, performerId, prescriptionId);

    IntStream.range(0, 3)
        .forEach(
            idx ->
                builder.add(
                    ErxMedicationDispenseBuilder.faker(kvid, performerId, prescriptionId).build()));

    val bundle = builder.build();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertEquals(6, bundle.getEntry().size());
  }
}
