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
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GematikDirVersion implements ProfileVersion {
  V0_11_12("0.11.12"),
  V0_11_25("0.11.25"),
  V1_0_0("1.0.0"),
  V1_0_1("1.0.1"),
  ;

  @Override
  public boolean omitZeroPatch() {
    return false;
  }

  @Override
  public boolean omitPatch() {
    // TODO: Johannes clear with Florian / Steven / Dirk
    if (this.equals(GematikDirVersion.V1_0_0) || this.equals(GematikDirVersion.V1_0_1)) {
      return false;
    }
    return this != V0_11_12;
  }

  @Override
  public String getName() {
    return "de.gematik.fhir.directory";
  }

  private final String version;
}
