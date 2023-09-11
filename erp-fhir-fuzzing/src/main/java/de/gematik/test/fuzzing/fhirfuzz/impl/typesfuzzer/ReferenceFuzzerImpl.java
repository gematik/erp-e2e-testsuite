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

package de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer;

import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.fhirfuzz.FhirTypeFuzz;
import de.gematik.test.fuzzing.fhirfuzz.impl.ListFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzOperationResult;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;

import java.util.LinkedList;
import java.util.List;

public class ReferenceFuzzerImpl implements FhirTypeFuzz<Reference> {
    private final FuzzerContext fuzzerContext;

    public ReferenceFuzzerImpl(FuzzerContext fuzzerContext) {
        this.fuzzerContext = fuzzerContext;
    }

    @Override
    public FuzzerContext getContext() {
        return fuzzerContext;
    }

    @Override
    public Reference fuzz(Reference reference) {
        val m = fuzzerContext.getRandomPart(getMutators());
        for (FuzzingMutator<Reference> f : m) {
            f.accept(reference);
        }
        return reference;
    }

    private List<FuzzingMutator<Reference>> getMutators() {
        val manipulators = new LinkedList<FuzzingMutator<Reference>>();
        manipulators.add(this::referenceFuzz);
        manipulators.add(this::typeFuzz);
        manipulators.add(this::identFuzz);
        manipulators.add(this::displayFuzz);
        manipulators.add(this::extensionFuzz);
        manipulators.add(this::fuzzId);
        if (getMapContent("BreakRanges").toLowerCase().matches("true")) {
            manipulators.add(this::exceedDisplayLength);
        }
        return manipulators;
    }

    private void referenceFuzz(Reference r) {
        val urlFuzz = fuzzerContext.getUrlFuzz();
        if (!r.hasReference()) {
            val url = urlFuzz.generateRandom();
            r.setReference(url);
            fuzzerContext.addLog(new FuzzOperationResult<>("set reference in Reference:", null, url));
        } else {
            val org = r.getReference();

            urlFuzz.fuzz(r::getReference, r::setReference);
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz reference in Reference:", org, r.getReference()));
        }
    }

    private void fuzzId(Reference r) {
        val id = r.hasId() ? r.getId() : null;
        fuzzerContext.getIdFuzzer().fuzz(r::hasId, r::getId, r::setId);
        fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Id in Reference ", id, r.hasId() ? r.getId() : null));
    }


    private void typeFuzz(Reference r) {
        val urlFuzz = fuzzerContext.getUrlFuzz();
        if (!r.hasType()) {
            val url = urlFuzz.generateRandom();
            r.setType(url);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Type in Reference:", null, url));
        } else {
            val org = r.getType();
            urlFuzz.fuzz(r::getType, r::setType);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Type in Reference:", org, r.getType()));
        }
    }

    private void identFuzz(Reference r) {
        val identifierFuzzer = fuzzerContext.getTypeFuzzerFor(Identifier.class);
        if (!r.hasIdentifier()) {
            identifierFuzzer.ifPresent(tf -> r.setIdentifier(tf.generateRandom()));
            fuzzerContext.addLog(new FuzzOperationResult<>("set Identifier in Reference:", null, r.hasIdentifier() ? r.getIdentifier() : null));
        } else {
            val org = r.getIdentifier().copy();
            identifierFuzzer.ifPresent(tf -> tf.fuzz(r::getIdentifier, r::setIdentifier));
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Identifier in Reference:", org, r.hasIdentifier() ? r.getIdentifier() : null));
        }
    }

    private void displayFuzz(Reference r) {
        val stringFuzzer = fuzzerContext.getIdFuzzer();
        if (!r.hasDisplay()) {
            val disp = stringFuzzer.generateRandom();
            r.setDisplay(disp);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Display in Reference:", null, disp));
        } else {
            val disp = r.getDisplay();
            stringFuzzer.fuzz(r::getDisplay, r::setDisplay);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Display in Reference:", disp, r.getDisplay()));
        }
    }

    private void exceedDisplayLength(Reference r) {
        if (r.hasDisplay()) {
            val disp = r.getDisplay();
            r.setDisplay(fuzzerContext.getStringFuzz().generateRandom(51));
            fuzzerContext.addLog(new FuzzOperationResult<>("set Displaylength up to 50 in Reference:", disp, r.getDisplay()));
        }
    }

    private void extensionFuzz(Reference r) {
        val extensionFuzzer = fuzzerContext.getTypeFuzzerFor(Extension.class, () -> new ExtensionFuzzerImpl(fuzzerContext));
        if (!r.hasExtension()) {
            val ext = extensionFuzzer.generateRandom();
            r.setExtension(List.of(ext));
            fuzzerContext.addLog(new FuzzOperationResult<>("Extension in Reference", null, ext));
        } else {
            val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, extensionFuzzer);
            listFuzzer.fuzz(r::getExtension, r::setExtension);
        }
    }


    public Reference generateRandom() {
        Reference reference = new Reference();
        reference.setReference(fuzzerContext.getUrlFuzz().generateRandom())
                .setType(fuzzerContext.getUrlFuzz().generateRandom())
                .setDisplay(fuzzerContext.getStringFuzz().generateRandom())
                .setId(fuzzerContext.getIdFuzzer().generateRandom());
        fuzzerContext.getTypeFuzzerFor(Identifier.class).ifPresent(tf -> reference.setIdentifier(tf.generateRandom()));
        return reference;
    }
}
