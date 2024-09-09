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
import de.gematik.test.erezept.fhir.resources.kbv.MedicalOrganization;
import de.gematik.test.erezept.fhir.values.BSNR;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;

public class MedicalOrganizationFaker {
  private final Map<String, Consumer<MedicalOrganizationBuilder>> builderConsumers =
      new HashMap<>();

  private MedicalOrganizationFaker() {
    builderConsumers.put("phone", b -> b.phone(fakerPhone()));
    builderConsumers.put("address", b -> b.address(fakerCity(), fakerZipCode(), fakerStreetName()));
    builderConsumers.put("name", b -> b.name(fakerName()));
    builderConsumers.put("email", b -> b.email(fakerEMail()));
    builderConsumers.put("bsnr", b -> b.bsnr(fakerBsnr()));
  }

  public static MedicalOrganizationFaker builder() {
    return new MedicalOrganizationFaker();
  }

  public MedicalOrganizationFaker withVersion(KbvItaForVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public MedicalOrganizationFaker withName(String name) {
    builderConsumers.computeIfPresent("name", (key, defaultValue) -> b -> b.name(name));
    return this;
  }

  public MedicalOrganizationFaker withPhoneNumber(String number) {
    builderConsumers.computeIfPresent("phone", (key, defaultValue) -> b -> b.phone(number));
    return this;
  }

  public MedicalOrganizationFaker withEmail(String email) {
    builderConsumers.computeIfPresent("email", (key, defaultValue) -> b -> b.email(email));
    return this;
  }

  public MedicalOrganizationFaker withBsnr(BSNR bsnr) {
    return withBsnr(bsnr.getValue());
  }

  public MedicalOrganizationFaker withBsnr(String bsnr) {
    builderConsumers.computeIfPresent("bsnr", (key, defaultValue) -> b -> b.bsnr(bsnr));
    return this;
  }

  public MedicalOrganizationFaker withAddress(String city, String postal, String street) {
    builderConsumers.computeIfPresent(
        "address", (key, defaultValue) -> b -> b.address(city, postal, street));
    return this;
  }

  public MedicalOrganization fake() {
    return this.toBuilder().build();
  }

  public MedicalOrganizationBuilder toBuilder() {
    val builder = MedicalOrganizationBuilder.builder();
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
