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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import lombok.val;
import org.junit.jupiter.api.Test;

class PatientFakerTest extends ParsingTest {
  @Test
  void buildFakerPatientWithKvnrAndInsuranceType() {
    val patient =
        PatientFaker.builder()
            .withKvnrAndInsuranceType(
                KVNR.random(),
                randomElement(VersicherungsArtDeBasis.PKV, VersicherungsArtDeBasis.GKV))
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, patient);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakePatientWithBirthDate() {
    val patient = PatientFaker.builder().withBirthDate(fakerBirthdayAsString()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, patient);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakePatientWithAddress() {
    val patient =
        PatientFaker.builder().withAddress(fakerCity(), fakerZipCode(), fakerStreetName()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, patient);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakePatientWithName() {
    val patient = PatientFaker.builder().withName("Max", "Mustermann").fake();
    val result = ValidatorUtil.encodeAndValidate(parser, patient);
    assertTrue(result.isSuccessful());
    assertEquals("Mustermann, Max", patient.getFullname());
  }
}
