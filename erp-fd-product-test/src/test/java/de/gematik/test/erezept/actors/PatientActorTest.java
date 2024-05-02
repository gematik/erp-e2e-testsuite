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

package de.gematik.test.erezept.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.valuesets.PayorType;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

class PatientActorTest extends ParsingTest {

  @Test
  void shouldProvideCorrectGkvData() {
    val patient = new PatientActor("Sina Hüllmann");
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.random(), patient.getName()));
    assertEquals(VersicherungsArtDeBasis.GKV, patient.getPatientInsuranceType());
    assertTrue(patient.getAssignerOrganization().isEmpty());
  }

  @Test
  void shouldProvideCorrectPkvData() {
    val patient = new PatientActor("Sina Hüllmann");
    patient.can(ProvidePatientBaseData.forPkvPatient(KVNR.random(), patient.getName()));
    assertEquals(VersicherungsArtDeBasis.PKV, patient.getPatientInsuranceType());

    if (KbvItaErpVersion.getDefaultVersion().compareTo(KbvItaErpVersion.V1_0_2) == 0)
      assertTrue(patient.getAssignerOrganization().isPresent());
  }

  @Test
  void shouldProvideCorrectDataAfterChange() {
    val patient = new PatientActor("Sina Hüllmann");
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.random(), patient.getName()));

    // initialised as GKV
    assertEquals(VersicherungsArtDeBasis.GKV, patient.getPatientInsuranceType());
    assertTrue(patient.getAssignerOrganization().isEmpty());

    // now change to PKV
    patient.changePatientInsuranceType(VersicherungsArtDeBasis.PKV);
    assertEquals(VersicherungsArtDeBasis.PKV, patient.getPatientInsuranceType());
    assertEquals(VersicherungsArtDeBasis.PKV, patient.getCoverageInsuranceType());

    if (KbvItaErpVersion.getDefaultVersion().compareTo(KbvItaErpVersion.V1_0_2) == 0)
      assertTrue(patient.getAssignerOrganization().isPresent());
  }

  @Test
  void shouldProvideCorrectDataAfterChangeCoverageType() {
    val patient = new PatientActor("Sina Hüllmann");
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.random(), patient.getName()));

    // initialised as GKV
    assertEquals(VersicherungsArtDeBasis.GKV, patient.getPatientInsuranceType());
    assertEquals(VersicherungsArtDeBasis.GKV, patient.getCoverageInsuranceType());

    // now change to PKV and BG
    patient.changePatientInsuranceType(VersicherungsArtDeBasis.PKV);
    patient.changeCoverageInsuranceType(VersicherungsArtDeBasis.BG);
    assertEquals(VersicherungsArtDeBasis.PKV, patient.getPatientInsuranceType());
    assertEquals(VersicherungsArtDeBasis.BG, patient.getCoverageInsuranceType());

    // change KVNR for TestCases
    val kvnr = "X123456789";
    patient.setKvnr(kvnr);
    assertEquals(kvnr, patient.getPatientData().getKvnr().getValue());
  }

  @ParameterizedTest(name = "Create Coverage with PayorType {0}")
  @EnumSource(value = PayorType.class)
  @NullSource
  void shouldProvideCoverageWithPayorType(PayorType payorType) {
    val patient = new PatientActor("Sina Hüllmann");
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.random(), patient.getName()));

    patient.setPayorType(payorType);
    val coverage = patient.getInsuranceCoverage();
    val result = ValidatorUtil.encodeAndValidate(parser, coverage);
    assertTrue(result.isSuccessful());
  }
}
