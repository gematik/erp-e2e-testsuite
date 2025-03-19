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

package de.gematik.test.fuzzing.fhirfuzz.utils;

import static java.text.MessageFormat.format;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FuzzOperationResult<T> {
  private final String description;
  private final T orgEntry;
  private final T newEntry;

  public FuzzOperationResult(String description, T orgEntry, T newEntry) {
    this.description = description;
    this.orgEntry = orgEntry;
    this.newEntry = newEntry;
  }

  @Override
  public String toString() {
    return format("{0}: {1} -> {2}", description, orgEntry, newEntry);
  }
}
