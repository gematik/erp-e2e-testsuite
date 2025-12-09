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

package de.gematik.test.erezept.fhir.builder.eu;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import de.gematik.test.erezept.fhir.r4.eu.EuHealthcareFacilityType;
import de.gematik.test.erezept.fhir.r4.eu.EuOrganization;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Identifier;

public class EuOrganizationFaker {

  private final Map<String, Consumer<EuOrganizationBuilder>> builderConsumers = new HashMap<>();

  private String name;

  private EuOrganizationFaker() {
    this.withAaddress(fakeAddress())
        .withIdentifier(
            new Identifier()
                .setSystem("http://unknown_eu-wide.system")
                .setValue(GemFaker.getFaker().company().name()))
        .withProviderType(EuHealthcareFacilityType.getDefault());
  }

  private Address fakeAddress() {
    return new Address()
        .addLine(GemFaker.fakerStreetName())
        .setCity(GemFaker.fakerCity())
        .setState(GemFaker.getFaker().address().state())
        .setPostalCode(GemFaker.fakerBsnr())
        .setCountry(GemFaker.fakerCountry().name());
  }

  public static EuOrganizationFaker faker() {
    return new EuOrganizationFaker();
  }

  public EuOrganizationFaker withName(String name) {
    this.name = name;
    return this;
  }

  public EuOrganizationFaker withVersion(EuVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public EuOrganizationFaker withNcpehCountry(IsoCountryCode ncpehCountry) {
    builderConsumers.put("ncpehCountry", b -> b.ncpehCountry(ncpehCountry));
    return this;
  }

  public EuOrganizationFaker withIdentifier(Identifier unknownIdentifier) {
    builderConsumers.put("unknownIdentifier", b -> b.identifier(unknownIdentifier));
    return this;
  }

  public EuOrganizationFaker withProviderType(EuHealthcareFacilityType providerType) {
    builderConsumers.put("providerType", b -> b.providerType(providerType));
    return this;
  }

  public EuOrganizationFaker withProfession(String profession) {
    builderConsumers.put("profession", b -> b.profession(profession));
    return this;
  }

  public EuOrganizationFaker withAaddress(Address address) {
    builderConsumers.put("address", b -> b.address(address));
    return this;
  }

  public EuOrganization fake() {
    return this.toBuilder().build();
  }

  public EuOrganizationBuilder toBuilder() {
    if (this.name == null) this.name = GemFaker.getFaker().name().name();
    val builder = EuOrganizationBuilder.builder(name);
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
