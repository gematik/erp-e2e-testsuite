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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.coding.WithNamingSystem;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.builder.AddressBuilder;
import de.gematik.bbriccs.fhir.de.builder.HumanNameBuilder;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.Country;
import de.gematik.bbriccs.fhir.de.valueset.IdentifierTypeDe;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.CommonNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.r4.InstitutionalOrganization;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class KbvPatientBuilder extends ResourceBuilder<KbvPatient, KbvPatientBuilder> {

  private final List<Identifier> identifiers = new LinkedList<>();
  private KbvItaForVersion kbvItaForVersion = KbvItaForVersion.getDefaultVersion();
  private String givenName;
  private String familyName;
  private String namePrefix;
  private Date birthDate;
  private Address address;

  private KVNR kvnr;
  private IdentifierTypeDe identifierTypeDe;
  private InsuranceTypeDe insuranceType;
  private Reference assignerRef;

  public static KbvPatientBuilder builder() {
    return new KbvPatientBuilder();
  }

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public KbvPatientBuilder version(KbvItaForVersion version) {
    this.kbvItaForVersion = version;
    return this;
  }

  public KbvPatientBuilder kvnr(KVNR kvnr, InsuranceTypeDe insuranceType) {
    this.kvnr = kvnr;
    this.identifierTypeDe =
        insuranceType.equals(InsuranceTypeDe.GKV) ? IdentifierTypeDe.GKV : IdentifierTypeDe.PKV;
    this.insuranceType = insuranceType;
    return this;
  }

  /**
   * Set the official name of the patient
   *
   * @param given given name
   * @param family family name
   * @return builder
   */
  public KbvPatientBuilder name(String given, String family) {
    return name(given, family, "");
  }

  public KbvPatientBuilder name(String given, String family, String prefix) {
    this.givenName = given;
    this.familyName = family;
    this.namePrefix = prefix;
    return this;
  }

  /**
   * Set the Birth-Date as String in default Format (dd.MM.yyyy)
   *
   * @param birthDate Date of Birth
   * @return builder
   */
  public KbvPatientBuilder birthDate(String birthDate) {
    return birthDate(birthDate, "dd.MM.yyyy");
  }

  @SneakyThrows
  public KbvPatientBuilder birthDate(String birthDate, String format) {
    val formatter = new SimpleDateFormat(format);
    return birthDate(formatter.parse(birthDate));
  }

  public KbvPatientBuilder birthDate(Date birthDate) {
    this.birthDate = birthDate;
    return this;
  }

  public KbvPatientBuilder address(String city, String postal, String street) {
    return address(Country.D, city, postal, street);
  }

  public KbvPatientBuilder address(Country country, String city, String postal, String street) {
    return address(
        AddressBuilder.ofBothTypes()
            .country(country)
            .city(city)
            .postal(postal)
            .street(street)
            .build());
  }

  public KbvPatientBuilder address(Address address) {
    this.address = address;
    return this;
  }

  public KbvPatientBuilder assigner(InstitutionalOrganization assigner) {
    this.assignerRef = assigner.asReferenceWithDisplay();
    return this;
  }

  @Override
  public KbvPatient build() {
    checkRequired();
    val patient =
        this.createResource(KbvPatient::new, KbvItaForStructDef.PATIENT, kbvItaForVersion);

    val humanNameBuilder =
        HumanNameBuilder.official()
            .prefix(this.namePrefix)
            .given(this.givenName)
            .family(this.familyName);

    HumanName humanName;
    if (kbvItaForVersion.compareTo(KbvItaForVersion.V1_1_0) < 0) {
      humanName = humanNameBuilder.buildSimple();
    } else {
      humanName = humanNameBuilder.build();
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
    if (insuranceType == InsuranceTypeDe.PKV
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
    val kvnrNamingSystem = getKvnrNamingSystem();

    val identifier = new Identifier();
    identifier.setType(identifierTypeDe.asCodeableConcept());
    identifier.setSystem(kvnrNamingSystem.getCanonicalUrl());
    identifier.setValue(kvnr.getValue());
    this.identifiers.add(identifier);
  }

  private WithNamingSystem getKvnrNamingSystem() {
    WithNamingSystem kvnrNamingSystem = null;
    if (kbvItaForVersion.compareTo(KbvItaForVersion.V1_1_0) < 0) {
      if (identifierTypeDe == IdentifierTypeDe.GKV) {
        kvnrNamingSystem = DeBasisProfilNamingSystem.KVID;
      } else if (identifierTypeDe == IdentifierTypeDe.PKV) {
        kvnrNamingSystem = CommonNamingSystem.ACME_IDS_PATIENT;
      }
    } else {
      if (identifierTypeDe == IdentifierTypeDe.GKV) {
        kvnrNamingSystem = DeBasisProfilNamingSystem.KVID_GKV_SID;
      } else if (identifierTypeDe == IdentifierTypeDe.PKV) {
        kvnrNamingSystem = DeBasisProfilNamingSystem.KVID_PKV_SID;
      }
    }
    return Optional.ofNullable(kvnrNamingSystem)
        .orElseThrow(
            () ->
                new BuilderException(
                    format(
                        "Patient-Builder contains unsupported IdentifierType {0}",
                        identifierTypeDe)));
  }
}
