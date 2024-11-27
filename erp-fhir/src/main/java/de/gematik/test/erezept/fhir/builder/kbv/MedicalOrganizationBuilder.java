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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.builder.AbstractOrganizationBuilder;
import de.gematik.test.erezept.fhir.builder.AddressBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.Hl7CodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.resources.kbv.MedicalOrganization;
import de.gematik.test.erezept.fhir.values.BSNR;
import de.gematik.test.erezept.fhir.values.IKNR;
import de.gematik.test.erezept.fhir.values.KSN;
import de.gematik.test.erezept.fhir.values.KZVA;
import de.gematik.test.erezept.fhir.values.Value;
import de.gematik.test.erezept.fhir.valuesets.Country;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;

/**
 * This builder will build an Organization which represents a Medical Practice. The characteristic
 * property of such an organization is the required BSNR (Betriebsstättennummer)
 */
public class MedicalOrganizationBuilder
    extends AbstractOrganizationBuilder<MedicalOrganizationBuilder> {

  private KbvItaForVersion kbvItaForVersion = KbvItaForVersion.getDefaultVersion();
  private BSNR bsnr;
  private KZVA kzva;
  private IKNR iknr;
  private KSN ksn;

  public static MedicalOrganizationBuilder builder() {
    return new MedicalOrganizationBuilder();
  }

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public MedicalOrganizationBuilder version(KbvItaForVersion version) {
    this.kbvItaForVersion = version;
    return this;
  }

  public MedicalOrganizationBuilder bsnr(String bsnr) {
    return bsnr(BSNR.from(bsnr));
  }

  public MedicalOrganizationBuilder bsnr(BSNR bsnr) {
    this.bsnr = bsnr;
    return self();
  }

  public MedicalOrganizationBuilder kzva(String kzva) {
    return kzva(KZVA.from(kzva));
  }

  public MedicalOrganizationBuilder kzva(KZVA kzva) {
    this.kzva = kzva;
    return this;
  }

  public MedicalOrganizationBuilder iknr(String iknr) {
    return iknr(IKNR.from(iknr));
  }

  public MedicalOrganizationBuilder iknr(IKNR iknr) {
    this.iknr = iknr;
    return this;
  }

  public MedicalOrganizationBuilder ksn(String iknr) {
    return ksn(KSN.from(iknr));
  }

  public MedicalOrganizationBuilder ksn(KSN iknr) {
    this.ksn = iknr;
    return this;
  }

  public MedicalOrganizationBuilder address(String city, String postal, String street) {
    return address(Country.D, city, postal, street);
  }

  public MedicalOrganizationBuilder address(
      Country country, String city, String postal, String street) {
    return address(AddressBuilder.address(country, city, postal, street, Address.AddressType.BOTH));
  }

  public MedicalOrganization build() {
    checkRequired();
    val identifier =
        identifierStream()
            .filter(Objects::nonNull)
            .map(this::mapTo)
            .findFirst()
            .orElseThrow(
                () ->
                    new BuilderException(
                        "Medical practice organization builder requires an identifier"));
    return MedicalOrganization.fromOrganization(
        buildOrganizationWith(
            () -> KbvItaForStructDef.ORGANIZATION.asCanonicalType(kbvItaForVersion), identifier));
  }

  protected void checkRequired() {
    if (identifierStream().count() != 1) {
      throw new BuilderException(
          "Medical Practice Organization requires exactly one Identifier (BSNR/KZVA/IKNR)");
    }
    this.checkRequired(name, "Medical Practice Organization requires a name");
    this.checkRequired(contactPoints, "Medical Practice Organization requires a contact");
  }

  private Stream<Value<String>> identifierStream() {
    return Stream.of(bsnr, kzva, iknr, ksn).filter(Objects::nonNull);
  }

  private Identifier mapTo(Value<String> identifierWrapper) {
    if (identifierWrapper instanceof BSNR mBsnr) {
      return mBsnr.asIdentifier();
    } else if (identifierWrapper instanceof KZVA mKzva) {
      if (kbvItaForVersion.equals(KbvItaForVersion.V1_0_3)) {
        return mKzva.asIdentifier(DeBasisNamingSystem.KZVA_ABRECHNUNGSNUMMER);
      } else {
        return mKzva.asIdentifier();
      }
    } else if (identifierWrapper instanceof IKNR mIknr) {
      Identifier iknrIdentifier;
      if (kbvItaForVersion.equals(KbvItaForVersion.V1_0_3)) {
        iknrIdentifier = mIknr.asIdentifier();
      } else {
        iknrIdentifier = mIknr.asIdentifier(DeBasisNamingSystem.IKNR_SID);
      }

      iknrIdentifier
          .getType()
          .addCoding(
              new Coding().setCode("XX").setSystem(Hl7CodeSystem.HL7_V2_0203.getCanonicalUrl()));
      return iknrIdentifier;
    } else if (identifierWrapper instanceof KSN mKsn) {
      return mKsn.asIdentifier();
    } else {
      throw new BuilderException(
          format(
              "Identifier for medical organization of type {0} is not supported yet",
              identifierWrapper.getClass()));
    }
  }
}
