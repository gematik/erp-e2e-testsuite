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

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;

import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.resources.InstitutionalOrganization;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;

public class PatientFaker {
  private final Map<String, Consumer<PatientBuilder>> builderConsumers = new HashMap<>();

  private PatientFaker() {
    builderConsumers.put("birthDate", b -> b.birthDate(fakerBirthday()));
    builderConsumers.put("address", b -> b.address(fakerCity(), fakerZipCode(), fakerStreetName()));
    builderConsumers.put("name", b -> b.name(fakerFirstName(), fakerLastName()));
    builderConsumers.put(
        "assignerRef", b -> b.assigner(AssignerOrganizationFaker.builder().fake()));
    builderConsumers.put("version", b -> b.version(KbvItaForVersion.getDefaultVersion()));
    builderConsumers.put(
        "kvnr",
        b ->
            b.kvnr(
                KVNR.random(),
                randomElement(VersicherungsArtDeBasis.GKV, VersicherungsArtDeBasis.PKV)));
  }

  public static PatientFaker builder() {
    return new PatientFaker();
  }

  public PatientFaker withKvnrAndInsuranceType(KVNR kvnr, VersicherungsArtDeBasis insuranceType) {
    builderConsumers.computeIfPresent(
        "kvnr", (key, defaultValue) -> b -> b.kvnr(kvnr, insuranceType));
    return this;
  }

  public PatientFaker withBirthDate(String birthDate) {
    builderConsumers.computeIfPresent(
        "birthDate", (key, defaultValue) -> b -> b.birthDate(birthDate));
    return this;
  }

  public PatientFaker withAddress(String city, String postal, String street) {
    builderConsumers.computeIfPresent(
        "address", (key, defaultValue) -> b -> b.address(city, postal, street));
    return this;
  }

  public PatientFaker withName(String givenName, String familyName) {
    builderConsumers.computeIfPresent(
        "name", (key, defaultValue) -> b -> b.name(givenName, familyName));
    return this;
  }

  public PatientFaker withAssignerRef(InstitutionalOrganization assigner) {
    builderConsumers.computeIfPresent(
        "assignerRef", (key, defaultValue) -> b -> b.assigner(assigner));
    return this;
  }

  public PatientFaker withVersion(KbvItaForVersion version) {
    builderConsumers.computeIfPresent("version", (key, defaultValue) -> b -> b.version(version));
    return this;
  }

  public KbvPatient fake() {
    return this.toBuilder().build();
  }

  public PatientBuilder toBuilder() {
    val builder = PatientBuilder.builder();
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
