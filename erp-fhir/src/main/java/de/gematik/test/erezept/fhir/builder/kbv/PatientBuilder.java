/*
 * Copyright 2023 gematik GmbH
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

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;
import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.builder.AddressBuilder;
import de.gematik.test.erezept.fhir.builder.HumanNameBuilder;
import de.gematik.test.erezept.fhir.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.INamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.CommonNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.references.kbv.OrganizationReference;
import de.gematik.test.erezept.fhir.resources.InstitutionalOrganization;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.valuesets.Country;
import de.gematik.test.erezept.fhir.valuesets.IdentifierTypeDe;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import lombok.*;
import org.hl7.fhir.r4.model.*;

@Getter(AccessLevel.PACKAGE)
public class PatientBuilder extends AbstractResourceBuilder<PatientBuilder> {

  private final List<Identifier> identifiers = new LinkedList<>();
  private KbvItaForVersion kbvItaForVersion = KbvItaForVersion.getDefaultVersion();
  private String givenName;
  private String familyName;
  private String namePrefix;
  private Date birthDate;
  private Address address;

  private KVNR kvnr;
  private IdentifierTypeDe identifierTypeDe;
  private VersicherungsArtDeBasis insuranceType;
  private Reference assignerRef;

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public PatientBuilder version(KbvItaForVersion version) {
    this.kbvItaForVersion = version;
    return this;
  }

  public PatientBuilder kvnr(@NonNull KVNR kvnr, @NonNull VersicherungsArtDeBasis insuranceType) {
    this.kvnr = kvnr;
    this.identifierTypeDe =
        insuranceType.equals(VersicherungsArtDeBasis.GKV)
            ? IdentifierTypeDe.GKV
            : IdentifierTypeDe.PKV;
    this.insuranceType = insuranceType;
    return self();
  }

  /**
   * Set the official name of the patient
   *
   * @param given given name
   * @param family family name
   * @return builder
   */
  public PatientBuilder name(@NonNull String given, @NonNull String family) {
    return name(given, family, "");
  }

  public PatientBuilder name(@NonNull String given, @NonNull String family, String prefix) {
    this.givenName = given;
    this.familyName = family;
    this.namePrefix = prefix;
    return self();
  }

  /**
   * Set the Birth-Date as String in default Format (dd.MM.yyyy)
   *
   * @param birthDate Date of Birth
   * @return builder
   */
  public PatientBuilder birthDate(@NonNull String birthDate) {
    return birthDate(birthDate, "dd.MM.yyyy");
  }

  @SneakyThrows
  public PatientBuilder birthDate(@NonNull String birthDate, @NonNull String format) {
    val formatter = new SimpleDateFormat(format);
    return birthDate(formatter.parse(birthDate));
  }

  public PatientBuilder birthDate(@NonNull Date birthDate) {
    this.birthDate = birthDate;
    return self();
  }

  public PatientBuilder address(
      @NonNull String city, @NonNull String postal, @NonNull String street) {
    return address(Country.D, city, postal, street);
  }

  public PatientBuilder address(
      @NonNull Country country,
      @NonNull String city,
      @NonNull String postal,
      @NonNull String street) {
    return address(AddressBuilder.address(country, city, postal, street, Address.AddressType.BOTH));
  }

  public PatientBuilder address(@NonNull Address address) {
    this.address = address;
    return self();
  }

  public PatientBuilder assigner(@NonNull InstitutionalOrganization assigner) {
    return assigner(new OrganizationReference(assigner.getId()), assigner.getName());
  }

  private PatientBuilder assigner(@NonNull OrganizationReference assigner, String displayValue) {
    this.assignerRef = assigner.asReference();
    this.assignerRef.setDisplay(displayValue);
    return self();
  }

  public KbvPatient build() {
    checkRequired();
    val patient = new KbvPatient();

    val profile = KbvItaForStructDef.PATIENT.asCanonicalType(kbvItaForVersion);
    val meta = new Meta().setProfile(List.of(profile));

    // set FHIR-specific values provided by HAPI
    patient.setId(this.getResourceId()).setMeta(meta);

    val humanNameBuilder =
        HumanNameBuilder.official()
            .prefix(this.namePrefix)
            .given(this.givenName)
            .family(this.familyName);

    HumanName humanName;
    if (kbvItaForVersion.compareTo(KbvItaForVersion.V1_1_0) < 0) {
      humanName = humanNameBuilder.build();
    } else {
      humanName = humanNameBuilder.buildExt();
    }

    patient
        .setIdentifier(identifiers)
        .setName(List.of(humanName))
        .setBirthDate(birthDate)
        .setAddress(List.of(address));

    return patient;
  }

  private void checkRequired() {
    this.checkRequired(identifierTypeDe, "Patient requires an identifierTypeDe");
    this.checkValueSet(identifierTypeDe, IdentifierTypeDe.PKV, IdentifierTypeDe.GKV);

    setIdentifier();
    if (insuranceType == VersicherungsArtDeBasis.PKV
        && kbvItaForVersion.compareTo(KbvItaForVersion.V1_1_0) < 0) {
      checkRequired(assignerRef, "PKV Patient requires an assigner");
      setPkvAssigner();
    }

    this.checkRequired(givenName, "Patient requires a given name");
    this.checkRequired(familyName, "Patient requires a family name");
    this.checkRequired(birthDate, "Patient requires a birthdate");
    this.checkRequired(address, "Patient requires an address");
  }

  private void setPkvAssigner() {
    val pkvIdentifier =
        this.identifiers.stream()
            .filter(
                identifier ->
                    identifier.getType().getCoding().stream()
                        .anyMatch(
                            coding ->
                                coding.getCode().equalsIgnoreCase(IdentifierTypeDe.PKV.getCode())))
            .findFirst()
            .orElseThrow(() -> new MissingFieldException(KbvPatient.class, "PKV Identifier"));

    val assigner = pkvIdentifier.getAssigner();
    assigner.setReference(assignerRef.getReference());
    assigner.setDisplay(assignerRef.getDisplay());
  }

  private void setIdentifier() {
    INamingSystem kvnrNamingSystem = null;
    if (kbvItaForVersion.compareTo(KbvItaForVersion.V1_1_0) < 0) {
      if (identifierTypeDe == IdentifierTypeDe.GKV) {
        kvnrNamingSystem = DeBasisNamingSystem.KVID;
      } else if (identifierTypeDe == IdentifierTypeDe.PKV) {
        kvnrNamingSystem = CommonNamingSystem.ACME_IDS_PATIENT;
      }
    } else {
      if (identifierTypeDe == IdentifierTypeDe.GKV) {
        kvnrNamingSystem = DeBasisNamingSystem.KVID_GKV;
      } else if (identifierTypeDe == IdentifierTypeDe.PKV) {
        kvnrNamingSystem = DeBasisNamingSystem.KVID_PKV;
      }
    }

    if (kvnrNamingSystem == null) {
      throw new BuilderException(
          format("Patient-Builder contains unsupported IdentifierType {0}", identifierTypeDe));
    }

    val identifier = new Identifier();
    identifier.setType(identifierTypeDe.asCodeableConcept());
    identifier.setSystem(kvnrNamingSystem.getCanonicalUrl());
    identifier.setValue(kvnr.getValue());
    this.identifiers.add(identifier);
  }

  public static PatientBuilder builder() {
    return new PatientBuilder();
  }

  @Deprecated(forRemoval = true)
  public static PatientBuilder faker() {
    return faker(KVNR.random());
  }

  @Deprecated(forRemoval = true)
  public static PatientBuilder faker(VersicherungsArtDeBasis insuranceType) {
    return faker(KVNR.random(), insuranceType);
  }

  @Deprecated(forRemoval = true)
  public static PatientBuilder faker(KVNR kvnr) {
    return faker(kvnr, randomElement(VersicherungsArtDeBasis.GKV, VersicherungsArtDeBasis.PKV));
  }

  public static PatientBuilder faker(KVNR kvnr, VersicherungsArtDeBasis insuranceType) {
    val builder = builder();
    builder
        .kvnr(kvnr, insuranceType)
        .name(fakerFirstName(), fakerLastName())
        .birthDate(fakerBirthday())
        .address(fakerCountry(), fakerCity(), fakerZipCode(), fakerStreetName());

    builder.identifierTypeDe =
        insuranceType.equals(VersicherungsArtDeBasis.GKV)
            ? IdentifierTypeDe.GKV
            : IdentifierTypeDe.PKV;
    if (builder.identifierTypeDe == IdentifierTypeDe.PKV) {
      builder.assigner(AssignerOrganizationFaker.builder().fake());
    }

    return builder;
  }
}
