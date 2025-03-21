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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrescribeEvdgaRequestDto {

  private PatientDto patient;
  private CoverageDto coverage;
  private HealthAppRequestDto healthAppRequest;

  public static Builder forKvnr(String kvnr) {
    return forPatient(PatientDto.withKvnr(kvnr).build());
  }

  public static Builder forPatient(PatientDto patient) {
    return new Builder(patient);
  }

  @RequiredArgsConstructor
  public static class Builder {
    private final PatientDto patient;
    private CoverageDto coverage;

    public Builder coveredBy(CoverageDto coverage) {
      this.coverage = coverage;
      return this;
    }

    public PrescribeEvdgaRequestDto healthAppRequest(HealthAppRequestDto healthAppRequest) {
      return new PrescribeEvdgaRequestDto(patient, coverage, healthAppRequest);
    }
  }
}
