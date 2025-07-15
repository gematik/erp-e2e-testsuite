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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.gematik.test.erezept.primsys.data.valuesets.PatientInsuranceTypeDto;
import lombok.*;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AcceptedPrescriptionDto {

  private String prescriptionId;
  private String prescriptionReference;
  private String forKvnr;
  private PatientInsuranceTypeDto patientInsuranceType;
  private String accessCode;
  private String secret;
  private CoverageDto insurance;
  private PznMedicationDto medication;
  private HealthAppRequestDto diga;

  public static Builder withPrescriptionId(String prescriptionId) {
    return new Builder(prescriptionId);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final String prescriptionId;
    private String prescriptionReference;
    private String forKvnr;
    private PatientInsuranceTypeDto patientInsuranceType;
    private String accessCode;
    private String secret;
    private CoverageDto insurance;

    public Builder forKvnr(String kvnr, PatientInsuranceTypeDto patientInsuranceType) {
      this.forKvnr = kvnr;
      this.patientInsuranceType = patientInsuranceType;
      return this;
    }

    public Builder withAccessCode(String accessCode) {
      this.accessCode = accessCode;
      return this;
    }

    public Builder withSecret(String secret) {
      this.secret = secret;
      return this;
    }

    public Builder coveredBy(CoverageDto insurance) {
      this.insurance = insurance;
      return this;
    }

    public Builder prescriptionReference(String reference) {
      this.prescriptionReference = reference;
      return this;
    }

    public AcceptedPrescriptionDto andMedication(PznMedicationDto medication) {
      val apdto = preBuildAcceptData();
      apdto.medication = medication;
      return apdto;
    }

    public AcceptedPrescriptionDto andDiGA(HealthAppRequestDto diga) {
      val apdto = preBuildAcceptData();
      apdto.diga = diga;
      return apdto;
    }

    private AcceptedPrescriptionDto preBuildAcceptData() {
      val apdto = new AcceptedPrescriptionDto();
      apdto.prescriptionId = this.prescriptionId;
      apdto.prescriptionReference = this.prescriptionReference;
      apdto.forKvnr = this.forKvnr;
      apdto.patientInsuranceType = this.patientInsuranceType;
      apdto.accessCode = this.accessCode;
      apdto.secret = this.secret;
      apdto.insurance = this.insurance;
      return apdto;
    }
  }
}
