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
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Period;

import java.util.LinkedList;
import java.util.List;

public class IdentifierFuzzerImpl implements FhirTypeFuzz<Identifier> {

    private final FuzzerContext fuzzerContext;


    public IdentifierFuzzerImpl(FuzzerContext fuzzerContext) {
        this.fuzzerContext = fuzzerContext;
    }

    public Identifier generateRandom() {
        Identifier identifier = new Identifier();
        identifier.setUse(fuzzerContext.getRandomOneOfClass(Identifier.IdentifierUse.class, Identifier.IdentifierUse.NULL));
        List<Coding> list = new LinkedList<>();
        fuzzerContext.getTypeFuzzerFor(Coding.class).ifPresent(tf -> list.add(tf.generateRandom()));
        identifier.setType(new CodeableConcept().setCoding(list));
        identifier.setSystem(fuzzerContext.getUrlFuzz().generateRandom());
        identifier.setValue(fuzzerContext.getStringFuzz().generateRandom());
        identifier.setPeriod(new Period().setEnd(fuzzerContext.getRandomDate()));
        return identifier;
    }


    @Override
    public Identifier fuzz(Identifier identifier) {
        val m = fuzzerContext.getRandomPart(getMutators());
        for (FuzzingMutator<Identifier> f : m) {
            f.accept(identifier);
        }
        return identifier;
    }

    @Override
    public FuzzerContext getContext() {
        return this.fuzzerContext;
    }

    private List<FuzzingMutator<Identifier>> getMutators() {
        val manipulators = new LinkedList<FuzzingMutator<Identifier>>();
        /* use */
        manipulators.add(this::useFuzz);
        /* Type */
        manipulators.add(this::typeFuzz);
        /* System */
        manipulators.add(this::urlFuzz);
        /* Value */
        manipulators.add(this::valueFuzz);
        /* Period */
        manipulators.add(this::periodFuzz);
        /* Extension */
        manipulators.add(this::extensionFuzz);
        return manipulators;
    }

    private void useFuzz(Identifier i) {
        if (!i.hasUse()) {
            val entry = (fuzzerContext.getRandomOneOfClass(Identifier.IdentifierUse.class, Identifier.IdentifierUse.NULL));
            i.setUse(entry);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Use in Identifier", null, entry));
        } else {
            var org = i.getUse();
            val newEntry = (fuzzerContext.getRandomOneOfClass(Identifier.IdentifierUse.class, List.of(org, Identifier.IdentifierUse.NULL)));
            i.setUse(newEntry);
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Use in Identifier", org, newEntry));
        }
    }

    private void typeFuzz(Identifier i) {
        var org = i.hasType() ? i.getType() : null;
        fuzzerContext.getTypeFuzzerFor(CodeableConcept.class).ifPresent(tf -> tf.fuzz(i::hasType, i::getType, i::setType));
        fuzzerContext.addLog(new FuzzOperationResult<>("set Type in Identifier", org, i.hasType() ? i.getType() : null));

    }

    private void urlFuzz(Identifier i) {
        val urlFuzz = fuzzerContext.getUrlFuzz();
        if (!i.hasSystem()) {
            val newEntry = urlFuzz.generateRandom();
            i.setSystem(newEntry);
            fuzzerContext.addLog(new FuzzOperationResult<>("set System in Identifier", null, newEntry));
        } else {
            var org = i.getSystem();
            urlFuzz.fuzz(i::getSystem, i::setSystem);
            fuzzerContext.addLog(new FuzzOperationResult<>("Fuzz System in Identifier", org, i.hasSystem() ? i.getSystem() : null));
        }
    }

    private void valueFuzz(Identifier i) {
        val idFuzzer = fuzzerContext.getIdFuzzer();
        if (!i.hasValue()) {
            val value = idFuzzer.generateRandom();
            i.setValue(value);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Value in Identifier", null, value));
        } else {
            val org = i.getValue();
            idFuzzer.fuzz(i::getValue, i::setValue);
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Value in Identifier", org, i.hasValue() ? i.getValue() : null));
        }
    }

    private void periodFuzz(Identifier i) {
        val periodFuzzer = fuzzerContext.getTypeFuzzerFor(Period.class);
        if (!i.hasPeriod()) {
            val newEntry = new Period().setStart(fuzzerContext.getRandomDate());
            i.setPeriod(newEntry);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Period in Identifier", null, newEntry));
        } else {
            val org = i.getPeriod();
            periodFuzzer.ifPresent(tf -> tf.fuzz(i::getPeriod, i::setPeriod));
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Period in Identifier", org, i.hasPeriod() ? i.getPeriod() : null));
        }
    }

    private void extensionFuzz(Identifier i) {
        val extensionFuzzer = fuzzerContext.getTypeFuzzerFor(Extension.class, () -> new ExtensionFuzzerImpl(fuzzerContext));
        if (!i.hasExtension()) {
            val ext = extensionFuzzer.generateRandom();
            i.setExtension(List.of(ext));
            fuzzerContext.addLog(new FuzzOperationResult<>("Extension in Identifier", null, ext));
        } else {
            val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, extensionFuzzer);
            listFuzzer.fuzz(i::getExtension, i::setExtension);
        }
    }

}
