/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.screenplay.abilities;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.fhir.valuesets.PayorType;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

class ProvidePatientBaseDataTest extends ParsingTest {

  @Test
  void shouldCreateGkvPatientFullName() {
    val patient = ProvidePatientBaseData.forGkvPatient(KVNR.from("X123456789"), "Fridolin Schraßer");

    assertEquals("Fridolin", patient.getPatient().getNameFirstRep().getGivenAsSingleString());
    assertEquals("Schraßer", patient.getPatient().getNameFirstRep().getFamily());
    assertEquals("Fridolin Schraßer", patient.getFullName());
    assertTrue(patient.getPatient().hasGkvKvnr());
    assertTrue(patient.isGKV());
    assertFalse(patient.isPKV());
    assertFalse(patient.hasRememberedConsent());
    assertEquals("X123456789", patient.getPatient().getKvnr().getValue());
    assertNotNull(patient.getIknr());
    assertFalse(patient.getIknr().isEmpty());
    assertEquals(IKNR.from(patient.getIknr()), patient.getInsuranceIknr());
    assertFalse(patient.getRememberedConsent().isPresent());
    assertFalse(patient.hasRememberedConsent());
    assertNotNull(patient.getInsuranceCoverage());
    assertNotNull(patient.getCoverageInsuranceType());
  }

  @Test
  void shouldCreateGkvPatient() {
    val patient = ProvidePatientBaseData.forGkvPatient(KVNR.from("X123456789"), "Fridolin", "Straßer");

    assertEquals("Fridolin", patient.getPatient().getNameFirstRep().getGivenAsSingleString());
    assertEquals("Straßer", patient.getPatient().getNameFirstRep().getFamily());
    assertEquals("Fridolin Straßer", patient.getFullName());
    assertTrue(patient.getPatient().hasGkvKvnr());
    assertTrue(patient.isGKV());
    assertFalse(patient.isPKV());
    assertFalse(patient.hasRememberedConsent());
    assertEquals("X123456789", patient.getPatient().getKvnr().getValue());
    assertNotNull(patient.getIknr());
    assertFalse(patient.getIknr().isEmpty());
    assertEquals(IKNR.from(patient.getIknr()), patient.getInsuranceIknr());
    assertFalse(patient.getRememberedConsent().isPresent());
    assertFalse(patient.hasRememberedConsent());
    assertNotNull(patient.getInsuranceCoverage());
    assertNotNull(patient.getCoverageInsuranceType());
  }

  @Test
  void shouldCreatePkvPatient() {
    val patient = ProvidePatientBaseData.forPkvPatient(KVNR.from("X123456789"), "Fridolin Straßer");

    assertEquals("Fridolin", patient.getPatient().getNameFirstRep().getGivenAsSingleString());
    assertEquals("Straßer", patient.getPatient().getNameFirstRep().getFamily());
    assertEquals("Fridolin Straßer", patient.getFullName());
    assertTrue(patient.getPatient().hasPkvKvnr());
    assertTrue(patient.isPKV());
    assertEquals("X123456789", patient.getPatient().getPkvId().orElseThrow().getValue());
    assertEquals("X123456789", patient.getPatient().getKvnr().getValue());
  }

  @Test
  void shouldCreatePkvPatient02() {
    val patient = ProvidePatientBaseData.forPkvPatient(KVNR.from("X123456789"), "Fridolin", "Straßer");

    assertEquals("Fridolin", patient.getPatient().getNameFirstRep().getGivenAsSingleString());
    assertEquals("Straßer", patient.getPatient().getNameFirstRep().getFamily());
    assertEquals("Fridolin Straßer", patient.getFullName());
    assertTrue(patient.getPatient().hasPkvKvnr());
    assertTrue(patient.isPKV());
    assertEquals("X123456789", patient.getPatient().getPkvId().orElseThrow().getValue());
    assertEquals("X123456789", patient.getPatient().getKvnr().getValue());
  }

  @Test
  void shouldCreatePkvWithFakedName() {
    val patient = ProvidePatientBaseData.forPatient(KVNR.from("X123456789"), "", "PKV");

    assertNotEquals("", patient.getPatient().getNameFirstRep().getGivenAsSingleString());
    assertNotEquals("", patient.getPatient().getNameFirstRep().getFamily());
    assertNotEquals("", patient.getFullName());
    assertTrue(patient.getPatient().hasPkvKvnr());
    assertEquals("X123456789", patient.getPatient().getPkvId().orElseThrow().getValue());
  }

  @Test
  void shouldGetCoverageInsuranceType() {
    val patient = ProvidePatientBaseData.forPatient(KVNR.from("X123456789"), "", "PKV");

    assertEquals(VersicherungsArtDeBasis.PKV, patient.getPatientInsuranceType());
    assertEquals(VersicherungsArtDeBasis.PKV, patient.getCoverageInsuranceType());
    assertTrue(patient.getPatient().hasPkvKvnr());
  }

  @Test
  void shouldGetChangedCoverageInsuranceType() {
    val patient = ProvidePatientBaseData.forPatient(KVNR.from("X123456789"), "", "PKV");
    patient.setCoverageInsuranceType(VersicherungsArtDeBasis.BG);

    assertEquals(VersicherungsArtDeBasis.PKV, patient.getPatientInsuranceType());
    assertEquals(VersicherungsArtDeBasis.BG, patient.getCoverageInsuranceType());
    assertTrue(patient.isPKV());
    assertFalse(patient.isGKV());
    assertTrue(patient.getPatient().hasPkvKvnr());

    val coverage = patient.getInsuranceCoverage();
    val result = ValidatorUtil.encodeAndValidate(parser, coverage);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(name = "Create Coverage with PayorType {0}")
  @EnumSource(value = PayorType.class)
  @NullSource
  void shouldCreateCoverageWithPayorType(PayorType payorType) {
    val patient = ProvidePatientBaseData.forPatient(KVNR.from("X123456789"), "", "PKV");
    patient.setPayorType(payorType);

    assertEquals(VersicherungsArtDeBasis.PKV, patient.getPatientInsuranceType());
    assertTrue(patient.isPKV());
    assertFalse(patient.isGKV());
    assertTrue(patient.getPatient().hasPkvKvnr());

    if (payorType != null)
      assertFalse(patient.getPayorType().isEmpty());
    else
      assertTrue(patient.getPayorType().isEmpty());

    val coverage = patient.getInsuranceCoverage();
    val result = ValidatorUtil.encodeAndValidate(parser, coverage);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldCreateGkvWithFakedName() {
    val patient = ProvidePatientBaseData.forPatient(KVNR.from("X123456789"), "", "GKV");

    assertNotEquals("", patient.getPatient().getNameFirstRep().getGivenAsSingleString());
    assertNotEquals("", patient.getPatient().getNameFirstRep().getFamily());
    assertNotEquals("", patient.getFullName());
    assertTrue(patient.getPatient().hasGkvKvnr());
    assertEquals("X123456789", patient.getPatient().getGkvId().orElseThrow().getValue());
  }
}
