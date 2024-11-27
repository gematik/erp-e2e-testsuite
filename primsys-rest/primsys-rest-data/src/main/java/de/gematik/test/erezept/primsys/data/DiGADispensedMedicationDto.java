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

package de.gematik.test.erezept.primsys.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Date;
import java.util.Optional;
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
public class DiGADispensedMedicationDto extends PznMedicationDto {

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
  private Date whenHandedOver;

  private String deepLink;
  private String redeemCode;

  public static Builder dispensed(PznMedicationDto medication) {
    return new Builder(medication);
  }

  @RequiredArgsConstructor
  public static class Builder {
    private final PznMedicationDto medication;
    private String deepLink;
    private String redeemCode;

    public Builder withRedeemCode(String redeemCode) {
      this.redeemCode = redeemCode;
      return this;
    }

    public Builder withDeepLink(String deepLink) {
      this.deepLink = deepLink;
      return this;
    }

    public DiGADispensedMedicationDto build() {
      val dto = new DiGADispensedMedicationDto();
      dto.setCategory(medication.getCategory());
      dto.setVaccine(medication.isVaccine());
      dto.setStandardSize(medication.getStandardSize());
      dto.setSupplyForm(medication.getSupplyForm());
      dto.setAmount(medication.getAmount());
      dto.setAmountUnit(medication.getAmountUnit());
      dto.setPzn(medication.getPzn());
      dto.setName(medication.getName());
      dto.setWhenHandedOver(new Date());

      Optional.ofNullable(this.deepLink).ifPresent(dto::setDeepLink);
      Optional.ofNullable(this.redeemCode).ifPresent(dto::setRedeemCode);

      return dto;
    }
  }
}
