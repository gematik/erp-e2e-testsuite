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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerValueSet;
import static de.gematik.test.erezept.fhir.builder.GemFaker.randomElement;
import static de.gematik.test.erezept.fhir.builder.kbv.KbvCoverageBuilder.insurance;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.values.BGInsuranceCoverageInfo;
import de.gematik.test.erezept.fhir.values.DynamicInsuranceCoverageInfo;
import de.gematik.test.erezept.fhir.values.GkvInsuranceCoverageInfo;
import de.gematik.test.erezept.fhir.values.PkvInsuranceCoverageInfo;
import de.gematik.test.erezept.fhir.valuesets.DmpKennzeichen;
import de.gematik.test.erezept.fhir.valuesets.PersonGroup;
import de.gematik.test.erezept.fhir.valuesets.VersichertenStatus;
import de.gematik.test.erezept.fhir.valuesets.Wop;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;

public class KbvCoverageFaker {

  private InsuranceTypeDe insurance = randomElement(InsuranceTypeDe.GKV, InsuranceTypeDe.PKV);
  private boolean setInsuranceTypeDirect = false;
  private final Map<String, Consumer<KbvCoverageBuilder>> builderConsumers = new HashMap<>();

  private KbvCoverageFaker() {
    this.withPersonGroup(fakerValueSet(PersonGroup.class))
        .withDmpKennzeichen(fakerValueSet(DmpKennzeichen.class))
        .withWop(fakerValueSet(Wop.class))
        .withInsuranceStatus(fakerValueSet(VersichertenStatus.class))
        .withBeneficiary(
            KbvPatientFaker.builder().withKvnrAndInsuranceType(KVNR.random(), insurance).fake());
  }

  public static KbvCoverageFaker builder() {
    return new KbvCoverageFaker();
  }

  public KbvCoverageFaker withInsuranceType(InsuranceTypeDe insuranceT) {
    this.insurance = insuranceT;
    setInsuranceTypeDirect = true;
    return this;
  }

  public KbvCoverageFaker withVersion(KbvItaForVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public KbvCoverageFaker withPersonGroup(PersonGroup group) {
    builderConsumers.put("personGroup", b -> b.personGroup(group));
    return this;
  }

  public KbvCoverageFaker withDmpKennzeichen(DmpKennzeichen kennzeichen) {
    builderConsumers.put("dmp", b -> b.dmpKennzeichen(kennzeichen));
    return this;
  }

  public KbvCoverageFaker withWop(Wop wop) {
    builderConsumers.put("wop", b -> b.wop(wop));
    return this;
  }

  public KbvCoverageFaker withInsuranceStatus(VersichertenStatus status) {
    builderConsumers.put("versichertenStatus", b -> b.versichertenStatus(status));
    return this;
  }

  public KbvCoverageFaker withBeneficiary(KbvPatient patient) {
    if (!setInsuranceTypeDirect) {
      this.withInsuranceType(patient.getInsuranceType());
    }
    builderConsumers.put("beneficiary", b -> b.beneficiary(patient));
    return this;
  }

  public KbvCoverage fake() {
    return this.toBuilder().build();
  }

  public KbvCoverageBuilder toBuilder() {
    val builder =
        switch (insurance) {
          case GKV -> insurance(randomElement(GkvInsuranceCoverageInfo.values()));
          case PKV -> insurance(randomElement(PkvInsuranceCoverageInfo.values()));
          case BG -> insurance(randomElement(BGInsuranceCoverageInfo.values()));
          default -> insurance(DynamicInsuranceCoverageInfo.random(insurance));
        };
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
