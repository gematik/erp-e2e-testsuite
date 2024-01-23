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

package de.gematik.test.erezept.fhir.parser.profiles;

import java.util.*;
import javax.annotation.Nullable;
import lombok.*;
import org.hl7.fhir.r4.model.*;

public interface IWithSystem {

  String getCanonicalUrl();

  default boolean matchAny(List<CanonicalType> canonicalTypes) {
    return canonicalTypes.stream().anyMatch(this::match);
  }

  default boolean match(Meta meta) {
    return matchAny(meta.getProfile());
  }

  default boolean match(Identifier identifier) {
    return match(identifier.getSystem());
  }

  default boolean match(CanonicalType canonicalType) {
    return match(canonicalType.asStringValue());
  }

  default boolean match(Coding coding) {
    return match(coding.getSystem());
  }

  default boolean match(@Nullable String url) {
    if (url == null) {
      return false;
    }
    val unversioned = url.split("\\|")[0];
    return this.getCanonicalUrl().equals(unversioned);
  }
}
