/*
 * Copyright (c) 2023 gematik GmbH
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
import de.gematik.test.erezept.fhir.builder.kbv.MedicalOrganizationBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.PractitionerBuilder;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.resources.kbv.MedicalOrganization;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.valuesets.Country;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import de.gematik.test.erezept.lei.cfg.DoctorConfiguration;
import lombok.NonNull;
import lombok.val;
import net.serenitybdd.screenplay.Ability;

/** The Ability to provide Base-Data (Stammdaten) */
public class ProvideDoctorBaseData implements Ability {

  private final String firstName;
  private final String lastName;
  private final BaseANR doctorNumber;
  private final QualificationType qualificationType;
  private final String bsnr;
  private final String phone;
  private final String email;
  private final String city;
  private final String postal;
  private final String street;

  private ProvideDoctorBaseData(DoctorConfiguration cfg) {
    val fullName = cfg.getName();

    String[] tokens;
    if (fullName != null && fullName.length() > 0) {
      tokens = fullName.split(" "); // split by first and last names
    } else {
      tokens = new String[0];
    }

    this.firstName = (tokens.length > 0) ? tokens[0] : GemFaker.fakerFirstName();
    this.lastName = (tokens.length > 1) ? tokens[1] : GemFaker.fakerLastName();

    this.qualificationType = cfg.getQualificationType();
    this.doctorNumber = BaseANR.randomFromQualification(this.qualificationType);

    this.bsnr = GemFaker.fakerBsnr();
    this.phone = GemFaker.fakerPhone();
    this.email = GemFaker.fakerEMail();
    this.city = GemFaker.fakerCity();
    this.postal = GemFaker.fakerZipCode();
    this.street = GemFaker.fakerStreetName();
  }

  public KbvPractitioner getPractitioner() {
    return PractitionerBuilder.builder()
        .anr(doctorNumber)
        .name(firstName, lastName, "Dr.")
        .addQualification(qualificationType)
        .addQualification("Super-Facharzt für alles Mögliche")
        .build();
  }

  public MedicalOrganization getMedicalOrganization() {
    return MedicalOrganizationBuilder.builder()
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
      @NonNull final DoctorConfiguration doctorConfiguration) {
    return new ProvideDoctorBaseData(doctorConfiguration);
  }
}
