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

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.CodingTypeFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodingTypeFuzzerImplTest {

    private static FuzzConfig fuzzConfig;
    private static FuzzerContext fuzzerContext;
    private static CodingTypeFuzzerImpl codingTypeFuzzerImpl;

    @BeforeAll
    static void setUpConf() {
        fuzzConfig = new FuzzConfig();
        fuzzConfig.setPercentOfEach(100.0f);
        fuzzConfig.setPercentOfAll(100.0f);
        fuzzConfig.setUseAllMutators(true);
        fuzzerContext = new FuzzerContext(fuzzConfig);
        codingTypeFuzzerImpl = new CodingTypeFuzzerImpl(fuzzerContext);
    }


    @RepeatedTest(REPETITIONS)
    void shouldFuzzCodingCode() {
        fuzzConfig.setUseAllMutators(true);
        fuzzConfig.setPercentOfAll(00.0f);
        val coding = new Coding(null, null, null);
        assertFalse(coding.hasCode());
        codingTypeFuzzerImpl.fuzz(coding);
        assertTrue(coding.hasCode());
        fuzzConfig.setPercentOfAll(100.0f);
        codingTypeFuzzerImpl.fuzz(coding);
        fuzzConfig.setPercentOfAll(00.0f);
        val c = "ABdA";
        coding.setCode(c);
        codingTypeFuzzerImpl.fuzz(coding);
        assertTrue(coding.hasCode());
        assertNotEquals(c, coding.getCode());

    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzCodingSystem() {
        fuzzConfig.setUseAllMutators(true);
        fuzzConfig.setPercentOfAll(00.0f);
        val coding = new Coding(null, null, null);
        assertFalse(coding.hasSystem());
        codingTypeFuzzerImpl.fuzz(coding);
        assertTrue(coding.hasSystem());
        fuzzConfig.setPercentOfAll(100.0f);
        codingTypeFuzzerImpl.fuzz(coding);
        fuzzConfig.setPercentOfAll(00.0f);
        val c = "http://soNuNuesch";
        coding.setSystem(c);
        codingTypeFuzzerImpl.fuzz(coding);
        assertTrue(coding.hasSystem());
        assertNotEquals(c, coding.getSystem());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzCodingVersion() {
        fuzzConfig.setUseAllMutators(true);
        fuzzConfig.setPercentOfAll(00.0f);
        val coding = new Coding(null, null, null);
        assertFalse(coding.hasVersion());
        codingTypeFuzzerImpl.fuzz(coding);
        assertTrue(coding.hasVersion());
        fuzzConfig.setPercentOfAll(100.0f);
        codingTypeFuzzerImpl.fuzz(coding);
        fuzzConfig.setPercentOfAll(00.0f);
        val c = "1.12.3.1";
        coding.setVersion(c);
        codingTypeFuzzerImpl.fuzz(coding);
        assertTrue(coding.hasVersion());
        assertNotEquals(c, coding.getVersion());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzCodingDiplay() {
        fuzzConfig.setUseAllMutators(true);
        fuzzConfig.setPercentOfAll(00.0f);
        val coding = new Coding(null, null, null);
        assertFalse(coding.hasDisplay());
        codingTypeFuzzerImpl.fuzz(coding);
        assertTrue(coding.hasDisplay());
        fuzzConfig.setPercentOfAll(100.0f);
        codingTypeFuzzerImpl.fuzz(coding);
        fuzzConfig.setPercentOfAll(00.0f);
        val c = "qsdaö#lsdm,2131 mqdlkw09ui23 öljkpoqwje öpoqwkepoi poqwepoiqwpo";
        coding.setDisplay(c);
        codingTypeFuzzerImpl.fuzz(coding);
        assertTrue(coding.hasDisplay());
        assertNotEquals(c, coding.getDisplay());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzCodingUserSelected() {
        fuzzConfig.setUseAllMutators(true);
        fuzzConfig.setPercentOfAll(00.0f);
        val coding = new Coding(null, null, null);
        assertFalse(coding.hasUserSelected());
        codingTypeFuzzerImpl.fuzz(coding);
        assertTrue(coding.hasUserSelected());
        val c = fuzzerContext.conditionalChance();
        coding.setUserSelected(c);
        codingTypeFuzzerImpl.fuzz(coding);
        assertTrue(coding.hasUserSelected());
        assertNotEquals(c, coding.getUserSelected());
    }

}
