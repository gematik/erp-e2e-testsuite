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

package de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer;

import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.fhirfuzz.FhirTypeFuzz;
import de.gematik.test.fuzzing.fhirfuzz.impl.ListFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzOperationResult;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;

import java.util.LinkedList;
import java.util.List;

public class CodeableConceptFuzzImpl implements FhirTypeFuzz<CodeableConcept> {

    final FuzzerContext fuzzerContext;

    public CodeableConceptFuzzImpl(FuzzerContext fuzzerContext) {
        this.fuzzerContext = fuzzerContext;

    }

    @Override
    public FuzzerContext getContext() {
        return fuzzerContext;
    }

    @Override
    public CodeableConcept fuzz(CodeableConcept cc) {
        val m = fuzzerContext.getRandomPart(getMutators());
        for (FuzzingMutator<CodeableConcept> f : m) {
            f.accept(cc);
        }
        return cc;
    }

    private List<FuzzingMutator<CodeableConcept>> getMutators() {
        val manipulators = new LinkedList<FuzzingMutator<CodeableConcept>>();
        manipulators.add(this::fuzzText);
        manipulators.add(this::fuzzCoding);
        manipulators.add(this::fuzzExtension);
        manipulators.add(this::fuzzId);
        return manipulators;
    }

    private void fuzzText(CodeableConcept cc) {
        if (!cc.hasText()) {
            val txt = fuzzerContext.getStringFuzz().generateRandom();
            cc.setText(txt);
            fuzzerContext.addLog(new FuzzOperationResult<>("Changes Text in CodeableConcept ", null, txt));
        } else {
            val value = cc.getText();
            fuzzerContext.getStringFuzz().fuzz(cc::getText, cc::setText);
            fuzzerContext.addLog(new FuzzOperationResult<>("Changes Text in CodeableConcept ", value, cc.hasText() ? cc.getText() : null));
        }
    }

    private void fuzzCoding(CodeableConcept cc) {
        val codingFuzz = fuzzerContext.getTypeFuzzerFor(Coding.class);
        if (!cc.hasCoding()) {
            codingFuzz.ifPresent(tf -> cc.setCoding(List.of(tf.generateRandom())));
            fuzzerContext.addLog(new FuzzOperationResult<>("set Coding in CodeableConcept ", null, cc.hasCoding() ? cc.getCoding() : null));
        } else {
            val org = cc.getCoding();
            codingFuzz.ifPresent(tf -> tf.fuzz(cc::getCodingFirstRep, o -> cc.setCoding(List.of(o))));
            fuzzerContext.addLog(new FuzzOperationResult<>("set Coding in CodeableConcept ", org, cc.hasCoding() ? cc.getCoding() : null));
        }
    }

    private void fuzzExtension(CodeableConcept cc) {
        val extFuzzer = fuzzerContext.getTypeFuzzerFor(Extension.class, () -> new ExtensionFuzzerImpl(fuzzerContext));
        if (!cc.hasExtension()) {
            val ext = extFuzzer.generateRandom();
            cc.setExtension(List.of(ext));
            fuzzerContext.addLog(new FuzzOperationResult<>("set Extension in CodeableConcept ", null, ext));
        } else {
            val ext = cc.getExtension();
            val listFuzz = new ListFuzzerImpl<>(fuzzerContext, extFuzzer);
            listFuzz.fuzz(cc::getExtension, cc::setExtension);
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Extension in CodeableConcept ", ext, cc.hasExtension() ? cc.getExtension() : null));
        }
    }

    private void fuzzId(CodeableConcept cc) {
        if (!cc.hasId()) {
            val id = fuzzerContext.getIdFuzzer().generateRandom();
            cc.setId(id);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Id in CodeableConcept ", null, id));
        } else {
            val id = cc.getId();
            fuzzerContext.getIdFuzzer().fuzz(cc::getId, cc::setId);
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Id in CodeableConcept ", id, cc.hasId() ? cc.getId() : null));
        }


    }

    public CodeableConcept generateRandom() {
        val cc = new CodeableConcept()
                .setText(fuzzerContext.getStringFuzz().generateRandom());
        cc.setId(fuzzerContext.getIdFuzzer().generateRandom());
        fuzzerContext.getTypeFuzzerFor(Coding.class).ifPresent(tf -> cc.setCoding(List.of(tf.generateRandom())));
        return cc;
    }


}
