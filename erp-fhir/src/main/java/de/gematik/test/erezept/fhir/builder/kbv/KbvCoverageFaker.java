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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerValueSet;
import static de.gematik.test.erezept.fhir.builder.GemFaker.randomElement;
import static de.gematik.test.erezept.fhir.builder.kbv.KbvCoverageBuilder.insurance;

import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.references.kbv.SubjectReference;
import de.gematik.test.erezept.fhir.resources.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;

public class KbvCoverageFaker {
  private VersicherungsArtDeBasis insurance =
      randomElement(VersicherungsArtDeBasis.GKV, VersicherungsArtDeBasis.PKV);
  private final Map<String, Consumer<KbvCoverageBuilder>> builderConsumers = new HashMap<>();
  private static final String DMP = "dmpKennzeichen";

  private KbvCoverageFaker() {
    builderConsumers.put("personGroup", b -> b.personGroup(fakerValueSet(PersonGroup.class)));
    builderConsumers.put(DMP, b -> b.dmpKennzeichen(fakerValueSet(DmpKennzeichen.class)));
    builderConsumers.put("wop", b -> b.wop(fakerValueSet(Wop.class)));
    builderConsumers.put(
        "versichertenStatus", b -> b.versichertenStatus(fakerValueSet(VersichertenStatus.class)));
    builderConsumers.put(
        "beneficiary",
        b ->
            b.beneficiary(
                PatientFaker.builder().withKvnrAndInsuranceType(KVNR.random(), insurance).fake()));
  }

  public static KbvCoverageFaker builder() {
    return new KbvCoverageFaker();
  }

  public KbvCoverageFaker withInsuranceType(VersicherungsArtDeBasis insuranceType) {
    this.insurance = insuranceType;
    return this;
  }

  public KbvCoverageFaker withVersion(KbvItaForVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public KbvCoverageFaker withPersonGroup(PersonGroup group) {
    builderConsumers.computeIfPresent(
        "personGroup", (key, defaultValue) -> b -> b.personGroup(group));
    return this;
  }

  public KbvCoverageFaker withDmpKennzeichen(DmpKennzeichen kennzeichen) {
    builderConsumers.computeIfPresent(
        DMP, (key, defaultValue) -> b -> b.dmpKennzeichen(kennzeichen));
    return this;
  }

  public KbvCoverageFaker withWop(Wop wop) {
    builderConsumers.computeIfPresent("wop", (key, defaultValue) -> b -> b.wop(wop));
    return this;
  }

  public KbvCoverageFaker withInsuranceStatus(VersichertenStatus status) {
    builderConsumers.computeIfPresent(
        "versichertenStatus", (key, defaultValue) -> b -> b.versichertenStatus(status));
    return this;
  }

  public KbvCoverageFaker withBeneficiary(KbvPatient patient) {
    this.withInsuranceType(patient.getInsuranceKind());
    return this.withBeneficiary(new SubjectReference(patient.getId()));
  }

  public KbvCoverageFaker withBeneficiary(SubjectReference reference) {
    builderConsumers.computeIfPresent(
        "beneficiary", (key, defaultValue) -> b -> b.beneficiary(reference));
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
