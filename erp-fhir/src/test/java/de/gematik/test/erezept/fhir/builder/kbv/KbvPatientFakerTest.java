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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerBirthday;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerCity;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerStreetName;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerZipCode;
import static de.gematik.test.erezept.fhir.builder.GemFaker.randomElement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import lombok.val;
import org.junit.jupiter.api.Test;

class KbvPatientFakerTest extends ErpFhirParsingTest {
  @Test
  void buildFakerPatientWithKvnrAndInsuranceType() {
    val patient =
        KbvPatientFaker.builder()
            .withKvnrAndInsuranceType(
                KVNR.random(), randomElement(InsuranceTypeDe.PKV, InsuranceTypeDe.GKV))
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, patient);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakePatientWithBirthDate() {
    val patient = KbvPatientFaker.builder().withBirthDate(fakerBirthday()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, patient);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakePatientWithAddress() {
    val patient =
        KbvPatientFaker.builder()
            .withAddress(fakerCity(), fakerZipCode(), fakerStreetName())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, patient);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakePatientWithName() {
    val patient = KbvPatientFaker.builder().withName("Max", "Mustermann").fake();
    val result = ValidatorUtil.encodeAndValidate(parser, patient);
    assertTrue(result.isSuccessful());
    assertEquals("Mustermann, Max", patient.getFullname());
  }
}
