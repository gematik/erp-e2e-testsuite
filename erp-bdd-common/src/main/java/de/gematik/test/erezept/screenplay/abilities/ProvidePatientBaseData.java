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

package de.gematik.test.erezept.screenplay.abilities;

import static java.text.MessageFormat.format;

import com.google.common.base.Strings;
import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.Country;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvCoverageBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvPatientBuilder;
import de.gematik.test.erezept.fhir.r4.erp.ErxConsent;
import de.gematik.test.erezept.fhir.r4.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.values.InsuranceCoverageInfo;
import de.gematik.test.erezept.fhir.valuesets.DmpKennzeichen;
import de.gematik.test.erezept.fhir.valuesets.PayorType;
import de.gematik.test.erezept.fhir.valuesets.VersichertenStatus;
import de.gematik.test.erezept.fhir.valuesets.Wop;
import java.util.Date;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.serenitybdd.screenplay.Ability;

public class ProvidePatientBaseData implements Ability {

  @Getter private KVNR kvnr;
  private final String firstName;
  private final String lastName;
  private final Date birthDate;
  private final String city;
  private final String postal;
  private final String street;

  @Getter private String iknr;
  private String insuranceName;

  @Setter private DmpKennzeichen dmpKennzeichen = DmpKennzeichen.NOT_SET;

  private final Wop wop;

  @Getter private InsuranceTypeDe patientInsuranceType;
  private InsuranceTypeDe coverageInsuranceType;
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
    this.wop = GemFaker.fakerValueSet(Wop.class);
  }

  public String getFullName() {
    return format("{0} {1}", firstName, lastName);
  }

  public InsuranceTypeDe getCoverageInsuranceType() {
    if (this.coverageInsuranceType == null) {
      // type of patient and coverage are mostly the same
      return this.getPatientInsuranceType();
    } else {
      // return only if coverage type is divergent from the one of the patient
      return this.coverageInsuranceType;
    }
  }

  public void setPatientInsuranceType(InsuranceTypeDe insuranceType) {
    this.patientInsuranceType = insuranceType;

    // when patient insurance type changes, we also have to adjust the (system name) of the KVNR
    // however, this can only be GKV or PKV
    val fhirType =
        insuranceType.equals(InsuranceTypeDe.PKV) ? InsuranceTypeDe.PKV : InsuranceTypeDe.GKV;
    this.kvnr = this.kvnr.as(fhirType);
    this.updateInsuranceInfo(this.getCoverageInsuranceType());
  }

  public void setCoverageInsuranceType(InsuranceTypeDe insuranceType) {
    this.coverageInsuranceType = insuranceType;
    this.updateInsuranceInfo(this.getCoverageInsuranceType());
  }

  public void setPayorType(@Nullable PayorType payorType) {
    this.payorType = payorType;
  }

  public Optional<PayorType> getPayorType() {
    return Optional.ofNullable(this.payorType);
  }

  private void updateInsuranceInfo(InsuranceTypeDe insuranceType) {
    val ici = InsuranceCoverageInfo.randomFor(insuranceType);
    this.iknr = ici.getIknr();
    this.insuranceName = ici.getName();
  }

  public KbvPatient getPatient() {
    return KbvPatientBuilder.builder()
        .kvnr(kvnr, patientInsuranceType)
        .name(firstName, lastName)
        .birthDate(birthDate)
        .address(Country.D, city, postal, street)
        .build();
  }

  public KbvCoverage getInsuranceCoverage() {
    val coverageType = this.getCoverageInsuranceType();
    val builder =
        KbvCoverageBuilder.insurance(iknr, insuranceName)
            .beneficiary(this.getPatient())
            .wop(wop)
            .dmpKennzeichen(dmpKennzeichen)
            .versichertenStatus(VersichertenStatus.MEMBERS);

    Optional.ofNullable(payorType)
        .ifPresentOrElse(builder::insuranceType, () -> builder.insuranceType(coverageType));

    return builder.build();
  }

  public IKNR getInsuranceIknr() {
    return IKNR.asSidIknr(this.iknr);
  }

  public boolean isPKV() {
    return this.kvnr.isPkv();
  }

  public boolean isGKV() {
    return this.kvnr.isGkv();
  }

  public boolean hasRememberedConsent() {
    return erxConsent != null;
  }

  public Optional<ErxConsent> getRememberedConsent() {
    return Optional.ofNullable(erxConsent);
  }

  public static ProvidePatientBaseData forGkvPatient(KVNR kvnr, String name) {
    return forPatient(kvnr, name, InsuranceTypeDe.GKV);
  }

  public static ProvidePatientBaseData forGkvPatient(
      KVNR kvnr, String givenName, String familyName) {
    return forPatient(kvnr, givenName, familyName, InsuranceTypeDe.GKV);
  }

  public static ProvidePatientBaseData forPkvPatient(KVNR kvnr, String fullName) {
    return forPatient(kvnr, fullName, InsuranceTypeDe.PKV);
  }

  public static ProvidePatientBaseData forPkvPatient(
      KVNR kvnr, String givenName, String familyName) {
    return forPatient(kvnr, givenName, familyName, InsuranceTypeDe.PKV);
  }

  public static ProvidePatientBaseData forPatient(
      KVNR kvnr, String fullName, String insuranceType) {
    return forPatient(kvnr, fullName, InsuranceTypeDe.fromCode(insuranceType));
  }

  public static ProvidePatientBaseData forPatient(
      KVNR kvnr, String fullName, InsuranceTypeDe insuranceType) {
    String[] tokens;
    if (!Strings.isNullOrEmpty(fullName)) {
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

    return forPatient(kvnr, givenName, familyName, insuranceType);
  }

  /**
   * Creates a new instance of {@link ProvidePatientBaseData} for a patient with the specified
   * details.
   *
   * <p><b>Note:</b> Because this method requires a {@link KVNR} <b>and</b> {@link InsuranceTypeDe}
   * which both carry the same information, it is very likely to mess up the data consistency from
   * the user perspective.
   *
   * <p>Imagine the following scenario: A user creates a {@link KVNR} with a {@link InsuranceTypeDe}
   * of {@code PKV}.
   *
   * <pre>{@code
   * val kvnr = KVNR.from("X110415294", InsuranceTypeDe.PKV);
   * val insuranceType                = InsuranceTypeDe.GKV;
   * val pbd = ProvidePatientBaseData.forPatient(kvnr, "Max", "Mustermann", insuranceType);
   *
   * // but here the twist: the insurance types are not the same
   * assert kvnr.getInsuranceType() == insuranceType; // AssertionError
   * }</pre>
   *
   * <p>This issue is prevented by taking the given {@link InsuranceTypeDe} and setting it on the
   * {@link KVNR} instance. However, whenever using this method, it is recommended to assert the
   * insurance type of the {@link KVNR} instance.
   *
   * <pre>{@code
   * val kvnr = KVNR.from("X110415294", InsuranceTypeDe.GKV);
   * val pbd = ProvidePatientBaseData.forPatient(kvnr, "Max", "Mustermann", kvnr.getInsuranceType());
   *
   * assert kvnr.getInsuranceType() == insuranceType; // OK
   *
   * }</pre>
   *
   * @param kvnr the KVNR (Krankenversichertennummer) of the patient
   * @param firstName the first name of the patient
   * @param givenName the given name of the patient
   * @param insuranceType the insurance type of the patient
   * @return a new instance of `ProvidePatientBaseData` with the specified details
   */
  public static ProvidePatientBaseData forPatient(
      KVNR kvnr, String firstName, String givenName, InsuranceTypeDe insuranceType) {
    val pbd = new ProvidePatientBaseData(kvnr, firstName, givenName);

    // this will trigger the fix for insuranceType of the KVNR; see javadocs
    pbd.setPatientInsuranceType(insuranceType);

    // ensure the fix is working
    // try this to assert pbd.getKvnr().getInsuranceType() == insuranceType; // NOSONAR

    return pbd;
  }
}
