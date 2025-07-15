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

package de.gematik.test.erezept.eml.fhir.profile;

import de.gematik.bbriccs.fhir.coding.version.ProfileVersion;
import de.gematik.bbriccs.fhir.coding.version.VersionUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EpaMedicationVersion implements ProfileVersion {
  V1_0_3("1.0.3"),
  V1_0_6("1.0.6"),
  ;

  private final String version;
  private final String name = "de.gematik.epa.medication";

  public static EpaMedicationVersion getDefaultVersion() {
    return VersionUtil.getDefaultVersionOptionally(
            EpaMedicationVersion.class, "de.gematik.epa.medication")
        .orElse(V1_0_6);
  }
}
