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
public class UrlFuzzImpl implements BaseFuzzer<String> {

    private final FuzzerContext fuzzerContext;
    private static final String HTTPS = "https://";
    public UrlFuzzImpl(FuzzerContext fuzzerContext) {
        this.fuzzerContext = fuzzerContext;
    }

    @Override
    public FuzzerContext getContext() {
        return fuzzerContext;
    }

    @Override
    public String fuzz(String s) {
        if (s == null) {
            log.info("given String at UriFuzzer was NULL");
            return this.generateRandom();
        }
        if (s.length() < 7)
            return s;
        val stringFuzz = new StringFuzzImpl(fuzzerContext);

        if (s.contains(HTTPS)) {
            return HTTPS + stringFuzz.fuzz(s.substring(8));
        }
        val secondHttps = "Https://";
        if (s.contains(secondHttps)) {
            return secondHttps + stringFuzz.fuzz(s.substring(8));
        }
        val http = "http://";
        if (s.contains(http)) {
            return http + stringFuzz.fuzz(s.substring(7));
        }
        val secondHttp = "Http://";
        if (s.contains(secondHttp)) {
            return secondHttp + stringFuzz.fuzz(s.substring(7));
        }
        return stringFuzz.fuzz(s);
    }


    public String generateRandom() {
        return HTTPS + fuzzerContext.getFaker().internet().url();
    }
}
