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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;

import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.builder.AddressBuilder;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.ErpNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import de.gematik.test.erezept.fhir.references.kbv.OrganizationReference;
import de.gematik.test.erezept.fhir.resources.InstitutionalOrganization;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.valuesets.Country;
import de.gematik.test.erezept.fhir.valuesets.IdentifierTypeDe;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;

public class PatientBuilder extends AbstractResourceBuilder<PatientBuilder> {

  private final List<Identifier> identifiers = new LinkedList<>();

  private HumanName name;
  private Date birthDate;
  private Address address;
  private IdentifierTypeDe identifierTypeDe;
  private OrganizationReference assignerRef;

  public static PatientBuilder builder() {
    return new PatientBuilder();
  }

  public static PatientBuilder faker() {
    return faker(fakerKvid());
  }

  public static PatientBuilder faker(IdentifierTypeDe identifierTypeDe) {
    return faker(fakerKvid(), identifierTypeDe);
  }

  public static PatientBuilder faker(String kvid) {
    return faker(kvid, randomElement(IdentifierTypeDe.GKV, IdentifierTypeDe.PKV));
  }

  public static PatientBuilder faker(String kvid, IdentifierTypeDe identifierTypeDe) {
    val builder = builder();
    builder
        .kvIdentifierDe(kvid, identifierTypeDe)
        .name(fakerFirstName(), fakerLastName())
        .birthDate(fakerBirthday())
        .address(fakerCountry(), fakerCity(), fakerZipCode(), fakerStreetName());

    if (identifierTypeDe == IdentifierTypeDe.PKV) {
      builder.assigner(AssignerOrganizationBuilder.faker().build());
    }

    return builder;
  }

  public PatientBuilder kvIdentifierDe(
      @NonNull String kvIdentifier, @NonNull IdentifierTypeDe identifierTypeDe) {
    val identifier = new Identifier();
    identifier.setType(identifierTypeDe.asCodeableConcept());
    if (identifierTypeDe == IdentifierTypeDe.GKV) {
      identifier.setSystem(ErpNamingSystem.KVID.getCanonicalUrl());
    } else if (identifierTypeDe == IdentifierTypeDe.PKV) {
      identifier.setSystem(ErpNamingSystem.ACME_IDS_PATIENT.getCanonicalUrl());
    }
    identifier.setValue(kvIdentifier);
    this.identifiers.add(identifier);
    this.identifierTypeDe = identifierTypeDe;
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
    this.name = new HumanName();
    this.name.setUse(HumanName.NameUse.OFFICIAL);
    this.name.addGiven(given).setFamily(family);
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
    this.assignerRef = assigner;
    this.assignerRef.setDisplay(displayValue);
    return self();
  }

  public KbvPatient build() {
    checkRequired();
    val patient = new KbvPatient();

    val profile = ErpStructureDefinition.KBV_FOR_PATIENT.asCanonicalType();
    val meta = new Meta().setProfile(List.of(profile));

    // set FHIR-specific values provided by HAPI
    patient.setId(this.getResourceId()).setMeta(meta);

    if (identifierTypeDe == IdentifierTypeDe.PKV) {
      setPkvAssigner();
    }

    patient
        .setIdentifier(identifiers)
        .setName(List.of(name))
        .setBirthDate(birthDate)
        .setAddress(List.of(address));

    return patient;
  }

  private void checkRequired() {
    this.checkRequired(identifierTypeDe, "Patient requires an identifierTypeDe");
    this.checkValueSet(identifierTypeDe, IdentifierTypeDe.PKV, IdentifierTypeDe.GKV);

    if (identifierTypeDe == IdentifierTypeDe.PKV) {
      checkRequired(assignerRef, "PKV Patient requires an assigner");
    }

    this.checkRequired(name, "Patient requires a name");
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
}
