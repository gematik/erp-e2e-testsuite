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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.test.erezept.fhir.r4.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.valuesets.DmpKennzeichen;
import de.gematik.test.erezept.fhir.valuesets.PayorType;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
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

  public void changePatientInsuranceType(InsuranceTypeDe insuranceType) {
    val bd = SafeAbility.getAbility(this, ProvidePatientBaseData.class);

    // hacky but we have many cases where we mix up those things
    val fixedInsuranceType =
        InsuranceTypeDe.PKV.equals(insuranceType) ? InsuranceTypeDe.PKV : InsuranceTypeDe.GKV;
    bd.setPatientInsuranceType(fixedInsuranceType);
  }

  public void setPayorType(PayorType payorType) {
    val bd = SafeAbility.getAbility(this, ProvidePatientBaseData.class);
    bd.setPayorType(payorType);
  }

  public void changeDmpKennzeichen(DmpKennzeichen dmpKennzeichen) {
    val bd = SafeAbility.getAbility(this, ProvidePatientBaseData.class);
    bd.setDmpKennzeichen(dmpKennzeichen);
  }

  public Optional<PayorType> getPayorType() {
    val bd = SafeAbility.getAbility(this, ProvidePatientBaseData.class);
    return bd.getPayorType();
  }

  public void changeCoverageInsuranceType(InsuranceTypeDe coverageType) {
    val bd = SafeAbility.getAbility(this, ProvidePatientBaseData.class);
    bd.setCoverageInsuranceType(coverageType);

    // hacky but we have many cases where we mix up those things
    if (InsuranceTypeDe.PKV.equals(coverageType)) {
      this.changePatientInsuranceType(coverageType);
    }
  }

  public InsuranceTypeDe getPatientInsuranceType() {
    val bd = SafeAbility.getAbility(this, ProvidePatientBaseData.class);
    return bd.getPatientInsuranceType();
  }

  public InsuranceTypeDe getCoverageInsuranceType() {
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

  @Override
  public String toString() {
    return format("{0} {1} {2}", this.getPatientInsuranceType().getCode(), type, this.getName());
  }
}
