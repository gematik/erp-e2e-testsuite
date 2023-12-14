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

import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguageCodeFuzzerImplTest {

    private static FuzzConfig fuzzConfig;
    private static FuzzerContext fuzzerContext;
    private static LanguageCodeFuzzerImpl languageFuzzer;


    @BeforeAll
    static void setUpConf() {
        fuzzConfig = new FuzzConfig();
        fuzzConfig.setUseAllMutators(true);
        fuzzerContext = new FuzzerContext(fuzzConfig);
        languageFuzzer = new LanguageCodeFuzzerImpl(fuzzerContext);
    }

    @BeforeEach
    void setupComp() {
        fuzzConfig.setPercentOfEach(100.0f);
        fuzzConfig.setPercentOfAll(100.0f);

    }

    @RepeatedTest(REPETITIONS)
    void getContext() {
        assertNotNull(languageFuzzer.getContext());
    }

    @RepeatedTest(REPETITIONS)
    void fuzz() {
        String teststring = "askjhasdnkb2qiuoehaksnc kajhawdi";
        assertNotEquals(teststring, languageFuzzer.fuzz(teststring));
    }

    @RepeatedTest(REPETITIONS)
    void generateRandom() {
        assertNotNull(languageFuzzer.generateRandom());
        assertTrue(languageFuzzer.generateRandom().length() >= 2);
    }

}