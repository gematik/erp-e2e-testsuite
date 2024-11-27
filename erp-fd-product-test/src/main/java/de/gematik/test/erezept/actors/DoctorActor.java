/*
 * Copyright 2024 gematik GmbH
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

import de.gematik.test.erezept.fhir.resources.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.resources.kbv.MedicalOrganization;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import de.gematik.test.erezept.screenplay.abilities.ProvideDoctorBaseData;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class DoctorActor extends ErpActor {

  public DoctorActor(String name) {
    super(ActorType.DOCTOR, name);
  }

  public KbvPractitioner getPractitioner() {
    val baseData = SafeAbility.getAbility(this, ProvideDoctorBaseData.class);
    return baseData.getPractitioner();
  }

  public String getHbaTelematikId() {
    val baseData = SafeAbility.getAbility(this, ProvideDoctorBaseData.class);
    return baseData.getHbaTelematikId();
  }

  public MedicalOrganization getMedicalOrganization() {
    val baseData = SafeAbility.getAbility(this, ProvideDoctorBaseData.class);
    return baseData.getMedicalOrganization();
  }

  public void changeQualificationType(QualificationType type) {
    val bd = SafeAbility.getAbility(this, ProvideDoctorBaseData.class);
    bd.setQualificationType(type);
    if (type != QualificationType.MIDWIFE) {
      bd.setDoctorNumber(BaseANR.randomFromQualification(type));
    }
  }
}
