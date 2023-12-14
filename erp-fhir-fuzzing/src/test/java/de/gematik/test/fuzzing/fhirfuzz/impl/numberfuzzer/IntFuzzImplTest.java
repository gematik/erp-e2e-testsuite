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

import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntFuzzImplTest {

    private static FuzzerContext fuzzerContext;
    private static IntFuzzImpl intFuzz;

    @BeforeAll
    static void setUpConf() {
        val fuzzConfig = new FuzzConfig();
        fuzzConfig.setPercentOfEach(100.0f);
        fuzzConfig.setPercentOfAll(100.0f);
        fuzzConfig.setUseAllMutators(true);
        val fuzzerContext = new FuzzerContext(fuzzConfig);
        intFuzz = new IntFuzzImpl(fuzzerContext);
    }

    @RepeatedTest(REPETITIONS)
    void fuzz() {
        val testObject = intFuzz.generateRandom();
        assertNotEquals(testObject, intFuzz.fuzz(testObject));
    }

    @Test
    void generateRandom() {
        assertNotNull(intFuzz.generateRandom());

    }

    @Test
    void getContext() {
        assertTrue(intFuzz.getContext().getFuzzConfig().getUseAllMutators());
    }
}