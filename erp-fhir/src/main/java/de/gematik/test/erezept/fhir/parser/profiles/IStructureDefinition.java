/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.fhir.parser.profiles;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.parser.profiles.version.ProfileVersion;
import lombok.NonNull;
import org.hl7.fhir.r4.model.CanonicalType;

public interface IStructureDefinition<T extends ProfileVersion<?>> extends IWithSystem {

  default String getVersionedUrl(@NonNull T version) {
    return this.getVersionedUrl(version, false);
  }

  default String getVersionedUrl(@NonNull T version, boolean cropPatch) {
    var v = version.getVersion();
    if (cropPatch) {
      v = v.substring(0, 3);
    }
    return format("{0}|{1}", this.getCanonicalUrl(), v);
  }

  default CanonicalType asCanonicalType() {
    return new CanonicalType(this.getCanonicalUrl());
  }

  default CanonicalType asCanonicalType(@NonNull T version) {
    return this.asCanonicalType(version, false);
  }

  /**
   * @param version to use for the canonical type
   * @param cropPatch if true the patch of the version will be cropped resulting in versions like
   *     1.0 (MAJOR.MINOR)
   * @return the canonical type
   */
  default CanonicalType asCanonicalType(@NonNull T version, boolean cropPatch) {
    return new CanonicalType(this.getVersionedUrl(version, cropPatch));
  }
}
