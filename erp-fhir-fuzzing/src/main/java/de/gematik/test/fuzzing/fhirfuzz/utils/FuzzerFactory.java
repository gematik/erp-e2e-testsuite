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

package de.gematik.test.fuzzing.fhirfuzz.utils;


import de.gematik.test.fuzzing.fhirfuzz.BaseFuzzer;
import de.gematik.test.fuzzing.fhirfuzz.FhirResourceFuzz;
import de.gematik.test.fuzzing.fhirfuzz.FhirTypeFuzz;
import de.gematik.test.fuzzing.fhirfuzz.impl.resourcefuzzer.CompositionFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.resourcefuzzer.CoverageFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.resourcefuzzer.MedicationFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.resourcefuzzer.MedicationRequestFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.resourcefuzzer.OrganisationFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.resourcefuzzer.PatientFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.resourcefuzzer.PractitionerFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.AddressFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.AnnotationTypeFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.CanonicalTypeFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.CodeableConceptFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.CodingTypeFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ContactPointFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.DateTypeFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.DosageFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ExtensionFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.HumanNameFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.IdentifierFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.MetaFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.NarrativeTypeFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.PeriodFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.RatioTypeFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ReferenceFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.RepeatFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.SignatureFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.SimpleQuantityFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.TimingFuzzImpl;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Ratio;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Signature;
import org.hl7.fhir.r4.model.Timing;
import org.hl7.fhir.r4.model.Type;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
public class FuzzerFactory {

    private final Map<Class<?>, List<FhirResourceFuzz<?>>> resourceFuzzer;
    private final Map<Class<?>, List<FhirTypeFuzz<?>>> typeFuzzer;
    private final Map<Class<?>, List<BaseFuzzer<?>>> baseFuzzer;
    private final FuzzerContext fuzzerContext;


    public FuzzerFactory(FuzzerContext fuzzerContext) {
        this.fuzzerContext = fuzzerContext;
        resourceFuzzer = new HashMap<>();
        typeFuzzer = new HashMap<>();
        baseFuzzer = new HashMap<>();
        setDefaultFuzzer();
    }

    // for Types
    public <T extends Type> Optional<FhirTypeFuzz<T>> getTypeFuzzerFor(Class<T> xClass) {
        val listOfClasFuzzer = getALLTypeFuzzerFor(xClass);
        if (listOfClasFuzzer.size() < 1) {
            log.info("You tried to fuzz an element without adding a Fuzzer to the List of ClassTypeFuzzer! Now you got an Optional.empty()");
            return Optional.empty();
        }
        val idx = fuzzerContext.getRandom().nextInt(listOfClasFuzzer.size());
        return Optional.of(listOfClasFuzzer.get(idx));
    }

    public <T extends Type> FhirTypeFuzz<T> getTypeFuzzerFor(Class<T> xClass, Supplier<FhirTypeFuzz<T>> defaultValue) {
        return getTypeFuzzerFor(xClass).orElse(defaultValue.get());
    }

    @SuppressWarnings("unchecked")
    public <T extends Type> List<FhirTypeFuzz<T>> getALLTypeFuzzerFor(Class<T> xClass) {
        var fuzzerList = typeFuzzer.get(xClass);
        if (fuzzerList == null) {
            fuzzerList = new LinkedList<>();
        }
        return fuzzerList.stream().map(x -> (FhirTypeFuzz<T>) x).toList();
    }

    // for Resources
    public <R extends Resource> Optional<FhirResourceFuzz<R>> getFuzzerFor(Class<R> xClass) {
        val listOfClasFuzzer = getALLFuzzerFor(xClass);
        if (listOfClasFuzzer.isEmpty()) {
            log.info("You tried to fuzz an element without adding a Fuzzer to the List of ClassResourceFuzzer! Now you got an Optional.empty()");
            return Optional.empty();
        }
        val idx = fuzzerContext.getRandom().nextInt(listOfClasFuzzer.size());
        return Optional.of(listOfClasFuzzer.get(idx));
    }

    @SuppressWarnings("unchecked")
    public <R extends Resource> List<FhirResourceFuzz<R>> getALLFuzzerFor(Class<R> xClass) {
        var fuzzerList = resourceFuzzer.get(xClass);
        if (fuzzerList == null) {
            fuzzerList = new LinkedList<>();
        }
        return fuzzerList.stream().map(x -> (FhirResourceFuzz<R>) x).toList();
    }

    //for Base Elements
    public <B extends Base> Optional<BaseFuzzer<B>> getBaseFuzzerFor(Class<B> bClass) {
        val listOfClasFuzzer = getALLBaseFuzzerFor(bClass);
        if (listOfClasFuzzer.isEmpty()) {
            log.info("You tried to fuzz an element without adding a Fuzzer to the List of ClassBaseFuzzer! Now you got an Optional.empty()");
            return Optional.empty();
        }
        val idx = fuzzerContext.getRandom().nextInt(listOfClasFuzzer.size());
        return Optional.of(listOfClasFuzzer.get(idx));
    }

    @SuppressWarnings("unchecked")
    public <B extends Base> List<BaseFuzzer<B>> getALLBaseFuzzerFor(Class<B> xClass) {
        var fuzzerList = baseFuzzer.get(xClass);
        if (fuzzerList == null) {
            fuzzerList = new LinkedList<>();
        }
        return fuzzerList.stream().map(x -> (BaseFuzzer<B>) x).toList();
    }

    public <R extends Resource> void addResourceFuzzer(Class<R> rClass, FhirResourceFuzz<R> fuzz) {
        var actualList = resourceFuzzer.get(rClass);
        if (actualList == null) actualList = new LinkedList<>();
        actualList.add(fuzz);
        resourceFuzzer.put(rClass, actualList);
    }

    public <T extends Type> void addTypeFuzzer(Class<T> rClass, FhirTypeFuzz<T> fuzz) {
        var actualList = typeFuzzer.get(rClass);
        if (actualList == null) actualList = new LinkedList<>();
        actualList.add(fuzz);
        typeFuzzer.put(rClass, actualList);
    }

    public <B extends Base> void addBaseFuzzer(Class<B> bClass, BaseFuzzer<B> fuzz) {
        var actualList = baseFuzzer.get(bClass);
        if (actualList == null) actualList = new LinkedList<>();
        actualList.add(fuzz);
        baseFuzzer.put(bClass, actualList);
    }


    public void setDefaultFuzzer() {
        // add ResourceFuzzer
        this.addResourceFuzzer(Composition.class, new CompositionFuzzImpl(fuzzerContext));
        this.addResourceFuzzer(Coverage.class, new CoverageFuzzImpl(fuzzerContext));
        this.addResourceFuzzer(Medication.class, new MedicationFuzzImpl(fuzzerContext));
        this.addResourceFuzzer(MedicationRequest.class, new MedicationRequestFuzzImpl(fuzzerContext));
        this.addResourceFuzzer(Organization.class, new OrganisationFuzzImpl(fuzzerContext));
        this.addResourceFuzzer(Patient.class, new PatientFuzzerImpl(fuzzerContext));
        this.addResourceFuzzer(Practitioner.class, new PractitionerFuzzImpl(fuzzerContext));

        //add TypeFuzzer
        this.addTypeFuzzer(Address.class, new AddressFuzzerImpl(fuzzerContext));
        this.addTypeFuzzer(Annotation.class, new AnnotationTypeFuzzImpl(fuzzerContext));
        this.addTypeFuzzer(CanonicalType.class, new CanonicalTypeFuzzerImpl(fuzzerContext));
        this.addTypeFuzzer(CodeableConcept.class, new CodeableConceptFuzzImpl(fuzzerContext));
        this.addTypeFuzzer(Coding.class, new CodingTypeFuzzerImpl(fuzzerContext));
        this.addTypeFuzzer(ContactPoint.class, new ContactPointFuzzImpl(fuzzerContext));
        this.addTypeFuzzer(DateType.class, new DateTypeFuzzImpl(fuzzerContext));
        this.addTypeFuzzer(Dosage.class, new DosageFuzzImpl(fuzzerContext));
        this.addTypeFuzzer(Extension.class, new ExtensionFuzzerImpl(fuzzerContext));
        this.addTypeFuzzer(HumanName.class, new HumanNameFuzzerImpl(fuzzerContext));
        this.addTypeFuzzer(Identifier.class, new IdentifierFuzzerImpl(fuzzerContext));
        this.addTypeFuzzer(Meta.class, new MetaFuzzerImpl(fuzzerContext));
        this.addTypeFuzzer(Narrative.class, new NarrativeTypeFuzzImpl(fuzzerContext));
        this.addTypeFuzzer(Period.class, new PeriodFuzzerImpl(fuzzerContext));
        this.addTypeFuzzer(Ratio.class, new RatioTypeFuzzerImpl(fuzzerContext));
        this.addTypeFuzzer(Reference.class, new ReferenceFuzzerImpl(fuzzerContext));
        this.addTypeFuzzer(Signature.class, new SignatureFuzzImpl(fuzzerContext));
        this.addTypeFuzzer(Quantity.class, new SimpleQuantityFuzzImpl(fuzzerContext));
        this.addTypeFuzzer(Timing.class, new TimingFuzzImpl(fuzzerContext));

        // add BackboneElementFuzzer
        this.addBaseFuzzer(Timing.TimingRepeatComponent.class, new RepeatFuzzImpl(fuzzerContext));
    }


    public void clearFuzzer() {
        this.resourceFuzzer.clear();
        this.typeFuzzer.clear();
        this.baseFuzzer.clear();
    }
}
