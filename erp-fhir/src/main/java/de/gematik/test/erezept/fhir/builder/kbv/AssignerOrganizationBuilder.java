/*
 * Copyright (c) 2023 gematik GmbH
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
import static java.text.MessageFormat.*;

import de.gematik.test.erezept.fhir.builder.*;
import de.gematik.test.erezept.fhir.exceptions.*;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.*;
import de.gematik.test.erezept.fhir.parser.profiles.systems.*;
import de.gematik.test.erezept.fhir.parser.profiles.version.*;
import de.gematik.test.erezept.fhir.references.kbv.*;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;

/**
 * This builder will build an Organization which represents an institution that is capable of
 * assigning healthcare insurance identifiers to its customers. The characteristic property of such
 * an organization is the required IKNR (Institutionskennzeichen)
 */
public class AssignerOrganizationBuilder
    extends AbstractOrganizationBuilder<AssignerOrganizationBuilder> {

  private KbvItaForVersion kbvItaForVersion = KbvItaForVersion.getDefaultVersion();
  private IKNR iknr;

  public static AssignerOrganizationBuilder builder() {
    return new AssignerOrganizationBuilder();
  }

  public static AssignerOrganizationBuilder faker() {
    return faker(new OrganizationReference(UUID.randomUUID().toString()), GemFaker.insuranceName());
  }

  public static AssignerOrganizationBuilder faker(@NonNull KbvPatient pkvPatient) {
    if (!pkvPatient.hasPkvId()) {
      throw new BuilderException(
          format(
              "Cannot build AssignerOrganization from Patient with {0} Insurance",
              pkvPatient.getInsuranceKind()));
    }
    val assignerRef =
        pkvPatient
            .getPkvAssigner()
            .orElseThrow(
                () ->
                    new BuilderException(
                        format(
                            "{0} Patient does not have an Assigner",
                            pkvPatient.getInsuranceKind())));

    val assignerName =
        pkvPatient
            .getPkvAssignerName()
            .orElseThrow(
                () ->
                    new BuilderException(
                        format(
                            "{0} Patient does not have an Assigner Name",
                            pkvPatient.getInsuranceKind())));

    return faker(assignerRef, assignerName);
  }

  public static AssignerOrganizationBuilder faker(@NonNull Reference reference, String name) {
    val builder = builder();

    // split the Reference which looks like Organization/<UUID>
    val refTokens = reference.getReference().split("/");
    // get the second token if available, otherwise the first one if reference was only <UUID>
    val resourceId = refTokens.length > 1 ? refTokens[1] : refTokens[0];
    builder.setResourceId(resourceId);
    builder
        .name(name)
        .iknr(fakerIknr())
        .phone(fakerPhone())
        .address(Country.D, fakerCity(), fakerZipCode(), fakerStreetName());
    return builder;
  }

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public AssignerOrganizationBuilder version(KbvItaForVersion version) {
    this.kbvItaForVersion = version;
    return this;
  }

  public AssignerOrganizationBuilder address(
      @NonNull String city, @NonNull String postal, @NonNull String street) {
    return address(Country.D, city, postal, street);
  }

  public AssignerOrganizationBuilder address(
      @NonNull Country country,
      @NonNull String city,
      @NonNull String postal,
      @NonNull String street) {
    return address(AddressBuilder.address(country, city, postal, street, Address.AddressType.BOTH));
  }

  public AssignerOrganizationBuilder iknr(@NonNull String value) {
    return iknr(IKNR.from(value));
  }

  public AssignerOrganizationBuilder iknr(@NonNull IKNR value) {
    this.iknr = value;
    return self();
  }

  public AssignerOrganization build() {
    checkRequired();
    var iknrNamingSystem = DeBasisNamingSystem.IKNR;

    if (kbvItaForVersion.compareTo(KbvItaForVersion.V1_1_0) >= 0) {
      iknrNamingSystem = DeBasisNamingSystem.IKNR_SID;
    }

    val assignerOrganization =
        AssignerOrganization.fromOrganization(
            buildOrganizationWith(
                () -> KbvItaForStructDef.ORGANIZATION.asCanonicalType(kbvItaForVersion),
                iknr.asIdentifier(iknrNamingSystem)));

    assignerOrganization
        .getIdentifierFirstRep()
        .getType()
        .addCoding(
            new Coding()
                .setCode("XX")
                .setSystem(
                    Hl7CodeSystem.HL7_V2_0203
                        .getCanonicalUrl())); // TODO: what about moving this to IKNR?

    return assignerOrganization;
  }

  protected void checkRequired() {
    this.checkRequired(iknr, "Assigner Organization requires an IKNR");
    this.checkRequired(name, "Assigner Organization requires a name");
    this.checkRequired(contactPoints, "Assigner Organization requires a contact");
  }
}
