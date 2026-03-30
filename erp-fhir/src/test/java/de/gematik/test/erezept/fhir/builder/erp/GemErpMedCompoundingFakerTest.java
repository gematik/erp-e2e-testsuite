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

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerBool;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerLotNumber;
import static de.gematik.test.erezept.fhir.builder.GemFaker.getFaker;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.test.erezept.eml.fhir.valuesets.EpaDrugCategory;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

@ParameterizedClass
@MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
@RequiredArgsConstructor
class GemErpMedCompoundingFakerTest extends ErpFhirParsingTest {

  private final ErpWorkflowVersion version;

  @RepeatedTest(5)
  void shouldRandomlyFake() {
    val medication = GemErpMedicationFaker.forMedicationCompounding(version).fake();
    assertTrue(ValidatorUtil.encodeAndValidate(parser, medication).isSuccessful());
  }

  @RepeatedTest(5)
  void shouldFakeWithFixedValues() {
    val medication = GemErpMedicationFaker.forMedicationCompounding(version);

    if (fakerBool()) medication.withSnomed(fakerLotNumber());
    if (fakerBool())
      medication.withAmount(getFaker().random().nextLong(7), getFaker().starTrek().character());
    if (fakerBool()) medication.withDrugCategory(EpaDrugCategory.C_00);
    if (fakerBool()) medication.withLotNumber(fakerLotNumber());
    if (fakerBool()) medication.withVaccineTrue(true);
    if (fakerBool()) medication.withAsk(ASK.from(getFaker().superhero().name()));

    assertTrue(ValidatorUtil.encodeAndValidate(parser, medication.fake()).isSuccessful());
  }

  @RepeatedTest(5)
  void shouldFakeEverythingWithAllFakerBools() {
    try (val mockFaker = mockStatic(GemFaker.class, Mockito.CALLS_REAL_METHODS)) {
      mockFaker.when(GemFaker::fakerBool).thenReturn(true);
      val medication = GemErpMedicationFaker.forMedicationCompounding(version).fake();
      assertTrue(ValidatorUtil.encodeAndValidate(parser, medication).isSuccessful());
    }
  }

  @Test
  void shouldFakeEverythingWithNoFakerBools() {
    try (val mockFaker = mockStatic(GemFaker.class, Mockito.CALLS_REAL_METHODS)) {
      mockFaker.when(GemFaker::fakerBool).thenReturn(false);
      val medication = GemErpMedicationFaker.forMedicationCompounding(version).fake();
      assertTrue(ValidatorUtil.encodeAndValidate(parser, medication).isSuccessful());
    }
  }

  @RepeatedTest(5)
  void shouldFakeWithMultipleAsks() {
    try (val mockFaker = mockStatic(GemFaker.class, Mockito.CALLS_REAL_METHODS)) {
      mockFaker.when(GemFaker::fakerBool).thenReturn(false);
      val medication =
          GemErpMedicationFaker.forMedicationCompounding(version)
              .withAsk(ASK.from(getFaker().superhero().name()))
              .withAsk(ASK.from(getFaker().superhero().name()))
              .withAsk(ASK.from(getFaker().superhero().name()))
              .withAsk(ASK.from(getFaker().superhero().name()))
              .withSnomed("1234567890")
              .withSnomed("123456789123456")
              .withSnomed("1234567891234567890")
              .withAtc(ATC.from(getFaker().superhero().name()))
              .withAtc(ATC.from(getFaker().superhero().name()))
              .fake();

      assertTrue(ValidatorUtil.encodeAndValidate(parser, medication).isSuccessful());
      assertEquals(10, medication.getCode().getCoding().size()); // 9 + 1 defaultValue
    }
  }

  @Test()
  void shouldBuildWithoutExplicitVersion() {
    val med = GemErpMedicationFaker.forMedicationCompounding().fake();
    assertNotEquals(
        ErpWorkflowStructDef.MEDICATION.getCanonicalUrl(),
        Objects.requireNonNull(med.getMeta().getProfile().stream().findFirst().orElse(null))
            .getValueAsString());
    assertTrue(
        ErpWorkflowStructDef.MEDICATION.getCanonicalUrl().length()
            < Objects.requireNonNull(med.getMeta().getProfile().stream().findFirst().orElse(null))
                .getValueAsString()
                .length());
    assertTrue(
        Objects.requireNonNull(med.getMeta().getProfile().stream().findFirst().orElse(null))
            .getValueAsString()
            .contains("|"));
  }
}
