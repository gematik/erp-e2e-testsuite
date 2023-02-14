/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.core.expectations.requirements;

import static java.text.MessageFormat.format;

import lombok.Getter;
import lombok.val;

@Getter
public class Requirement {

  private final String id;
  private final String description;
  private final boolean custom;

  Requirement(String id, String description) {
    this(id, description, false);
  }

  Requirement(String id, String description, boolean isCustom) {
    this.id = id;
    this.description = description;
    this.custom = isCustom;
  }

  /**
   * @deprecated will be removed and shall not be used:
   * @param description
   * @return
   */
  @Deprecated(forRemoval = true) // temporary workaround
  @SuppressWarnings({"java:S2676"})
  public static Requirement custom(String description) {
    val uniqueId =
        format(
            "CUSTOM_{0}",
            String.valueOf(
                Math.abs(description.hashCode()) % 255)); // no "custom" requirements in the future!
    return new Requirement(uniqueId, description, true);
  }

  @Override
  public String toString() {
    return format("{0} - {1}", id, description);
  }
}
