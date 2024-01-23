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

package de.gematik.test.erezept.screenplay.abilities;

import static java.text.MessageFormat.*;

import de.gematik.test.erezept.fhir.builder.*;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.*;
import javax.annotation.Nullable;
import lombok.*;
import net.serenitybdd.screenplay.*;

public class ProvidePatientBaseData implements Ability {

  @Getter @Setter private KVNR kvnr;
  private final String firstName;
  private final String lastName;
  private final Date birthDate;
  private final String city;
  private final String postal;
  private final String street;

  @Getter private String iknr;
  private String insuranceName;

  private AssignerOrganization pkvAssignerOrganization;

  private final Wop wop;

  private VersicherungsArtDeBasis patientInsuranceType;
  private VersicherungsArtDeBasis coverageInsuranceType;
  @Nullable private PayorType payorType;

  @Setter private ErxConsent erxConsent;

  private ProvidePatientBaseData(KVNR kvnr, String firstName, String lastName) {
    this.kvnr = kvnr;
    this.firstName = firstName;
    this.lastName = lastName;
    this.birthDate = GemFaker.fakerBirthday();
    this.city = GemFaker.fakerCity();
    this.postal = GemFaker.fakerZipCode();
    this.street = GemFaker.fakerStreetName();

    this.updateInsuranceInfo(this.getCoverageInsuranceType());
    this.wop = GemFaker.fakerValueSet(Wop.class);
  }

  public String getFullName() {
    return format("{0} {1}", firstName, lastName);
  }

  public VersicherungsArtDeBasis getCoverageInsuranceType() {
    if (this.coverageInsuranceType == null) {
      // type of patient and coverage are mostly the same
      return this.getPatientInsuranceType();
    } else {
      // return only if coverage type is divergent from the one of the patient
      return this.coverageInsuranceType;
    }
  }

  public void setPatientInsuranceType(VersicherungsArtDeBasis insuranceType) {
    this.patientInsuranceType = insuranceType;
    this.updateInsuranceInfo(this.getCoverageInsuranceType());
  }

  public void setCoverageInsuranceType(VersicherungsArtDeBasis insuranceType) {
    this.coverageInsuranceType = insuranceType;
    this.updateInsuranceInfo(this.getCoverageInsuranceType());
  }

  public void setPayorType(@Nullable PayorType payorType) {
    this.payorType = payorType;
  }

  public Optional<PayorType> getPayorType() {
    return Optional.ofNullable(this.payorType);
  }

  public VersicherungsArtDeBasis getPatientInsuranceType() {
    return this.patientInsuranceType != null
        ? this.patientInsuranceType
        : VersicherungsArtDeBasis.GKV;
  }

  private void updateInsuranceInfo(VersicherungsArtDeBasis insuranceType) {
    val ici = InsuranceCoverageInfo.randomFor(insuranceType);
    this.iknr = ici.getIknr();
    this.insuranceName = ici.getName();
  }

  public KbvPatient getPatient() {
    val pb =
        PatientBuilder.builder()
            .kvnr(kvnr, patientInsuranceType)
            .name(firstName, lastName)
            .birthDate(birthDate)
            .address(Country.D, city, postal, street);

    if (patientInsuranceType == VersicherungsArtDeBasis.PKV) {
      if (pkvAssignerOrganization == null) {
        // create Assigner on first call and reuse on later calls to prevent non-deterministic
        // behaviour!
        pkvAssignerOrganization = AssignerOrganizationBuilder.faker().build();
      }
      pb.assigner(pkvAssignerOrganization);
    }

    return pb.build();
  }

  public KbvCoverage getInsuranceCoverage() {
    val coverageType = this.getCoverageInsuranceType();
    val builder =
        KbvCoverageBuilder.insurance(iknr, insuranceName)
            .beneficiary(this.getPatient())
            .wop(wop)
            .versichertenStatus(VersichertenStatus.MEMBERS);

    Optional.ofNullable(payorType)
        .ifPresentOrElse(builder::versicherungsArt, () -> builder.versicherungsArt(coverageType));

    return builder.build();
  }

  public IKNR getInsuranceIknr() {
    return IKNR.from(this.iknr);
  }

  public boolean isPKV() {
    return patientInsuranceType == VersicherungsArtDeBasis.PKV;
  }

  public boolean isGKV() {
    return patientInsuranceType == VersicherungsArtDeBasis.GKV;
  }

  public boolean hasRememberedConsent() {
    return erxConsent != null;
  }

  public Optional<ErxConsent> getRememberedConsent() {
    return Optional.ofNullable(erxConsent);
  }

  public static ProvidePatientBaseData forGkvPatient(KVNR kvnr, String name) {
    return forPatient(kvnr, name, VersicherungsArtDeBasis.GKV);
  }

  public static ProvidePatientBaseData forGkvPatient(
      KVNR kvnr, String givenName, String familyName) {
    return forPatient(kvnr, givenName, familyName, VersicherungsArtDeBasis.GKV);
  }

  public static ProvidePatientBaseData forPkvPatient(KVNR kvnr, String fullName) {
    return forPatient(kvnr, fullName, VersicherungsArtDeBasis.PKV);
  }

  public static ProvidePatientBaseData forPkvPatient(
      KVNR kvnr, String givenName, String familyName) {
    return forPatient(kvnr, givenName, familyName, VersicherungsArtDeBasis.PKV);
  }

  public static ProvidePatientBaseData forPatient(
      KVNR kvnr, String fullName, String versicherungsArt) {
    return forPatient(kvnr, fullName, VersicherungsArtDeBasis.fromCode(versicherungsArt));
  }

  public static ProvidePatientBaseData forPatient(
      KVNR kvnr, String fullName, VersicherungsArtDeBasis versicherungsArt) {
    String[] tokens;
    if (fullName != null && fullName.length() > 0) {
      tokens = fullName.split(" "); // split by first and last names
    } else {
      tokens = new String[0];
    }

    val givenName = (tokens.length > 0) ? tokens[0] : GemFaker.fakerFirstName();

    String familyName;
    if (tokens.length > 1) {
      // take only the last token as names might become very long
      familyName = tokens[tokens.length - 1];
    } else {
      familyName = GemFaker.fakerLastName();
    }

    return forPatient(kvnr, givenName, familyName, versicherungsArt);
  }

  public static ProvidePatientBaseData forPatient(
      KVNR kvnr, String firstName, String givenName, VersicherungsArtDeBasis versicherungsArt) {
    val ret = new ProvidePatientBaseData(kvnr, firstName, givenName);
    ret.setPatientInsuranceType(versicherungsArt);
    return ret;
  }
}
