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

package de.gematik.test.fuzzing.fhirfuzz.impl.resourcefuzzer;

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.CodingTypeFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ExtensionFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.IdentifierFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.MetaFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.RatioTypeFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Ratio;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MedicationFuzzImplTest {
    private static FuzzConfig fuzzConfig;
    private static FuzzerContext fuzzerContext;

    private static MedicationFuzzImpl medicationFuzz;
    private Medication medication;


    @BeforeAll
    static void setUpConf() {
        fuzzConfig = new FuzzConfig();
        fuzzConfig.setPercentOfEach(100.0f);
        fuzzConfig.setPercentOfAll(100.0f);
        fuzzConfig.setUseAllMutators(true);
        fuzzConfig.setDetailSetup(new HashMap<>());
        fuzzerContext = new FuzzerContext(fuzzConfig);
        medicationFuzz = new MedicationFuzzImpl(fuzzerContext);
    }

    @BeforeEach
    void setupComp() {
        fuzzConfig.setPercentOfEach(100.0f);
        fuzzConfig.setPercentOfAll(100.0f);
        medication = new Medication();
    }


    @Test
    void generateRandom() {
        assertNotNull(medicationFuzz.generateRandom().getId());
    }

    @RepeatedTest(REPETITIONS)
    void getContext() {
        assertNotNull(medicationFuzz.getContext());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzId() {
        assertFalse(medication.hasId());
        medicationFuzz.fuzz(medication);
        assertTrue(medication.hasId());
        medicationFuzz.fuzz(medication);
        val teststring = fuzzerContext.getStringFuzz().generateRandom(150);
        medication.setId(teststring);
        fuzzConfig.setPercentOfAll(0.00f);
        medicationFuzz.fuzz(medication);
        assertNotEquals(teststring, medication.getId());
    }

    @RepeatedTest(REPETITIONS)
    void shoulFuzzLanguage() {
        assertFalse(medication.hasLanguage());
        medicationFuzz.fuzz(medication);
        assertTrue(medication.hasLanguage());
        medicationFuzz.fuzz(medication);
        val teststring = "123.345.5678";
        medication.setLanguage((teststring));
        fuzzConfig.setPercentOfAll(0.00f);
        medicationFuzz.fuzz(medication);
        assertNotEquals(teststring, medication.getLanguage());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzMeta() {
        assertFalse(medication.hasMeta());
        medicationFuzz.fuzz(medication);
        assertTrue(medication.hasMeta());
        medicationFuzz.fuzz(medication);
        val meta = fuzzerContext.getTypeFuzzerFor(Meta.class, () -> new MetaFuzzerImpl(fuzzerContext)).generateRandom();
        medication.setMeta(meta.copy());
        fuzzConfig.setPercentOfAll(0.00f);
        medicationFuzz.fuzz(medication);
        assertNotEquals(meta.getProfile(), medication.getMeta().getProfile());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzIdentifier() {
        assertFalse(medication.hasIdentifier());
        medicationFuzz.fuzz(medication);
        medicationFuzz.fuzz(medication);
        assertFalse(medication.hasIdentifier());
        val identiList = List.of(fuzzerContext.getTypeFuzzerFor(Identifier.class, () -> new IdentifierFuzzerImpl(fuzzerContext)).generateRandom());
        val input = identiList.get(0).getValue();
        medication.setIdentifier(identiList);
        fuzzConfig.setPercentOfAll(0.00f);
        medicationFuzz.fuzz(medication);
        assertNotEquals(input, medication.getIdentifier().get(0).getValue());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzForm() {
        assertFalse(medication.hasForm());
        medicationFuzz.fuzz(medication);
        assertTrue(medication.hasForm());
        fuzzConfig.setPercentOfAll(100.00f);
        medicationFuzz.fuzz(medication);
        val coding = fuzzerContext.getTypeFuzzerFor(Coding.class, () -> new CodingTypeFuzzerImpl(fuzzerContext)).generateRandom();
        List<Coding> codeList = new LinkedList<>();
        codeList.add(coding);
        val type = new CodeableConcept().setCoding(codeList);
        medication.setForm(type.copy());
        fuzzConfig.setPercentOfAll(0.00f);
        medicationFuzz.fuzz(medication);
        assertNotEquals(type.getCodingFirstRep(), medication.getForm().getCodingFirstRep());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzExtension() {
        assertFalse(medication.hasExtension());
        medicationFuzz.fuzz(medication);
        assertTrue(medication.hasExtension());
        medicationFuzz.fuzz(medication);
        assertFalse(medication.hasExtension());
        val ext = fuzzerContext.getTypeFuzzerFor(Extension.class, () -> new ExtensionFuzzerImpl(fuzzerContext)).generateRandom();
        medication.setExtension(List.of(ext.copy()));
        fuzzConfig.setPercentOfAll(0.00f);
        medicationFuzz.fuzz(medication);
        assertNotEquals(ext.getUrl(), medication.getExtension().get(0).getUrl());
    }


    @RepeatedTest(REPETITIONS)
    void shouldFuzzCode() {
        assertFalse(medication.hasForm());
        medicationFuzz.fuzz(medication);
        assertTrue(medication.hasForm());
        fuzzConfig.setPercentOfAll(100.00f);
        medicationFuzz.fuzz(medication);
        val coding = fuzzerContext.getTypeFuzzerFor(Coding.class, () -> new CodingTypeFuzzerImpl(fuzzerContext)).generateRandom();
        List<Coding> codeList = new LinkedList<>();
        codeList.add(coding);
        val type = new CodeableConcept().setCoding(codeList);
        medication.setCode(type.copy());
        fuzzConfig.setPercentOfAll(0.00f);
        medicationFuzz.fuzz(medication);
        assertNotEquals(type.getCodingFirstRep(), medication.getCode().getCodingFirstRep());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzStatus() {
        assertFalse(medication.hasStatus());
        medicationFuzz.fuzz(medication);
        assertTrue(medication.hasStatus());
        fuzzConfig.setPercentOfAll(100.00f);
        medicationFuzz.fuzz(medication);
        val status = fuzzerContext.getRandomOneOfClass(Medication.MedicationStatus.class, List.of(Medication.MedicationStatus.NULL));
        medication.setStatus(status);
        fuzzConfig.setPercentOfAll(0.00f);
        medicationFuzz.fuzz(medication);
        assertNotEquals(status, medication.getStatus());
    }

    @RepeatedTest(REPETITIONS)
    void shouldAcceptDetailSetupAndFuzzesCodeText() {
        assertFalse(medication.hasCode());
        medicationFuzz.fuzz(medication);
        assertTrue(medication.hasCode());
        medication.getCode().setText("123");
        assertFalse(medication.getCode().getText().length() > 50);
        fuzzerContext.getFuzzConfig().getDetailSetup().put("BreakRanges", "TRUE");
        medicationFuzz.fuzz(medication);
        assertTrue(medication.getCode().getText().length() > 50);
        fuzzerContext.getFuzzConfig().getDetailSetup().remove("BreakRanges");

    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzAmount() {
        assertFalse(medication.hasAmount());
        medicationFuzz.fuzz(medication);
        assertTrue(medication.hasAmount());
        fuzzConfig.setPercentOfAll(100.00f);
        medicationFuzz.fuzz(medication);
        val ratio = fuzzerContext.getTypeFuzzerFor(Ratio.class, () -> new RatioTypeFuzzerImpl(fuzzerContext)).generateRandom();
        medication.setAmount(ratio.copy());
        fuzzConfig.setPercentOfAll(0.00f);
        medicationFuzz.fuzz(medication);
        assertNotEquals(ratio, medication.getAmount());
    }
}