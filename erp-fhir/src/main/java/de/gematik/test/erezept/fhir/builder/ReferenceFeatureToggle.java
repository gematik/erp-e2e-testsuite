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

package de.gematik.test.erezept.fhir.builder;

import de.gematik.bbriccs.toggle.FeatureToggle;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class ReferenceFeatureToggle implements FeatureToggle<ReferenceFeatureToggle.RefencingType> {

  public static final String TOGGLE_KEY = "erp.fhir.references";

  @Override
  public String getKey() {
    return TOGGLE_KEY;
  }

  @Override
  public Function<String, RefencingType> getConverter() {
    return this::fromValue;
  }

  private RefencingType fromValue(String value) {
    return Stream.of(RefencingType.values())
        .filter(t -> t.value.equalsIgnoreCase(value))
        .findFirst()
        .orElseGet(this::getDefaultValue);
  }

  @Override
  public RefencingType getDefaultValue() {
    return RefencingType.UUID;
  }

  @RequiredArgsConstructor
  public enum RefencingType {
    UUID("uuid"),
    HTTP("http"),
    ;

    @Getter private final String value;
  }
}
