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

package de.gematik.test.erezept.fhir.resources.kbv;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.*;
import de.gematik.test.erezept.fhir.exceptions.*;
import de.gematik.test.erezept.fhir.parser.profiles.systems.*;
import de.gematik.test.erezept.fhir.resources.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.hl7.fhir.r4.model.*;

@Slf4j
@Getter
@ResourceDef(name = "Coverage")
@SuppressWarnings({"java:S110"})
public class KbvCoverage extends Coverage implements ErpFhirResource {

  public VersicherungsArtDeBasis getInsuranceKind() {
    return this.getType().getCoding().stream()
        .filter(
            coding ->
                coding
                    .getSystem()
                    .equals(DeBasisCodeSystem.VERSICHERUNGSART_DE_BASIS.getCanonicalUrl()))
        .map(coding -> VersicherungsArtDeBasis.fromCode(coding.getCode()))
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    KbvCoverage.class, DeBasisCodeSystem.VERSICHERUNGSART_DE_BASIS));
  }

  public IKNR getIknr() {
    return this.getPayor().stream()
        .filter(
            p ->
                p.getIdentifier().getSystem().equals(DeBasisNamingSystem.IKNR.getCanonicalUrl())
                    || p.getIdentifier()
                        .getSystem()
                        .equals(DeBasisNamingSystem.IKNR_SID.getCanonicalUrl()))
        .map(p -> IKNR.from(p.getIdentifier().getValue()))
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    KbvCoverage.class, DeBasisNamingSystem.IKNR, DeBasisNamingSystem.IKNR_SID));
  }

  public String getName() {
    return this.getPayorFirstRep().getDisplay();
  }

  @Override
  public String getDescription() {
    val type = this.getInsuranceKind().getDisplay();
    val iknr = this.getIknr().getValue();
    return format("{0} {1} (IKNR: {2})", getName(), type, iknr);
  }

  public static KbvCoverage fromCoverage(Coverage adaptee) {
    if (adaptee instanceof KbvCoverage kbvCoverage) {
      return kbvCoverage;
    } else {
      val kbvCoverage = new KbvCoverage();
      adaptee.copyValues(kbvCoverage);
      return kbvCoverage;
    }
  }

  public static KbvCoverage fromCoverage(Resource adaptee) {
    return fromCoverage((Coverage) adaptee);
  }
}
