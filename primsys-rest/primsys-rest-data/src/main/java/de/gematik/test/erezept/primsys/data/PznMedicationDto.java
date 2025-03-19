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

package de.gematik.test.erezept.primsys.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.gematik.test.erezept.primsys.data.valuesets.MedicationCategoryDto;
import de.gematik.test.erezept.primsys.data.valuesets.MedicationTypeDto;
import de.gematik.test.erezept.primsys.data.valuesets.StandardSizeDto;
import de.gematik.test.erezept.primsys.data.valuesets.SupplyFormDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PznMedicationDto {

  private MedicationTypeDto type = MedicationTypeDto.PZN;
  private MedicationCategoryDto category;
  private boolean vaccine = false;
  private StandardSizeDto standardSize;
  private SupplyFormDto supplyForm;
  private String packagingSize;
  private String packagingUnit;
  private String pzn;
  private String name;

  public static Builder medicine(String pzn, String name) {
    return new Builder(MedicationCategoryDto.C_00, pzn, name);
  }

  @RequiredArgsConstructor
  public static class Builder {
    private final MedicationCategoryDto category;
    private boolean vaccine = false;
    private StandardSizeDto standardSize;
    private SupplyFormDto supplyForm;
    private String packagingSize;
    private String packagingUnit;
    private final String pzn;
    private final String name;

    public Builder isVaccine(boolean vaccine) {
      this.vaccine = vaccine;
      return this;
    }

    public Builder standardSize(StandardSizeDto standardSize) {
      this.standardSize = standardSize;
      return this;
    }

    public Builder supplyForm(SupplyFormDto supplyForm) {
      this.supplyForm = supplyForm;
      return this;
    }

    public Builder amount(String packagingSize, String packagingUnit) {
      this.packagingSize = packagingSize;
      this.packagingUnit = packagingUnit;
      return this;
    }

    public PznMedicationDto asPrescribed() {
      return new PznMedicationDto(
          MedicationTypeDto.PZN,
          category,
          vaccine,
          standardSize,
          supplyForm,
          packagingSize,
          packagingUnit,
          pzn,
          name);
    }
  }
}
