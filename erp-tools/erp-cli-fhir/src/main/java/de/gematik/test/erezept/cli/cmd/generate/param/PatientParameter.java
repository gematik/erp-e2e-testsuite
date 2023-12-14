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

package de.gematik.test.erezept.cli.cmd.generate.param;

import de.gematik.test.erezept.cli.converter.GermanDateConverter;
import de.gematik.test.erezept.cli.converter.NameConverter;
import de.gematik.test.erezept.cli.util.*;
import de.gematik.test.erezept.fhir.builder.*;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.values.IKNR;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.*;
import lombok.*;
import picocli.*;
import picocli.CommandLine.Mixin;

public class PatientParameter implements BaseResourceParameter {

  @CommandLine.Option(
      names = {"--patient"},
      paramLabel = "<NAME>",
      type = NameWrapper.class,
      converter = NameConverter.class,
      description = "The name of the patient with format \"<givenName> <familyName>\"")
  private NameWrapper fullName;

  @CommandLine.Option(
      names = {"--birthdate"},
      paramLabel = "<DATE>",
      type = String.class,
      converter = GermanDateConverter.class,
      description = "The birthdate of the patient with format dd.MM.yyyy")
  private Date birhtDate;

  @Getter @Mixin private KvnrParameter kvnrParameter;

  private AssignerOrganization assignerOrganization;

  public AssignerOrganization getAssignerOrganization() {
    if (assignerOrganization == null) {
      // will be required only for PKV in old KBV-Profiles
      this.assignerOrganization =
          AssignerOrganizationBuilder.builder()
              .name(GemFaker.insuranceName())
              .iknr(IKNR.random())
              .phone(GemFaker.fakerPhone())
              .build();
    }
    return assignerOrganization;
  }

  public Date getBirthDate() {
    return this.getOrDefault(birhtDate, GemFaker::fakerBirthday);
  }

  public NameWrapper getFullName() {
    return this.getOrDefault(fullName, NameWrapper::randomName);
  }

  public KbvPatient createPatient() {
    val name = getFullName();
    return PatientBuilder.builder()
        .kvnr(kvnrParameter.getKvnr(), kvnrParameter.getInsuranceType())
        .name(name.getFirstName(), name.getLastName())
        .assigner(getAssignerOrganization()) // will be used only for PKV patients
        .birthDate(getBirthDate())
        .address(
            Country.D, GemFaker.fakerCity(), GemFaker.fakerZipCode(), GemFaker.fakerStreetName())
        .build();
  }
}
