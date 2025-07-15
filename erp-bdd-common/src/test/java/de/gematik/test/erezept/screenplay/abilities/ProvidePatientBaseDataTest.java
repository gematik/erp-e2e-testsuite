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

package de.gematik.test.erezept.screenplay.abilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.valuesets.DmpKennzeichen;
import de.gematik.test.erezept.fhir.valuesets.PayorType;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

class ProvidePatientBaseDataTest extends ErpFhirParsingTest {

  @Test
  void shouldCreateGkvPatientFullName() {
    val patient =
        ProvidePatientBaseData.forGkvPatient(KVNR.from("X123456789"), "Fridolin Schraßer");

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
    assertEquals(IKNR.asSidIknr(patient.getIknr()), patient.getInsuranceIknr());
    assertFalse(patient.getRememberedConsent().isPresent());
    assertFalse(patient.hasRememberedConsent());
    assertNotNull(patient.getInsuranceCoverage());
    assertNotNull(patient.getCoverageInsuranceType());
  }

  @Test
  void shouldCreateGkvPatient() {
    val patient =
        ProvidePatientBaseData.forGkvPatient(KVNR.from("X123456789"), "Fridolin", "Straßer");

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
    assertEquals(IKNR.asSidIknr(patient.getIknr()), patient.getInsuranceIknr());
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
    if (KbvItaErpVersion.getDefaultVersion().compareTo(KbvItaErpVersion.V1_1_0) <= 0) {
      assertTrue(patient.getPatient().hasPkvKvnr());
      assertTrue(patient.isPKV());
      assertEquals("X123456789", patient.getPatient().getPkvId().orElseThrow().getValue());
    } else {
      assertFalse(patient.getPatient().hasPkvKvnr());
    }
    assertEquals("X123456789", patient.getPatient().getKvnr().getValue());
  }

  @Test
  void shouldCreatePkvPatient02() {
    val patient =
        ProvidePatientBaseData.forPkvPatient(KVNR.from("X123456789"), "Fridolin", "Straßer");

    assertEquals("Fridolin", patient.getPatient().getNameFirstRep().getGivenAsSingleString());
    assertEquals("Straßer", patient.getPatient().getNameFirstRep().getFamily());
    assertEquals("Fridolin Straßer", patient.getFullName());

    if (KbvItaErpVersion.getDefaultVersion().compareTo(KbvItaErpVersion.V1_1_0) <= 0) {
      assertTrue(patient.getPatient().hasPkvKvnr());
      assertTrue(patient.isPKV());
      assertEquals("X123456789", patient.getPatient().getPkvId().orElseThrow().getValue());
    } else {
      assertFalse(patient.getPatient().hasPkvKvnr());
    }
    assertEquals("X123456789", patient.getPatient().getKvnr().getValue());
  }

  @Test
  void shouldCreatePkvWithFakedName() {
    val patient = ProvidePatientBaseData.forPatient(KVNR.from("X123456789"), "", "PKV");

    assertNotEquals("", patient.getPatient().getNameFirstRep().getGivenAsSingleString());
    assertNotEquals("", patient.getPatient().getNameFirstRep().getFamily());
    assertNotEquals("", patient.getFullName());
    if (KbvItaErpVersion.getDefaultVersion().compareTo(KbvItaErpVersion.V1_1_0) <= 0) {
      assertTrue(patient.getPatient().hasPkvKvnr());
      assertTrue(patient.isPKV());
      assertEquals("X123456789", patient.getPatient().getPkvId().orElseThrow().getValue());
    } else {
      assertFalse(patient.getPatient().hasPkvKvnr());
    }
  }

  @Test
  void shouldGetCoverageInsuranceType() {
    val patient =
        ProvidePatientBaseData.forPatient(KVNR.from("X123456789"), "Fridolin Straßer", "PKV");

    assertEquals(InsuranceTypeDe.PKV, patient.getPatientInsuranceType());
    assertEquals(InsuranceTypeDe.PKV, patient.getCoverageInsuranceType());
    if (KbvItaErpVersion.getDefaultVersion().compareTo(KbvItaErpVersion.V1_1_0) <= 0) {
      assertTrue(patient.getPatient().hasPkvKvnr());
      assertTrue(patient.isPKV());
    } else {
      assertFalse(patient.getPatient().hasPkvKvnr());
    }
  }

  @Test
  void shouldGetChangedCoverageInsuranceType() {
    val patient =
        ProvidePatientBaseData.forPatient(KVNR.from("X123456789"), "Fridolin Straßer", "PKV");
    patient.setCoverageInsuranceType(InsuranceTypeDe.BG);

    assertEquals(InsuranceTypeDe.PKV, patient.getPatientInsuranceType());
    assertEquals(InsuranceTypeDe.BG, patient.getCoverageInsuranceType());

    if (KbvItaErpVersion.getDefaultVersion().compareTo(KbvItaErpVersion.V1_1_0) <= 0) {
      assertTrue(patient.getPatient().hasPkvKvnr());
      assertTrue(patient.isPKV());
      assertFalse(patient.isGKV());
    } else {
      assertFalse(patient.getPatient().hasPkvKvnr());
    }
    val coverage = patient.getInsuranceCoverage();
    val result = ValidatorUtil.encodeAndValidate(parser, coverage);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldChangeKvnrAfterChangingCoverage() {
    val patient =
        ProvidePatientBaseData.forPatient(
            KVNR.from("X123456789"), "Fridolin Straßer", InsuranceTypeDe.GKV);
    assertEquals(InsuranceTypeDe.GKV, patient.getPatientInsuranceType());
    assertEquals(InsuranceTypeDe.GKV, patient.getKvnr().getInsuranceType());

    patient.setPatientInsuranceType(InsuranceTypeDe.PKV);
    assertEquals(InsuranceTypeDe.PKV, patient.getPatientInsuranceType());
    assertEquals(InsuranceTypeDe.PKV, patient.getKvnr().getInsuranceType());

    patient.setCoverageInsuranceType(InsuranceTypeDe.BG);
    assertEquals(InsuranceTypeDe.PKV, patient.getKvnr().getInsuranceType());
  }

  @ParameterizedTest(name = "Create Coverage with PayorType {0}")
  @EnumSource(value = PayorType.class)
  @NullSource
  void shouldCreateCoverageWithPayorType(PayorType payorType) {
    val patient = ProvidePatientBaseData.forPatient(KVNR.from("X123456789"), "", "PKV");
    patient.setPayorType(payorType);

    assertEquals(InsuranceTypeDe.PKV, patient.getPatientInsuranceType());
    assertTrue(patient.isPKV());
    assertFalse(patient.isGKV());
    if (KbvItaErpVersion.getDefaultVersion().compareTo(KbvItaErpVersion.V1_1_0) <= 0) {
      assertTrue(patient.getPatient().hasPkvKvnr());
      assertTrue(patient.isPKV());
    } else {
      assertFalse(patient.getPatient().hasPkvKvnr());
    }
    if (payorType != null) assertFalse(patient.getPayorType().isEmpty());
    else assertTrue(patient.getPayorType().isEmpty());

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

  @Test
  void shouldCreateCoverageWithDmpKennzeichen() {
    val patient = ProvidePatientBaseData.forPatient(KVNR.from("X123456789"), "", "PKV");
    patient.setDmpKennzeichen(DmpKennzeichen.DM1);

    val coverage = patient.getInsuranceCoverage();
    val result = ValidatorUtil.encodeAndValidate(parser, coverage);

    assertTrue(result.isSuccessful());
    assertEquals(DmpKennzeichen.DM1, coverage.getDmpKennzeichen());
  }
}
