/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.fhir.builder.dav;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;

import de.gematik.test.erezept.fhir.builder.AbstractOrganizationBuilder;
import de.gematik.test.erezept.fhir.builder.AddressBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import de.gematik.test.erezept.fhir.resources.dav.PharmacyOrganization;
import de.gematik.test.erezept.fhir.values.IKNR;
import de.gematik.test.erezept.fhir.valuesets.Country;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.Address;

/**
 * This builder will build an Organization which represents an institution that is capable of
 * assigning healthcare insurance identifiers to its customers. The characteristic property of such
 * an organization is the required IKNR (Institutionskennzeichen)
 */
public class PharmacyOrganizationBuilder
    extends AbstractOrganizationBuilder<PharmacyOrganizationBuilder> {

  private IKNR iknr;

  public static PharmacyOrganizationBuilder builder() {
    return new PharmacyOrganizationBuilder();
  }

  public static PharmacyOrganizationBuilder faker() {
    return faker(pharmacyName());
  }

  public static PharmacyOrganizationBuilder faker(@NonNull String name) {
    val builder = builder();
    builder.name(name).iknr(fakerIknr()).address(fakerCity(), fakerZipCode(), fakerStreetName());
    return builder;
  }

  public PharmacyOrganizationBuilder address(
      @NonNull String city, @NonNull String postal, @NonNull String street) {
    return address(Country.D, city, postal, street);
  }

  public PharmacyOrganizationBuilder address(
      @NonNull Country country,
      @NonNull String city,
      @NonNull String postal,
      @NonNull String street) {
    return address(
        AddressBuilder.address(country, city, postal, street, Address.AddressType.PHYSICAL));
  }

  public PharmacyOrganizationBuilder iknr(@NonNull String value) {
    return iknr(IKNR.from(value));
  }

  public PharmacyOrganizationBuilder iknr(@NonNull IKNR value) {
    this.iknr = value;
    return self();
  }

  public PharmacyOrganization build() {
    checkRequired();
    return PharmacyOrganization.fromOrganization(
        buildOrganizationWith(ErpStructureDefinition.DAV_PKV_PR_ERP_APOTHEKE, iknr.asIdentifier()));
  }

  protected void checkRequired() {
    this.checkRequired(iknr, "Pharmacy Organization requires an IKNR");
    this.checkRequired(name, "Pharmacy Organization requires a name");
  }
}
