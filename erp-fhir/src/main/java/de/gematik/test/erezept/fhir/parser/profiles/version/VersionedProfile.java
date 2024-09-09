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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.parser.profiles.CustomProfiles;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Getter
@RequiredArgsConstructor
public class VersionedProfile<V extends ProfileVersion<V>> {

  private final CustomProfiles profile;
  private final ProfileVersion<V> profileVersion;

  @Override
  public String toString() {
    return format("{0} v{1}", profile.getName(), profileVersion.getVersion());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    val other = (VersionedProfile<?>) o;
    return profile == other.profile && Objects.equals(profileVersion, other.profileVersion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(profile, profileVersion);
  }
}
