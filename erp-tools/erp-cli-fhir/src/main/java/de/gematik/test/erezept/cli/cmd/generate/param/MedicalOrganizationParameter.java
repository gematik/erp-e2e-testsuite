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

package de.gematik.test.erezept.cli.cmd.generate.param;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.builder.*;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import lombok.*;
import picocli.*;

@Getter
public class MedicalOrganizationParameter implements BaseResourceParameter {

  @CommandLine.Option(
      names = {"--org", "--organization"},
      paramLabel = "<NAME>",
      type = String.class,
      description = "The name of the medical organization")
  private String medOrgName;

  @CommandLine.Option(
      names = {"--bsnr"},
      paramLabel = "<BSNR>",
      type = String.class,
      description =
          "BSNR (Betriebsstättennummer) is a unique 9-digit number which identifies the medical organization")
  private String bsnr;

  public void changeOrganizationName(String medOrgName) {
    this.medOrgName = medOrgName;
  }

  public boolean hasCLIOrganizationName() {
    return medOrgName != null && !medOrgName.isEmpty() && !medOrgName.isBlank();
  }

  public String getOrganizationName() {
    return this.getOrDefault(medOrgName, () -> format("Arztpraxis {0}", GemFaker.fakerLastName()));
  }

  public String getBsnr() {
    return this.getOrDefault(bsnr, GemFaker::fakerBsnr);
  }

  public MedicalOrganization createMedicalOrganization() {
    return MedicalOrganizationBuilder.builder()
        .name(getOrganizationName())
        .bsnr(getBsnr())
        .phone(GemFaker.fakerPhone())
        .email(GemFaker.fakerEMail())
        .address(
            Country.D, GemFaker.fakerCity(), GemFaker.fakerZipCode(), GemFaker.fakerStreetName())
        .build();
  }
}
