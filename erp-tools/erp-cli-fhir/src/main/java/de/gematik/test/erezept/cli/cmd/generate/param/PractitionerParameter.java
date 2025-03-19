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
 */

package de.gematik.test.erezept.cli.cmd.generate.param;

import de.gematik.test.erezept.cli.converter.NameConverter;
import de.gematik.test.erezept.cli.util.*;
import de.gematik.test.erezept.fhir.builder.*;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.r4.kbv.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import lombok.*;
import picocli.*;

@Getter
public class PractitionerParameter implements BaseResourceParameter {

  @CommandLine.Option(
      names = {"--doc", "--doctor"},
      paramLabel = "<NAME>",
      type = NameWrapper.class,
      converter = NameConverter.class,
      description = "The name of the practitioner doctor with format \"<givenName> <familyName>\"")
  private NameWrapper fullName;

  @CommandLine.Option(
      names = {"--anr"},
      paramLabel = "<NUM>",
      type = String.class,
      description =
          "Define a LANR/ZANR (depending on the qualification type) value to the practitioner"
              + " doctor (default=random)")
  private String anrValue;

  @CommandLine.Option(
      names = {"--qualification"},
      paramLabel = "<TYPE>",
      type = QualificationType.class,
      description =
          "The qualification type of the practitioner doctor from ${COMPLETION-CANDIDATES}"
              + " (default=random)")
  private QualificationType qualificationType;

  public BaseANR getANR() {
    return (anrValue != null)
        ? BaseANR.forQualification(getQualificationType(), anrValue)
        : BaseANR.randomFromQualification(getQualificationType());
  }

  public QualificationType getQualificationType() {
    this.qualificationType =
        this.getOrDefault(
            qualificationType,
            () -> GemFaker.randomElement(QualificationType.DOCTOR, QualificationType.DENTIST));
    return qualificationType;
  }

  public NameWrapper getFullName() {
    this.fullName = this.getOrDefault(fullName, NameWrapper::randomName);
    return fullName;
  }

  public KbvPractitioner createPractitioner() {
    val name = getFullName();
    return KbvPractitionerBuilder.builder()
        .anr(getANR())
        .name(name.getFirstName(), name.getLastName(), "Dr.")
        .addQualification(getQualificationType())
        .addQualification(GemFaker.randomElement(DoctorProfession.values()).getNaming())
        .build();
  }
}
