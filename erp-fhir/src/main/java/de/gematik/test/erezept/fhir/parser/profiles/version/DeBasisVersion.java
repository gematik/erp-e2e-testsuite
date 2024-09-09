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
public enum DeBasisVersion implements ProfileVersion<DeBasisVersion> {
  /*
  Note: validFromDate and validUntilDate are only required to distinguish the proper Version via current date.
  This won't be needed for this profile thus valid throughout the whole time
   */
  V0_9_13("0.9.13", LocalDate.of(1970, Month.JANUARY, 1), LocalDate.of(2070, Month.DECEMBER, 31)),
  V1_3_2("1.3.2", LocalDate.of(1970, Month.JANUARY, 1), LocalDate.of(2070, Month.DECEMBER, 31));

  private final String version;
  private final LocalDate validFromDate;
  private final LocalDate validUntilDate;
  private final CustomProfiles customProfile = CustomProfiles.DE_BASIS_PROFIL_R4;

  public static DeBasisVersion fromString(String input) {
    return ProfileVersion.fromString(DeBasisVersion.class, input);
  }

  public static DeBasisVersion getDefaultVersion() {
    return ProfileVersion.getDefaultVersion(
        DeBasisVersion.class, CustomProfiles.DE_BASIS_PROFIL_R4);
  }
}
