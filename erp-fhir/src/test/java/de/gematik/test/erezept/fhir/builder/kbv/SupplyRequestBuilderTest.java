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


package de.gematik.test.erezept.fhir.builder.kbv;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.test.erezept.fhir.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.resources.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.PZN;
import lombok.val;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.SupplyRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupplyRequestBuilderTest extends ParsingTest {

    private static KbvCoverage coverage;

    private static KbvErpMedication medication;

    private static Practitioner practitioner;

    @BeforeAll
    static void setup() {
        coverage = KbvCoverageBuilder.faker().build();
        medication = KbvErpMedicationBuilder.faker().pzn(PZN.random(), "randomMedication").build();
        practitioner = PractitionerBuilder.faker().build();
    }

    @Test
    void buildShouldBuild() {
        assertNotNull(SupplyRequestBuilder
                .withCoverage(coverage)
                .medication(medication)
                .requester(practitioner)
                .build());
    }

    @Test
    void fakeSupplyRequestShouldWork() {
        KbvCoverage coverage = KbvCoverageBuilder.faker().build();
        SupplyRequest supplyRequest = SupplyRequestBuilder
                .withCoverage(coverage)
                .medication(medication)
                .requester(practitioner)
                .build();
        assertEquals("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_PracticeSupply|1.1.0", supplyRequest.getMeta().getProfile().get(0).getValue());
    }

    @Test
    void fakeForPatientShouldWork() {
        val sr = SupplyRequestBuilder
                .fakeForPatient(PatientBuilder.faker().build())
                .medication(medication)
                .requester(practitioner)
                .coverage(coverage)
                .build();
        assertNotNull(sr);
    }

    @Test
    void supplyRequestShouldBeValid() {
        val supplyRequest = SupplyRequestBuilder
                .withCoverage(coverage)
                .medication(KbvErpMedicationBuilder.faker().build())
                .requester(PractitionerBuilder.faker().build())
                .build();
        val resultSupplyRequest = ValidatorUtil.encodeAndValidate(parser, supplyRequest);
        assertTrue(resultSupplyRequest.isSuccessful());

    }

    @Test
    void shouldThrowNullPointerExcCausedByMissingCoverage() {
        val patient = PatientBuilder.faker().build();
        var sr = SupplyRequestBuilder
                .fakeForPatient(patient)
                .requester(practitioner);
        assertThrows(BuilderException.class, sr::build);
    }

    @Test
    void shouldThrowNullPointerExcCausedByMissingRequester() {
        val sr = SupplyRequestBuilder
                .withCoverage(coverage);
        assertThrows(BuilderException.class, sr::build);
    }

    @Test
    void shouldThrowNullPointerExcCausedByMissingMedicationReference() {
        val sr = SupplyRequestBuilder
                .withCoverage(coverage)
                .requester(practitioner);
        assertThrows(BuilderException.class, sr::build);
    }

    @Test
    void shouldThrowNullPointerExcCausedByEmpty() {
        val sr = SupplyRequestBuilder
                .withCoverage(new KbvCoverage())
                .requester(practitioner);
        assertThrows(BuilderException.class, sr::build);
    }

    @Test
    void shouldThrowNullPointerException() {
        val sr = SupplyRequestBuilder
                .withCoverage(new KbvCoverage());

        assertThrows(NullPointerException.class,
                () -> sr.medication(null));
    }

    @Test
    void authoredOnShouldWork() {
        val supplyRequest = SupplyRequestBuilder
                .withCoverage(coverage)
                .medication(KbvErpMedicationBuilder.faker().build())
                .requester(PractitionerBuilder.faker().build())
                .authoredOn(new Date())
                .build();
        assertNotNull(supplyRequest);
    }

    @Test
    void authoredOnShouldWorkWithTemporalPrecision() {
        val supplyRequest = SupplyRequestBuilder
                .withCoverage(coverage)
                .medication(KbvErpMedicationBuilder.faker().build())
                .requester(PractitionerBuilder.faker().build())
                .authoredOn(new Date(), TemporalPrecisionEnum.DAY)
                .build();
        assertNotNull(supplyRequest);
    }



}