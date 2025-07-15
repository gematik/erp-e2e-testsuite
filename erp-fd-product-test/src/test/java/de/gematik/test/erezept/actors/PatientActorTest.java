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

package de.gematik.test.erezept.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.valuesets.DmpKennzeichen;
import de.gematik.test.erezept.fhir.valuesets.PayorType;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

class PatientActorTest extends ErpFhirParsingTest {

  @Test
  void shouldProvideCorrectGkvData() {
    val patient = new PatientActor("Sina Hüllmann");
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.randomGkv(), patient.getName()));
    assertEquals(InsuranceTypeDe.GKV, patient.getPatientInsuranceType());
  }

  @Test
  void shouldProvideCorrectPkvData() {
    val patient = new PatientActor("Sina Hüllmann");
    patient.can(ProvidePatientBaseData.forPkvPatient(KVNR.randomPkv(), patient.getName()));
    assertEquals(InsuranceTypeDe.PKV, patient.getPatientInsuranceType());
  }

  @Test
  void shouldProvideCorrectDataAfterChange() {
    val patient = new PatientActor("Sina Hüllmann");
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.randomGkv(), patient.getName()));

    // initialised as GKV
    assertEquals(InsuranceTypeDe.GKV, patient.getPatientInsuranceType());

    // now change to PKV
    patient.changePatientInsuranceType(InsuranceTypeDe.PKV);
    assertEquals(InsuranceTypeDe.PKV, patient.getPatientInsuranceType());
    assertEquals(InsuranceTypeDe.PKV, patient.getCoverageInsuranceType());
  }

  @Test
  void shouldAutomaticallyChangePatientInsuranceTypeOnPkv() {
    val patient = new PatientActor("Sina Hüllmann");
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.randomGkv(), patient.getName()));

    // initialised as GKV
    assertEquals(InsuranceTypeDe.GKV, patient.getPatientInsuranceType());
    assertEquals(InsuranceTypeDe.GKV, patient.getCoverageInsuranceType());

    // now change to PKV
    patient.changeCoverageInsuranceType(InsuranceTypeDe.PKV);
    assertEquals(InsuranceTypeDe.PKV, patient.getPatientInsuranceType());
    assertEquals(InsuranceTypeDe.PKV, patient.getCoverageInsuranceType());
  }

  @Test
  void shouldAutomaticallyChangeCoverageInsuranceTypeOnPkv() {
    val patient = new PatientActor("Sina Hüllmann");
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.randomGkv(), patient.getName()));

    // initialised as GKV
    assertEquals(InsuranceTypeDe.GKV, patient.getPatientInsuranceType());
    assertEquals(InsuranceTypeDe.GKV, patient.getCoverageInsuranceType());

    // now change to PKV
    patient.changePatientInsuranceType(InsuranceTypeDe.PKV);
    assertEquals(InsuranceTypeDe.PKV, patient.getPatientInsuranceType());
    assertEquals(InsuranceTypeDe.PKV, patient.getCoverageInsuranceType());
  }

  @Test
  void shouldProvideCorrectDataAfterChangeCoverageType() {
    val patient = new PatientActor("Sina Hüllmann");
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.randomGkv(), patient.getName()));

    // initialised as GKV
    assertEquals(InsuranceTypeDe.GKV, patient.getPatientInsuranceType());
    assertEquals(InsuranceTypeDe.GKV, patient.getCoverageInsuranceType());

    // now change to PKV and BG
    patient.changePatientInsuranceType(InsuranceTypeDe.PKV);
    patient.changeCoverageInsuranceType(InsuranceTypeDe.BG);
    assertEquals(InsuranceTypeDe.PKV, patient.getPatientInsuranceType());
    assertEquals(InsuranceTypeDe.BG, patient.getCoverageInsuranceType());
  }

  @Test
  void shouldChangeDmpKennzeichen() {
    val patient = new PatientActor("Sina Hüllmann");
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.randomGkv(), patient.getName()));

    // initialised as Not_Set
    assertEquals(DmpKennzeichen.NOT_SET, patient.getInsuranceCoverage().getDmpKennzeichen());

    // now change to DmpKennzeichen.DM1
    patient.changeDmpKennzeichen(DmpKennzeichen.DM1);
    assertEquals(DmpKennzeichen.DM1, patient.getInsuranceCoverage().getDmpKennzeichen());
  }

  @ParameterizedTest(name = "Create Coverage with PayorType {0}")
  @EnumSource(value = PayorType.class)
  @NullSource
  void shouldProvideCoverageWithPayorType(PayorType payorType) {
    val patient = new PatientActor("Sina Hüllmann");
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.randomGkv(), patient.getName()));

    patient.setPayorType(payorType);
    val coverage = patient.getInsuranceCoverage();
    val result = ValidatorUtil.encodeAndValidate(parser, coverage);
    assertTrue(result.isSuccessful());
  }
}
