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
import de.gematik.bbriccs.fhir.coding.WithSystem;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.DeBasisProfilStructDef;
import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.profiles.systems.KbvCodeSystem;
import de.gematik.test.erezept.fhir.r4.ErpFhirResource;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;

@Slf4j
@Getter
@ResourceDef(name = "Coverage")
@SuppressWarnings({"java:S110"})
public class KbvCoverage extends Coverage implements ErpFhirResource {

  @Override
  public Reference asReference() {
    return ErpFhirResource.createReference(ResourceType.Coverage, getId());
  }

  public Reference asReferenceWithDisplay() {
    val display = this.getPayorFirstRep().getDisplay();
    return asReference().setDisplay(display);
  }

  public InsuranceTypeDe getInsuranceKind() {
    return getInsuranceKindOptional()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    KbvCoverage.class, DeBasisProfilCodeSystem.VERSICHERUNGSART_DE_BASIS));
  }

  public Optional<InsuranceTypeDe> getInsuranceKindOptional() {
    return this.getType().getCoding().stream()
        .filter(DeBasisProfilCodeSystem.VERSICHERUNGSART_DE_BASIS::matches)
        .map(InsuranceTypeDe::fromCode)
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
        .filter(KbvCodeSystem.PAYOR_TYPE::matches)
        .map(PayorType::fromCode)
        .findFirst();
  }

  public IKNR getIknrOrThrow() {
    return this.getIknr()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    KbvCoverage.class,
                    DeBasisProfilNamingSystem.IKNR,
                    DeBasisProfilNamingSystem.IKNR_SID));
  }

  public Optional<IKNR> getIknr() {
    return this.getPayor().stream()
        .filter(
            reference ->
                WithSystem.anyOf(DeBasisProfilNamingSystem.IKNR_SID, DeBasisProfilNamingSystem.IKNR)
                    .matchesReferenceIdentifier(reference))
        .map(reference -> IKNR.from(reference.getIdentifier()))
        .findFirst();
  }

  public Optional<IKNR> getAlternativeIknr() {
    return this.getPayor().stream()
        .flatMap(p -> p.getIdentifier().getExtension().stream())
        .filter(KbvItaForStructDef.ALTERNATIVE_IK::matches)
        .map(ext -> ext.castToIdentifier(ext.getValue()))
        .map(IKNR::from)
        .findFirst();
  }

  public String getName() {
    return this.getPayorFirstRep().getDisplay();
  }

  public Optional<Wop> getWop() {
    return this.getExtension().stream()
        .filter(DeBasisProfilStructDef.GKV_WOP::matches)
        .map(ext -> Wop.fromCode(ext.getValue().castToCoding(ext.getValue()).getCode()))
        .findFirst();
  }

  public Optional<VersichertenStatus> getInsurantStateOptional() {
    return this.getExtension().stream()
        .filter(DeBasisProfilStructDef.GKV_VERSICHERTENART::matches)
        .map(
            ext ->
                VersichertenStatus.fromCode(ext.getValue().castToCoding(ext.getValue()).getCode()))
        .findFirst();
  }

  public VersichertenStatus getInsurantState() {
    return getInsurantStateOptional()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    this.getClass(), DeBasisProfilStructDef.GKV_VERSICHERTENART));
  }

  public Optional<PersonGroup> getPersonGroupOptional() {
    return this.getExtension().stream()
        .filter(DeBasisProfilStructDef.GKV_PERSON_GROUP::matches)
        .map(ext -> PersonGroup.fromCode(ext.getValue().castToCoding(ext.getValue()).getCode()))
        .findFirst();
  }

  public Optional<DmpKennzeichen> getDmpKennzeichenOptional() {
    return this.getExtension().stream()
        .filter(DeBasisProfilStructDef.GKV_DMP_KENNZEICHEN::matches)
        .map(ext -> DmpKennzeichen.fromCode(ext.getValue().castToCoding(ext.getValue()).getCode()))
        .findFirst();
  }

  public DmpKennzeichen getDmpKennzeichen() {
    return getDmpKennzeichenOptional()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    this.getClass(), DeBasisProfilStructDef.GKV_DMP_KENNZEICHEN));
  }

  public PersonGroup getPersonGroup() {
    return getPersonGroupOptional()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    this.getClass(), DeBasisProfilStructDef.GKV_PERSON_GROUP));
  }

  @Override
  public String getDescription() {
    val type =
        this.getInsuranceKindOptional()
            .map(InsuranceTypeDe::getDisplay)
            .or(() -> this.getPayorType().map(PayorType::getDisplay))
            .orElse("N/A");
    val iknr = this.getIknrOrThrow().getValue();
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
