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

package de.gematik.test.fuzzing.fhirfuzz.impl.resourcefuzzer;

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.CodingTypeFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.MetaFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ReferenceFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompositionFuzzImplTest {
    private static FuzzConfig fuzzConfig;
    private static FuzzerContext fuzzerContext;
    private static CompositionFuzzImpl compFuzzer;
    private Composition composition;
    private static final String TESTSTRING = "TestSTRING";



    @BeforeAll
    static void setUpConf() {
        fuzzConfig = new FuzzConfig();
        fuzzConfig.setPercentOfEach(100.0f);
        fuzzConfig.setPercentOfAll(100.0f);
        fuzzConfig.setUseAllMutators(true);
        fuzzConfig.setDetailSetup(new HashMap<>());
        fuzzerContext = new FuzzerContext(fuzzConfig);
        compFuzzer = new CompositionFuzzImpl(fuzzerContext);
    }

    @BeforeEach
    void setupComp() {
        fuzzConfig.setPercentOfEach(100.0f);
        fuzzConfig.setPercentOfAll(100.0f);
        composition = new Composition();
    }

    @RepeatedTest(REPETITIONS)
    void shouldGetRandomCompStatus() {
        assertNotNull(fuzzerContext.getRandomOneOfClass(Composition.CompositionStatus.class));
    }

    @Test
    void getRandomEnum() {
        val e = fuzzerContext.getRandomOneOfClass(Composition.CompositionStatus.class);
        assertNotNull(e);
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzId() {
        assertFalse(composition.hasId());
        compFuzzer.fuzz(composition);
        assertTrue(composition.hasId());
        compFuzzer.fuzz(composition);
        val teststring = fuzzerContext.getIdFuzzer().generateRandom();
        composition.setId(teststring);
        fuzzConfig.setPercentOfAll(0.00f);
        compFuzzer.fuzz(composition);
        assertNotEquals(teststring, composition.getId());
    }

    @RepeatedTest(REPETITIONS)
    void shoulFuzzIdentifier() {
        assertFalse(composition.hasIdentifier());
        compFuzzer.fuzz(composition);
        assertTrue(composition.hasIdentifier());
        compFuzzer.fuzz(composition);
        Identifier identifier = new Identifier();
        val teststring = "123.345.5678";
        composition.setIdentifier(identifier.setSystem(teststring));
        fuzzConfig.setPercentOfAll(0.00f);
        compFuzzer.fuzz(composition);
        assertNotEquals(teststring, composition.getIdentifier().getSystem());
    }

    @RepeatedTest(REPETITIONS)
    void shoulFuzzLanguage() {
        assertFalse(composition.hasLanguage());
        compFuzzer.fuzz(composition);
        assertTrue(composition.hasLanguage());
        compFuzzer.fuzz(composition);
        val teststring = "123.345.5678";
        composition.setLanguage((teststring));
        fuzzConfig.setPercentOfAll(0.00f);
        compFuzzer.fuzz(composition);
        assertNotEquals(teststring, composition.getLanguage());
    }


    @RepeatedTest(REPETITIONS)
    void shouldFuzzCompStatus() {
        assertFalse(composition.hasStatus());
        compFuzzer.fuzz(composition);
        assertTrue(composition.hasStatus());
        assertNotNull(composition.getStatus());
        val status = fuzzerContext.getRandomOneOfClass(Composition.CompositionStatus.class);
        composition.setStatus(status);
        fuzzConfig.setPercentOfAll(0.00f);
        compFuzzer.fuzz(composition);
        assertNotEquals(status, composition.getStatus());

    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzCompMeta() {
        assertFalse(composition.hasMeta());
        compFuzzer.fuzz(composition);
        assertTrue(composition.hasMeta());
        assertNotNull(composition.getMeta());
        compFuzzer.fuzz(composition);
        val metaFuzzer = fuzzerContext.getTypeFuzzerFor(Meta.class, () -> new MetaFuzzerImpl(fuzzerContext));
        val meta = metaFuzzer.generateRandom();
        composition.setMeta(meta.copy());
        fuzzConfig.setPercentOfAll(0.00f);
        compFuzzer.fuzz(composition);
        assertNotEquals(meta.getProfile(), composition.getMeta().getProfile());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzCompType() {
        assertFalse(composition.hasType());
        compFuzzer.fuzz(composition);
        assertTrue(composition.hasType());
        fuzzConfig.setPercentOfAll(100.00f);
        compFuzzer.fuzz(composition);
        val codingTypeFuzzerImpl = fuzzerContext.getTypeFuzzerFor(Coding.class);
        List<Coding> list = new LinkedList<>();
        list.add(fuzzerContext.getTypeFuzzerFor(Coding.class, () -> new CodingTypeFuzzerImpl(fuzzerContext)).generateRandom());
        val type = new CodeableConcept().setCoding(list);
        composition.setType(type.copy());
        fuzzConfig.setPercentOfAll(0.00f);
        compFuzzer.fuzz(composition);
        assertNotEquals(type.getCodingFirstRep(), composition.getType().getCodingFirstRep());
    }


    @RepeatedTest(REPETITIONS)
    void shouldFuzzCompSubject() {
        assertFalse(composition.hasSubject());
        compFuzzer.fuzz(composition);
        assertTrue(composition.hasSubject());
        fuzzConfig.setPercentOfAll(100.00f);
        compFuzzer.fuzz(composition);
        val ref = fuzzerContext.getTypeFuzzerFor(Reference.class, () -> new ReferenceFuzzerImpl(fuzzerContext)).generateRandom();
        composition.setSubject(ref.copy());
        fuzzConfig.setPercentOfAll(0.00f);
        compFuzzer.fuzz(composition);
        assertNotEquals(ref.getReference(), composition.getSubject().getReference());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzCompEncounter() {
        assertFalse(composition.hasEncounter());
        compFuzzer.fuzz(composition);
        assertTrue(composition.hasEncounter());
        fuzzConfig.setPercentOfAll(100.00f);
        compFuzzer.fuzz(composition);
        val ref = fuzzerContext.getTypeFuzzerFor(Reference.class, () -> new ReferenceFuzzerImpl(fuzzerContext)).generateRandom();
        composition.setEncounter(ref.copy());
        fuzzConfig.setPercentOfAll(0.00f);
        compFuzzer.fuzz(composition);
        assertNotEquals(ref.getReference(), composition.getEncounter().getReference());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzCompDate() {
        assertFalse(composition.hasDate());
        compFuzzer.fuzz(composition);
        assertTrue(composition.hasDate());
        fuzzConfig.setPercentOfAll(100.00f);
        compFuzzer.fuzz(composition);
        assertFalse(composition.hasDate());
        val date = new Date(fuzzerContext.generateFakeLong());
        composition.setDate(new Date(date.getTime()));
        fuzzConfig.setPercentOfAll(0.00f);
        compFuzzer.fuzz(composition);
        assertNotEquals(date.getTime(), composition.getDate().getTime());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzCompAuthor() {
        assertFalse(composition.hasAuthor());
        compFuzzer.fuzz(composition);
        assertTrue(composition.hasAuthor());
        fuzzConfig.setPercentOfAll(100.00f);
        compFuzzer.fuzz(composition);
        assertFalse(composition.hasAuthor());
        val ref = fuzzerContext.getTypeFuzzerFor(Reference.class, () -> new ReferenceFuzzerImpl(fuzzerContext)).generateRandom();
        composition.setAuthor(List.of(ref.copy()));
        fuzzConfig.setPercentOfAll(0.00f);
        compFuzzer.fuzz(composition);
        assertNotEquals(ref.getReference(), composition.getAuthor().get(0).getReference());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzCompTitle() {
        assertFalse(composition.hasTitle());
        compFuzzer.fuzz(composition);
        assertTrue(composition.hasTitle());
        fuzzConfig.setPercentOfAll(100.00f);
        compFuzzer.fuzz(composition);
        val ref = TESTSTRING;
        composition.setTitle(ref);
        fuzzConfig.setPercentOfAll(0.00f);
        compFuzzer.fuzz(composition);
        assertNotEquals(ref, composition.getTitle());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzCompConfidentiality() {
        assertFalse(composition.hasConfidentiality());
        compFuzzer.fuzz(composition);
        assertTrue(composition.hasConfidentiality());
        fuzzConfig.setPercentOfAll(100.00f);
        compFuzzer.fuzz(composition);
        val ref = TESTSTRING;
        composition.setConfidentiality(fuzzerContext.getRandomOneOfClass(Composition.DocumentConfidentiality.class));
        fuzzConfig.setPercentOfAll(0.00f);
        compFuzzer.fuzz(composition);
        assertNotEquals(ref, composition.getConfidentiality());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzCompCustodian() {
        assertFalse(composition.hasCustodian());
        compFuzzer.fuzz(composition);
        assertTrue(composition.hasCustodian());
        fuzzConfig.setPercentOfAll(100.00f);
        compFuzzer.fuzz(composition);
        val ref = fuzzerContext.getTypeFuzzerFor(Reference.class, () -> new ReferenceFuzzerImpl(fuzzerContext)).generateRandom();
        composition.setCustodian(ref.copy());
        fuzzConfig.setPercentOfAll(0.00f);
        compFuzzer.fuzz(composition);
        assertNotEquals(ref, composition.getCustodian());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzCompRelatesTo() {
        assertFalse(composition.hasRelatesTo());
        compFuzzer.fuzz(composition);
        assertTrue(composition.hasRelatesTo());
        fuzzConfig.setPercentOfAll(100.00f);
        compFuzzer.fuzz(composition);
        val ref = new Composition.CompositionRelatesToComponent();
        composition.setRelatesTo(List.of(ref));
        fuzzConfig.setPercentOfAll(0.00f);
        compFuzzer.fuzz(composition);
        assertNotEquals(ref, composition.getRelatesToFirstRep());
    }

    @RepeatedTest(REPETITIONS)
    void shouldGenerateRandom() {
        assertNotNull(compFuzzer.generateRandom());
    }

    @Test
    void shouldGetContext() {
        assertNotNull(compFuzzer.getContext());
    }

}










