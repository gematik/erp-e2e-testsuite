/*
 * Copyright (c) 2022 gematik GmbH
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import de.gematik.test.erezept.fhir.exceptions.InvalidBaseANR;
import de.gematik.test.erezept.fhir.parser.profiles.ErpCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.ErpNamingSystem;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Identifier;
import org.junit.Test;

public class BaseANRTest {

  @Test
  public void shouldCreateZANR() {
    val identifer = new Identifier();
    identifer.setSystem(ErpNamingSystem.ZAHNARZTNUMMER.getCanonicalUrl());
    identifer.setValue("123456789");

    val coding = identifer.getType().getCodingFirstRep();
    coding.setSystem(ErpCodeSystem.IDENTIFIER_TYPE_DE_BASIS.getCanonicalUrl());
    coding.setCode("ZANR");

    val anr = BaseANR.fromIdentifier(identifer);
    assertEquals(BaseANR.ANRType.ZANR, anr.getType());
    assertEquals("123456789", anr.getValue());
  }

  @Test
  public void shouldCreateLANR() {
    val identifer = new Identifier();
    identifer.setSystem(ErpNamingSystem.KBV_NS_BASE_ANR.getCanonicalUrl());
    identifer.setValue("123456789");

    val coding = identifer.getType().getCodingFirstRep();
    coding.setSystem(ErpCodeSystem.HL7_V2_0203.getCanonicalUrl());
    coding.setCode("LANR");

    val anr = BaseANR.fromIdentifier(identifer);
    assertEquals(BaseANR.ANRType.LANR, anr.getType());
    assertEquals("123456789", anr.getValue());
  }

  @Test
  public void shouldThrowOnUndicidableANRCode01() {
    val identifer = new Identifier();
    identifer.setSystem(ErpNamingSystem.ZAHNARZTNUMMER.getCanonicalUrl());
    identifer.setValue("123456789");

    val coding = identifer.getType().getCodingFirstRep();
    coding.setSystem(ErpNamingSystem.KBV_PRUEFNUMMER.getCanonicalUrl());
    coding.setCode("LANR");

    assertThrows(InvalidBaseANR.class, () -> BaseANR.fromIdentifier(identifer));
  }

  @Test
  public void shouldThrowOnUndicidableANRCode02() {
    val identifer = new Identifier();
    identifer.setSystem(ErpNamingSystem.ACME_IDS_PATIENT.getCanonicalUrl());
    identifer.setValue("123456789");

    val coding = identifer.getType().getCodingFirstRep();
    coding.setSystem(ErpNamingSystem.ZAHNARZTNUMMER.getCanonicalUrl());
    coding.setCode("ZANR");

    assertThrows(InvalidBaseANR.class, () -> BaseANR.fromIdentifier(identifer));
  }

  @Test
  public void shouldThrowOnUndicidableANRCode03() {
    val identifer = new Identifier();
    identifer.setSystem(ErpNamingSystem.ZAHNARZTNUMMER.getCanonicalUrl());
    identifer.setValue("123456789");

    val coding = identifer.getType().getCodingFirstRep();
    coding.setSystem(ErpCodeSystem.IDENTIFIER_TYPE_DE_BASIS.getCanonicalUrl());
    coding.setCode("LANR");

    assertThrows(InvalidBaseANR.class, () -> BaseANR.fromIdentifier(identifer));
  }

  @Test
  public void shouldThrowOnUndicidableANRCode04() {
    val identifer = new Identifier();
    identifer.setSystem(ErpNamingSystem.KBV_NS_BASE_ANR.getCanonicalUrl());
    identifer.setValue("123456789");

    val coding = identifer.getType().getCodingFirstRep();
    coding.setSystem(ErpCodeSystem.HL7_V2_0203.getCanonicalUrl());
    coding.setCode("ZANR");

    assertThrows(InvalidBaseANR.class, () -> BaseANR.fromIdentifier(identifer));
  }

  @Test
  public void shouldParseLANRTypeFromValidCode() {
    val codes = List.of("LANR", "lanr", "Lanr");
    codes.forEach(code -> assertEquals(BaseANR.ANRType.LANR, BaseANR.ANRType.fromCode(code)));
  }

  @Test
  public void shouldParseZANRTypeFromValidCode() {
    val codes = List.of("ZANR", "zanr", "Zanr");
    codes.forEach(code -> assertEquals(BaseANR.ANRType.ZANR, BaseANR.ANRType.fromCode(code)));
  }

  @Test
  public void shouldThrowOnInvalidTypeCode() {
    val codes = List.of("Arztnummer", "Zahnarztnummer", "ZANRT", "LANRT", "");
    codes.forEach(
        code -> assertThrows(IllegalArgumentException.class, () -> BaseANR.ANRType.fromCode(code)));
  }

  @Test
  public void shouldCreateValidBaseANR() {
    val types = List.of(QualificationType.DOCTOR, QualificationType.DENTIST);
    types.forEach(
        t -> {
          val test = BaseANR.randomFromQualification(t);
          assertTrue(test.checkValue());
        });
  }

  @Test
  public void shouldCheckValidLANR() {
    val lanr = new LANR("754236701");
    assertTrue(lanr.checkValue());
  }

  @Test
  public void shouldNotCheckInvalidLANR() {
    val lanr = new LANR("111136701");
    assertFalse(lanr.checkValue());
  }
}
