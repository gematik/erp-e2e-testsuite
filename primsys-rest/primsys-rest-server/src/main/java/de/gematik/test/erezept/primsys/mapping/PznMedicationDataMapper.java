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

package de.gematik.test.erezept.primsys.mapping;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNBuilder;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import de.gematik.test.erezept.primsys.data.PznMedicationDto;
import de.gematik.test.erezept.primsys.data.valuesets.MedicationCategoryDto;
import de.gematik.test.erezept.primsys.data.valuesets.MedicationTypeDto;
import de.gematik.test.erezept.primsys.data.valuesets.StandardSizeDto;
import de.gematik.test.erezept.primsys.data.valuesets.SupplyFormDto;
import lombok.val;

public class PznMedicationDataMapper extends DataMapper<PznMedicationDto, KbvErpMedication> {

  public PznMedicationDataMapper(PznMedicationDto dto) {
    super(dto);
  }

  protected void complete() {
    ensure(dto::getCategory, dto::setCategory, () -> fakerValueSet(MedicationCategoryDto.class));

    ensure(dto::getName, dto::setName, GemFaker::fakerDrugName);

    ensure(dto::getType, dto::setType, () -> MedicationTypeDto.PZN);
    if (dto.getType().equals(MedicationTypeDto.PZN)) {
      ensure(
          dto::getStandardSize, dto::setStandardSize, () -> fakerValueSet(StandardSizeDto.class));
      ensure(
          dto::getSupplyForm,
          dto::setSupplyForm,
          () -> fakerValueSet(SupplyFormDto.class, SupplyFormDto.LYO));
      ensure(dto::getPzn, dto::setPzn, () -> PZN.random().getValue());
      ensure(dto::getAmount, dto::setAmount, () -> fakerAmount(1, 20));
      ensure(dto::getAmountUnit, dto::setAmountUnit, () -> "Stk");
    }
  }

  @Override
  protected KbvErpMedication convertInternal() {
    return KbvErpMedicationPZNBuilder.builder()
        .isVaccine(dto.isVaccine())
        .normgroesse(this.getStandardSize())
        .darreichungsform(this.getDarreichungsform())
        .amount(dto.getAmount(), dto.getAmountUnit())
        .pzn(dto.getPzn(), dto.getName())
        .category(MedicationCategory.C_00)
        .build();
  }

  private StandardSize getStandardSize() {
    return StandardSize.fromCode(dto.getStandardSize().getCode());
  }

  private Darreichungsform getDarreichungsform() {
    return Darreichungsform.fromCode(dto.getSupplyForm().getCode());
  }

  public static PznMedicationDataMapper from(KbvErpMedication medication) {
    val dto = new PznMedicationDto();
    dto.setVaccine(medication.isVaccine());
    dto.setStandardSize(StandardSizeDto.fromCode(medication.getStandardSize().getCode()));
    medication
        .getDarreichungsformFirstRep()
        .ifPresent(df -> dto.setSupplyForm(SupplyFormDto.fromCode(df.getCode())));
    medication.getPackagingAmount().ifPresent(dto::setAmount);
    medication.getPackagingUnit().ifPresent(dto::setAmountUnit);
    medication.getPznOptional().ifPresent(pzn -> dto.setPzn(pzn.getValue()));
    dto.setName(medication.getMedicationName());
    dto.setCategory(MedicationCategoryDto.fromCode(medication.getCategoryFirstRep().getCode()));

    val typeDto =
        medication
            .getMedicationType()
            .map(type -> MedicationTypeDto.fromCode(type.getCode()))
            .orElse(MedicationTypeDto.PZN);
    dto.setType(typeDto);

    return from(dto);
  }

  public static PznMedicationDataMapper from(PznMedicationDto dto) {
    return new PznMedicationDataMapper(dto);
  }

  public static PznMedicationDataMapper random() {
    return from(randomDto());
  }

  public static PznMedicationDto randomDto() {
    return new PznMedicationDto();
  }
}
