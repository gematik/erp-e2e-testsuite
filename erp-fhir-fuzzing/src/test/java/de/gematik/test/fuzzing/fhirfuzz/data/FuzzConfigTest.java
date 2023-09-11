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

package de.gematik.test.fuzzing.fhirfuzz.data;

import de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FuzzConfigTest {

    @RepeatedTest(CentralIterationSetupForTests.REPETITIONS)
    void shouldSetupConfCorrect() {
        FuzzConfig fuzzConfig = new FuzzConfig();
        String name = "firstConf";
        fuzzConfig.setName(name);

        float percOfAll = 0.5f;
        float percOfEach = 2.0f;
        fuzzConfig.setPercentOfAll(percOfAll);
        fuzzConfig.setPercentOfEach(percOfEach);
        assertEquals(name, fuzzConfig.getName());
        assertEquals(percOfAll, fuzzConfig.getPercentOfAll());
        assertEquals(percOfEach, fuzzConfig.getPercentOfEach());

    }


    @RepeatedTest(CentralIterationSetupForTests.REPETITIONS)
    void allCouldBeNull() {
        FuzzConfig config = new FuzzConfig();
        assertNull(config.getPercentOfAll());
        assertNull(config.getPercentOfEach());
        assertNull(config.getName());
    }


    @RepeatedTest(CentralIterationSetupForTests.REPETITIONS)
    void getDefaultShouldWork() {
        val fuzzconf = FuzzConfig.getDefault();
        assertNotNull(fuzzconf);

    }

    @RepeatedTest(CentralIterationSetupForTests.REPETITIONS)
    void getRandomShouldWork() {
        val fuzzconf = FuzzConfig.getRandom();
        assertNotNull(fuzzconf);
    }

    @RepeatedTest(CentralIterationSetupForTests.REPETITIONS)
    void toStringShouldWork() {
        val fuzzconf = FuzzConfig.getRandom();
        assertTrue(fuzzconf.toString().length() > 20);
    }

}
