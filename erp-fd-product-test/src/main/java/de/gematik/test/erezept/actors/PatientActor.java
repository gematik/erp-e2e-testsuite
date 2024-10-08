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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.test.erezept.fhir.builder.kbv.AssignerOrganizationFaker;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.resources.kbv.AssignerOrganization;
import de.gematik.test.erezept.fhir.resources.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.valuesets.DmpKennzeichen;
import de.gematik.test.erezept.fhir.valuesets.PayorType;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@Getter
public class PatientActor extends ErpActor {

  public PatientActor(String name) {
    super(ActorType.PATIENT, name);
  }

  public KVNR getKvnr() {
    val bd = SafeAbility.getAbility(this, ProvidePatientBaseData.class);
    return bd.getKvnr();
  }

  public Egk getEgk() {
    return SafeAbility.getAbility(this, ProvideEGK.class).getEgk();
  }

  public void changePatientInsuranceType(VersicherungsArtDeBasis insuranceType) {
    val bd = SafeAbility.getAbility(this, ProvidePatientBaseData.class);
    bd.setPatientInsuranceType(insuranceType);
  }

  public void setPayorType(PayorType payorType) {
    val bd = SafeAbility.getAbility(this, ProvidePatientBaseData.class);
    bd.setPayorType(payorType);
  }

  public void setKvnr(String kvnr) {
    this.setKvnr(KVNR.from(kvnr));
  }

  public void setKvnr(KVNR kvnr) {
    val bd = SafeAbility.getAbility(this, ProvidePatientBaseData.class);
    bd.setKvnr(kvnr);
  }

  public void changeDmpKennzeichen(DmpKennzeichen dmpKennzeichen) {
    val bd = SafeAbility.getAbility(this, ProvidePatientBaseData.class);
    bd.setDmpKennzeichen(dmpKennzeichen);
  }

  public Optional<PayorType> getPayorType() {
    val bd = SafeAbility.getAbility(this, ProvidePatientBaseData.class);
    return bd.getPayorType();
  }

  public void changeCoverageInsuranceType(VersicherungsArtDeBasis coverageType) {
    val bd = SafeAbility.getAbility(this, ProvidePatientBaseData.class);
    bd.setCoverageInsuranceType(coverageType);
  }

  public VersicherungsArtDeBasis getPatientInsuranceType() {
    val bd = SafeAbility.getAbility(this, ProvidePatientBaseData.class);
    return bd.getPatientInsuranceType();
  }

  public VersicherungsArtDeBasis getCoverageInsuranceType() {
    val bd = SafeAbility.getAbility(this, ProvidePatientBaseData.class);
    return bd.getCoverageInsuranceType();
  }

  public KbvPatient getPatientData() {
    val bd = SafeAbility.getAbility(this, ProvidePatientBaseData.class);
    return bd.getPatient();
  }

  public KbvCoverage getInsuranceCoverage() {
    val bd = SafeAbility.getAbility(this, ProvidePatientBaseData.class);
    return bd.getInsuranceCoverage();
  }

  public Optional<AssignerOrganization> getAssignerOrganization() {
    Optional<AssignerOrganization> ret = Optional.empty();
    // an assigner Organization for PKV is only required for kbv.ita.erp == 1.0.2
    if (this.getPatientInsuranceType() == VersicherungsArtDeBasis.PKV
        && KbvItaErpVersion.getDefaultVersion().compareTo(KbvItaErpVersion.V1_1_0) < 0) {

      // for now, we do not have the AssignerOrganization (which was faked anyway for getting a
      // Reference + Name
      // build a faked one matching the Reference of the patient
      val fakedAssignerOrganization =
          AssignerOrganizationFaker.builder().forPatient(this.getPatientData()).fake();
      ret = Optional.of(fakedAssignerOrganization);
    }
    return ret;
  }

  @Override
  public String toString() {
    return format("{0} {1} {2}", this.getPatientInsuranceType().getCode(), type, this.getName());
  }
}
