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

import com.fasterxml.jackson.annotation.JsonInclude;
import de.gematik.test.erezept.primsys.data.valuesets.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoverageDto {
    private InsuranceTypeDto insuranceType;
    private PayorTypeDto payorType;
    private String iknr;
    private String name;
    private WopDto wop;
    private InsurantStateDto insurantState;
    private PersonGroupDto personGroup;

    public static Builder ofType(InsuranceTypeDto type) {
        return new Builder(type, null);
    }

    public static Builder ofType(PayorTypeDto type) {
        return new Builder(null, type);
    }

    @RequiredArgsConstructor
    public static class Builder {
        private final InsuranceTypeDto insuranceType;
        private final PayorTypeDto payorType;
        private String iknr;
        private String name;
        private WopDto wop;
        private InsurantStateDto insurantState;
        private PersonGroupDto personGroup;

        public Builder named(String name) {
            this.name = name;
            return this;
        }

        public Builder withIknr(String iknr) {
            this.iknr = iknr;
            return this;
        }

        public Builder resident(WopDto wop) {
            this.wop = wop;
            return this;
        }

        public Builder insurantState(InsurantStateDto insurantState) {
            this.insurantState = insurantState;
            return this;
        }

        public Builder personGroup(PersonGroupDto personGroup) {
            this.personGroup = personGroup;
            return this;
        }

        public CoverageDto build() {
            return new CoverageDto(insuranceType, payorType, iknr, name, wop, insurantState, personGroup);
        }
    }
}
