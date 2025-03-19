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
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AbdaErpBasisVersion implements ProfileVersion {
  V1_2_1("1.2.1"),
  V1_3_1("1.3.1"),
  V1_4("1.4.2"),
  ;

  public static final String PROFILE_NAME = "de.abda.erezeptabgabedatenbasis";
  private final String version;

  @Override
  public String getName() {
    return PROFILE_NAME;
  }

  public static AbdaErpBasisVersion getDefaultVersion() {
    return VersionUtil.getDefaultVersion(AbdaErpBasisVersion.class, PROFILE_NAME);
  }
}
