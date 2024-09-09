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

package de.gematik.test.erezept.primsys.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.gematik.test.erezept.primsys.data.valuesets.InsuranceTypeDto;
import java.util.Date;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientDto {

  @JsonProperty(required = true)
  private String kvnr;

  private InsuranceTypeDto insuranceType;
  private String firstName;
  private String lastName;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
  private Date birthDate;

  private String city;
  private String postal;
  private String street;

  public static Builder withKvnr(String kvnr) {
    return new Builder(kvnr);
  }

  @RequiredArgsConstructor
  public static class Builder {
    private final String kvnr;
    private InsuranceTypeDto insuranceType = InsuranceTypeDto.GKV;
    private String firstName;
    private String lastName;
    private Date birthDate;
    private String city;
    private String postal;
    private String street;

    public Builder withInsuranceType(InsuranceTypeDto insuranceType) {
      this.insuranceType = insuranceType;
      return this;
    }

    public Builder withFirstName(String firstName) {
      this.firstName = firstName;
      return this;
    }

    public Builder withLastName(String lastName) {
      this.lastName = lastName;
      return this;
    }

    public Builder named(String firstName, String lastName) {
      return withFirstName(firstName).withLastName(lastName);
    }

    public Builder bornOn(Date birthDate) {
      this.birthDate = birthDate;
      return this;
    }

    public Builder city(String city) {
      this.city = city;
      return this;
    }

    public Builder postal(String postal) {
      this.postal = postal;
      return this;
    }

    public Builder street(String street) {
      this.street = street;
      return this;
    }

    public Builder address(String postal, String city, String street) {
      return postal(postal).city(city).street(street);
    }

    public PatientDto build() {
      return new PatientDto(
          kvnr, insuranceType, firstName, lastName, birthDate, city, postal, street);
    }
  }
}
