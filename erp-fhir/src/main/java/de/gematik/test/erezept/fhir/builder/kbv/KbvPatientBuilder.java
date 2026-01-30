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

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.de.builder.AddressBuilder;
import de.gematik.bbriccs.fhir.de.builder.HumanNameBuilder;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.Country;
import de.gematik.bbriccs.fhir.de.valueset.IdentifierTypeDe;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Identifier;

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

  public KbvPatientBuilder kvnr(KVNR kvnr) {
    this.kvnr = kvnr;
    return this;
  }

  /**
   * @param kvnr to be used for the patient
   * @param insuranceType of the patient !! from kbvItaForVersion. 1.2.0
   *     DeBasisProfilNamingSystem.KVID_GKV_SID is fixed value
   * @return the builder
   * @deprecated use the {@link KbvPatientBuilder#kvnr(KVNR)} method instead and make sure the KVNR
   *     bears the required insurance type of patient correctly
   */
  @Deprecated(since = "0.13.0", forRemoval = true)
  public KbvPatientBuilder kvnr(KVNR kvnr, InsuranceTypeDe insuranceType) {
    // to prevent breaks: overwrite the given current with the desired insuranceType
    return kvnr(kvnr.as(insuranceType));
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

  @Override
  public KbvPatient build() {
    checkRequired();
    val patient =
        this.createResource(KbvPatient::new, KbvItaForStructDef.PATIENT, kbvItaForVersion);

    if (kbvItaForVersion.isBiggerThan(KbvItaForVersion.V1_2_0)) patient.getMeta().setVersionId("1");

    setIdentifier();

    val humanName =
        HumanNameBuilder.official()
            .prefix(this.namePrefix)
            .given(this.givenName)
            .family(this.familyName)
            .build();

    patient
        .setIdentifier(identifiers)
        .setName(List.of(humanName))
        .setBirthDate(birthDate)
        .setAddress(List.of(address));

    return patient;
  }

  private void checkRequired() {
    this.checkRequired(kvnr, "Patient requires a KVNR");
    this.checkRequired(givenName, "Patient requires a given name");
    this.checkRequired(familyName, "Patient requires a family name");
    this.checkRequired(birthDate, "Patient requires a birthdate");
    this.checkRequired(address, "Patient requires an address");
  }

  private void setIdentifier() {
    IdentifierTypeDe identifierType;

    if (kbvItaForVersion.compareTo(KbvItaForVersion.V1_1_0) <= 0) {

      if (this.kvnr.getInsuranceType().equals(InsuranceTypeDe.PKV)) {
        identifierType = IdentifierTypeDe.PKV;
      } else {
        identifierType = IdentifierTypeDe.GKV;
      }
      val identifier = kvnr.asIdentifier().setType(identifierType.asCodeableConcept());
      this.identifiers.add(identifier);
    } else {

      // from KbvItaForVersion.V1_2_0 GKV is a fixed value
      kvnr = kvnr.as(InsuranceTypeDe.GKV);
      val identifier = kvnr.asIdentifier().setType(IdentifierTypeDe.KVZ10.asCodeableConcept());
      this.identifiers.add(identifier);
    }
  }
}
