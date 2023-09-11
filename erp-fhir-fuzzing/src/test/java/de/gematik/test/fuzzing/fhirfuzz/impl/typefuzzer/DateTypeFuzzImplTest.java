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

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.DateTypeFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ExtensionFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Extension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import java.util.List;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateTypeFuzzImplTest {

    static DateTypeFuzzImpl dateTypeFuzz;
    static FuzzerContext fuzzerContext;
    static FuzzConfig fuzzConfig;
    DateType dateType;

    @BeforeAll
    static void setupStatic() {
        fuzzConfig = new FuzzConfig();
        fuzzerContext = new FuzzerContext(fuzzConfig);
        dateTypeFuzz = new DateTypeFuzzImpl(fuzzerContext);
        fuzzConfig.setUseAllMutators(true);
    }

    @BeforeEach
    void setup() {
        fuzzConfig.setPercentOfAll(100f);
        fuzzConfig.setPercentOfEach(100f);
        dateType = new DateType();
    }

    @RepeatedTest(REPETITIONS)
    void getContext() {
        assertNotNull(dateTypeFuzz.getContext());
    }

    @RepeatedTest(REPETITIONS)
    void generateRandom() {
        assertNotNull(dateTypeFuzz.generateRandom());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzId() {
        fuzzConfig.setPercentOfAll(00.0f);
        assertFalse(dateType.hasId());
        dateTypeFuzz.fuzz(dateType);
        assertTrue(dateType.hasId());
        fuzzConfig.setPercentOfAll(100.0f);
        dateTypeFuzz.fuzz(dateType);
        fuzzConfig.setPercentOfAll(00.0f);
        val text = fuzzerContext.getIdFuzzer().generateRandom();
        dateType.setId(text);
        dateTypeFuzz.fuzz(dateType);
        assertTrue(dateType.hasId());
        assertNotEquals(text, dateType.getId());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzExtension() {
        fuzzConfig.setPercentOfAll(00.0f);
        assertFalse(dateType.hasExtension());
        dateTypeFuzz.fuzz(dateType);
        assertTrue(dateType.hasExtension());
        fuzzConfig.setPercentOfAll(100.0f);
        dateTypeFuzz.fuzz(dateType);
        assertFalse(dateType.hasExtension());
        fuzzConfig.setPercentOfAll(00.0f);
        val text = fuzzerContext.getTypeFuzzerFor(Extension.class, () -> new ExtensionFuzzerImpl(fuzzerContext)).generateRandom();
        var testString = text.getUrl();
        dateType.setExtension(List.of(text));
        dateTypeFuzz.fuzz(dateType);
        assertTrue(dateType.hasExtension());
        assertNotEquals(testString, dateType.getExtension().get(0).getUrl());
    }


    @RepeatedTest(REPETITIONS)
    void shouldFuzzValue() {
        fuzzConfig.setPercentOfAll(00.0f);
        assertFalse(dateType.hasValue());
        dateTypeFuzz.fuzz(dateType);
        assertTrue(dateType.hasValue());
        fuzzConfig.setPercentOfAll(100.0f);
        dateTypeFuzz.fuzz(dateType);
        assertFalse(dateType.hasValue());
        fuzzConfig.setPercentOfAll(00.0f);
        val date = fuzzerContext.getRandomDateWithFactor(5);
        var time = date.getTime();
        dateType.setValue(date);
        dateTypeFuzz.fuzz(dateType);
        assertTrue(dateType.hasValue());
        assertNotEquals(time, dateType.getValue().getTime());
    }
}
