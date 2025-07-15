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

import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.eml.fhir.valuesets.EpaDrugCategory;
import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationPZNBuilderORIGINAL_BUILDER;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import de.gematik.test.erezept.primsys.data.PznDispensedMedicationDto;
import de.gematik.test.erezept.primsys.data.PznMedicationBatchDto;
import de.gematik.test.erezept.primsys.data.valuesets.MedicationCategoryDto;
import de.gematik.test.erezept.primsys.data.valuesets.MedicationTypeDto;
import de.gematik.test.erezept.primsys.data.valuesets.StandardSizeDto;
import de.gematik.test.erezept.primsys.data.valuesets.SupplyFormDto;
import lombok.val;

public class GemErpMedicationDataMapper
    extends DataMapper<PznDispensedMedicationDto, GemErpMedication> {

  public GemErpMedicationDataMapper(PznDispensedMedicationDto dto) {
    super(dto);
  }

  protected void complete() {
    // Medication code, name, or ingredients must be specified
    // for now we are only dealing with PZN medications
    ensure(dto::getPzn, dto::setPzn, () -> PZN.random().getValue());
  }

  @Override
  protected GemErpMedication convertInternal() {
    val builder = GemErpMedicationPZNBuilderORIGINAL_BUILDER.builder();

    builder.isVaccine(dto.isVaccine());
    this.setIfPresent(
        dto::getStandardSize,
        dtoSize -> builder.normgroesse(StandardSize.fromCode(dtoSize.getCode())));
    this.setIfPresent(
        dto::getSupplyForm,
        dtoForm -> builder.darreichungsform(Darreichungsform.fromCode(dtoForm.getCode())));
    this.setIfPresent(
        dto::getPackagingSize,
        dtoPackagingSize -> builder.packaging(dtoPackagingSize + " " + dto.getPackagingUnit()));
    this.setIfPresent(dto::getPzn, dtoPzn -> builder.pzn(PZN.from(dtoPzn), dto.getName()));
    this.setIfPresent(
        dto::getCategory,
        dtoCategory -> builder.category(EpaDrugCategory.fromCode(dtoCategory.getCode())));
    this.setIfPresent(dto::getBatch, dtoBatch -> builder.lotNumber(dtoBatch.getLotNumber()));

    return builder.build();
  }

  public static GemErpMedicationDataMapper from(GemErpMedication medication) {
    val dto = new PznDispensedMedicationDto();
    dto.setVaccine(medication.isVaccine());
    medication
        .getStandardSize()
        .ifPresent(size -> dto.setStandardSize(StandardSizeDto.fromCode(size.getCode())));
    medication
        .getDarreichungsform()
        .ifPresent(df -> dto.setSupplyForm(SupplyFormDto.fromCode(df.getCode())));
    medication
        .getAmountNumerator()
        .ifPresent(amount -> dto.setPackagingSize(String.valueOf(amount)));
    medication.getAmountNumeratorUnit().ifPresent(dto::setPackagingUnit);
    medication.getPzn().ifPresent(pzn -> dto.setPzn(pzn.getValue()));
    medication.getNameFromCodeOreContainedRessource().ifPresent(dto::setName);
    medication
        .getBatchLotNumber()
        .ifPresent(
            batchLotNum -> {
              val batch = new PznMedicationBatchDto();
              batch.setLotNumber(batchLotNum);
              dto.setBatch(batch);
            });
    medication
        .getCategory()
        .ifPresent(category -> dto.setCategory(MedicationCategoryDto.fromCode(category.getCode())));

    // by default for now
    dto.setType(MedicationTypeDto.UNDEFINED);

    return from(dto);
  }

  public static GemErpMedicationDataMapper from(PznDispensedMedicationDto dto) {
    return new GemErpMedicationDataMapper(dto);
  }

  public static GemErpMedicationDataMapper random() {
    return from(randomDto());
  }

  public static PznDispensedMedicationDto randomDto() {
    return new PznDispensedMedicationDto();
  }
}
