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

package de.gematik.test.fuzzing.fhirfuzz.impl.typefuzzer;

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.PeriodFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.Period;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

import java.util.Date;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeriodFuzzerImplTest {

    private static FuzzConfig fuzzConfig;
    private static FuzzerContext fuzzerContext;
    private static PeriodFuzzerImpl periodFuzzer;

    @BeforeAll
    static void setUpConf() {
        fuzzConfig = new FuzzConfig();
        fuzzConfig.setPercentOfEach(100.0f);
        fuzzConfig.setPercentOfAll(100.0f);
        fuzzConfig.setUseAllMutators(true);
        fuzzerContext = new FuzzerContext(fuzzConfig);
        periodFuzzer = new PeriodFuzzerImpl(fuzzerContext);
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzStart() {
        fuzzConfig.setUseAllMutators(true);
        fuzzConfig.setPercentOfAll(00.0f);
        Period period = new Period();
        assertFalse(period.hasStart());
        periodFuzzer.fuzz(period);
        assertTrue(period.hasStart());
        fuzzConfig.setPercentOfAll(100.0f);
        periodFuzzer.fuzz(period);
        assertFalse(period.hasStart());
        fuzzConfig.setPercentOfAll(00.0f);
        val start = fuzzerContext.getRandomDate();
        period.setStart(start);
        periodFuzzer.fuzz(period);
        assertTrue(period.hasStart());
        assertNotEquals(start, period.getStart());

    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzEnd() {
        fuzzConfig.setUseAllMutators(true);
        fuzzConfig.setPercentOfAll(00.0f);
        Period period = new Period();
        assertFalse(period.hasEnd());
        periodFuzzer.fuzz(period);
        assertTrue(period.hasEnd());
        fuzzConfig.setPercentOfAll(100.0f);
        periodFuzzer.fuzz(period);
        assertFalse(period.hasEnd());
        fuzzConfig.setPercentOfAll(00.0f);
        val end = fuzzerContext.getRandomDate();
        period.setEnd(new Date(end.getTime()));
        periodFuzzer.fuzz(period);
        assertTrue(period.hasEnd());
        assertNotEquals(end.getTime(), period.getEnd().getTime());
    }

}