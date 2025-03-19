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

package de.gematik.test.fuzzing.fhirfuzz;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;

/**
 * this Interface has generic implementations in BaseFuzzer concrete implementation in implemented
 * ResourceFuzzerImpl
 *
 * @param <T> restrict the generic Type of BaseFuzzer to Resource
 */
public interface FhirResourceFuzz<T extends Resource> extends BaseFuzzer<T> {

  default T fuzzTilInvalid(T value, FhirParser parser) {
    val inputPOE = getContext().getFuzzConfig().getPercentOfEach();
    val inputPOA = getContext().getFuzzConfig().getPercentOfAll();
    val inputPOM = getContext().getFuzzConfig().getUsedPercentOfMutators();
    var counter = 5;
    var finalCounter = 5;
    boolean isValid;
    do {
      do {
        fuzz(value);
        counter--;
        isValid = parser.isValid(parser.encode(value, EncodingType.XML));
      } while (isValid && counter > 0);
      counter = 5;
      finalCounter--;
      getContext()
          .getFuzzConfig()
          .setUsedPercentOfMutators(getContext().getFuzzConfig().getUsedPercentOfMutators() + 1f);
      getContext()
          .getFuzzConfig()
          .setPercentOfEach(getContext().getFuzzConfig().getPercentOfEach() + 1f);
      getContext()
          .getFuzzConfig()
          .setPercentOfAll(getContext().getFuzzConfig().getPercentOfAll() + 1f);
    } while (isValid && finalCounter > 0);
    getContext().getFuzzConfig().setUsedPercentOfMutators(inputPOM);
    getContext().getFuzzConfig().setPercentOfEach(inputPOE);
    getContext().getFuzzConfig().setPercentOfAll(inputPOA);
    return value;
  }
}
