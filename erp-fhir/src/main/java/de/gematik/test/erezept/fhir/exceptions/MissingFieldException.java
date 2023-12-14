/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.fhir.exceptions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.parser.profiles.IWithSystem;
import de.gematik.test.erezept.fhir.valuesets.IValueSet;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;

public class MissingFieldException extends RuntimeException {
  public MissingFieldException(Class<? extends Resource> clazz, IWithSystem field) {
    this(clazz, field.getCanonicalUrl());
  }

  public MissingFieldException(Class<? extends Resource> clazz, IWithSystem... fields) {
    this(
        clazz,
        Arrays.stream(fields).map(IWithSystem::getCanonicalUrl).collect(Collectors.joining(" | ")));
  }

  public MissingFieldException(Class<? extends Resource> clazz, IValueSet valueSet) {
    this(clazz, format("{0}#{1}", valueSet.getCodeSystem(), valueSet.getCode()));
  }

  public MissingFieldException(Class<? extends Resource> clazz, IValueSet... valueSets) {
    this(clazz, Arrays.stream(valueSets).toList());
  }

  public MissingFieldException(Class<? extends Resource> clazz, List<? extends IValueSet> fields) {
    this(
        clazz,
        fields.stream()
            .map(valueSet -> format("{0}#{1}", valueSet.getCodeSystem(), valueSet.getCode()))
            .collect(Collectors.joining(" | ")));
  }

  public MissingFieldException(Class<? extends Resource> clazz, ResourceType type) {
    this(clazz, type.name());
  }

  public MissingFieldException(Class<? extends Resource> clazz, ResourceType... types) {
    this(clazz, Arrays.stream(types).map(Enum::name).collect(Collectors.joining(" | ")));
  }

  public MissingFieldException(Class<? extends Resource> clazz, String fieldName) {
    super(format("Missing Field {0} in Object of type {1}", fieldName, clazz.getSimpleName()));
  }
}
