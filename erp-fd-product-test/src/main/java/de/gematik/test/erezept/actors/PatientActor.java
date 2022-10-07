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

package de.gematik.test.erezept.actors;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.builder.kbv.AssignerOrganizationBuilder;
import de.gematik.test.erezept.fhir.resources.kbv.AssignerOrganization;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Coverage;

@Slf4j
@Getter
public class PatientActor extends ErpActor {

  public PatientActor(String name) {
    super(ActorType.PATIENT, name);
  }

  public String getKvnr() {
    val bd = SafeAbility.getAbility(this, ProvidePatientBaseData.class);
    return bd.getKvid();
  }

  public void changeInsuranceType(VersicherungsArtDeBasis insuranceType) {
    val bd = SafeAbility.getAbility(this, ProvidePatientBaseData.class);
    bd.setVersicherungsArt(insuranceType);
  }

  public VersicherungsArtDeBasis getInsuranceType() {
    val bd = SafeAbility.getAbility(this, ProvidePatientBaseData.class);
    return bd.getVersicherungsArt();
  }

  public KbvPatient getPatientData() {
    val bd = SafeAbility.getAbility(this, ProvidePatientBaseData.class);
    return bd.getPatient();
  }

  public Coverage getInsuranceCoverage() {
    val bd = SafeAbility.getAbility(this, ProvidePatientBaseData.class);
    return bd.getInsuranceCoverage();
  }

  public Optional<AssignerOrganization> getAssignerOrganization() {
    Optional<AssignerOrganization> ret = Optional.empty();
    if (this.getInsuranceType() == VersicherungsArtDeBasis.PKV) {
      // for now, we do not have the AssignerOrganization (which was faked anyways for getting a
      // Reference + Name
      // build a faked one matching the Reference of the patient
      val fakedAssignerOrganization =
          AssignerOrganizationBuilder.faker(this.getPatientData()).build();
      ret = Optional.of(fakedAssignerOrganization);
    }
    return ret;
  }

  @Override
  public String toString() {
    return format("{0} {1} {2}", this.getInsuranceType().getCode(), type, this.getName());
  }
}
