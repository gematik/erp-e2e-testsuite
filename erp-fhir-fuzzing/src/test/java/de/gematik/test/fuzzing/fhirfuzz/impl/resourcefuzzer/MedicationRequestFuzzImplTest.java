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

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.DosageFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ExtensionFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.IdentifierFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.MetaFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.SimpleQuantityFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MedicationRequestFuzzImplTest {

    private static FuzzConfig fuzzConfig;
    private static FuzzerContext fuzzerContext;

    private static MedicationRequestFuzzImpl medicationRequestFuzz;
    private MedicationRequest medicationRe;



    @BeforeAll
    static void setUpConf() {
        fuzzConfig = new FuzzConfig();
        fuzzConfig.setPercentOfEach(100.0f);
        fuzzConfig.setPercentOfAll(100.0f);
        fuzzConfig.setUseAllMutators(true);
        fuzzerContext = new FuzzerContext(fuzzConfig);
        medicationRequestFuzz = new MedicationRequestFuzzImpl(fuzzerContext);
    }

    @BeforeEach
    void setupComp() {
        fuzzConfig.setPercentOfEach(100.0f);
        fuzzConfig.setPercentOfAll(100.0f);
        medicationRe = new MedicationRequest();
    }


    @RepeatedTest(REPETITIONS)
    void generateRandom() {
        assertNotNull(medicationRequestFuzz.generateRandom().getId());
    }

    @Test
    void getContext() {
        assertNotNull(medicationRequestFuzz.getContext());
    }


    @RepeatedTest(REPETITIONS)
    void shouldFuzzLanguage() {
        assertFalse(medicationRe.hasLanguage());
        medicationRequestFuzz.fuzz(medicationRe);
        assertTrue(medicationRe.hasLanguage());
        medicationRequestFuzz.fuzz(medicationRe);
        val teststring = "123.345.5678";
        medicationRe.setLanguage((teststring));
        fuzzConfig.setPercentOfAll(0.00f);
        medicationRequestFuzz.fuzz(medicationRe);
        assertNotEquals(teststring, medicationRe.getLanguage());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzStatus() {
        assertFalse(medicationRe.hasStatus());
        medicationRequestFuzz.fuzz(medicationRe);
        assertTrue(medicationRe.hasStatus());
        medicationRequestFuzz.fuzz(medicationRe);
        val testObject = fuzzerContext.getRandomOneOfClass(MedicationRequest.MedicationRequestStatus.class);
        medicationRe.setStatus((testObject));
        fuzzConfig.setPercentOfAll(0.00f);
        medicationRequestFuzz.fuzz(medicationRe);
        assertNotEquals(testObject, medicationRe.getStatus());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzSubj() {
        assertFalse(medicationRe.hasSubject());
        medicationRequestFuzz.fuzz(medicationRe);
        assertTrue(medicationRe.hasSubject());
        medicationRequestFuzz.fuzz(medicationRe);
        fuzzerContext.getTypeFuzzerFor(Reference.class).ifPresent(tf -> tf.generateRandom());
        val testObject = medicationRe.getSubject();
        medicationRe.setSubject(testObject.copy());
        fuzzConfig.setPercentOfAll(0.00f);
        medicationRequestFuzz.fuzz(medicationRe);
        assertNotEquals(testObject, medicationRe.getSubject());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzNote() {
        assertFalse(medicationRe.hasNote());
        medicationRequestFuzz.fuzz(medicationRe);
        assertTrue(medicationRe.hasNote());
        medicationRequestFuzz.fuzz(medicationRe);
        fuzzerContext.getTypeFuzzerFor(Annotation.class).ifPresent(tf -> medicationRe.setNote(List.of(tf.generateRandom())));
        val testObject = medicationRe.getNote().get(0);
        medicationRe.setNote(List.of(testObject.copy()));
        fuzzConfig.setPercentOfAll(0.00f);
        medicationRequestFuzz.fuzz(medicationRe);
        assertNotEquals(testObject.getText(), medicationRe.getNote().get(0).getText());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzInsurance() {
        assertFalse(medicationRe.hasInsurance());
        medicationRequestFuzz.fuzz(medicationRe);
        assertTrue(medicationRe.hasInsurance());
        medicationRequestFuzz.fuzz(medicationRe);
        fuzzerContext.getTypeFuzzerFor(Reference.class).ifPresent(tf -> medicationRe.setInsurance(List.of(tf.generateRandom())));
        val testObject = medicationRe.getInsurance().get(0);
        medicationRe.setInsurance(List.of(testObject.copy()));
        fuzzConfig.setPercentOfAll(0.00f);
        medicationRequestFuzz.fuzz(medicationRe);
        assertNotEquals(testObject.getReference(), medicationRe.getInsuranceFirstRep().getReference());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzRequester() {
        assertFalse(medicationRe.hasRequester());
        medicationRequestFuzz.fuzz(medicationRe);
        assertTrue(medicationRe.hasRequester());
        medicationRequestFuzz.fuzz(medicationRe);
        fuzzerContext.getTypeFuzzerFor(Reference.class).ifPresent(tf -> medicationRe.setRequester(tf.generateRandom()));
        val testObject = medicationRe.getRequester();
        medicationRe.setRequester(testObject.copy());
        fuzzConfig.setPercentOfAll(0.00f);
        medicationRequestFuzz.fuzz(medicationRe);
        assertNotEquals(testObject, medicationRe.getRequester());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzMedication() {
        assertFalse(medicationRe.hasMedication());
        medicationRequestFuzz.fuzz(medicationRe);
        assertTrue(medicationRe.hasMedication());
        medicationRequestFuzz.fuzz(medicationRe);
        fuzzerContext.getTypeFuzzerFor(Reference.class).ifPresent(tf -> tf.generateRandom());
        val testObject = medicationRe.getMedication();
        medicationRe.setMedication(testObject.copy());
        fuzzConfig.setPercentOfAll(0.00f);
        medicationRequestFuzz.fuzz(medicationRe);
        assertNotEquals(testObject, medicationRe.getMedication());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzIntend() {
        assertFalse(medicationRe.hasIntent());
        medicationRequestFuzz.fuzz(medicationRe);
        assertTrue(medicationRe.hasIntent());
        medicationRequestFuzz.fuzz(medicationRe);
        val testObject = fuzzerContext.getRandomOneOfClass(MedicationRequest.MedicationRequestIntent.class);
        medicationRe.setIntent((testObject));
        fuzzConfig.setPercentOfAll(0.00f);
        medicationRequestFuzz.fuzz(medicationRe);
        assertNotEquals(testObject, medicationRe.getIntent());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzMeta() {
        assertFalse(medicationRe.hasMeta());
        medicationRequestFuzz.fuzz(medicationRe);
        assertTrue(medicationRe.hasMeta());
        medicationRequestFuzz.fuzz(medicationRe);
        val meta = fuzzerContext.getTypeFuzzerFor(Meta.class, () -> new MetaFuzzerImpl(fuzzerContext)).generateRandom();
        medicationRe.setMeta(meta.copy());
        fuzzConfig.setPercentOfAll(0.00f);
        medicationRequestFuzz.fuzz(medicationRe);
        assertNotEquals(meta.getProfile(), medicationRe.getMeta().getProfile());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzIdentifier() {
        assertFalse(medicationRe.hasIdentifier());
        medicationRequestFuzz.fuzz(medicationRe);
        assertTrue(medicationRe.hasIdentifier());
        medicationRequestFuzz.fuzz(medicationRe);
        assertFalse(medicationRe.hasIdentifier());
        val identiList = List.of(fuzzerContext.getTypeFuzzerFor(Identifier.class, () -> new IdentifierFuzzerImpl(fuzzerContext)).generateRandom());
        val input = identiList.get(0).getValue();
        medicationRe.setIdentifier(identiList);
        fuzzConfig.setPercentOfAll(0.00f);
        medicationRequestFuzz.fuzz(medicationRe);
        assertNotEquals(input, medicationRe.getIdentifier().get(0).getValue());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzDispRequest() {
        assertFalse(medicationRe.hasDispenseRequest());
        medicationRequestFuzz.fuzz(medicationRe);
        assertTrue(medicationRe.hasDispenseRequest());
        val input = new MedicationRequest.MedicationRequestDispenseRequestComponent().setQuantity(
                fuzzerContext.getTypeFuzzerFor(Quantity.class, () -> new SimpleQuantityFuzzImpl(fuzzerContext)).generateRandom());
        medicationRe.setDispenseRequest(input.copy());
        medicationRequestFuzz.fuzz(medicationRe);
        assertNotEquals(input.getQuantity(), medicationRe.getDispenseRequest().getQuantity());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzExtension() {
        assertFalse(medicationRe.hasExtension());
        medicationRequestFuzz.fuzz(medicationRe);
        assertTrue(medicationRe.hasExtension());
        medicationRequestFuzz.fuzz(medicationRe);
        assertFalse(medicationRe.hasExtension());
        val ext = fuzzerContext.getTypeFuzzerFor(Extension.class, () -> new ExtensionFuzzerImpl(fuzzerContext)).generateRandom();
        medicationRe.setExtension(List.of(ext.copy()));
        fuzzConfig.setPercentOfAll(0.00f);
        medicationRequestFuzz.fuzz(medicationRe);
        assertNotEquals(ext.getUrl(), medicationRe.getExtension().get(0).getUrl());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzCompDate() {
        assertFalse(medicationRe.hasAuthoredOn());
        medicationRequestFuzz.fuzz(medicationRe);
        assertTrue(medicationRe.hasAuthoredOn());
        fuzzConfig.setPercentOfAll(100.00f);
        medicationRequestFuzz.fuzz(medicationRe);
        assertFalse(medicationRe.hasAuthoredOn());
        val date = new Date(fuzzerContext.generateFakeLong());
        medicationRe.setAuthoredOn(new Date(date.getTime()));
        fuzzConfig.setPercentOfAll(0.00f);
        medicationRequestFuzz.fuzz(medicationRe);
        assertNotEquals(date.getTime(), medicationRe.getAuthoredOn().getTime());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzDosage() {
        assertFalse(medicationRe.hasDosageInstruction());
        medicationRequestFuzz.fuzz(medicationRe);
        assertTrue(medicationRe.hasDosageInstruction());
        fuzzConfig.setPercentOfAll(100.00f);
        medicationRequestFuzz.fuzz(medicationRe);
        assertFalse(medicationRe.hasDosageInstruction());
        val dosage = fuzzerContext.getTypeFuzzerFor(Dosage.class, () -> new DosageFuzzImpl(fuzzerContext)).generateRandom();
        medicationRe.setDosageInstruction(List.of(dosage.copy()));
        fuzzConfig.setPercentOfAll(0.00f);
        medicationRequestFuzz.fuzz(medicationRe);
        assertNotEquals(dosage.getText(), medicationRe.getDosageInstruction().get(0).getText());
    }

    @RepeatedTest(REPETITIONS)
    void shouldAcceptDetailSetup() {
        fuzzerContext.getFuzzConfig().setDetailSetup(new HashMap<>());
        fuzzerContext.getFuzzConfig().getDetailSetup().put("KBV", "TRUE");
        assertFalse(medicationRe.hasIdentifier());
        medicationRequestFuzz.fuzz(medicationRe);
        assertFalse(medicationRe.hasIdentifier());
        fuzzerContext.getFuzzConfig().getDetailSetup().remove("KBV");
        medicationRequestFuzz.fuzz(medicationRe);
        assertTrue(medicationRe.hasIdentifier());
        fuzzConfig.setPercentOfAll(100.00f);
        val identiList = List.of(fuzzerContext.getTypeFuzzerFor(Identifier.class, () -> new IdentifierFuzzerImpl(fuzzerContext)).generateRandom());
        val input = identiList.get(0).getValue();
        medicationRe.setIdentifier(identiList);
        fuzzConfig.setPercentOfAll(0.00f);
        medicationRequestFuzz.fuzz(medicationRe);
        assertNotEquals(input, medicationRe.getIdentifier().get(0).getValue());
    }
}
