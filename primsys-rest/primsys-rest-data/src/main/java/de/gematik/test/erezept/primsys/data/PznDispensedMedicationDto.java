/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.primsys.data;

import java.util.Date;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PznDispensedMedicationDto extends PznMedicationDto {

  private PznMedicationBatchDto batch;

  public static Builder dispensed(PznMedicationDto medication) {
    return new Builder(medication);
  }

  @RequiredArgsConstructor
  public static class Builder {
    private final PznMedicationDto medication;

    public PznDispensedMedicationDto withBatchInfo(String lotNumber, Date expiryDate) {
      val batch = new PznMedicationBatchDto(lotNumber, expiryDate);
      val dto = new PznDispensedMedicationDto(batch);
      dto.setCategory(medication.getCategory());
      dto.setVaccine(medication.isVaccine());
      dto.setStandardSize(medication.getStandardSize());
      dto.setSupplyForm(medication.getSupplyForm());
      dto.setAmount(medication.getAmount());
      dto.setAmountUnit(medication.getAmountUnit());
      dto.setPzn(medication.getPzn());
      dto.setName(medication.getName());
      
      return dto;
    }
  }
}
