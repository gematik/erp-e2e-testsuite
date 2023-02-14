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

package de.gematik.test.erezept.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import lombok.val;
import org.junit.jupiter.api.Test;

class PatientActorTest {

  @Test
  void shouldProvideCorrectGkvData() {
    val patient = new PatientActor("Sina Hüllmann");
    patient.can(ProvidePatientBaseData.forGkvPatient("X123456789", patient.getName()));
    assertEquals(VersicherungsArtDeBasis.GKV, patient.getInsuranceType());
    assertTrue(patient.getAssignerOrganization().isEmpty());
  }

  @Test
  void shouldProvideCorrectPkvData() {
    val patient = new PatientActor("Sina Hüllmann");
    patient.can(ProvidePatientBaseData.forPkvPatient("X123456789", patient.getName()));
    assertEquals(VersicherungsArtDeBasis.PKV, patient.getInsuranceType());
    assertTrue(patient.getAssignerOrganization().isPresent());
  }

  @Test
  void shouldProvideCorrectDataAfterChange() {
    val patient = new PatientActor("Sina Hüllmann");
    patient.can(ProvidePatientBaseData.forGkvPatient("X123456789", patient.getName()));

    // initialised as GKV
    assertEquals(VersicherungsArtDeBasis.GKV, patient.getInsuranceType());
    assertTrue(patient.getAssignerOrganization().isEmpty());

    // now change to PKV
    patient.changeInsuranceType(VersicherungsArtDeBasis.PKV);
    assertEquals(VersicherungsArtDeBasis.PKV, patient.getInsuranceType());
    assertTrue(patient.getAssignerOrganization().isPresent());
  }
}
