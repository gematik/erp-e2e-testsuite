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

package de.gematik.test.fuzzing.fhirfuzz.impl.stringtypes;

import de.gematik.test.fuzzing.fhirfuzz.BaseFuzzer;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class StringFuzzImpl implements BaseFuzzer<String> {
    private static final int DEFAULT_STRING_LENGTH_THOUSAND = 1000;
    private final FuzzerContext fuzzerContext;

    public StringFuzzImpl(FuzzerContext fuzzerContext) {
        this.fuzzerContext = fuzzerContext;
    }


    @Override
    public String fuzz(String s) {
        if (s == null) {
            log.info("given String to fuzz was null!");
            return null;
        }
        char[] chars = s.toCharArray();
        for (int iter = 0; iter < chars.length; iter++) {
            if (fuzzerContext.conditionalChance(fuzzerContext.getFuzzConfig().getPercentOfEach())) {
                val num = fuzzerContext.nextInt(33, 126);
                chars[iter] = (char) num;
            }
        }
        return String.valueOf(chars);
    }


    @Override
    public FuzzerContext getContext() {
        return fuzzerContext;
    }


    /**
     * generates a random String length that is given
     * with lowe- and upperCase letters
     * !!! Attention !!! Method is using Faker.regexify()
     * and could produce StackOverFlow
     * <p>
     * length of String is random 1 -> 1000;
     *
     * @return random String of Chars
     */
    @Override
    public String generateRandom() {
        return generateRandom(fuzzerContext.getRandom().nextInt(1, DEFAULT_STRING_LENGTH_THOUSAND));
    }

    /**
     * generates a random String length that is given
     * with lowe- and upperCase letters
     * !!! Attention !!! Method is using Faker.regexify()
     * and could produce StackOverFlow
     *
     * @param length of String
     * @return random String of letters
     */

    public String generateRandom(int length) {
        StringBuilder result = new StringBuilder();
        for (int iter = 0; iter < length; iter++) {
            result.append((char) (fuzzerContext.getRandom().nextInt(26) + 'a'));
        }
        return result.toString();
    }

}
