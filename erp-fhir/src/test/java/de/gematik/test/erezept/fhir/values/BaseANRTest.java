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

package de.gematik.test.erezept.fhir.values;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.exceptions.*;
import de.gematik.test.erezept.fhir.parser.profiles.systems.*;
import de.gematik.test.erezept.fhir.values.BaseANR.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class BaseANRTest {

  @Test
  void shouldCreateZANR() {
    val identifier = new Identifier();
    identifier.setSystem(DeBasisNamingSystem.ZAHNARZTNUMMER.getCanonicalUrl());
    identifier.setValue("123456789");

    val coding = identifier.getType().getCodingFirstRep();
    coding.setSystem(DeBasisCodeSystem.IDENTIFIER_TYPE_DE_BASIS.getCanonicalUrl());
    coding.setCode("ZANR");

    val anr = BaseANR.fromIdentifier(identifier);
    assertEquals(BaseANR.ANRType.ZANR, anr.getType());
    assertEquals("123456789", anr.getValue());
    assertEquals(
        DeBasisCodeSystem.IDENTIFIER_TYPE_DE_BASIS.getCanonicalUrl(), anr.getCodeSystemUrl());
    assertEquals(DeBasisNamingSystem.ZAHNARZTNUMMER.getCanonicalUrl(), anr.getNamingSystemUrl());
    // just for the sake of code-coverage
    assertEquals(anr.hashCode(), anr.hashCode());
  }

  @Test
  void shouldCreateLANR() {
    val identifier = new Identifier();
    identifier.setSystem(KbvNamingSystem.BASE_ANR.getCanonicalUrl());
    identifier.setValue("123456789");

    val coding = identifier.getType().getCodingFirstRep();
    coding.setSystem(Hl7CodeSystem.HL7_V2_0203.getCanonicalUrl());
    coding.setCode("LANR");

    val anr = BaseANR.fromIdentifier(identifier);
    assertEquals(BaseANR.ANRType.LANR, anr.getType());
    assertEquals("123456789", anr.getValue());
    assertEquals(Hl7CodeSystem.HL7_V2_0203.getCanonicalUrl(), anr.getCodeSystemUrl());
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
      assertEquals(Hl7CodeSystem.HL7_V2_0203.getCanonicalUrl(), anr.getCodeSystemUrl());
      assertEquals(KbvNamingSystem.BASE_ANR.getCanonicalUrl(), anr.getNamingSystemUrl());
    } else if (qt == QualificationType.DENTIST) {
      assertEquals(ANRType.ZANR, anr.getType());
      assertEquals(
          DeBasisCodeSystem.IDENTIFIER_TYPE_DE_BASIS.getCanonicalUrl(), anr.getCodeSystemUrl());
      assertEquals(DeBasisNamingSystem.ZAHNARZTNUMMER.getCanonicalUrl(), anr.getNamingSystemUrl());
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
            qt -> {
              assertThrows(
                  IllegalArgumentException.class, () -> BaseANR.forQualification(qt, "123456789"));
            });
  }

  @Test
  void shouldThrowOnUndecidableANRCode01() {
    val identifier = new Identifier();
    identifier.setSystem(DeBasisNamingSystem.ZAHNARZTNUMMER.getCanonicalUrl());
    identifier.setValue("123456789");

    val coding = identifier.getType().getCodingFirstRep();
    coding.setSystem(KbvNamingSystem.PRUEFNUMMER.getCanonicalUrl());
    coding.setCode("LANR");

    assertThrows(InvalidBaseANR.class, () -> BaseANR.fromIdentifier(identifier));
  }

  @Test
  void shouldThrowOnUndecidableANRCode02() {
    val identifier = new Identifier();
    identifier.setSystem(CommonNamingSystem.ACME_IDS_PATIENT.getCanonicalUrl());
    identifier.setValue("123456789");

    val coding = identifier.getType().getCodingFirstRep();
    coding.setSystem(DeBasisNamingSystem.ZAHNARZTNUMMER.getCanonicalUrl());
    coding.setCode("ZANR");

    assertThrows(InvalidBaseANR.class, () -> BaseANR.fromIdentifier(identifier));
  }

  @Test
  void shouldThrowOnUndecidableANRCode03() {
    val identifier = new Identifier();
    identifier.setSystem(DeBasisNamingSystem.ZAHNARZTNUMMER.getCanonicalUrl());
    identifier.setValue("123456789");

    val coding = identifier.getType().getCodingFirstRep();
    coding.setSystem(VersicherungsArtDeBasis.CODE_SYSTEM.getCanonicalUrl());
    coding.setCode("LANR");

    assertThrows(InvalidBaseANR.class, () -> BaseANR.fromIdentifier(identifier));
  }

  @Test
  void shouldThrowOnUndecidableANRCode04() {
    val identifier = new Identifier();
    identifier.setSystem(KbvNamingSystem.BASE_ANR.getCanonicalUrl());
    identifier.setValue("123456789");

    val coding = identifier.getType().getCodingFirstRep();
    coding.setSystem(Hl7CodeSystem.HL7_V2_0203.getCanonicalUrl());
    coding.setCode("ZANR");

    assertThrows(InvalidBaseANR.class, () -> BaseANR.fromIdentifier(identifier));
  }

  @Test
  void shouldParseLANRTypeFromValidCode() {
    val codes = List.of("LANR", "lanr", "Lanr");
    codes.forEach(code -> assertEquals(BaseANR.ANRType.LANR, BaseANR.ANRType.fromCode(code)));
  }

  @Test
  void shouldParseZANRTypeFromValidCode() {
    val codes = List.of("ZANR", "zanr", "Zanr");
    codes.forEach(code -> assertEquals(BaseANR.ANRType.ZANR, BaseANR.ANRType.fromCode(code)));
  }

  @Test
  void shouldThrowOnInvalidTypeCode() {
    val codes = List.of("Arztnummer", "Zahnarztnummer", "ZANRT", "LANRT", "");
    codes.forEach(
        code -> assertThrows(IllegalArgumentException.class, () -> BaseANR.ANRType.fromCode(code)));
  }

  @Test
  void shouldCreateValidBaseANR() {
    val types = List.of(QualificationType.DOCTOR, QualificationType.DENTIST);
    types.forEach(
        t -> {
          val test = BaseANR.randomFromQualification(t);
          assertTrue(test.checkValue());
        });
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
    assertTrue(lanr.checkValue());
  }

  @Test
  void shouldNotCheckInvalidLANR() {
    val lanr = new LANR("111136701");
    assertFalse(lanr.checkValue());
  }

  @Test
  void shouldGiveValidNamingSystems() {
    val validNamingSystems = BaseANR.ANRType.validNamingSystems();
    assertNotNull(validNamingSystems);
    assertFalse(validNamingSystems.isEmpty());
  }

  @Test
  void shouldThrowOnIllegalQualificationTypes() {
    Arrays.stream(QualificationType.values())
        .filter(qt -> !qt.equals(QualificationType.DOCTOR) && !qt.equals(QualificationType.DENTIST))
        .forEach(
            qt -> {
              assertThrows(
                  IllegalArgumentException.class, () -> BaseANR.randomFromQualification(qt));
            });
  }
}
