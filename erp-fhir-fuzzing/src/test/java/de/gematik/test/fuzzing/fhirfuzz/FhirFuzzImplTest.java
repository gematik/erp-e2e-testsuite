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

package de.gematik.test.fuzzing.fhirfuzz;

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.MetaFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FhirFuzzImplTest {
    private static final String TESTSTRING = "very short TestSTRING";
    private static FuzzConfig fuzzConfig;
    private static FuzzerContext fuzzerContext;
    private static FhirFuzzImpl fhirFuzz;
    private Bundle bundle;

    @BeforeAll
    static void setUpConf() {
        fuzzConfig = new FuzzConfig();
        fuzzConfig.setUseAllMutators(true);
        fuzzerContext = new FuzzerContext(fuzzConfig);
        fhirFuzz = new FhirFuzzImpl(fuzzerContext);
    }

    @BeforeEach
    void setupComp() {
        fuzzConfig.setPercentOfEach(100.0f);
        fuzzConfig.setPercentOfAll(100.0f);
        bundle = new Bundle();
    }

    @Test
    void getContext() {
        assertNotNull(fhirFuzz.getContext());
    }

    @Test
    void shouldFuzzIdentifier() {
        assertFalse(bundle.hasIdentifier());
        fhirFuzz.fuzz(bundle);
        assertTrue(bundle.hasIdentifier());
        fhirFuzz.fuzz(bundle);
         Identifier identifier = new Identifier();
        val teststring = "123.345.5678";
        bundle.setIdentifier(identifier.setSystem(teststring));
        fuzzConfig.setPercentOfAll(0.00f);
        fhirFuzz.fuzz(bundle);
        assertNotEquals(teststring, bundle.getIdentifier().getSystem());
    }

    @Test
    void shouldFuzzId() {
        assertFalse(bundle.hasId());
        fhirFuzz.fuzz(bundle);
        assertTrue(bundle.hasId());
        fhirFuzz.fuzz(bundle);
        val teststring = "123.345.5678";
        bundle.setId(teststring);
        fuzzConfig.setPercentOfAll(0.00f);
        fhirFuzz.fuzz(bundle);
        assertNotEquals(teststring, bundle.getId());
    }

    @Test
    void shouldFuzzType() {
        assertFalse(bundle.hasType());
        fhirFuzz.fuzz(bundle);
        assertTrue(bundle.hasType());
        val type = fuzzerContext.getRandomOneOfClass(Bundle.BundleType.class, Bundle.BundleType.NULL);
        val typeAsString = type.toString();
        bundle.setType(type);
        fuzzConfig.setPercentOfAll(0.00f);
        fhirFuzz.fuzz(bundle);
        assertNotEquals(typeAsString, bundle.getType().toString());
    }

    @Test
    void shouldFuzzMeta() {
        assertFalse(bundle.hasMeta());
        fhirFuzz.fuzz(bundle);
        assertTrue(bundle.hasMeta());
        assertNotNull(bundle.getMeta());
        fhirFuzz.fuzz(bundle);
        val meta = fuzzerContext.getTypeFuzzerFor(Meta.class, () -> new MetaFuzzerImpl(fuzzerContext)).generateRandom();
        bundle.setMeta(meta.copy());
        fuzzConfig.setPercentOfAll(0.00f);
        fhirFuzz.fuzz(bundle);
        assertNotEquals(meta.getProfile(), bundle.getMeta().getProfile());
    }

    @Test
    void shouldFuzzLanguage() {
        assertFalse(bundle.hasLanguage());
        fhirFuzz.fuzz(bundle);
        assertTrue(bundle.hasLanguage());
        fhirFuzz.fuzz(bundle);
        val lang = fuzzerContext.getStringFuzz().generateRandom(150);
        bundle.setLanguage(lang);
        fuzzConfig.setPercentOfAll(0.00f);
        fhirFuzz.fuzz(bundle);
        assertNotEquals(lang, bundle.getLanguage());
    }


}