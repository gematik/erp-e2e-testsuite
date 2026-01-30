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

package de.gematik.test.erezept.fhir.builder.kbv;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.coding.SemanticValue;
import de.gematik.bbriccs.fhir.coding.WithSystem;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.HL7CodeSystem;
import de.gematik.bbriccs.fhir.de.builder.AddressBuilder;
import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.bbriccs.fhir.de.valueset.Country;
import de.gematik.test.erezept.fhir.builder.AbstractOrganizationBuilder;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvMedicalOrganization;
import de.gematik.test.erezept.fhir.values.BSNR;
import de.gematik.test.erezept.fhir.values.KSN;
import de.gematik.test.erezept.fhir.values.KZVA;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.Identifier;

/**
 * This builder will build an Organization which represents a Medical Practice. The characteristic
 * property of such an organization is the required BSNR (Betriebsstättennummer)
 */
public class KbvMedicalOrganizationBuilder
    extends AbstractOrganizationBuilder<KbvMedicalOrganization, KbvMedicalOrganizationBuilder> {

  private KbvItaForVersion kbvItaForVersion = KbvItaForVersion.getDefaultVersion();
  private BSNR bsnr;
  private KZVA kzva;
  private IKNR iknr;
  private KSN ksn;

  public static KbvMedicalOrganizationBuilder builder() {
    return new KbvMedicalOrganizationBuilder();
  }

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public KbvMedicalOrganizationBuilder version(KbvItaForVersion version) {
    this.kbvItaForVersion = version;
    return this;
  }

  public KbvMedicalOrganizationBuilder bsnr(String bsnr) {
    return bsnr(BSNR.from(bsnr));
  }

  public KbvMedicalOrganizationBuilder bsnr(BSNR bsnr) {
    this.bsnr = bsnr;
    return self();
  }

  public KbvMedicalOrganizationBuilder kzva(String kzva) {
    return kzva(KZVA.from(kzva));
  }

  public KbvMedicalOrganizationBuilder kzva(KZVA kzva) {
    this.kzva = kzva;
    return this;
  }

  public KbvMedicalOrganizationBuilder iknr(String iknr) {
    return iknr(IKNR.asSidIknr(iknr));
  }

  public KbvMedicalOrganizationBuilder iknr(IKNR iknr) {
    this.iknr = iknr;
    return this;
  }

  public KbvMedicalOrganizationBuilder ksn(String iknr) {
    return ksn(KSN.from(iknr));
  }

  public KbvMedicalOrganizationBuilder ksn(KSN iknr) {
    this.ksn = iknr;
    return this;
  }

  public KbvMedicalOrganizationBuilder address(String city, String postal, String street) {
    return address(Country.D, city, postal, street);
  }

  public KbvMedicalOrganizationBuilder address(
      Country country, String city, String postal, String street) {
    return address(
        AddressBuilder.ofBothTypes()
            .country(country)
            .city(city)
            .postal(postal)
            .street(street)
            .build());
  }

  @Override
  public KbvMedicalOrganization build() {
    checkRequired();
    val identifier = firstIdentifier();
    val oranization =
        KbvMedicalOrganization.fromOrganization(
            buildOrganizationWith(
                () -> KbvItaForStructDef.ORGANIZATION.asCanonicalType(kbvItaForVersion),
                identifier));

    if (kbvItaForVersion.isBiggerThan(KbvItaForVersion.V1_2_0)) {
      oranization.getMeta().setVersionId("1");
    }

    return oranization;
  }

  protected void checkRequired() {
    if (countIdentifier() != 1) {
      throw new BuilderException(
          "Medical Practice Organization requires exactly one Identifier (BSNR/KZVA/IKNR)");
    }
    this.checkRequired(name, "Medical Practice Organization requires a name");
    this.checkRequired(contactPoints, "Medical Practice Organization requires a contact");
  }

  public long countIdentifier() {
    return Stream.of(bsnr, kzva, iknr, ksn).filter(Objects::nonNull).count();
  }

  private Identifier firstIdentifier() {
    return Stream.of(bsnr, kzva, iknr, ksn)
        .filter(Objects::nonNull)
        .map(this::mapTo)
        .findFirst()
        .orElseThrow(
            () ->
                new BuilderException(
                    "Medical practice organization builder requires an identifier"));
  }

  private <S extends WithSystem> Identifier mapTo(SemanticValue<String, S> identifierWrapper) {
    if (identifierWrapper instanceof BSNR mBsnr) {
      return mBsnr.asIdentifier();
    } else if (identifierWrapper instanceof KZVA mKzva) {
      return mKzva.asIdentifier();
    } else if (identifierWrapper instanceof IKNR mIknr) {
      val iknrIdentifier = mIknr.asIdentifier(DeBasisProfilNamingSystem.IKNR_SID);
      iknrIdentifier.getType().addCoding(HL7CodeSystem.HL7_V2_0203.asCoding("XX"));
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
