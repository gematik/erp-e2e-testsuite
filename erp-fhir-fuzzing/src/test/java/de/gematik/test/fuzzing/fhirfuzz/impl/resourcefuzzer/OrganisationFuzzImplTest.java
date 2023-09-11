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

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.AddressFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ContactPointFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ExtensionFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.IdentifierFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.MetaFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.NarrativeTypeFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Organization;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrganisationFuzzImplTest {
    private static FuzzConfig fuzzConfig;
    private static FuzzerContext fuzzerContext;
    private static OrganisationFuzzImpl organisationFuzz;
    private Organization organization;


    @BeforeAll
    static void setUpConf() {
        fuzzConfig = new FuzzConfig();
        fuzzConfig.setPercentOfEach(100.0f);
        fuzzConfig.setPercentOfAll(100.0f);
        fuzzConfig.setUseAllMutators(true);
        fuzzerContext = new FuzzerContext(fuzzConfig);
        organisationFuzz = new OrganisationFuzzImpl(fuzzerContext);
    }

    @BeforeEach
    void setupComp() {
        fuzzConfig.setPercentOfEach(100.0f);
        fuzzConfig.setPercentOfAll(100.0f);
        organization = new Organization();
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzId() {
        assertFalse(organization.hasId());
        organisationFuzz.fuzz(organization);
        assertTrue(organization.hasId());
        organisationFuzz.fuzz(organization);
        val teststring = fuzzerContext.getStringFuzz().generateRandom(150);
        organization.setId(teststring);
        fuzzConfig.setPercentOfAll(0.00f);
        organisationFuzz.fuzz(organization);
        assertNotEquals(teststring, organization.getId());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzLanguage() {
        assertFalse(organization.hasLanguage());
        organisationFuzz.fuzz(organization);
        assertTrue(organization.hasLanguage());
        organisationFuzz.fuzz(organization);
        val teststring = "123.345.5678";
        organization.setLanguage((teststring));
        fuzzConfig.setPercentOfAll(0.00f);
        organisationFuzz.fuzz(organization);
        assertNotEquals(teststring, organization.getLanguage());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzMeta() {
        assertFalse(organization.hasMeta());
        organisationFuzz.fuzz(organization);
        assertTrue(organization.hasMeta());
        organisationFuzz.fuzz(organization);
        val meta = fuzzerContext.getTypeFuzzerFor(Meta.class, () -> new MetaFuzzerImpl(fuzzerContext)).generateRandom();
        organization.setMeta(meta.copy());
        fuzzConfig.setPercentOfAll(0.00f);
        organisationFuzz.fuzz(organization);
        assertNotEquals(meta.getProfile(), organization.getMeta().getProfile());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzText() {
        assertFalse(organization.hasText());
        organisationFuzz.fuzz(organization);
        assertTrue(organization.hasText());
        val testObject = fuzzerContext.getTypeFuzzerFor(Narrative.class, () -> new NarrativeTypeFuzzImpl(fuzzerContext)).generateRandom();
        organization.setText(testObject.copy());
        fuzzConfig.setPercentOfAll(0.00f);
        organisationFuzz.fuzz(organization);
        assertNotEquals(testObject, organization.getText());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzIdentifier() {
        assertFalse(organization.hasIdentifier());
        organisationFuzz.fuzz(organization);
        organisationFuzz.fuzz(organization);
        assertFalse(organization.hasIdentifier());
        val identiList = List.of(fuzzerContext.getTypeFuzzerFor(Identifier.class, () -> new IdentifierFuzzerImpl(fuzzerContext)).generateRandom());
        val input = identiList.get(0).getValue();
        organization.setIdentifier(identiList);
        fuzzConfig.setPercentOfAll(0.00f);
        organisationFuzz.fuzz(organization);
        assertNotEquals(input, organization.getIdentifier().get(0).getValue());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzActive() {
        assertFalse(organization.hasActive());
        organisationFuzz.fuzz(organization);
        assertTrue(organization.hasActive());
        organisationFuzz.fuzz(organization);
        assertFalse(organization.getActive());
        organization.setActive(true);
        fuzzConfig.setPercentOfAll(0.00f);
        organisationFuzz.fuzz(organization);
        assertFalse(organization.getActive());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzName() {
        assertFalse(organization.hasName());
        organisationFuzz.fuzz(organization);
        assertTrue(organization.hasName());
        val hName = fuzzerContext.getStringFuzz().generateRandom(50);
        organization.setName(hName);
        fuzzConfig.setPercentOfAll(0.00f);
        organisationFuzz.fuzz(organization);
        assertNotEquals(hName, organization.getName());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzAddress() {
        Address address = fuzzerContext.getTypeFuzzerFor(Address.class, () -> new AddressFuzzerImpl(fuzzerContext)).generateRandom();
        assertFalse(organization.hasAddress());
        organisationFuzz.fuzz(organization);
        assertTrue(organization.hasAddress());
        organisationFuzz.fuzz(organization);
        assertFalse(organization.hasAddress());
        organization.setAddress(List.of(address.copy()));
        fuzzConfig.setPercentOfAll(0.00f);
        organisationFuzz.fuzz(organization);
        assertNotEquals(address.getCity(), organization.getAddress().get(0).getCity());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzExtension() {
        assertFalse(organization.hasExtension());
        organisationFuzz.fuzz(organization);
        assertTrue(organization.hasExtension());
        organisationFuzz.fuzz(organization);
        assertFalse(organization.hasExtension());
        val ext = fuzzerContext.getTypeFuzzerFor(Extension.class, () -> new ExtensionFuzzerImpl(fuzzerContext)).generateRandom();
        organization.setExtension(List.of(ext.copy()));
        fuzzConfig.setPercentOfAll(0.00f);
        organisationFuzz.fuzz(organization);
        assertNotEquals(ext.getUrl(), organization.getExtension().get(0).getUrl());
    }

    @RepeatedTest(REPETITIONS)
    void shouldFuzzTelcom() {
        assertFalse(organization.hasTelecom());
        organisationFuzz.fuzz(organization);
        assertTrue(organization.hasTelecom());
        organisationFuzz.fuzz(organization);
        assertFalse(organization.hasTelecom());
        val ext = fuzzerContext.getTypeFuzzerFor(ContactPoint.class, () -> new ContactPointFuzzImpl(fuzzerContext)).generateRandom();
        organization.setTelecom(List.of(ext.copy()));
        fuzzConfig.setPercentOfAll(0.00f);
        organisationFuzz.fuzz(organization);
        assertNotEquals(ext.getValue(), organization.getTelecom().get(0).getValue());
    }

    @Test
    void generateRandom() {
        assertNotNull(organisationFuzz.generateRandom().getAddress());
    }

    @Test
    void getContext() {
        assertNotNull(organisationFuzz.getContext());
    }
}
