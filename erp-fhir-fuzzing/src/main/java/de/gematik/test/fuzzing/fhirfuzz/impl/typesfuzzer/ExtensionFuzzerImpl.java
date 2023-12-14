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
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UriType;

import java.util.LinkedList;
import java.util.List;

public class ExtensionFuzzerImpl implements FhirTypeFuzz<Extension> {
    private final FuzzerContext fuzzerContext;

    public ExtensionFuzzerImpl(FuzzerContext fuzzerContext) {
        this.fuzzerContext = fuzzerContext;
    }

    @Override
    public FuzzerContext getContext() {
        return fuzzerContext;
    }

    @Override
    public Extension fuzz(Extension ex) {
        val m = fuzzerContext.getRandomPart(getMutators());
        for (FuzzingMutator<Extension> f : m) {
            f.accept(ex);
        }
        if (ex.hasValue() && ex.hasExtension()) {
            if (fuzzerContext.conditionalChance(50.f)) {
                ex.setValue(null);
            } else {
                ex.setExtension(null);
            }
        }
        return ex;
    }

    public Extension generateRandom() {
        val ex = new Extension();
        ex.setUrl(fuzzerContext.getUrlFuzz().generateRandom())
                .setUrlElement(new UriType(fuzzerContext.getUrlFuzz().generateRandom()))
                .setId(fuzzerContext.getIdFuzzer().generateRandom())
                .setIdElement(new StringType(fuzzerContext.getStringFuzz().generateRandom()));
        return ex;
    }

    private List<FuzzingMutator<Extension>> getMutators() {
        val manipulators = new LinkedList<FuzzingMutator<Extension>>();
        manipulators.add(this::fuzzUrl);
        manipulators.add(this::fuzzValue);
        manipulators.add(this::fuzzExt);
        manipulators.add(this::fuzzId);
        manipulators.add(this::fuzzType);
        return manipulators;
    }

    private void fuzzUrl(Extension ex) {
        if (!ex.hasUrl()) {
            val url = fuzzerContext.getUrlFuzz().generateRandom();
            ex.setUrl(url);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Url in Extension ", null, url));
        } else {
            val url = ex.getUrl();
            fuzzerContext.getUrlFuzz().fuzz(ex::getUrl, o -> ex.setUrl(fuzzerContext.getUrlFuzz().fuzz(o)));
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzzes URI in Extension ", url, ex.hasUrl() ? ex.getUrl() : null));
        }
    }

    private void fuzzId(Extension ex) {
        val value = ex.hasId() ? ex.getId() : null;
        fuzzerContext.getIdFuzzer().fuzz(ex::hasId, ex::getId, ex::setId);
        fuzzerContext.addLog(new FuzzOperationResult<>("Changes Id in Extension ", value, ex.hasId() ? ex.getId() : null));
    }

    private void fuzzExt(Extension ex) {
        if (!ex.hasExtension()) {
            val ex2 = this.generateRandom();
            ex.setExtension(List.of(ex2));
            fuzzerContext.addLog(new FuzzOperationResult<>("Changes Extension in Extension ", null, List.of(ex2)));
        } else {
            val value = ex.getExtension();
            val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, this);
            listFuzzer.fuzz(ex::getExtension, ex::setExtension);
            fuzzerContext.addLog(new FuzzOperationResult<>("Changes Extension in Extension ", value, ex.hasUrlElement() ? ex.getUrlElement() : null));
        }
    }

    private void fuzzValue(Extension ex) {
        val value = fuzzerContext.getStringFuzz().generateRandom();
        ex.setValue(new StringType(value));
        ex.setExtension(null);
        fuzzerContext.addLog(new FuzzOperationResult<>("set Value in Extension and set Extension to null", null, value));
    }

    private void fuzzType(Extension ex) {
        if (ex.hasType("CodingType")) {
            for (val e : ex.getExtension()) {
                if (e.getValue() instanceof StringType stringType) {
                    stringType.setValue(fuzzerContext.getStringFuzz().generateRandom());
                } else if (e.getValue() instanceof BooleanType booleanType) {
                    booleanType.setValue((!booleanType.booleanValue()));
                } else if (e.getValue() instanceof DateType dateType) {
                    val dateTypeFuzz = fuzzerContext.getTypeFuzzerFor(DateType.class);
                    val org = dateType.getId();
                    dateTypeFuzz.ifPresent(tf -> tf.fuzz(dateType));
                    fuzzerContext.addLog(new FuzzOperationResult<>("Changes DateType in Extension ", org, dateType));
                    } else if (e.getValue() instanceof CodeableConcept codeableConcept) {
                    val codingTypeFuzzer = fuzzerContext.getTypeFuzzerFor(Coding.class);
                    codingTypeFuzzer.ifPresent(tf -> tf.fuzz(() -> (Coding) codeableConcept.getCoding(), o -> codeableConcept.setCoding(List.of(o))));
                    }
                }
            }

    }

}
