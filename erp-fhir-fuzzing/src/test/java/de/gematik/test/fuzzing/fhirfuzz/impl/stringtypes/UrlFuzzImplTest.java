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

import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UrlFuzzImplTest {
    static FuzzConfig fuzzConfig;
    static FuzzerContext fuzzerContext;
    static UrlFuzzImpl uriFuzzer;

    @BeforeAll
    static void setup() {
        fuzzConfig = new FuzzConfig();
        fuzzerContext = new FuzzerContext(fuzzConfig);
        uriFuzzer = new UrlFuzzImpl(fuzzerContext);
    }

    @RepeatedTest(REPETITIONS)
    void getContext() {
        assertNotNull(uriFuzzer.getContext());
    }

    @ParameterizedTest
    @CsvSource({
            "'http://aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 40 ",
            "'Http://www.bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb,cccccccccccccccccccccccccccccccccccccccccccccccc,mlknweoiflnkyxlknasdpo90ß32msc.,mpo0.,ascd', 15",
            "'https://abcde123456ABCDE', 80",
            "'Https://1234566789', 70",
            "'https://abraCadabraundRumpelPumpel', 60",
    })
    void shouldFuzzUri(String s, float percent) {

        fuzzConfig.setPercentOfEach(percent);
        var fuzzerCont = new FuzzerContext(fuzzConfig);
        var org = s;
        var startStr = s.substring(0, 7);
        var fuzzedStr = uriFuzzer.fuzz(s);
        assertNotEquals(org, fuzzedStr);
        assertEquals(startStr, fuzzedStr.substring(0, 7));
    }


    @RepeatedTest(REPETITIONS)
    void shouldGenerateRandom() {
        assertNotNull(uriFuzzer.generateRandom());
        assertTrue(uriFuzzer.generateRandom().startsWith("https://"));
    }

}
