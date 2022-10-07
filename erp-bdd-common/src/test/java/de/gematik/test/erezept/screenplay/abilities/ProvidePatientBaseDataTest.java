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

package de.gematik.test.erezept.screenplay.abilities;

import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.junit.Test;

public class ProvidePatientBaseDataTest {

  @Test
  public void shouldCreateGkvPatient() {
    val patient = ProvidePatientBaseData.forGkvPatient("X123456789", "Fridolin Straßer");

    assertEquals("Fridolin", patient.getPatient().getNameFirstRep().getGivenAsSingleString());
    assertEquals("Straßer", patient.getPatient().getNameFirstRep().getFamily());
    assertEquals("Fridolin Straßer", patient.getFullName());
    assertTrue(patient.getPatient().hasGkvId());
    assertEquals("X123456789", patient.getPatient().getKvid().orElseThrow());
  }

  @Test
  public void shouldCreatePkvPatient() {
    val patient = ProvidePatientBaseData.forPkvPatient("X123456789", "Fridolin Straßer");

    assertEquals("Fridolin", patient.getPatient().getNameFirstRep().getGivenAsSingleString());
    assertEquals("Straßer", patient.getPatient().getNameFirstRep().getFamily());
    assertEquals("Fridolin Straßer", patient.getFullName());
    assertTrue(patient.getPatient().hasPkvId());
    assertEquals("X123456789", patient.getPatient().getPkvId().orElseThrow());
  }

  @Test
  public void shouldCreatePkvWithFakedName() {
    val patient = ProvidePatientBaseData.forPatient("X123456789", "", "PKV");

    assertNotEquals("", patient.getPatient().getNameFirstRep().getGivenAsSingleString());
    assertNotEquals("", patient.getPatient().getNameFirstRep().getFamily());
    assertNotEquals("", patient.getFullName());
    assertTrue(patient.getPatient().hasPkvId());
    assertEquals("X123456789", patient.getPatient().getPkvId().orElseThrow());
  }

  @Test
  public void shouldCreateGkvWithFakedName() {
    val patient = ProvidePatientBaseData.forPatient("X123456789", "", "GKV");

    assertNotEquals("", patient.getPatient().getNameFirstRep().getGivenAsSingleString());
    assertNotEquals("", patient.getPatient().getNameFirstRep().getFamily());
    assertNotEquals("", patient.getFullName());
    assertTrue(patient.getPatient().hasGkvId());
    assertEquals("X123456789", patient.getPatient().getKvid().orElseThrow());
  }
}
