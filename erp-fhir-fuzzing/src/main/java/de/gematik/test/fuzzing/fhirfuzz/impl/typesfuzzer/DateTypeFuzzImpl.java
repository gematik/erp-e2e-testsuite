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

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.fhirfuzz.FhirTypeFuzz;
import de.gematik.test.fuzzing.fhirfuzz.impl.ListFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzOperationResult;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Extension;

import java.util.LinkedList;
import java.util.List;


public class DateTypeFuzzImpl implements FhirTypeFuzz<DateType> {

    private final FuzzerContext fuzzerContext;

    public DateTypeFuzzImpl(FuzzerContext fuzzerContext) {
        this.fuzzerContext = fuzzerContext;
    }

    @Override
    public FuzzerContext getContext() {
        return fuzzerContext;
    }

    private List<FuzzingMutator<DateType>> getMutators() {
        val manipulators = new LinkedList<FuzzingMutator<DateType>>();
        manipulators.add(this::idFuzz);
        manipulators.add(this::exFuzz);
        manipulators.add(this::precisionFuzz);
        manipulators.add(this::valueFuzz);
        return manipulators;
    }

    @Override
    public DateType fuzz(DateType dType) {
        val m = fuzzerContext.getRandomPart(getMutators());
        for (val f : m) {
            f.accept(dType);
        }
        return dType;
    }


    public DateType generateRandom() {
        return new DateType(
                fuzzerContext.getRandomDateWithFactor(10));
    }

    private void idFuzz(DateType d) {
        if (!d.hasId()) {
            val id = fuzzerContext.getIdFuzzer().generateRandom();
            d.setId(id);
            fuzzerContext.addLog(new FuzzOperationResult<>("Changes Id in DataType", null, id));
        } else {
            val org = d.getId();
            fuzzerContext.getIdFuzzer().fuzz(d::getId, d::setId);
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Id in DataType:", org, d.hasId() ? d.getId() : null));
        }
    }

    private void exFuzz(DateType d) {
        val extensionFuzzer = fuzzerContext.getTypeFuzzerFor(Extension.class, () -> new ExtensionFuzzerImpl(fuzzerContext));
        if (!d.hasExtension()) {
            val ext = extensionFuzzer.generateRandom();
            d.setExtension(List.of(ext));
            fuzzerContext.addLog(new FuzzOperationResult<>("Extension in DataType", null, ext));
        } else {
            val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, extensionFuzzer);
            listFuzzer.fuzz(d::getExtension, d::setExtension);
        }
    }

    private void precisionFuzz(DateType d) {
        if (!d.hasTime()) {
            val pres = fuzzerContext.getRandomOneOfClass(TemporalPrecisionEnum.class);
            d.setPrecision(pres);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Precision in DataType", null, pres));
        } else {
            val pre = d.getPrecision();
            val newPre = fuzzerContext.getRandomOneOfClass(TemporalPrecisionEnum.class, List.of(pre));
            d.setPrecision(newPre);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Precision in DataType",
                    pre, newPre));
        }

    }

    private void valueFuzz(DateType d) {
        if (!d.hasValue()) {
            val value = fuzzerContext.getRandomDateWithFactor(3);
            d.setValue(value);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Value in DataType", null, value));
        } else {
            val infoText = "fuzz Value in DataType";
            if (fuzzerContext.conditionalChance()) {
                val value = d.getValue();
                d.setValue(null);
                fuzzerContext.addLog(new FuzzOperationResult<>(infoText, value, null));
            } else {
                val value = d.getValue();
                val newValue = fuzzerContext.getRandomDateWithFactor(5);
                d.setValue(newValue);
                fuzzerContext.addLog(new FuzzOperationResult<>(infoText, value, newValue));
            }
        }
    }



}
