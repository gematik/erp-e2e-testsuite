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

package de.gematik.test.erezept.fhir.profiles.version;

import de.gematik.bbriccs.fhir.coding.version.ProfileVersion;
import de.gematik.bbriccs.fhir.coding.version.VersionUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DavKbvCsVsVersion implements ProfileVersion {
  V1_0_2("1.0.2"),
  V1_0_3("1.0.3"),
  V1_6_0("1.6.0");

  public static final String PROFILE_NAME = "gematik.kbv.sfhir.cs.vs";
  private final String version;
  private final String name = PROFILE_NAME;

  @Deprecated(since = "until knowing for what") // NOSONAR
  public static DavKbvCsVsVersion getDefaultVersion() {
    return VersionUtil.getDefaultVersion(DavKbvCsVsVersion.class, PROFILE_NAME);
  }
}
