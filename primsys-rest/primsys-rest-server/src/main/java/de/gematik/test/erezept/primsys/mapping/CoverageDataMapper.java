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

package de.gematik.test.erezept.primsys.mapping;

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerValueSet;

import de.gematik.bbriccs.fhir.coding.SemanticValue;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.builder.kbv.KbvCoverageBuilder;
import de.gematik.test.erezept.fhir.r4.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.values.InsuranceCoverageInfo;
import de.gematik.test.erezept.fhir.valuesets.DmpKennzeichen;
import de.gematik.test.erezept.fhir.valuesets.PersonGroup;
import de.gematik.test.erezept.fhir.valuesets.VersichertenStatus;
import de.gematik.test.erezept.fhir.valuesets.Wop;
import de.gematik.test.erezept.primsys.data.CoverageDto;
import de.gematik.test.erezept.primsys.data.valuesets.*;
import lombok.val;

public class CoverageDataMapper extends DataMapper<CoverageDto, KbvCoverage> {

  private final KbvPatient beneficiary;

  public CoverageDataMapper(CoverageDto dto, KbvPatient beneficiary) {
    super(dto);
    this.beneficiary = beneficiary;
    this.completeCoverage();
  }

  @Override
  protected void complete() {
    ensure(
        dto::getWop,
        dto::setWop,
        () -> WopDto.fromCode(fakerValueSet(Wop.class, Wop.DUMMY).getCode()));
    ensure(
        dto::getInsurantState,
        dto::setInsurantState,
        () -> InsurantStateDto.fromCode(fakerValueSet(VersichertenStatus.class).getCode()));
    ensure(
        dto::getPersonGroup,
        dto::setPersonGroup,
        () -> PersonGroupDto.fromCode(fakerValueSet(PersonGroup.class).getCode()));
  }

  /**
   * This method is required here because the beneficiary cannot be accessed in this.complete()
   * during the constructor call!
   */
  private void completeCoverage() {
    val coverageInfo = InsuranceCoverageInfo.randomFor(beneficiary.getInsuranceType());
    ensure(dto::getIknr, dto::setIknr, coverageInfo::getIknr);
    ensure(dto::getName, dto::setName, coverageInfo::getName);
    if (dto.getPayorType() == null) {
      ensure(
          dto::getInsuranceType,
          dto::setInsuranceType,
          () -> InsuranceTypeDto.fromCode(coverageInfo.getInsuranceType().getCode()));
    }
  }

  @Override
  protected KbvCoverage convertInternal() {
    return KbvCoverageBuilder.insurance(dto.getIknr(), dto.getName())
        .beneficiary(beneficiary)
        .personGroup(getPersonGroup())
        .dmpKennzeichen(DmpKennzeichen.NOT_SET) // NOT SET YET
        .wop(getWop())
        .versichertenStatus(getInsurantState())
        .insuranceType(getInsuranceType())
        .build();
  }

  private PersonGroup getPersonGroup() {
    return PersonGroup.fromCode(dto.getPersonGroup().getCode());
  }

  private Wop getWop() {
    return Wop.fromCode(dto.getWop().getCode());
  }

  private VersichertenStatus getInsurantState() {
    return VersichertenStatus.fromCode(dto.getInsurantState().getCode());
  }

  private InsuranceTypeDe getInsuranceType() {
    return InsuranceTypeDe.fromCode(dto.getInsuranceType().getCode());
  }

  public static CoverageDataMapper from(KbvCoverage coverage, KbvPatient beneficiary) {
    val dto = new CoverageDto();
    coverage.getIknr().map(SemanticValue::getValue).ifPresent(dto::setIknr);
    coverage.getWop().ifPresent(wop -> dto.setWop(WopDto.fromCode(wop.getCode())));
    dto.setName(coverage.getName());
    coverage
        .getInsurantStateOptional()
        .ifPresent(state -> dto.setInsurantState(InsurantStateDto.fromCode(state.getCode())));
    coverage
        .getInsuranceKindOptional()
        .ifPresent(it -> dto.setInsuranceType(InsuranceTypeDto.fromCode(it.getCode())));
    coverage.getPayorType().ifPresent(pt -> dto.setPayorType(PayorTypeDto.fromCode(pt.getCode())));
    coverage
        .getPersonGroupOptional()
        .ifPresent(group -> dto.setPersonGroup(PersonGroupDto.fromCode(group.getCode())));

    return from(dto, beneficiary);
  }

  public static CoverageDataMapper from(CoverageDto dto, KbvPatient beneficiary) {
    return new CoverageDataMapper(dto, beneficiary);
  }

  public static CoverageDataMapper randomFor(KbvPatient beneficiary) {
    return new CoverageDataMapper(new CoverageDto(), beneficiary);
  }

  public static CoverageDto randomDto() {
    val insuranceType = fakerValueSet(InsuranceTypeDe.class);
    val insurance = InsuranceCoverageInfo.randomFor(insuranceType);
    return CoverageDto.ofType(InsuranceTypeDto.fromCode(insuranceType.getCode()))
        .personGroup(fakerValueSet(PersonGroupDto.class))
        .insurantState(fakerValueSet(InsurantStateDto.class))
        .resident(fakerValueSet(WopDto.class))
        .named(insurance.getName())
        .withIknr(insurance.getIknr())
        .build();
  }
}
