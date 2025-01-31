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

package de.gematik.test.erezept.fhir.resources.kbv;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.*;
import de.gematik.test.erezept.fhir.exceptions.*;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.DeBasisStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.*;
import de.gematik.test.erezept.fhir.resources.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.Optional;
import lombok.*;
import lombok.extern.slf4j.*;
import org.hl7.fhir.r4.model.*;

@Slf4j
@Getter
@ResourceDef(name = "Coverage")
@SuppressWarnings({"java:S110"})
public class KbvCoverage extends Coverage implements ErpFhirResource {

  public VersicherungsArtDeBasis getInsuranceKind() {
    return getInsuranceKindOptional()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    KbvCoverage.class, DeBasisCodeSystem.VERSICHERUNGSART_DE_BASIS));
  }

  public Optional<VersicherungsArtDeBasis> getInsuranceKindOptional() {
    return this.getType().getCoding().stream()
        .filter(coding -> DeBasisCodeSystem.VERSICHERUNGSART_DE_BASIS.match(coding.getSystem()))
        .map(coding -> VersicherungsArtDeBasis.fromCode(coding.getCode()))
        .findFirst();
  }

  public boolean hasInsuranceKind() {
    return getInsuranceKindOptional().isPresent();
  }

  public boolean hasPayorType() {
    return getPayorType().isPresent();
  }

  public Optional<PayorType> getPayorType() {
    return this.getType().getCoding().stream()
        .filter(coding -> PayorType.CODE_SYSTEM.match(coding.getSystem()))
        .map(coding -> PayorType.fromCode(coding.getCode()))
        .findFirst();
  }

  public IKNR getIknr() {
    return this.getPayor().stream()
        .filter(
            p ->
                DeBasisNamingSystem.IKNR_SID.match(p.getIdentifier())
                    || DeBasisNamingSystem.IKNR.match(p.getIdentifier()))
        .map(p -> IKNR.from(p.getIdentifier().getValue()))
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    KbvCoverage.class, DeBasisNamingSystem.IKNR, DeBasisNamingSystem.IKNR_SID));
  }

  public Optional<IKNR> getAlternativeIknr() {
    return this.getPayor().stream()
        .flatMap(p -> p.getIdentifier().getExtension().stream())
        .filter(KbvItaForStructDef.ALTERNATIVE_IK::match)
        .map(ext -> ext.castToIdentifier(ext.getValue()).getValue())
        .map(IKNR::from)
        .findFirst();
  }

  public String getName() {
    return this.getPayorFirstRep().getDisplay();
  }

  public Optional<Wop> getWop() {
    return this.getExtension().stream()
        .filter(DeBasisStructDef.GKV_WOP::match)
        .map(ext -> Wop.fromCode(ext.getValue().castToCoding(ext.getValue()).getCode()))
        .findFirst();
  }

  public Optional<VersichertenStatus> getInsurantStateOptional() {
    return this.getExtension().stream()
        .filter(DeBasisStructDef.GKV_VERSICHERTENART::match)
        .map(
            ext ->
                VersichertenStatus.fromCode(ext.getValue().castToCoding(ext.getValue()).getCode()))
        .findFirst();
  }

  public VersichertenStatus getInsurantState() {
    return getInsurantStateOptional()
        .orElseThrow(
            () -> new MissingFieldException(this.getClass(), DeBasisStructDef.GKV_VERSICHERTENART));
  }

  public Optional<PersonGroup> getPersonGroupOptional() {
    return this.getExtension().stream()
        .filter(DeBasisStructDef.GKV_PERSON_GROUP::match)
        .map(ext -> PersonGroup.fromCode(ext.getValue().castToCoding(ext.getValue()).getCode()))
        .findFirst();
  }

  public Optional<DmpKennzeichen> getDmpKennzeichenOptional() {
    return this.getExtension().stream()
        .filter(DeBasisStructDef.GKV_DMP_KENNZEICHEN::match)
        .map(ext -> DmpKennzeichen.fromCode(ext.getValue().castToCoding(ext.getValue()).getCode()))
        .findFirst();
  }

  public DmpKennzeichen getDmpKennzeichen() {
    return getDmpKennzeichenOptional()
        .orElseThrow(
            () -> new MissingFieldException(this.getClass(), DeBasisStructDef.GKV_DMP_KENNZEICHEN));
  }

  public PersonGroup getPersonGroup() {
    return getPersonGroupOptional()
        .orElseThrow(
            () -> new MissingFieldException(this.getClass(), DeBasisStructDef.GKV_PERSON_GROUP));
  }

  @Override
  public String getDescription() {
    val type =
        this.getInsuranceKindOptional()
            .map(VersicherungsArtDeBasis::getDisplay)
            .or(() -> this.getPayorType().map(PayorType::getDisplay))
            .orElse("N/A");
    val iknr = this.getIknr().getValue();
    return format("{1} ''{0}'' (IKNR: {2})", getName(), type, iknr);
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
