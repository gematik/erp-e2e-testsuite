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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.fhir.r4.kbv;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.profiles.systems.KbvCodeSystem;
import de.gematik.test.erezept.fhir.r4.ErpFhirResource;
import de.gematik.test.erezept.fhir.values.AsvFachgruppennummer;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@ResourceDef(name = "Practitioner")
@SuppressWarnings({"java:S110"})
public class KbvPractitioner extends Practitioner implements ErpFhirResource {

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

  public String getFullName() {
    return this.getNameFirstRep().getNameAsSingleString();
  }

  public QualificationType getQualificationType() {
    return QualificationType.from(this.getQualification())
        .orElseThrow(
            () -> new MissingFieldException(this.getClass(), KbvCodeSystem.QUALIFICATION_TYPE));
  }

  public Optional<AsvFachgruppennummer> getAsvFachgruppennummer() {
    return AsvFachgruppennummer.from(this.getQualification());
  }

  public List<String> getAdditionalQualifications() {
    return this.getQualification().stream()
        .filter(qualification -> qualification.getCode().hasText())
        .map(qualification -> qualification.getCode().getText())
        .toList();
  }

  public Optional<BaseANR> getANR() {
    return this.getIdentifier().stream().filter(BaseANR::matches).map(BaseANR::from).findFirst();
  }

  @Override
  public String getDescription() {
    val qualificationType = this.getQualificationType();
    val anr =
        getANR()
            .map(it -> format("{0}: {1}", it.getType().name(), it.getValue()))
            .orElse("BSNR/LANR: n/a");

    return format("{0} {1} mit {2}", qualificationType.getDisplay(), getFullName(), anr);
  }
}
