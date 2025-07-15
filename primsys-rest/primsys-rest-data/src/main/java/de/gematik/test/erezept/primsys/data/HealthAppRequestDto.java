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
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HealthAppRequestDto {

  private String pzn;
  private String name;
  private boolean ser; // soziales Entschädigungsrecht

  private HealthAppRequestDto(Builder builder) {
    this.pzn = builder.pzn;
    this.name = builder.name;
    this.ser = builder.ser;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String pzn;
    private String name;
    private boolean ser; // soziales Entschädigungsrecht

    public Builder pzn(String pzn) {
      this.pzn = pzn;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder relatesToSocialCompensationLaw(boolean ser) {
      return ser(ser);
    }

    public Builder ser(boolean ser) {
      this.ser = ser;
      return this;
    }

    public HealthAppRequestDto build() {
      return new HealthAppRequestDto(this);
    }
  }
}
