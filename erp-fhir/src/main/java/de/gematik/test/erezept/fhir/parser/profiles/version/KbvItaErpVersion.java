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
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KbvItaErpVersion implements ProfileVersion {
  V1_0_2("1.0.2", LocalDate.of(1970, Month.JANUARY, 1), LocalDate.of(2023, Month.JULY, 31)),
  V1_1_0("1.1.0", LocalDate.of(2023, Month.AUGUST, 1), LocalDate.of(2070, Month.DECEMBER, 31));

  private static final String PROFILE_NAME = "kbv.ita.erp";
  private final String version;
  private final String name = PROFILE_NAME;
  private final LocalDate validFromDate;
  private final LocalDate validUntilDate;

  @Override
  public boolean omitZeroPatch() {
    return false;
  }

  public static KbvItaErpVersion getDefaultVersion() {
    return VersionUtil.getDefaultVersion(KbvItaErpVersion.class, PROFILE_NAME);
  }

  public static KbvItaErpVersion fromString(String version) {
    return Stream.of(KbvItaErpVersion.values())
        .filter(v -> v.isEqual(version))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown version: " + version));
  }
}
