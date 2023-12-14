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

package de.gematik.test.fuzzing.fhirfuzz.impl.stringtypes;

import de.gematik.test.fuzzing.fhirfuzz.BaseFuzzer;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LanguageCodeFuzzerImpl implements BaseFuzzer<String> {
    private final FuzzerContext fuzzerContext;

    public LanguageCodeFuzzerImpl(FuzzerContext fuzzerContext) {
        this.fuzzerContext = fuzzerContext;
    }

    @Override
    public FuzzerContext getContext() {
        return fuzzerContext;
    }

    @Override
    public String fuzz(String id) {
        if (id == null) {
            log.info("given String to fuzz was null!");
            return null;
        }
        char[] chars = id.toCharArray();
        for (int iter = 0; iter < chars.length; iter++) {
            if (fuzzerContext.conditionalChance(fuzzerContext.getFuzzConfig().getPercentOfEach() * 5)) {
                chars[iter] = (char) (fuzzerContext.getRandom().nextInt(26) + 'a');
            }
        }
        return String.valueOf(chars);
    }

    @Override
    public String generateRandom() {
        int languageCodeLength = 2;
        StringBuilder result = new StringBuilder();
        for (int iter = 0; iter < languageCodeLength; iter++) {
            result.append((char) (fuzzerContext.getRandom().nextInt(26) + 'a'));
        }
        return result.toString();
    }
}
