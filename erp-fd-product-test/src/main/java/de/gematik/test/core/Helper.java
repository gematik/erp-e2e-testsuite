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

package de.gematik.test.core;

import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hl7.fhir.r4.model.Coding;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Helper {

  public static boolean compareCodings(Coding coding1, Coding coding2) {
    return coding1.getCode().equals(coding2.getCode())
        && coding1.getSystem().equals(coding2.getSystem());
  }

  public static Optional<Coding> findBySystem(Coding coding, List<Coding> codings) {
    return codings.stream().filter(c -> c.getSystem().equals(coding.getSystem())).findFirst();
  }
}
