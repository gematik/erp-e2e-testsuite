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

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.CodingTypeFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ReferenceFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.SignatureFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Signature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import java.util.List;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SignatureFuzzImplTest {
    private static FuzzConfig fuzzConfig;
    private static FuzzerContext fuzzerContext;
    private static SignatureFuzzImpl signatureFuzzer;
    private Signature signature;


    @BeforeAll
    static void setUpConf() {
        fuzzConfig = new FuzzConfig();
        fuzzConfig.setPercentOfEach(100.0f);
        fuzzConfig.setPercentOfAll(100.0f);
        fuzzConfig.setUseAllMutators(true);
        fuzzerContext = new FuzzerContext(fuzzConfig);
        signatureFuzzer = new SignatureFuzzImpl(fuzzerContext);
    }

    @BeforeEach
    void setupComp() {
        fuzzConfig.setPercentOfEach(100.0f);
        fuzzConfig.setPercentOfAll(100.0f);
        signature = new Signature();
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzType() {
        assertFalse(signature.hasType());
        signatureFuzzer.fuzz(signature);
        assertTrue(signature.hasType());
        val codings = List.of(fuzzerContext.getTypeFuzzerFor(Coding.class, () -> new CodingTypeFuzzerImpl(fuzzerContext)).generateRandom());
        val teststring = codings.get(0).getId();
        signature.setType(codings);
        fuzzConfig.setPercentOfAll(0.00f);
        signatureFuzzer.fuzz(signature);
        assertNotEquals(teststring, signature.getType().get(0).getId());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzWho() {
        assertFalse(signature.hasWho());
        signatureFuzzer.fuzz(signature);
        assertTrue(signature.hasWho());
        val codings = fuzzerContext.getTypeFuzzerFor(Reference.class, () -> new ReferenceFuzzerImpl(fuzzerContext)).generateRandom();
        val teststring = codings.getId();
        signature.setWho(codings);
        fuzzConfig.setPercentOfAll(0.00f);
        signatureFuzzer.fuzz(signature);
        assertNotEquals(teststring, signature.getWho().getId());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzOnbehalfOf() {
        assertFalse(signature.hasOnBehalfOf());
        signatureFuzzer.fuzz(signature);
        assertTrue(signature.hasOnBehalfOf());
        val codings = fuzzerContext.getTypeFuzzerFor(Reference.class, () -> new ReferenceFuzzerImpl(fuzzerContext)).generateRandom();
        val teststring = codings.getId();
        signature.setOnBehalfOf(codings);
        fuzzConfig.setPercentOfAll(0.00f);
        signatureFuzzer.fuzz(signature);
        assertNotEquals(teststring, signature.getOnBehalfOf().getId());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzId() {
        assertFalse(signature.hasId());
        signatureFuzzer.fuzz(signature);
        assertTrue(signature.hasId());
        val teststring = fuzzerContext.getIdFuzzer().generateRandom();
        signature.setId(teststring);
        fuzzerContext.getFuzzConfig().setPercentOfAll(0.00f);
        signatureFuzzer.fuzz(signature);
        assertNotEquals(teststring, signature.getId());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzTargetFormat() {
        assertFalse(signature.hasTargetFormat());
        signatureFuzzer.fuzz(signature);
        assertTrue(signature.hasTargetFormat());
        val teststring = fuzzerContext.getStringFuzz().generateRandom();
        signature.setTargetFormat(teststring);
        fuzzerContext.getFuzzConfig().setPercentOfAll(0.00f);
        signatureFuzzer.fuzz(signature);
        assertNotEquals(teststring, signature.getTargetFormat());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzData() {
        assertFalse(signature.hasData());
        signatureFuzzer.fuzz(signature);
        assertTrue(signature.hasData());
        val teststring = fuzzerContext.getStringFuzz().generateRandom().getBytes();
        signature.setData(teststring.clone());
        fuzzerContext.getFuzzConfig().setPercentOfAll(0.00f);
        signatureFuzzer.fuzz(signature);
        assertNotEquals(teststring, signature.getData());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzSiFormat() {
        assertFalse(signature.hasSigFormat());
        signatureFuzzer.fuzz(signature);
        assertTrue(signature.hasSigFormat());
        val teststring = fuzzerContext.getStringFuzz().generateRandom();
        signature.setSigFormat(teststring);
        fuzzerContext.getFuzzConfig().setPercentOfAll(0.00f);
        signatureFuzzer.fuzz(signature);
        assertNotEquals(teststring, signature.getSigFormat());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzWhen() {
        assertFalse(signature.hasWhen());
        signatureFuzzer.fuzz(signature);
        assertTrue(signature.hasWhen());
        val testDate = fuzzerContext.getRandomDate();
        val testLong = testDate.getTime();
        signature.setWhen(testDate);
        fuzzerContext.getFuzzConfig().setPercentOfAll(0.00f);
        signatureFuzzer.fuzz(signature);
        assertNotEquals(testLong, signature.getWhen().getTime());
    }


    @RepeatedTest(REPETITIONS)
    void generateRandom() {
        assertNotNull(signatureFuzzer.generateRandom().getType().get(0));
    }

    @RepeatedTest(REPETITIONS)
    void getContext() {
        assertNotNull(signatureFuzzer.getContext());
    }
}
