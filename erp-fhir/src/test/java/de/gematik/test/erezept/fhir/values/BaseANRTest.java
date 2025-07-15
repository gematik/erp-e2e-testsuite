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

package de.gematik.test.erezept.fhir.values;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.HL7CodeSystem;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.exceptions.InvalidBaseANR;
import de.gematik.test.erezept.fhir.profiles.systems.CommonNamingSystem;
import de.gematik.test.erezept.fhir.profiles.systems.KbvNamingSystem;
import de.gematik.test.erezept.fhir.values.BaseANR.ANRType;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import java.util.Arrays;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.ValueSource;

class BaseANRTest {

  @Test
  void shouldCreateZANR() {
    val identifier = new Identifier();
    identifier.setSystem(DeBasisProfilNamingSystem.KZBV_ZAHNARZTNUMMER.getCanonicalUrl());
    identifier.setValue("123456789");

    val coding = identifier.getType().getCodingFirstRep();
    coding.setSystem(DeBasisProfilCodeSystem.IDENTIFIER_TYPE_DE_BASIS.getCanonicalUrl());
    coding.setCode("ZANR");

    val anr = BaseANR.from(identifier);
    assertEquals(BaseANR.ANRType.ZANR, anr.getType());
    assertEquals("123456789", anr.getValue());
    assertEquals(
        DeBasisProfilCodeSystem.IDENTIFIER_TYPE_DE_BASIS.getCanonicalUrl(), anr.getCodeSystemUrl());
    assertEquals(
        DeBasisProfilNamingSystem.KZBV_ZAHNARZTNUMMER.getCanonicalUrl(), anr.getNamingSystemUrl());
    // just for the sake of code-coverage
    assertEquals(anr.hashCode(), anr.hashCode());
    assertEquals(7, anr.getChecksum());
  }

  @Test
  void shouldCreateLANR() {
    val identifier = new Identifier();
    identifier.setSystem(KbvNamingSystem.BASE_ANR.getCanonicalUrl());
    identifier.setValue("123456789");

    val coding = identifier.getType().getCodingFirstRep();
    coding.setSystem(HL7CodeSystem.HL7_V2_0203.getCanonicalUrl());
    coding.setCode("LANR");

    val anr = BaseANR.from(identifier);
    assertEquals(BaseANR.ANRType.LANR, anr.getType());
    assertEquals("123456789", anr.getValue());
    assertEquals(HL7CodeSystem.HL7_V2_0203.getCanonicalUrl(), anr.getCodeSystemUrl());
    assertEquals(KbvNamingSystem.BASE_ANR.getCanonicalUrl(), anr.getNamingSystemUrl());
  }

  @ParameterizedTest(name = "Create BaseANR for QualificationType {0}")
  @EnumSource(
      value = QualificationType.class,
      names = {"DOCTOR", "DENTIST"})
  void shouldCreateDependingOnQualification(QualificationType qt) {
    val anr = BaseANR.forQualification(qt, "123456789");
    if (qt == QualificationType.DOCTOR) {
      assertEquals(ANRType.LANR, anr.getType());
      assertEquals(HL7CodeSystem.HL7_V2_0203.getCanonicalUrl(), anr.getCodeSystemUrl());
      assertEquals(KbvNamingSystem.BASE_ANR.getCanonicalUrl(), anr.getNamingSystemUrl());
    } else if (qt == QualificationType.DENTIST) {
      assertEquals(ANRType.ZANR, anr.getType());
      assertEquals(
          DeBasisProfilCodeSystem.IDENTIFIER_TYPE_DE_BASIS.getCanonicalUrl(),
          anr.getCodeSystemUrl());
      assertEquals(
          DeBasisProfilNamingSystem.KZBV_ZAHNARZTNUMMER.getCanonicalUrl(),
          anr.getNamingSystemUrl());
    } else {
      fail();
    }
    assertEquals("123456789", anr.getValue());
  }

  @Test
  void shouldThrowOnUnsupportedQualificationType() {
    List.of(
            QualificationType.DOCTOR_IN_TRAINING,
            QualificationType.DOCTOR_AS_REPLACEMENT,
            QualificationType.MIDWIFE)
        .forEach(
            qt ->
                assertThrows(
                    IllegalArgumentException.class,
                    () -> BaseANR.forQualification(qt, "123456789")));
  }

  @Test
  void shouldThrowOnUndecidableANRCode01() {
    val identifier = new Identifier();
    identifier.setSystem(DeBasisProfilNamingSystem.KZBV_ZAHNARZTNUMMER.getCanonicalUrl());
    identifier.setValue("123456789");

    val coding = identifier.getType().getCodingFirstRep();
    coding.setSystem(KbvNamingSystem.PRUEFNUMMER.getCanonicalUrl());
    coding.setCode("LANR");

    assertThrows(InvalidBaseANR.class, () -> BaseANR.from(identifier));
  }

  @Test
  void shouldThrowOnUndecidableANRCode02() {
    val identifier = new Identifier();
    identifier.setSystem(CommonNamingSystem.ACME_IDS_PATIENT.getCanonicalUrl());
    identifier.setValue("123456789");

    val coding = identifier.getType().getCodingFirstRep();
    coding.setSystem(DeBasisProfilNamingSystem.KZBV_ZAHNARZTNUMMER.getCanonicalUrl());
    coding.setCode("ZANR");

    assertThrows(InvalidBaseANR.class, () -> BaseANR.from(identifier));
  }

  @Test
  void shouldThrowOnUndecidableANRCode03() {
    val identifier = new Identifier();
    identifier.setSystem(DeBasisProfilNamingSystem.KZBV_ZAHNARZTNUMMER.getCanonicalUrl());
    identifier.setValue("123456789");

    val coding = identifier.getType().getCodingFirstRep();
    coding.setSystem(InsuranceTypeDe.CODE_SYSTEM.getCanonicalUrl());
    coding.setCode("LANR");

    assertThrows(InvalidBaseANR.class, () -> BaseANR.from(identifier));
  }

  @Test
  void shouldThrowOnUndecidableANRCode04() {
    val identifier = new Identifier();
    identifier.setSystem(KbvNamingSystem.BASE_ANR.getCanonicalUrl());
    identifier.setValue("123456789");

    val coding = identifier.getType().getCodingFirstRep();
    coding.setSystem(HL7CodeSystem.HL7_V2_0203.getCanonicalUrl());
    coding.setCode("ZANR");

    assertThrows(InvalidBaseANR.class, () -> BaseANR.from(identifier));
  }

  @ParameterizedTest
  @ValueSource(strings = {"LANR", "lanr", "Lanr"})
  void shouldParseLANRTypeFromValidCode(String code) {
    assertEquals(BaseANR.ANRType.LANR, BaseANR.ANRType.fromCode(code));
  }

  @ParameterizedTest
  @ValueSource(strings = {"ZANR", "zanr", "Zanr"})
  void shouldParseZANRTypeFromValidCode(String code) {
    assertEquals(BaseANR.ANRType.ZANR, BaseANR.ANRType.fromCode(code));
  }

  @ParameterizedTest
  @ValueSource(strings = {"Arztnummer", "Zahnarztnummer", "ZANRT", "LANRT"})
  @EmptySource
  void shouldThrowOnInvalidTypeCode(String code) {
    assertThrows(IllegalArgumentException.class, () -> BaseANR.ANRType.fromCode(code));
  }

  @ParameterizedTest
  @EnumSource(value = DeBasisProfilCodeSystem.class, mode = Mode.INCLUDE, names = "PZN")
  void shouldThrowOnInvalidTypeIdentifier(DeBasisProfilCodeSystem cs) {
    val identifier = new Identifier().setType(cs.asCodeableConcept("123123123"));
    assertThrows(InvalidBaseANR.class, () -> BaseANR.ANRType.from(identifier));
  }

  @ParameterizedTest
  @EnumSource(value = QualificationType.class, names = "MIDWIFE", mode = EnumSource.Mode.EXCLUDE)
  void shouldCreateValidBaseANR(QualificationType type) {
    val test = BaseANR.randomFromQualification(type);
    assertTrue(test.isValid());
  }

  @Test
  void shouldDetectPractitionerFromCode() {
    Arrays.stream(BaseANR.ANRType.values())
        .map(
            type ->
                new Coding()
                    .setSystem(type.getCodeSystem().getCanonicalUrl())
                    .setCode(type.getCodeType().getCode()))
        .forEach(coding -> assertTrue(BaseANR.isPractitioner(coding)));
  }

  @Test
  void shouldDetectPractitionerFromIdentifier() {
    Arrays.stream(BaseANR.ANRType.values())
        .map(
            type ->
                new Coding()
                    .setSystem(type.getCodeSystem().getCanonicalUrl())
                    .setCode(type.getCodeType().getCode()))
        .map(
            coding -> {
              val id = new Identifier();
              id.getType().addCoding(coding);
              return id;
            })
        .forEach(identifier -> assertTrue(BaseANR.isPractitioner(identifier)));
  }

  @Test
  void shouldDetectInvalidPractitionerCode() {
    Arrays.stream(BaseANR.ANRType.values())
        .map(
            type ->
                new Coding()
                    .setSystem(type.getCodeSystem().getCanonicalUrl())
                    .setCode(type.getCodeType().getCode() + "X"))
        .forEach(coding -> assertFalse(BaseANR.isPractitioner(coding)));
  }

  @Test
  void shouldDetectInvalidPractitionerSystem() {
    Arrays.stream(BaseANR.ANRType.values())
        .map(
            type ->
                new Coding()
                    .setSystem("X" + type.getCodeSystem().getCanonicalUrl())
                    .setCode(type.getCodeType().getCode()))
        .forEach(coding -> assertFalse(BaseANR.isPractitioner(coding)));
  }

  @Test
  void shouldCheckValidLANR() {
    val lanr = new LANR("754236701");
    assertTrue(lanr.isValid());
  }

  @Test
  void shouldNotCheckInvalidLANR() {
    val lanr = new LANR("111136701");
    assertFalse(lanr.isValid());
  }

  @Test
  void shouldGiveValidNamingSystems() {
    val validNamingSystems = BaseANR.ANRType.validNamingSystems();
    assertNotNull(validNamingSystems);
    assertFalse(validNamingSystems.isEmpty());
  }

  @Test
  void shouldThrowOnIllegalQualificationTypes() {
    val qt = QualificationType.MIDWIFE;
    assertThrows(IllegalArgumentException.class, () -> BaseANR.randomFromQualification(qt));
  }
}
