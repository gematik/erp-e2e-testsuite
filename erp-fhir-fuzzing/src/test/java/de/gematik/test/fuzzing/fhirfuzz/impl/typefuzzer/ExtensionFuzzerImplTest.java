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

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ExtensionFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UriType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtensionFuzzerImplTest {


    private static FuzzConfig fuzzConfig;
    private static FuzzerContext fuzzerContext;

    private static ExtensionFuzzerImpl extensionFuzzerImpl;

    private Extension extension;

    @BeforeAll
    static void setUpConf() {
        fuzzConfig = new FuzzConfig();
        fuzzConfig.setUseAllMutators(true);
        fuzzerContext = new FuzzerContext(fuzzConfig);
        extensionFuzzerImpl = new ExtensionFuzzerImpl(fuzzerContext);
    }

    @BeforeEach
    void setupComp() {
        fuzzConfig.setUseAllMutators(true);
        fuzzConfig.setPercentOfEach(100.0f);
        fuzzConfig.setPercentOfAll(100.0f);
        extension = new Extension();
    }

    @RepeatedTest(REPETITIONS)
    void getContext() {
        assertNotNull(extensionFuzzerImpl.getContext());
    }

    @RepeatedTest(REPETITIONS)
    void generateRandom() {
        assertTrue(extensionFuzzerImpl.generateRandom().hasUrl());
        assertTrue(extensionFuzzerImpl.generateRandom().hasUrlElement());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzUrl() {
        assertFalse(extension.hasUrl());
        extensionFuzzerImpl.fuzz(extension);
        assertTrue(extension.hasUrl());
        extensionFuzzerImpl.fuzz(extension);
        val teststring = fuzzerContext.getStringFuzz().generateRandom(150);
        extension.setUrl(teststring);
        fuzzConfig.setPercentOfAll(0.00f);
        extensionFuzzerImpl.fuzz(extension);
        assertNotEquals(teststring, extension.getId());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzId() {
        assertFalse(extension.hasId());
        extensionFuzzerImpl.fuzz(extension);
        assertTrue(extension.hasId());
        extensionFuzzerImpl.fuzz(extension);
        val teststring = fuzzerContext.getIdFuzzer().generateRandom();
        extension.setId(teststring);
        fuzzConfig.setPercentOfAll(0.00f);
        extensionFuzzerImpl.fuzz(extension);
        assertNotEquals(teststring, extension.getId());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzUrlElement() {
        assertFalse(extension.hasUrlElement());
        extensionFuzzerImpl.fuzz(extension);
        assertTrue(extension.hasUrlElement());
        extensionFuzzerImpl.fuzz(extension);
        val teststring = new UriType(fuzzerContext.getStringFuzz().generateRandom(150));
        extension.setUrlElement(teststring.copy());
        fuzzConfig.setPercentOfAll(0.00f);
        extensionFuzzerImpl.fuzz(extension);
        assertNotEquals(teststring.toString(), extension.getUrlElement().toString());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzIdElement() {
        assertFalse(extension.hasIdElement());
        extensionFuzzerImpl.fuzz(extension);
        assertTrue(extension.hasIdElement());
        extensionFuzzerImpl.fuzz(extension);
        val teststring = fuzzerContext.getIdFuzzer().generateRandom();
        extension.setIdElement(new StringType(teststring));
        fuzzConfig.setPercentOfAll(0.00f);
        extensionFuzzerImpl.fuzz(extension);
        assertNotEquals(teststring, extension.getIdElement().toString());
    }


}
