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

package de.gematik.test.erezept.fhir.parser.profiles.version;

import de.gematik.bbriccs.fhir.coding.version.ProfileVersion;
import de.gematik.bbriccs.fhir.coding.version.VersionUtil;
import java.time.LocalDate;
import java.time.Month;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErpWorkflowVersion implements ProfileVersion {
  V1_1_1("1.1.1", LocalDate.of(2020, Month.JANUARY, 1), LocalDate.of(2023, Month.JULY, 31)),
  V1_2_0("1.2.0", LocalDate.of(2023, Month.AUGUST, 1), LocalDate.of(2024, Month.OCTOBER, 31)),
  V1_3_0("1.3.0", LocalDate.of(2024, Month.NOVEMBER, 1), LocalDate.of(2025, Month.MARCH, 31)),
  V1_4_0("1.4.0", LocalDate.of(2025, Month.APRIL, 1), LocalDate.of(2070, Month.JANUARY, 1));

  public static final String PROFILE_NAME = "de.gematik.erezept-workflow.r4";
  private final String version;
  private final LocalDate validFromDate;
  private final LocalDate validUntilDate;

  @Override
  public String getName() {
    return PROFILE_NAME;
  }

  public static ErpWorkflowVersion getDefaultVersion() {
    return VersionUtil.getDefaultVersion(ErpWorkflowVersion.class, PROFILE_NAME);
  }
}
