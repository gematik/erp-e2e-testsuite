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

package de.gematik.test.erezept.fhir.resources.kbv;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.ErpCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.ErpNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.StructureDefinitionFixedUrls;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@ResourceDef(name = "Practitioner", profile = StructureDefinitionFixedUrls.KBV_PR_FOR_PRACTITIONER)
@SuppressWarnings({"java:S110"})
public class KbvPractitioner extends Practitioner {

  public String getFullName() {
    return this.getNameFirstRep().getNameAsSingleString();
  }

  public QualificationType getQualificationType() {
    return this.getQualification().stream()
        .filter(
            quali ->
                quali
                    .getCode()
                    .getCodingFirstRep()
                    .getSystem()
                    .equals(ErpCodeSystem.QUALIFICATION_TYPE.getCanonicalUrl()))
        .map(quali -> QualificationType.fromCode(quali.getCode().getCodingFirstRep().getCode()))
        .findFirst()
        .orElseThrow(
            () -> new MissingFieldException(this.getClass(), ErpCodeSystem.QUALIFICATION_TYPE));
  }

  public List<String> getAdditionalQualifications() {
    return this.getQualification().stream()
        .filter(
            quali -> (quali.getCode().getText() != null) && !quali.getCode().getText().isEmpty())
        .map(quali -> quali.getCode().getText())
        .collect(Collectors.toList());
  }

  public BaseANR getANR() {
    return this.identifier.stream()
        .filter(
            identifier ->
                identifier.getSystem().equals(ErpNamingSystem.KBV_NS_BASE_ANR.getCanonicalUrl())
                    || identifier
                        .getSystem()
                        .equals(ErpNamingSystem.ZAHNARZTNUMMER.getCanonicalUrl()))
        .map(BaseANR::fromIdentifier)
        .findFirst()
        .orElseThrow(
            () -> new MissingFieldException(this.getClass(), "Doctors Number LANR or ZANR"));
  }

  public BaseANR.ANRType getANRType() {
    return this.getANR().getType();
  }

  public static KbvPractitioner fromPractitioner(Practitioner adaptee) {
    val kbvPractitioner = new KbvPractitioner();
    adaptee.copyValues(kbvPractitioner);
    return kbvPractitioner;
  }

  public static KbvPractitioner fromPractitioner(Resource adaptee) {
    return fromPractitioner((Practitioner) adaptee);
  }
}
