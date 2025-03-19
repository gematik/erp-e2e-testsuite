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
 */

package de.gematik.test.erezept.screenplay.abilities;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.config.dto.actor.DoctorConfiguration;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvNamingSystem;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import lombok.val;
import org.junit.jupiter.api.Test;

class ProvideDoctorBaseDataTest extends ErpFhirBuildingTest {

  @Test
  void shouldCreateDoctorBaseDataFromValidConfig() {
    val cfgDto = new DoctorConfiguration();
    cfgDto.setName("Bernd Claudius");
    cfgDto.setQualificationType("Arzt");

    val doc = ProvideDoctorBaseData.fromConfiguration(cfgDto, "UselessTestId");

    assertEquals("Bernd", doc.getPractitioner().getNameFirstRep().getGivenAsSingleString());
    assertEquals("Claudius", doc.getPractitioner().getNameFirstRep().getFamily());
    assertNotNull(doc.getPractitioner().getANR().getValue());
    assertNotEquals("", doc.getPractitioner().getANR().getValue());
    assertEquals(BaseANR.ANRType.LANR, doc.getPractitioner().getANR().getType());
    assertEquals(QualificationType.DOCTOR, doc.getPractitioner().getQualificationType());
    assertNotNull(doc.getPractitioner());
    assertNotNull(doc.getMedicalOrganization());
  }

  @Test
  void shouldCreateDentistBaseDataFromValidConfig() {
    val cfgDto = new DoctorConfiguration();
    cfgDto.setName("Bernd Claudius");
    cfgDto.setQualificationType("Zahnarzt");

    val doc = ProvideDoctorBaseData.fromConfiguration(cfgDto, "UselessTestId");

    assertEquals("Bernd", doc.getPractitioner().getNameFirstRep().getGivenAsSingleString());
    assertEquals("Claudius", doc.getPractitioner().getNameFirstRep().getFamily());
    assertNotNull(doc.getPractitioner().getANR().getValue());
    assertNotEquals("", doc.getPractitioner().getANR().getValue());
    assertEquals(BaseANR.ANRType.ZANR, doc.getPractitioner().getANR().getType());
    assertEquals(QualificationType.DENTIST, doc.getPractitioner().getQualificationType());
    assertNotNull(doc.getPractitioner());
    assertNotNull(doc.getMedicalOrganization());
  }

  @Test
  void shouldCreateDoctorWithFakedName() {
    val cfgDto = new DoctorConfiguration();
    cfgDto.setName("");
    cfgDto.setQualificationType("Zahnarzt");

    val doc = ProvideDoctorBaseData.fromConfiguration(cfgDto, "UselessTestId");

    assertNotEquals("", doc.getPractitioner().getNameFirstRep().getGivenAsSingleString());
    assertNotEquals("", doc.getPractitioner().getNameFirstRep().getFamily());
    assertNotNull(doc.getPractitioner().getANR().getValue());
    assertNotEquals("", doc.getPractitioner().getANR().getValue());
    assertEquals(BaseANR.ANRType.ZANR, doc.getPractitioner().getANR().getType());
    assertEquals(QualificationType.DENTIST, doc.getPractitioner().getQualificationType());
  }

  @Test
  void shouldGenerateAsvPrctitioner() {
    val cfgDto = new DoctorConfiguration();
    cfgDto.setName("Bernd Claudius");
    cfgDto.setQualificationType("Arzt");
    val doc = ProvideDoctorBaseData.fromConfiguration(cfgDto, "UselessTestId");
    doc.setAsv(true);
    val pract = doc.getPractitioner();
    assertEquals(3, pract.getQualification().size());
    assertTrue(
        doc.getPractitioner().getQualification().stream()
            .map(
                q ->
                    q.getCode().getCoding().stream()
                        .filter(
                            c ->
                                c.getSystem()
                                    .contains(
                                        KbvNamingSystem.ASV_FACHGRUPPENNUMMER.getCanonicalUrl())))
            .findFirst()
            .isPresent());
  }
}
