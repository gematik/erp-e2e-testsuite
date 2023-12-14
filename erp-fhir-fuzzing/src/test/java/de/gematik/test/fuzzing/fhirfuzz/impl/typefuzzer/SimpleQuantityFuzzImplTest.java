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

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.SimpleQuantityFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.Quantity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleQuantityFuzzImplTest {
    private static FuzzConfig fuzzConfig;
    private static FuzzerContext fuzzerContext;

    private static SimpleQuantityFuzzImpl quantityFuzzer;
    private Quantity quantity;


    @BeforeAll
    static void setUpConf() {
        fuzzConfig = new FuzzConfig();
        fuzzConfig.setPercentOfEach(100.0f);
        fuzzConfig.setPercentOfAll(100.0f);
        fuzzConfig.setUseAllMutators(true);
        fuzzerContext = new FuzzerContext(fuzzConfig);
        quantityFuzzer = new SimpleQuantityFuzzImpl(fuzzerContext);
    }

    @BeforeEach
    void setupComp() {
        fuzzConfig.setPercentOfEach(100.0f);
        fuzzConfig.setPercentOfAll(100.0f);
        quantity = new Quantity();
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzUnit() {
        assertFalse(quantity.hasUnit());
        quantityFuzzer.fuzz(quantity);
        assertTrue(quantity.hasUnit());
        quantityFuzzer.fuzz(quantity);
        val testObject = fuzzerContext.getStringFuzz().generateRandom(15);
        quantity.setUnit(testObject);
        fuzzConfig.setPercentOfAll(0.00f);
        quantityFuzzer.fuzz(quantity);
        assertNotEquals(testObject, quantity.getUnit());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzSystem() {
        assertFalse(quantity.hasSystem());
        quantityFuzzer.fuzz(quantity);
        assertTrue(quantity.hasSystem());
        quantityFuzzer.fuzz(quantity);
        val testObject = fuzzerContext.getStringFuzz().generateRandom(15);
        quantity.setSystem(testObject);
        fuzzConfig.setPercentOfAll(0.00f);
        quantityFuzzer.fuzz(quantity);
        assertNotEquals(testObject, quantity.getSystem());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzValue() {
        assertFalse(quantity.hasValue());
        quantityFuzzer.fuzz(quantity);
        assertTrue(quantity.hasValue());
        quantityFuzzer.fuzz(quantity);
        val testObject = fuzzerContext.getIntFuzz().generateRandom();
        quantity.setValue(testObject);
        fuzzConfig.setPercentOfAll(0.00f);
        quantityFuzzer.fuzz(quantity);
        assertNotEquals(testObject, quantity.getValue());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzCode() {
        assertFalse(quantity.hasCode());
        quantityFuzzer.fuzz(quantity);
        assertTrue(quantity.hasCode());
        quantityFuzzer.fuzz(quantity);
        val testObject = fuzzerContext.getStringFuzz().generateRandom(15);
        quantity.setCode(testObject);
        fuzzConfig.setPercentOfAll(0.00f);
        quantityFuzzer.fuzz(quantity);
        assertNotEquals(testObject, quantity.getCode());
    }


    @RepeatedTest(REPETITIONS)
    void generateRandom() {
        assertNotNull(quantityFuzzer.generateRandom().getValue());
    }

    @RepeatedTest(REPETITIONS)
    void getContext() {
        assertNotNull(quantityFuzzer.getContext());
    }
}