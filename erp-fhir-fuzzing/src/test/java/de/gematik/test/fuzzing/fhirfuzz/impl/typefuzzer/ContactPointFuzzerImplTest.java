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

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ContactPointFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.PeriodFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Period;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ContactPointFuzzerImplTest {
    private static FuzzConfig fuzzConfig;
    private static FuzzerContext fuzzerContext;
    private static ContactPointFuzzImpl pointFuzz;
    private ContactPoint contactPoint;

    @BeforeAll
    static void setUpConf() {
        fuzzConfig = new FuzzConfig();
        fuzzConfig.setPercentOfEach(100.0f);
        fuzzConfig.setPercentOfAll(100.0f);
        fuzzConfig.setUseAllMutators(true);
        fuzzerContext = new FuzzerContext(fuzzConfig);
        pointFuzz = new ContactPointFuzzImpl(fuzzerContext);
    }

    @BeforeEach
    void setupComp() {
        fuzzConfig.setPercentOfEach(100.0f);
        fuzzConfig.setPercentOfAll(100.0f);
        contactPoint = new ContactPoint();
    }

    @RepeatedTest(REPETITIONS)
    void getContext() {
        assertNotNull(pointFuzz.generateRandom());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzSystem() {
        assertFalse(contactPoint.hasSystem());
        pointFuzz.fuzz(contactPoint);
        Assertions.assertTrue(contactPoint.hasSystem());
        val testObject = fuzzerContext.getRandomOneOfClass(ContactPoint.ContactPointSystem.class, ContactPoint.ContactPointSystem.NULL);
        contactPoint.setSystem(testObject);
        fuzzConfig.setPercentOfAll(0.00f);
        pointFuzz.fuzz(contactPoint);
        assertNotEquals(testObject, contactPoint.getSystem());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzValue() {
        assertFalse(contactPoint.hasValue());
        pointFuzz.fuzz(contactPoint);
        Assertions.assertTrue(contactPoint.hasValue());
        val testObject = fuzzerContext.getStringFuzz().generateRandom(50);
        contactPoint.setValue(testObject);
        fuzzConfig.setPercentOfAll(0.00f);
        pointFuzz.fuzz(contactPoint);
        assertNotEquals(testObject, contactPoint.getValue());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzUse() {
        assertFalse(contactPoint.hasUse());
        pointFuzz.fuzz(contactPoint);
        Assertions.assertTrue(contactPoint.hasUse());
        val testObject = fuzzerContext.getRandomOneOfClass(ContactPoint.ContactPointUse.class, ContactPoint.ContactPointUse.NULL);
        contactPoint.setUse(testObject);
        fuzzConfig.setPercentOfAll(0.00f);
        pointFuzz.fuzz(contactPoint);
        assertNotEquals(testObject, contactPoint.getUse());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzRank() {
        assertFalse(contactPoint.hasRank());
        pointFuzz.fuzz(contactPoint);
        Assertions.assertTrue(contactPoint.hasRank());
        val testObject = fuzzerContext.getIntFuzz().generateRandom();
        contactPoint.setRank(testObject);
        fuzzConfig.setPercentOfAll(0.00f);
        pointFuzz.fuzz(contactPoint);
        assertNotEquals(testObject, contactPoint.getRank());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzPeriod() {
        assertFalse(contactPoint.hasPeriod());
        pointFuzz.fuzz(contactPoint);
        Assertions.assertTrue(contactPoint.hasPeriod());
        val testObject = fuzzerContext.getTypeFuzzerFor(Period.class, () -> new PeriodFuzzerImpl(fuzzerContext)).generateRandom();
        contactPoint.setPeriod(testObject.copy());
        fuzzConfig.setPercentOfAll(0.00f);
        pointFuzz.fuzz(contactPoint);
        assertNotEquals(testObject, contactPoint.getPeriod());
    }


    @RepeatedTest(REPETITIONS)
    void generateRandom() {
        assertNotNull(pointFuzz.generateRandom());
    }
}
