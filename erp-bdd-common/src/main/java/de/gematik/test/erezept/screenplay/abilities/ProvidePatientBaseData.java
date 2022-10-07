/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.screenplay.abilities;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.AssignerOrganizationBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.CoverageBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.PatientBuilder;
import de.gematik.test.erezept.fhir.resources.erp.ErxConsent;
import de.gematik.test.erezept.fhir.resources.kbv.AssignerOrganization;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.values.IKNR;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.serenitybdd.screenplay.Ability;
import org.hl7.fhir.r4.model.Coverage;

public class ProvidePatientBaseData implements Ability {

  @Getter @Setter private String kvid;
  private final String firstName;
  private final String lastName;
  private final Date birthDate;
  private final String city;
  private final String postal;
  private final String street;

  @Getter private final String iknr;
  private final String insuranceName;

  private AssignerOrganization pkvAssignerOrganization;

  private final Wop wop;

  @Getter @Setter private VersicherungsArtDeBasis versicherungsArt;

  @Setter private ErxConsent erxConsent;

  private ProvidePatientBaseData(String kvid, String firstName, String lastName) {
    this.kvid = kvid;
    this.firstName = firstName;
    this.lastName = lastName;
    this.birthDate = GemFaker.fakerBirthday();
    this.city = GemFaker.fakerCity();
    this.postal = GemFaker.fakerZipCode();
    this.street = GemFaker.fullStreetName();

    this.iknr = GemFaker.fakerIknr();
    this.insuranceName = GemFaker.insuranceName();
    this.wop = GemFaker.fakerValueSet(Wop.class);
  }

  public String getFullName() {
    return format("{0} {1}", firstName, lastName);
  }

  public KbvPatient getPatient() {
    IdentifierTypeDe identifierTypeDe =
        (versicherungsArt == VersicherungsArtDeBasis.PKV)
            ? IdentifierTypeDe.PKV
            : IdentifierTypeDe.GKV;
    val pb =
        PatientBuilder.builder()
            .kvIdentifierDe(kvid, identifierTypeDe)
            .name(firstName, lastName)
            .birthDate(birthDate)
            .address(Country.D, city, postal, street);

    if (versicherungsArt == VersicherungsArtDeBasis.PKV) {
      if (pkvAssignerOrganization == null) {
        // create Assigner on first call and reuse on later calls to prevent non-deterministic
        // behaviour!
        pkvAssignerOrganization = AssignerOrganizationBuilder.faker().build();
      }
      pb.assigner(pkvAssignerOrganization);
    }

    return pb.build();
  }

  public Coverage getInsuranceCoverage() {
    return CoverageBuilder.insurance(iknr, insuranceName)
        .beneficiary(this.getPatient())
        .wop(wop)
        .versichertenStatus(VersichertenStatus.MEMBERS)
        .versicherungsArt(versicherungsArt)
        .build();
  }

  public IKNR getInsuranceIknr() {
    return IKNR.from(this.iknr);
  }

  public boolean isPKV() {
    return versicherungsArt == VersicherungsArtDeBasis.PKV;
  }

  public boolean isGKV() {
    return versicherungsArt == VersicherungsArtDeBasis.GKV;
  }

  public boolean hasRememberedConsent() {
    return erxConsent != null;
  }

  public Optional<ErxConsent> getRememberedConsent() {
    return Optional.ofNullable(erxConsent);
  }

  public static ProvidePatientBaseData forGkvPatient(String kvid, String name) {
    return forPatient(kvid, name, VersicherungsArtDeBasis.GKV);
  }

  public static ProvidePatientBaseData forGkvPatient(
      String kvid, String givenName, String familyName) {
    return forPatient(kvid, givenName, familyName, VersicherungsArtDeBasis.GKV);
  }

  public static ProvidePatientBaseData forPkvPatient(String kvid, String fullName) {
    return forPatient(kvid, fullName, VersicherungsArtDeBasis.PKV);
  }

  public static ProvidePatientBaseData forPkvPatient(
      String kvid, String givenName, String familyName) {
    return forPatient(kvid, givenName, familyName, VersicherungsArtDeBasis.PKV);
  }

  public static ProvidePatientBaseData forPatient(
      String kvid, String fullName, String versicherungsArt) {
    return forPatient(kvid, fullName, VersicherungsArtDeBasis.fromCode(versicherungsArt));
  }

  public static ProvidePatientBaseData forPatient(
      String kvid, String fullName, VersicherungsArtDeBasis versicherungsArt) {
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

    return forPatient(kvid, givenName, familyName, versicherungsArt);
  }

  public static ProvidePatientBaseData forPatient(
      String kvid, String firstName, String givenName, VersicherungsArtDeBasis versicherungsArt) {
    val ret = new ProvidePatientBaseData(kvid, firstName, givenName);
    ret.setVersicherungsArt(versicherungsArt);
    return ret;
  }
}
