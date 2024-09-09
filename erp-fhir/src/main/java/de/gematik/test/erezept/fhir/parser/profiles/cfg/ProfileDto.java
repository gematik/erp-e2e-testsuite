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

package de.gematik.test.erezept.fhir.parser.profiles.cfg;

import de.gematik.test.erezept.fhir.parser.profiles.CustomProfiles;
import de.gematik.test.erezept.fhir.parser.profiles.version.ProfileVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.VersionedProfile;
import java.beans.Transient;
import lombok.Data;
import lombok.val;

@Data
public class ProfileDto {

  private String name;
  private String version;

  @Transient
  @SuppressWarnings({
    "java:S1452"
  }) // we can't know the concrete value beforehand, and we don't even need one
  public VersionedProfile<?> getVersionedProfile() {
    val cp = CustomProfiles.fromName(this.getName());
    val profileVersion = ProfileVersion.fromString(cp.getVersionClass(), this.getVersion());
    return new VersionedProfile<>(cp, profileVersion);
  }
}
