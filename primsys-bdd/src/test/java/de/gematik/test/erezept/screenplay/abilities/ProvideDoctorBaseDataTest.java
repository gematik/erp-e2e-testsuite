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

import static org.junit.Assert.*;

import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import de.gematik.test.erezept.lei.cfg.DoctorConfiguration;
import lombok.val;
import org.junit.Test;

public class ProvideDoctorBaseDataTest {

  @Test
  public void shouldCreateDoctorBaseDataFromValidConfig() {
    val cfg = new DoctorConfiguration();
    cfg.setName("Bernd Claudius");
    cfg.setQualificationType("Arzt");

    val doc = ProvideDoctorBaseData.fromConfiguration(cfg);

    assertEquals("Bernd", doc.getPractitioner().getNameFirstRep().getGivenAsSingleString());
    assertEquals("Claudius", doc.getPractitioner().getNameFirstRep().getFamily());
    assertNotNull(doc.getPractitioner().getANR().getValue());
    assertNotEquals("", doc.getPractitioner().getANR().getValue());
    assertEquals(BaseANR.ANRType.LANR, doc.getPractitioner().getANR().getType());
    assertEquals(QualificationType.DOCTOR, doc.getPractitioner().getQualificationType());
  }

  @Test
  public void shouldCreateDentistBaseDataFromValidConfig() {
    val cfg = new DoctorConfiguration();
    cfg.setName("Bernd Claudius");
    cfg.setQualificationType("Zahnarzt");

    val doc = ProvideDoctorBaseData.fromConfiguration(cfg);

    assertEquals("Bernd", doc.getPractitioner().getNameFirstRep().getGivenAsSingleString());
    assertEquals("Claudius", doc.getPractitioner().getNameFirstRep().getFamily());
    assertNotNull(doc.getPractitioner().getANR().getValue());
    assertNotEquals("", doc.getPractitioner().getANR().getValue());
    assertEquals(BaseANR.ANRType.ZANR, doc.getPractitioner().getANR().getType());
    assertEquals(QualificationType.DENTIST, doc.getPractitioner().getQualificationType());
  }

  @Test
  public void shouldCreateDoctorWithFakedName() {
    val cfg = new DoctorConfiguration();
    cfg.setName("");
    cfg.setQualificationType("Zahnarzt");

    val doc = ProvideDoctorBaseData.fromConfiguration(cfg);

    assertNotEquals("", doc.getPractitioner().getNameFirstRep().getGivenAsSingleString());
    assertNotEquals("", doc.getPractitioner().getNameFirstRep().getFamily());
    assertNotNull(doc.getPractitioner().getANR().getValue());
    assertNotEquals("", doc.getPractitioner().getANR().getValue());
    assertEquals(BaseANR.ANRType.ZANR, doc.getPractitioner().getANR().getType());
    assertEquals(QualificationType.DENTIST, doc.getPractitioner().getQualificationType());
  }
}
