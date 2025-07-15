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

package de.gematik.test.erezept.primsys.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PznDispensedMedicationDto extends PznMedicationDto {

  private PznMedicationBatchDto batch;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
  private Date whenHandedOver;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
  private Date whenPrepared;

  private List<String> dosageInstructions = new LinkedList<>();
  private List<String> notes = new LinkedList<>();

  public static Builder dispensed(PznMedicationDto medication) {
    return new Builder(medication);
  }

  @RequiredArgsConstructor
  public static class Builder {
    private final PznMedicationDto medication;

    public PznDispensedMedicationDto withBatchInfo(String lotNumber, Date expiryDate) {
      val batch = new PznMedicationBatchDto(lotNumber, expiryDate);
      val dto = new PznDispensedMedicationDto();
      dto.setBatch(batch);
      dto.setCategory(medication.getCategory());
      dto.setVaccine(medication.isVaccine());
      dto.setStandardSize(medication.getStandardSize());
      dto.setSupplyForm(medication.getSupplyForm());
      dto.setPackagingSize(medication.getPackagingSize());
      dto.setPackagingUnit(medication.getPackagingUnit());
      dto.setPzn(medication.getPzn());
      dto.setName(medication.getName());
      dto.setWhenHandedOver(new Date());
      dto.setWhenPrepared(new Date());
      dto.setDosageInstructions(new LinkedList<>());
      dto.setNotes(new LinkedList<>());

      return dto;
    }
  }
}
