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

import static de.gematik.test.erezept.fhir.builder.GemFaker.randomElement;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.AsvFachgruppennummer;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.values.LANR;
import de.gematik.test.erezept.fhir.values.ZANR;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import lombok.val;
import org.junit.jupiter.api.Test;

class KbvPractitionerFakerTest extends ErpFhirParsingTest {
  @Test
  void buildFakerPractitionerWithName() {
    val practitioner = KbvPractitionerFaker.builder().withName("Max Mustermann").fake();
    val practitioner2 = KbvPractitionerFaker.builder().withName("Max", "Mustermann").fake();
    val result = ValidatorUtil.encodeAndValidate(parser, practitioner);
    val result2 = ValidatorUtil.encodeAndValidate(parser, practitioner2);
    assertEquals("Max Mustermann", practitioner.getFullName());
    assertEquals("Max Mustermann", practitioner2.getFullName());
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  void buildFakerPatientWithQualificationType() {
    val qualificationType = randomElement(QualificationType.DOCTOR, QualificationType.DENTIST);
    val practitioner =
        KbvPractitionerFaker.builder()
            .withQualificationType(qualificationType)
            .withQualificationType("Doctor")
            .withQualificationType(AsvFachgruppennummer.from("00"))
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, practitioner);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakerPractitionerWithLanr() {
    val lanr = LANR.random();
    val practitioner = KbvPractitionerFaker.builder().withLanr(lanr.getValue()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, practitioner);
    assertTrue(result.isSuccessful());
    val anr = assertDoesNotThrow(() -> practitioner.getANR().orElseThrow());
    assertEquals(BaseANR.ANRType.LANR, anr.getType());
  }

  @Test
  void buildFakerPractitionerWithZanr() {
    val zanr = ZANR.random();
    val practitioner = KbvPractitionerFaker.builder().withZanr(zanr.getValue()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, practitioner);
    assertTrue(result.isSuccessful());
    val anr = assertDoesNotThrow(() -> practitioner.getANR().orElseThrow());
    assertEquals(BaseANR.ANRType.ZANR, anr.getType());
  }
}
