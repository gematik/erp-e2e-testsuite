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

package de.gematik.test.erezept.fhir.builder.dav;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;

import de.gematik.test.erezept.fhir.parser.profiles.version.AbdaErpPkvVersion;
import de.gematik.test.erezept.fhir.resources.dav.PharmacyOrganization;
import de.gematik.test.erezept.fhir.values.IKNR;
import de.gematik.test.erezept.fhir.valuesets.Country;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;

public class PharmacyOrganizationFaker {
  private final Map<String, Consumer<PharmacyOrganizationBuilder>> builderConsumers =
      new HashMap<>();

  private PharmacyOrganizationFaker() {
    builderConsumers.put("iknr", b -> b.iknr(IKNR.random()));
    builderConsumers.put("name", b -> b.name(pharmacyName()));
    builderConsumers.put(
        "address", b -> b.address(fakerCountry(), fakerCity(), fakerZipCode(), fakerStreetName()));
  }

  public static PharmacyOrganizationFaker builder() {
    return new PharmacyOrganizationFaker();
  }

  public PharmacyOrganizationFaker withVersion(AbdaErpPkvVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public PharmacyOrganizationFaker withName(String name) {
    builderConsumers.computeIfPresent("name", (key, defaultValue) -> b -> b.name(name));
    return this;
  }

  public PharmacyOrganizationFaker withIknr(IKNR iknr) {
    builderConsumers.computeIfPresent("iknr", (key, defaultValue) -> b -> b.iknr(iknr));
    return this;
  }

  public PharmacyOrganizationFaker withIknr(String iknr) {
    return this.withIknr(IKNR.from(iknr));
  }

  public PharmacyOrganizationFaker withAddress(
      Country country, String city, String postal, String street) {
    builderConsumers.computeIfPresent(
        "address", (key, defaultValue) -> b -> b.address(country, city, postal, street));
    return this;
  }

  public PharmacyOrganizationFaker withAddress(String city, String postal, String street) {
    return this.withAddress(Country.D, city, postal, street);
  }

  public PharmacyOrganization fake() {
    return this.toBuilder().build();
  }

  public PharmacyOrganizationBuilder toBuilder() {
    val builder = PharmacyOrganizationBuilder.builder();
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
