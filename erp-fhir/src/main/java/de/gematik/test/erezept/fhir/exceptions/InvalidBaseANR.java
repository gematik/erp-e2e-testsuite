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

package de.gematik.test.erezept.fhir.exceptions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.values.BaseANR;
import org.hl7.fhir.r4.model.Identifier;

public class InvalidBaseANR extends RuntimeException {

  public InvalidBaseANR(Identifier identifier) {
    super(
        format(
            "Given Identifier with Code {0} is not decidable as LANR or ZANR",
            identifier.getType().getCodingFirstRep().getCode()));
  }

  public InvalidBaseANR(BaseANR.ANRType type) {
    super(format("Given ANR Type {0} is invalid (or not yet implemented!)", type));
  }
}
