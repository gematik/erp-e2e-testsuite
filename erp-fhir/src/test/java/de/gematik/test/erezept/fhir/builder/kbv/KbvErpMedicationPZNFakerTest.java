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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.valuesets.BaseMedicationType;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

@ParameterizedClass
@MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaErpVersions")
@RequiredArgsConstructor
class KbvErpMedicationPZNFakerTest extends ErpFhirParsingTest {

  private final KbvItaErpVersion erpVersion;

  @Test
  void buildFakerKbvErpMedicationPZNWithType() {
    val medication =
        KbvErpMedicationPZNFaker.builder(erpVersion)
            .withType(BaseMedicationType.MEDICAL_PRODUCT)
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakerKbvErpMedicationPZNWithVaccine() {
    val medication = KbvErpMedicationPZNFaker.builder(erpVersion).withVaccine(fakerBool()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakerKbvErpMedicationPZNWithStandardSize() {
    val medication =
        KbvErpMedicationPZNFaker.builder(erpVersion).withStandardSize(StandardSize.random()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldFakeKbvErpMedicationPznWithSupplyForm() {
    val medication =
        KbvErpMedicationPZNFaker.builder(erpVersion).withSupplyForm(Darreichungsform.SCH).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakerKbvErpMedicationPZNWithPZNMedicationName() {
    val medication =
        KbvErpMedicationPZNFaker.builder(erpVersion)
            .withPznMedication(PZN.random(), fakerDrugName())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakerKbvErpMedicationPZNWithAmount() {
    val medication = KbvErpMedicationPZNFaker.builder(erpVersion).withAmount(fakerAmount()).fake();
    val medication2 =
        KbvErpMedicationPZNFaker.builder(erpVersion).withAmount(fakerAmount(), "Stk").fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    val result2 = ValidatorUtil.encodeAndValidate(parser, medication2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }
}
