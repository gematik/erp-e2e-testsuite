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

package de.gematik.test.erezept.fhir.parser.profiles.version;

import de.gematik.test.erezept.fhir.parser.profiles.CustomProfiles;
import java.time.LocalDate;
import java.time.Month;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AbdaErpPkvVersion implements ProfileVersion<AbdaErpPkvVersion> {
  V1_1_0("1.1.0", LocalDate.of(1970, Month.JANUARY, 1), LocalDate.of(2023, Month.JUNE, 30)),
  V1_2_0("1.2.0", LocalDate.of(2023, Month.JULY, 1), LocalDate.of(2070, Month.DECEMBER, 31));

  private final String version;
  private final LocalDate validFromDate;
  private final LocalDate validUntilDate;
  private final CustomProfiles customProfile = CustomProfiles.ABDA_ERP_ABGABE_PKV;

  public static AbdaErpPkvVersion getDefaultVersion() {
    return ProfileVersion.getDefaultVersion(
        AbdaErpPkvVersion.class, CustomProfiles.ABDA_ERP_ABGABE_PKV);
  }
}
