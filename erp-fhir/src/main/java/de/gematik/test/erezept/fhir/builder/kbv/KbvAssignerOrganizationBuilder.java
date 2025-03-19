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

import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.HL7CodeSystem;
import de.gematik.bbriccs.fhir.de.builder.AddressBuilder;
import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.bbriccs.fhir.de.valueset.Country;
import de.gematik.test.erezept.fhir.builder.AbstractOrganizationBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.r4.kbv.AssignerOrganization;
import lombok.val;

/**
 * This builder will build an Organization which represents an institution that is capable of
 * assigning healthcare insurance identifiers to its customers. The characteristic property of such
 * an organization is the required IKNR (Institutionskennzeichen)
 */
public class KbvAssignerOrganizationBuilder
    extends AbstractOrganizationBuilder<AssignerOrganization, KbvAssignerOrganizationBuilder> {

  private KbvItaForVersion kbvItaForVersion = KbvItaForVersion.getDefaultVersion();
  private IKNR iknr;

  public static KbvAssignerOrganizationBuilder builder() {
    return new KbvAssignerOrganizationBuilder();
  }

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public KbvAssignerOrganizationBuilder version(KbvItaForVersion version) {
    this.kbvItaForVersion = version;
    return this;
  }

  public KbvAssignerOrganizationBuilder address(String city, String postal, String street) {
    return address(Country.D, city, postal, street);
  }

  public KbvAssignerOrganizationBuilder address(
      Country country, String city, String postal, String street) {
    return address(
        AddressBuilder.ofBothTypes()
            .country(country)
            .city(city)
            .postal(postal)
            .street(street)
            .build());
  }

  public KbvAssignerOrganizationBuilder iknr(String value) {
    return iknr(IKNR.asArgeIknr(value));
  }

  public KbvAssignerOrganizationBuilder iknr(IKNR value) {
    this.iknr = value;
    return self();
  }

  @Override
  public AssignerOrganization build() {
    checkRequired();
    var iknrNamingSystem = DeBasisProfilNamingSystem.IKNR;

    if (kbvItaForVersion.compareTo(KbvItaForVersion.V1_1_0) >= 0) {
      iknrNamingSystem = DeBasisProfilNamingSystem.IKNR_SID;
    }

    val assignerOrganization =
        AssignerOrganization.fromOrganization(
            buildOrganizationWith(
                () -> KbvItaForStructDef.ORGANIZATION.asCanonicalType(kbvItaForVersion),
                iknr.asIdentifier(iknrNamingSystem)));

    assignerOrganization
        .getIdentifierFirstRep()
        .getType()
        .addCoding(HL7CodeSystem.HL7_V2_0203.asCoding("XX"));

    return assignerOrganization;
  }

  protected void checkRequired() {
    this.checkRequired(iknr, "Assigner Organization requires an IKNR");
    this.checkRequired(name, "Assigner Organization requires a name");
    this.checkRequired(contactPoints, "Assigner Organization requires a contact");
  }
}
