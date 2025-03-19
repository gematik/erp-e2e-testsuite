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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerBirthday;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerCity;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerFirstName;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerLastName;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerStreetName;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerZipCode;
import static de.gematik.test.erezept.fhir.builder.GemFaker.randomElement;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.r4.InstitutionalOrganization;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;

public class KbvPatientFaker {
  private final Map<String, Consumer<KbvPatientBuilder>> builderConsumers = new HashMap<>();

  private KbvPatientFaker() {
    this.withBirthDate(fakerBirthday())
        .withAddress(fakerCity(), fakerZipCode(), fakerStreetName())
        .withName(fakerFirstName(), fakerLastName())
        .withAssignerRef(KbvAssignerOrganizationFaker.builder().fake())
        .withVersion(KbvItaForVersion.getDefaultVersion())
        .withKvnrAndInsuranceType(
            KVNR.random(), randomElement(InsuranceTypeDe.GKV, InsuranceTypeDe.PKV));
  }

  public static KbvPatientFaker builder() {
    return new KbvPatientFaker();
  }

  public KbvPatientFaker withInsuranceType(InsuranceTypeDe insuranceType) {
    builderConsumers.put("kvnr", b -> b.kvnr(KVNR.random(), insuranceType));
    return this;
  }

  public KbvPatientFaker withKvnrAndInsuranceType(KVNR kvnr, InsuranceTypeDe insuranceType) {
    builderConsumers.put("kvnr", b -> b.kvnr(kvnr, insuranceType));
    return this;
  }

  public KbvPatientFaker withBirthDate(Date birthDate) {
    builderConsumers.put("birthDate", b -> b.birthDate(birthDate));
    return this;
  }

  public KbvPatientFaker withAddress(String city, String postal, String street) {
    builderConsumers.put("address", b -> b.address(city, postal, street));
    return this;
  }

  public KbvPatientFaker withName(String givenName, String familyName) {
    builderConsumers.put("name", b -> b.name(givenName, familyName));
    return this;
  }

  public KbvPatientFaker withAssignerRef(InstitutionalOrganization assigner) {
    builderConsumers.put("assignerRef", b -> b.assigner(assigner));
    return this;
  }

  public KbvPatientFaker withVersion(KbvItaForVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public KbvPatient fake() {
    return this.toBuilder().build();
  }

  public KbvPatientBuilder toBuilder() {
    val builder = KbvPatientBuilder.builder();
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
