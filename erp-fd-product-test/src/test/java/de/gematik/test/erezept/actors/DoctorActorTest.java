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

import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.ErpFdTestsuiteFactory;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import de.gematik.test.erezept.screenplay.abilities.ProvideDoctorBaseData;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DoctorActorTest {

  private DoctorActor createDoc(String name) {
    StopwatchProvider.init();
    val config = ErpFdTestsuiteFactory.create();
    val doctor = new DoctorActor(name);
    val docConfig = config.getDoctorConfig(doctor.getName());
    val provideBaseData = ProvideDoctorBaseData.fromConfiguration(docConfig);
    doctor.can(provideBaseData);
    return doctor;
  }


  @Test
  void shouldProvideCorrectData() {
    val doctor = createDoc("Adelheid Ulmenwald");
    assertNotNull(doctor.getPractitioner());
    assertNotNull(doctor.getMedicalOrganization());
    assertNotNull(doctor.getMedicalOrganization());
  }

  @ParameterizedTest
  @EnumSource(value = QualificationType.class)
  void shouldProvideCorrectDataAfterChange(QualificationType type) {
    val doctor = createDoc("Adelheid Ulmenwald");
    doctor.changeQualificationType(type);
    val bd = SafeAbility.getAbility(doctor, ProvideDoctorBaseData.class);
    assertEquals(type, bd.getPractitioner().getQualificationType());
  }
}
