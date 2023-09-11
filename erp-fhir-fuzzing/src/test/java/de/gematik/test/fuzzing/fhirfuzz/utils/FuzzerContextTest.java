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

package de.gematik.test.fuzzing.fhirfuzz.utils;

import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FuzzerContextTest {
    static FuzzerContext fuzzerContext;


    @BeforeAll
    static void setup() {
        FuzzConfig fuzzConfig = new FuzzConfig();
        fuzzConfig.setPercentOfEach(100.00F);
        fuzzConfig.setPercentOfEach(100.00f);
        fuzzerContext = new FuzzerContext(fuzzConfig);
    }

    @RepeatedTest(REPETITIONS)
    void generateFakeLong() {
        assertNotNull(fuzzerContext.generateFakeLong());
        assertTrue(fuzzerContext.generateFakeLong() >= 0L);

        //dateTimeType.get
    }


    @RepeatedTest(REPETITIONS)
    void conditionalChance() {
        var res = fuzzerContext.conditionalChance(100.0f);
        assertTrue(res);
        var res2 = fuzzerContext.conditionalChance(0.0f);
        assertFalse(res2);

    }

    @RepeatedTest(REPETITIONS)
    void shouldGetRandomConf() {
        FuzzConfig fuzzConfig = FuzzConfig.getRandom();
        assertNotNull(fuzzerContext);
        assertNotNull(fuzzConfig.toString());
    }

    @RepeatedTest(REPETITIONS)
    void testRandom() {
        int counter = 0;
        for (int i = 0; i < 10000; i++) {
            val res = (int) fuzzerContext.getRandom().nextFloat(2f);
            if (res > 0) counter++;
        }
        assertTrue(counter > 3000);
        assertTrue(counter < 7000);
    }

    @RepeatedTest(REPETITIONS)
    void shouldGetRandomTime() {
        assertNotNull(fuzzerContext.getRandomDate());

    }
}
