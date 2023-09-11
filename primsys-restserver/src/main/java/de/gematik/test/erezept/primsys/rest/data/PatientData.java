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

package de.gematik.test.erezept.primsys.rest.data;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;
import static de.gematik.test.erezept.primsys.utils.Strings.getOrDefault;

import com.fasterxml.jackson.annotation.JsonAlias;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.primsys.utils.Strings;
import java.text.SimpleDateFormat;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.val;

@Data
@XmlRootElement
public class PatientData {

  private static final String DATE_FORMAT = "dd.MM.yyyy";

  @JsonAlias(value = "kvid")
  private String kvnr;

  private String firstName;
  private String lastName;
  private String birthDate;
  private String city;
  private String postal;
  private String street;

  public String getFirstName() {
    if (Strings.isNullOrEmpty(firstName)) {
      firstName = fakerFirstName();
    }
    return firstName;
  }

  public String getLastName() {
    if (Strings.isNullOrEmpty(lastName)) {
      lastName = fakerLastName();
    }
    return lastName;
  }

  public String getBirthDate() {
    if (Strings.isNullOrEmpty(birthDate)) {
      birthDate = fakerBirthdayAsString();
    }
    return birthDate;
  }

  public String getCity() {
    if (Strings.isNullOrEmpty(city)) {
      city = fakerCity();
    }
    return city;
  }

  public String getPostal() {
    if (Strings.isNullOrEmpty(postal)) {
      postal = fakerZipCode();
    }
    return postal;
  }

  public String getStreet() {
    if (Strings.isNullOrEmpty(street)) {
      street = fakerStreetName();
    }
    return street;
  }

  public static PatientData create() {
    val p = new PatientData();
    p.kvnr = KVNR.random().getValue();
    p.firstName = fakerFirstName();
    p.lastName = fakerLastName();
    p.birthDate = new SimpleDateFormat(DATE_FORMAT).format(fakerBirthday());
    p.city = fakerCity();
    p.postal = fakerZipCode();
    p.street = fakerStreetName();
    return p;
  }

  public void fakeMissing() {
    this.kvnr = getOrDefault(this.kvnr, KVNR.random().getValue());
    this.firstName = getOrDefault(this.firstName, fakerFirstName());
    this.lastName = getOrDefault(this.lastName, fakerLastName());
    this.birthDate =
        getOrDefault(this.birthDate, new SimpleDateFormat(DATE_FORMAT).format(fakerBirthday()));
    this.city = getOrDefault(this.city, fakerCity());
    this.postal = getOrDefault(this.postal, fakerZipCode());
    this.street = getOrDefault(this.street, fakerStreetName());
  }

  public static PatientData fromKbvBundle(KbvErpBundle bundle) {
    val p = new PatientData();
    p.kvnr = bundle.getKvnr().getValue();
    p.firstName = bundle.getPatientGivenName();
    p.lastName = bundle.getPatientFamilyName();
    p.birthDate = new SimpleDateFormat(DATE_FORMAT).format(bundle.getPatientBirthDate());
    p.city = bundle.getPatientAddressCity();
    p.postal = bundle.getPatientAddressPostalCode();
    p.street = bundle.getPatientAddressStreet();
    return p;
  }
}
