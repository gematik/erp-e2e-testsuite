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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;

import de.gematik.test.erezept.fhir.builder.AbstractOrganizationBuilder;
import de.gematik.test.erezept.fhir.builder.AddressBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import de.gematik.test.erezept.fhir.resources.kbv.MedicalOrganization;
import de.gematik.test.erezept.fhir.values.BSNR;
import de.gematik.test.erezept.fhir.valuesets.Country;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.Address;

/**
 * This builder will build an Organization which represents a Medical Practice. The characteristic
 * property of such an organization is the required BSNR (Betriebsstättennummer)
 */
public class MedicalOrganizationBuilder
    extends AbstractOrganizationBuilder<MedicalOrganizationBuilder> {

  private BSNR bsnr;

  public static MedicalOrganizationBuilder builder() {
    return new MedicalOrganizationBuilder();
  }

  public static MedicalOrganizationBuilder faker() {
    val builder = builder();
    builder
        .name(fakerName())
        .bsnr(fakerBsnr())
        .phone(fakerPhone())
        .email(fakerEMail())
        .address(fakerCountry(), fakerCity(), fakerZipCode(), fakerStreetName());
    return builder;
  }

  public MedicalOrganizationBuilder bsnr(@NonNull String bsnr) {
    return bsnr(BSNR.from(bsnr));
  }

  public MedicalOrganizationBuilder bsnr(@NonNull BSNR bsnr) {
    this.bsnr = bsnr;
    return self();
  }

  public MedicalOrganizationBuilder address(
      @NonNull String city, @NonNull String postal, @NonNull String street) {
    return address(Country.D, city, postal, street);
  }

  public MedicalOrganizationBuilder address(
      @NonNull Country country,
      @NonNull String city,
      @NonNull String postal,
      @NonNull String street) {
    return address(AddressBuilder.address(country, city, postal, street, Address.AddressType.BOTH));
  }

  public MedicalOrganization build() {
    checkRequired();
    return MedicalOrganization.fromOrganization(
        buildOrganizationWith(ErpStructureDefinition.KBV_ORGANIZATION, bsnr.asIdentifier()));
  }

  protected void checkRequired() {
    this.checkRequired(bsnr, "Medical Practice Organization requires a BSNR");
    this.checkRequired(name, "Medical Practice Organization requires a name");
    this.checkRequired(contactPoints, "Medical Practice Organization requires a contact");
  }
}
