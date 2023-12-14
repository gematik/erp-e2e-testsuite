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

package de.gematik.test.fuzzing.fhirfuzz.impl.numberfuzzer;

import de.gematik.test.fuzzing.fhirfuzz.BaseFuzzer;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;

public class IntFuzzImpl implements BaseFuzzer<Integer> {

    private final FuzzerContext fuzzerContext;

    public IntFuzzImpl(FuzzerContext fuzzerContext) {
        this.fuzzerContext = fuzzerContext;
    }

    @Override
    public Integer fuzz(Integer value) {
        Integer result;
        do {
            result = fuzzerContext.getRandom().nextInt();
        } while (value.equals(result));
        return result;
    }

    @Override
    public Integer generateRandom() {
        return fuzzerContext.getRandom().nextInt();
    }

    @Override
    public FuzzerContext getContext() {
        return fuzzerContext;
    }
}
