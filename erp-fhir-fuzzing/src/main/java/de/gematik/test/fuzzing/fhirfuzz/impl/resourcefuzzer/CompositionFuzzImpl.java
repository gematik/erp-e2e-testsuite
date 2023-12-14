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

import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.fhirfuzz.FhirResourceFuzz;
import de.gematik.test.fuzzing.fhirfuzz.impl.ListFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.stringtypes.StringFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.CodeableConceptFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ExtensionFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ReferenceFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzOperationResult;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Reference;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class CompositionFuzzImpl implements FhirResourceFuzz<Composition> {

    private final FuzzerContext fuzzerContext;

    public CompositionFuzzImpl(FuzzerContext fuzzerContext) {
        this.fuzzerContext = fuzzerContext;
    }

    @Override
    public Composition generateRandom() {
        val comp = new Composition();
        fuzzerContext.getTypeFuzzerFor(Identifier.class).ifPresent(tf -> comp.setIdentifier(tf.generateRandom()));
        comp.setStatus(fuzzerContext.getRandomOneOfClass(Composition.CompositionStatus.class, Composition.CompositionStatus.NULL));
        fuzzerContext.getTypeFuzzerFor(Meta.class).ifPresent(tf -> comp.setMeta(tf.generateRandom()));
        List<Coding> list = new LinkedList<>();
        fuzzerContext.getTypeFuzzerFor(Coding.class).ifPresent(tf -> list.add(tf.generateRandom()));
        comp.setType(new CodeableConcept().setCoding(list));
        comp.setId(fuzzerContext.getIdFuzzer().generateRandom());
        return comp;
    }

    @Override
    public FuzzerContext getContext() {
        return fuzzerContext;
    }

    @Override
    public Composition fuzz(Composition comp) {
        val m = fuzzerContext.getRandomPart(getMutators());
        for (FuzzingMutator<Composition> f : m) {
            f.accept(comp);
        }
        return comp;
    }

    private List<FuzzingMutator<Composition>> getMutators() {
        val manipulators = new LinkedList<FuzzingMutator<Composition>>();
            manipulators.add(this::identifyFuzz);
            manipulators.add(this::statusFuzz);
            manipulators.add(this::metaFuzz);
            manipulators.add(this::typeFuzz);
            manipulators.add(this::idFuzz);
            manipulators.add(this::categoryFuzz);
            manipulators.add(this::subjectFuzz);
            manipulators.add(this::encounterFuzz);
            manipulators.add(this::dateFuzz);
            manipulators.add(this::autorsFuzz);
            manipulators.add(this::titleFuzz);
            manipulators.add(this::confidentFuzz);
            manipulators.add(this::custodianFuzz);
            manipulators.add(this::relatesToFuzz);
            manipulators.add(this::extensionFuzz);
            manipulators.add(this::langFuzz);
        return manipulators;
    }

    private void langFuzz(Composition c) {
        if (!c.hasLanguage()) {
            val lang = fuzzerContext.getLanguageCodeFuzzer().generateRandom();
            c.setLanguage(lang);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Language in Composition", null, lang));
        } else {
            var org = c.getLanguage();
            c.getLanguageElement();
            fuzzerContext.getLanguageCodeFuzzer().fuzz(c::getLanguage, c::setLanguage);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Language in Composition", org, c.hasLanguage() ? c.getLanguage() : null));
        }
    }

    private void identifyFuzz(Composition c) {
        val ident = c.hasIdentifier() ? c.getIdentifier() : null;
        fuzzerContext.getTypeFuzzerFor(Identifier.class).ifPresent(tf -> tf.fuzz(c::hasIdentifier, c::getIdentifier, c::setIdentifier));
        fuzzerContext.addLog(new FuzzOperationResult<>("set Identifier in Composition:", ident, c.hasIdentifier() ? c.getIdentifier() : null));
    }

    private void statusFuzz(Composition c) {
        if (!c.hasStatus()) {
            val compStatus = fuzzerContext.getRandomOneOfClass(Composition.CompositionStatus.class, Composition.CompositionStatus.NULL);
            c.setStatus(compStatus);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Status in Composition:", null, compStatus));
        } else {
            val org = c.getStatus();
            val newEntry = fuzzerContext.getRandomOneOfClass(Composition.CompositionStatus.class, List.of(org, Composition.CompositionStatus.NULL));
            c.setStatus(newEntry);
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Status in Composition:", org, newEntry));
        }
    }


    private void metaFuzz(Composition c) {
        val meta = c.hasMeta() ? c.getMeta() : null;
        fuzzerContext.getTypeFuzzerFor(Meta.class).ifPresent(tf -> tf.fuzz(c::hasMeta, c::getMeta, c::setMeta));
        fuzzerContext.addLog(new FuzzOperationResult<>("set Meta in Composition:", meta, c.hasMeta() ? c.getMeta() : null));
    }

    private void typeFuzz(Composition c) {
        val orgCoding = c.hasType() ? c.getType().copy() : null;
        fuzzerContext.getTypeFuzzerFor(CodeableConcept.class).ifPresent(tf -> tf.fuzz(c::hasType, c::getType, c::setType));
        fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Type in Composition:", orgCoding, c.hasType() ? c.getType() : null));
    }

    private void idFuzz(Composition c) {
        val orgId = c.hasId() ? c.getId() : null;
        fuzzerContext.getIdFuzzer().fuzz(c::hasId, c::getId, c::setId);
        fuzzerContext.addLog(new FuzzOperationResult<>("set ID in Composition:", orgId, c.hasId() ? c.getId() : null));
    }

    private void categoryFuzz(Composition c) {
        var codConceptFuzz = fuzzerContext.getTypeFuzzerFor(CodeableConcept.class, () -> new CodeableConceptFuzzImpl(fuzzerContext));
        val cat = c.hasCategory() ? c.getCategory() : null;
        if (cat == null) {
            val newEntry = codConceptFuzz.generateRandom();
            c.setCategory(List.of(newEntry));
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Category in Composition:", null, newEntry));
        } else {
            val listFuzz = new ListFuzzerImpl<>(fuzzerContext, codConceptFuzz);
            val orgCoding = c.hasCategory() ? c.getCategory() : null;
            listFuzz.fuzz(c::getCategory, c::setCategory);
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Category in Composition:", orgCoding, c.hasCategory() ? c.getCategory() : null));
        }
    }

    private void subjectFuzz(Composition c) {
        val orgSub = c.hasSubject() ? c.getSubject().copy() : null;
        fuzzerContext.getTypeFuzzerFor(Reference.class).ifPresent(tf -> tf.fuzz(c::hasSubject, c::getSubject, c::setSubject));
        fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Subject in Composition:", orgSub, c.hasSubject() ? c.getSubject() : null));
    }

    private void encounterFuzz(Composition c) {
        val orgSub = c.hasEncounter() ? c.getEncounter().copy() : null;
        fuzzerContext.getTypeFuzzerFor(Reference.class).ifPresent(tf -> tf.fuzz(c::hasEncounter, c::getEncounter, c::setEncounter));
        fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Encounter in Composition:", orgSub, c.hasEncounter() ? c.getEncounter() : null));
    }

    private void dateFuzz(Composition c) {
        if (!c.hasDate()) {
            val date = fuzzerContext.getRandomDate();
            c.setDate(date);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Date in Composition:", null, date));
        } else if (fuzzerContext.conditionalChance()) {
            val date = c.getDate();
            c.setDate(null);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Date in Composition:", date, c.getDate()));
        } else {
            val date = c.getDate();
            Date newDate;
            do {
                newDate = fuzzerContext.getRandomDate();
            } while (newDate == date);
            c.setDate(newDate);
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Date in Composition:", date, newDate));
        }
    }

    private void autorsFuzz(Composition c) {
        val referenceFuzzer = fuzzerContext.getTypeFuzzerFor(Reference.class, () -> new ReferenceFuzzerImpl(fuzzerContext));
        if (!c.hasAuthor()) {
            val auth = referenceFuzzer.generateRandom();
            c.setAuthor(List.of(auth));
            fuzzerContext.addLog(new FuzzOperationResult<>("set Author in Composition:", null, auth));
        } else {
            val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, referenceFuzzer);
            val org = c.getAuthor();
            listFuzzer.fuzz(c::getAuthor, c::setAuthor);
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Authors in Composition:", org, c.hasAuthor() ? c.getAuthor() : null));
        }
    }

    private void titleFuzz(Composition c) {
        StringFuzzImpl stringFuzz = fuzzerContext.getStringFuzz();
        val org = c.hasTitle() ? c.getTitle() : null;
        stringFuzz.fuzz(c::hasTitle, c::getTitle, c::setTitle);
        fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Title in Composition:", org, "?"));
    }

    private void confidentFuzz(Composition c) {
        if (!c.hasConfidentiality()) {
            val conf = fuzzerContext.getRandomOneOfClass(Composition.DocumentConfidentiality.class, Composition.DocumentConfidentiality.NULL);
            c.setConfidentiality(conf);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Confidentiality in Composition:", null, conf));
        } else {
            val org = c.hasConfidentiality() ? c.getConfidentiality() : null;
            fuzzerContext.getRandomOneOfClass(Composition.DocumentConfidentiality.class, List.of(org, Composition.DocumentConfidentiality.NULL));
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Confidentiality in Composition:", org, c.hasConfidentiality() ? c.getConfidentiality() : null));
        }
    }

    private void custodianFuzz(Composition c) {
        val ref = c.hasCustodian() ? c.getCustodian().getReference() : null;
        fuzzerContext.getTypeFuzzerFor(Reference.class).ifPresent(tf -> tf.fuzz(c::hasCustodian, c::getCustodian, c::setCustodian));
        fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Custodian in Composition:", ref, c.hasCustodian() ? c.getCustodian().getReference() : null));
    }


    private void relatesToFuzz(Composition c) {
        val fhirIdentifierFuzzer = fuzzerContext.getTypeFuzzerFor(Identifier.class);
        if (!c.hasRelatesTo()) {
            val relTo = new Composition.CompositionRelatesToComponent();
            relTo.setCode(fuzzerContext.getRandomOneOfClass(Composition.DocumentRelationshipType.class, Composition.DocumentRelationshipType.NULL));
            fhirIdentifierFuzzer.ifPresent(tf -> relTo.setTarget(tf.generateRandom()));
            c.setRelatesTo(List.of(relTo));
            fuzzerContext.addLog(new FuzzOperationResult<>("set RelatesTo in Composition:", null, relTo.getTarget().getId()));
        } else {
            val orgRelTo = c.getRelatesToFirstRep().copy();
            fhirIdentifierFuzzer.ifPresent(tf -> tf.fuzz(c::getIdentifier, c::setIdentifier));
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz RelatesTo in Composition:", orgRelTo, c.hasRelatesTo() ? c.getRelatesToFirstRep() : null));
        }
    }

    private void extensionFuzz(Composition c) {
        val extensionFuzzer = fuzzerContext.getTypeFuzzerFor(Extension.class, () -> new ExtensionFuzzerImpl(fuzzerContext));
        if (!c.hasExtension()) {
            val ext = extensionFuzzer.generateRandom();
            c.setExtension(List.of(ext));
            fuzzerContext.addLog(new FuzzOperationResult<>("Extension in Composition", null, ext));
        } else {
            val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, extensionFuzzer);
            val org = c.getExtension();
            listFuzzer.fuzz(c::getExtension, c::setExtension);
            fuzzerContext.addLog(new FuzzOperationResult<>("Extension in Composition", org, c.hasExtension() ? c.getExtension() : null));
        }
    }


}
