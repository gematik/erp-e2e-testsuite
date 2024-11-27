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

package de.gematik.test.erezept.screenplay.abilities;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.config.dto.actor.DoctorConfiguration;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.MedicalOrganizationBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.PractitionerBuilder;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.resources.kbv.MedicalOrganization;
import de.gematik.test.erezept.fhir.values.AsvFachgruppennummer;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.valuesets.Country;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.serenitybdd.screenplay.Ability;

/** The Ability to provide Base-Data (Stammdaten) */
public class ProvideDoctorBaseData implements Ability {

  private final String practitionerId;
  private final String medicationOrganizationId;
  private final String firstName;
  private final String lastName;
  private AsvFachgruppennummer asvFachgruppennummer;
  @Setter private BaseANR doctorNumber;
  @Setter private QualificationType qualificationType;
  private final String bsnr;
  private final String phone;
  private final String email;
  private final String city;
  private final String postal;
  private final String street;
  @Setter private boolean isAsv = false;

  @Getter private final String hbaTelematikId;

  private ProvideDoctorBaseData(DoctorConfiguration cfg, String telematikID) {
    this.practitionerId = UUID.randomUUID().toString();
    this.medicationOrganizationId = UUID.randomUUID().toString();

    val fullName = cfg.getName();
    String[] tokens;
    if (fullName != null && !fullName.isEmpty()) {
      tokens = fullName.split(" "); // split by first and last names
    } else {
      tokens = new String[0];
    }

    this.firstName = (tokens.length > 0) ? tokens[0] : GemFaker.fakerFirstName();
    this.lastName = (tokens.length > 1) ? tokens[1] : GemFaker.fakerLastName();

    this.qualificationType = QualificationType.fromDisplay(cfg.getQualificationType());
    this.doctorNumber = BaseANR.randomFromQualification(this.qualificationType);

    this.bsnr = GemFaker.fakerBsnr();
    this.phone = GemFaker.fakerPhone();
    this.email = GemFaker.fakerEMail();
    this.city = GemFaker.fakerCity();
    this.postal = GemFaker.fakerZipCode();
    this.street = GemFaker.fakerStreetName();
    this.hbaTelematikId = telematikID;
  }

  public KbvPractitioner getPractitioner() {
    if (isAsv) {
      this.asvFachgruppennummer = AsvFachgruppennummer.from("555555013");
      return getAsvPractitioner();
    } else return getStandardPractitioner();
  }

  private KbvPractitioner getAsvPractitioner() {
    return PractitionerBuilder.builder()
        .setResourceId(practitionerId)
        .name(firstName, lastName, "Dr.")
        .addQualification(qualificationType)
        .addQualification(asvFachgruppennummer)
        .addQualification("Super-Facharzt für alles Mögliche")
        .build();
  }

  private KbvPractitioner getStandardPractitioner() {
    return PractitionerBuilder.builder()
        .setResourceId(practitionerId)
        .anr(doctorNumber)
        .name(firstName, lastName, "Dr.")
        .addQualification(qualificationType)
        .addQualification("Super-Facharzt für alles Mögliche")
        .build();
  }

  public MedicalOrganization getMedicalOrganization() {
    return MedicalOrganizationBuilder.builder()
        .setResourceId(medicationOrganizationId)
        .bsnr(bsnr)
        .phone(phone)
        .email(email)
        .name(createMedicationOrganizationName())
        .address(Country.D, city, postal, street)
        .build();
  }

  private String createMedicationOrganizationName() {
    String name;
    if (qualificationType == QualificationType.DENTIST) {
      name = format("Zahnarztpraxis {0}", lastName);
    } else {
      name = format("Arztpraxis {0}", lastName);
    }
    return name;
  }

  public static ProvideDoctorBaseData fromConfiguration(
      DoctorConfiguration doctorConfiguration, String hbaTelematikId) {
    return new ProvideDoctorBaseData(doctorConfiguration, hbaTelematikId);
  }
}
