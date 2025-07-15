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

package de.gematik.test.core.expectations.requirements;

import lombok.*;

public enum DurationOperationRequirement implements RequirementsSet {
  CREATE(ErpAfos.A_20165.getRequirement(), 460, 620),
  ACTIVATE_FLOWTYPE_160(ErpAfos.A_20165.getRequirement(), 400, 550),
  ACTIVATE_FLOWTYPE_169(ErpAfos.A_20165.getRequirement(), 400, 550),
  ACTIVATE_FLOWTYPE_200(ErpAfos.A_20165.getRequirement(), 400, 550),
  ACTIVATE_FLOWTYPE_209(ErpAfos.A_20165.getRequirement(), 400, 550),
  SIGN_DOCUMENT(Requirement.custom("Konnektor Schnittstellenoperation Sign Document"), 900, 900);
  @Getter private final Requirement requirement;
  @Getter private final long average;
  @Getter private final long quantile;

  DurationOperationRequirement(Requirement req, long average, long quantile) {
    this.requirement = req;
    this.average = average;
    this.quantile = quantile;
  }
}
