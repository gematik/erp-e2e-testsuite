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

package de.gematik.test.erezept.fhir.builder.dav;

import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.builder.AddressBuilder;
import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.bbriccs.fhir.de.valueset.Country;
import de.gematik.test.erezept.fhir.builder.AbstractOrganizationBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.AbdaErpPkvStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.AbdaErpPkvVersion;
import de.gematik.test.erezept.fhir.r4.dav.PharmacyOrganization;

/**
 * This builder will build an Organization which represents an institution that is capable of
 * assigning healthcare insurance identifiers to its customers. The characteristic property of such
 * an organization is the required IKNR (Institutionskennzeichen)
 */
public class PharmacyOrganizationBuilder
    extends AbstractOrganizationBuilder<PharmacyOrganization, PharmacyOrganizationBuilder> {

  private AbdaErpPkvVersion version = AbdaErpPkvVersion.getDefaultVersion();
  private IKNR iknr;

  public static PharmacyOrganizationBuilder builder() {
    return new PharmacyOrganizationBuilder();
  }

  public PharmacyOrganizationBuilder version(AbdaErpPkvVersion version) {
    this.version = version;
    return self();
  }

  public PharmacyOrganizationBuilder address(String city, String postal, String street) {
    return address(Country.D, city, postal, street);
  }

  public PharmacyOrganizationBuilder address(
      Country country, String city, String postal, String street) {
    return address(
        AddressBuilder.ofPhysicalType()
            .country(country)
            .city(city)
            .postal(postal)
            .street(street)
            .build());
  }

  public PharmacyOrganizationBuilder iknr(String value) {
    return iknr(IKNR.asSidIknr(value));
  }

  public PharmacyOrganizationBuilder iknr(IKNR value) {
    this.iknr = value;
    return self();
  }

  @Override
  public PharmacyOrganization build() {
    checkRequired();
    return PharmacyOrganization.fromOrganization(
        buildOrganizationWith(
            () -> AbdaErpPkvStructDef.APOTHEKE.asCanonicalType(version),
            iknr.asIdentifier(DeBasisProfilNamingSystem.IKNR_SID)));
  }

  protected void checkRequired() {
    this.checkRequired(iknr, "Pharmacy Organization requires an IKNR");
    this.checkRequired(name, "Pharmacy Organization requires a name");
  }
}
