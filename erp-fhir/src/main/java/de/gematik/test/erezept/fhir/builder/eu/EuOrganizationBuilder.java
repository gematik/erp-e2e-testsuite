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

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.erezept.fhir.profiles.definitions.GemErpEuStructDef;
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import de.gematik.test.erezept.fhir.r4.eu.EuHealthcareFacilityType;
import de.gematik.test.erezept.fhir.r4.eu.EuOrganization;
import de.gematik.test.erezept.fhir.r4.eu.NcpehCountryExt;
import de.gematik.test.erezept.fhir.values.BSNR;
import de.gematik.test.erezept.fhir.values.KZVA;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;

public class EuOrganizationBuilder extends ResourceBuilder<EuOrganization, EuOrganizationBuilder> {

  private EuVersion version = EuVersion.getDefaultVersion();
  private String name;
  private NcpehCountryExt ncpehCountry;
  private Identifier unknownPharmacyNo;
  private TelematikID telematikId;
  private BSNR bsnr;
  private KZVA kzva;
  private IKNR iknr;
  private EuHealthcareFacilityType providerType;
  private String profession;
  private Address address;

  public static EuOrganizationBuilder builder(String name) {
    val builder = new EuOrganizationBuilder();
    builder.name = name;
    return builder;
  }

  public EuOrganizationBuilder version(EuVersion version) {
    this.version = version;
    return this;
  }

  public EuOrganizationBuilder ncpehCountry(IsoCountryCode ncpehCountry) {
    this.ncpehCountry = new NcpehCountryExt(ncpehCountry);
    return this;
  }

  public EuOrganizationBuilder identifier(Identifier unknownIdentifier) {
    this.unknownPharmacyNo = unknownIdentifier;
    return this;
  }

  public EuOrganizationBuilder identifier(TelematikID telematikId) {
    this.telematikId = telematikId;
    return this;
  }

  public EuOrganizationBuilder identifier(BSNR bsnr) {
    this.bsnr = bsnr;
    return this;
  }

  public EuOrganizationBuilder identifier(KZVA kzva) {
    this.kzva = kzva;
    return this;
  }

  public EuOrganizationBuilder identifier(IKNR iknr) {
    this.iknr = iknr;
    return this;
  }

  public EuOrganizationBuilder providerType(EuHealthcareFacilityType providerType) {
    this.providerType = providerType;
    return this;
  }

  public EuOrganizationBuilder profession(String profession) {
    this.profession = profession;
    return this;
  }

  public EuOrganizationBuilder address(Address address) {
    this.address = address;
    return this;
  }

  @Override
  public EuOrganization build() {
    checkRequired();
    val euOrganization =
        createResource(EuOrganization::new, GemErpEuStructDef.EU_ORGANIZATION, version);
    Optional.ofNullable(ncpehCountry)
        .ifPresent(countryExt -> euOrganization.addExtension(countryExt.asExtension()));
    Optional.ofNullable(telematikId)
        .ifPresent(tId -> euOrganization.getIdentifier().add(tId.asIdentifier()));
    Optional.ofNullable(bsnr)
        .ifPresent(bs -> euOrganization.getIdentifier().add(bs.asIdentifier()));
    Optional.ofNullable(kzva)
        .ifPresent(kv -> euOrganization.getIdentifier().add(kv.asIdentifier()));
    Optional.ofNullable(iknr)
        .ifPresent(ik -> euOrganization.getIdentifier().add(ik.asIdentifier()));
    Optional.ofNullable(unknownPharmacyNo)
        .ifPresent(unknown -> euOrganization.getIdentifier().add(unknown));
    Optional.ofNullable(providerType)
        .ifPresent(pT -> euOrganization.getType().add(new CodeableConcept(pT.asCoding())));
    Optional.ofNullable(profession)
        .ifPresent(
            pr -> euOrganization.getType().add(new CodeableConcept(new Coding().setCode(pr))));
    Optional.ofNullable(name).ifPresent(euOrganization::setName);
    Optional.ofNullable(address)
        .ifPresent(
            ad -> {
              List<Address> addressList = List.of(ad);
              euOrganization.setAddress(addressList);
            });

    return euOrganization;
  }

  private void checkRequired() {
    val identifierList = new ArrayList<>();
    identifierList.add(unknownPharmacyNo);
    identifierList.add(telematikId);
    identifierList.add(bsnr);
    identifierList.add(kzva);
    identifierList.add(iknr);

    this.checkRequiredList(
        identifierList,
        1,
        "EuOrganization requires a minimum of one Identifier out of: unknownPharmacyNo,"
            + " telematikId, bsnr, kzva, iknr as Identifier");
    this.checkRequired(name, "EuOrganization requires a name");
    this.checkRequired(address, "EuOrganization requires a address");
  }
}
