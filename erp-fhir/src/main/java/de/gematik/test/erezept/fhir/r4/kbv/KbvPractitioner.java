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

package de.gematik.test.erezept.fhir.r4.kbv;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvCodeSystem;
import de.gematik.test.erezept.fhir.r4.ErpFhirResource;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@ResourceDef(name = "Practitioner")
@SuppressWarnings({"java:S110"})
public class KbvPractitioner extends Practitioner implements ErpFhirResource {

  public String getFullName() {
    return this.getNameFirstRep().getNameAsSingleString();
  }

  public QualificationType getQualificationType() {
    return this.getQualification().stream()
        .map(PractitionerQualificationComponent::getCode)
        .filter(KbvCodeSystem.QUALIFICATION_TYPE::matches)
        .map(
            qualification ->
                QualificationType.fromCode(qualification.getCodingFirstRep().getCode()))
        .findFirst()
        .orElseThrow(
            () -> new MissingFieldException(this.getClass(), KbvCodeSystem.QUALIFICATION_TYPE));
  }

  public List<String> getAdditionalQualifications() {
    return this.getQualification().stream()
        .filter(qualification -> qualification.getCode().hasText())
        .map(qualification -> qualification.getCode().getText())
        .toList();
  }

  public BaseANR getANR() {
    return this.identifier.stream()
        .filter(BaseANR::hasValidIdentifier)
        .map(BaseANR::fromIdentifier)
        .findFirst()
        .orElseThrow(
            () -> new MissingFieldException(this.getClass(), "Doctors Number LANR or ZANR"));
  }

  public BaseANR.ANRType getANRType() {
    return this.getANR().getType();
  }

  @Override
  public String getDescription() {
    val qualificationType = getQualificationType();
    val anr = getANR();

    return format(
        "{0} {1} mit {2} {3}",
        qualificationType.getDisplay(), getFullName(), anr.getType(), anr.getValue());
  }

  public static KbvPractitioner fromPractitioner(Practitioner adaptee) {
    if (adaptee instanceof KbvPractitioner kbvPractitioner) {
      return kbvPractitioner;
    } else {
      val kbvPractitioner = new KbvPractitioner();
      adaptee.copyValues(kbvPractitioner);
      return kbvPractitioner;
    }
  }

  public static KbvPractitioner fromPractitioner(Resource adaptee) {
    return fromPractitioner((Practitioner) adaptee);
  }
}
