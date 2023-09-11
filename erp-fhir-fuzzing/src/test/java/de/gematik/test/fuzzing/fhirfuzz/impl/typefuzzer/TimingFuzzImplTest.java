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

package de.gematik.test.fuzzing.fhirfuzz.impl.typefuzzer;

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.CodeableConceptFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.TimingFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Timing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import java.util.List;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TimingFuzzImplTest {


    private static FuzzConfig fuzzConfig;
    private static FuzzerContext fuzzerContext;

    private static TimingFuzzImpl timingFuzz;

    private Timing timing;

    @BeforeAll
    static void setUpConf() {
        fuzzConfig = new FuzzConfig();
        fuzzConfig.setPercentOfEach(100.0f);
        fuzzConfig.setPercentOfAll(100.0f);
        fuzzConfig.setUseAllMutators(true);
        fuzzerContext = new FuzzerContext(fuzzConfig);
        timingFuzz = new TimingFuzzImpl(fuzzerContext);
    }

    @BeforeEach
    void setupComp() {
        fuzzConfig.setPercentOfEach(100.0f);
        fuzzConfig.setPercentOfAll(100.0f);
        timing = new Timing();
    }

    @RepeatedTest(REPETITIONS)
    void fuzzEvent() {
        assertFalse(timing.hasEvent());
        timingFuzz.fuzz(timing);
        Assertions.assertTrue(timing.hasEvent());
        val testObject = new DateTimeType(fuzzerContext.getRandomDate());
        timing.setEvent(List.of(testObject));
        fuzzConfig.setPercentOfAll(0.00f);
        timingFuzz.fuzz(timing);
        assertNotEquals(testObject, timing.getEvent().get(0));
    }

    @RepeatedTest(REPETITIONS)
    void fuzzRepeat() {
        assertFalse(timing.hasRepeat());
        timingFuzz.fuzz(timing);
        Assertions.assertTrue(timing.hasRepeat());
        val testObject = timingFuzz.generateRandom().getRepeat();
        timing.setRepeat(testObject.copy());
        timingFuzz.fuzz(timing);
        assertNotEquals(testObject, timing.getRepeat());
        assertNotEquals(testObject.getCount(), timing.getRepeat().getCount());
    }

    @RepeatedTest(REPETITIONS)
    void fuzzCode() {
        assertFalse(timing.hasCode());
        timingFuzz.fuzz(timing);
        Assertions.assertTrue(timing.hasCode());
        val testObject = fuzzerContext.getTypeFuzzerFor(CodeableConcept.class, () -> new CodeableConceptFuzzImpl(fuzzerContext)).generateRandom();
        timing.setCode(testObject.copy());
        fuzzConfig.setPercentOfAll(0.00f);
        timingFuzz.fuzz(timing);
        assertNotEquals(testObject, timing.getCode());
        assertNotEquals(testObject.getCodingFirstRep(), timing.getCode().getCodingFirstRep());
    }

    @RepeatedTest(REPETITIONS)
    void generateRandom() {
        assertNotNull(timingFuzz.generateRandom());
        assertNotNull(timingFuzz.generateRandom().getEvent());
    }

    @RepeatedTest(REPETITIONS)
    void getContext() {
        assertNotNull(timingFuzz.getContext());
    }
}
