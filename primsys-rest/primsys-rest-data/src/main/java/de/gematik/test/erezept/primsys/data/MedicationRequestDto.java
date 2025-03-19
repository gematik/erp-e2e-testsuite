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
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicationRequestDto {

  private String dosage;
  private Integer packageQuantity;
  private String note;
  private boolean substitutionAllowed; // aut-idem
  private boolean bvg; // Bundesversorgungsgesetz
  private boolean emergencyFee;
  @Nullable private MvoDto mvo;

  private MedicationRequestDto(Builder builder) {
    this.dosage = builder.dosage;
    this.packageQuantity = builder.packageQuantity;
    this.note = builder.note;
    this.substitutionAllowed = builder.substitutionAllowed;
    this.bvg = builder.bvg;
    this.emergencyFee = builder.emergencyFee;
    this.mvo = builder.mvo;
  }

  public static Builder medicationRequest() {
    return new Builder();
  }

  public static class Builder {
    private String dosage;
    private Integer packageQuantity;
    private String note = "";
    private boolean substitutionAllowed = true; // aut-idem
    private boolean bvg = true; // Bundesversorgungsgesetz
    private boolean emergencyFee = false;
    private MvoDto mvo;

    public Builder dosage(String dosage) {
      this.dosage = dosage;
      return this;
    }

    public Builder packageQuantity(Integer packageQuantity) {
      this.packageQuantity = packageQuantity;
      return this;
    }

    public Builder note(String note) {
      this.note = note;
      return this;
    }

    public Builder substitutionAllowed(boolean substitutionAllowed) {
      this.substitutionAllowed = substitutionAllowed;
      return this;
    }

    public Builder bvg(boolean bvg) {
      this.bvg = bvg;
      return this;
    }

    public Builder emergencyFee(boolean emergencyFee) {
      this.emergencyFee = emergencyFee;
      return this;
    }

    public Builder mvo(MvoDto mvo) {
      this.mvo = mvo;
      return this;
    }

    public MedicationRequestDto build() {
      return new MedicationRequestDto(this);
    }
  }
}
