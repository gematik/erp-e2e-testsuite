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
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzOperationResult;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.Quantity;

import java.util.LinkedList;
import java.util.List;

public class SimpleQuantityFuzzImpl implements FhirTypeFuzz<Quantity> {

    private final FuzzerContext fuzzerContext;

    public SimpleQuantityFuzzImpl(FuzzerContext fuzzerContext) {
        this.fuzzerContext = fuzzerContext;
    }

    @Override
    public Quantity fuzz(Quantity value) {
        val m = fuzzerContext.getRandomPart(getMutators());
        for (FuzzingMutator<Quantity> f : m) {
            f.accept(value);
        }
        return value;
    }

    private List<FuzzingMutator<Quantity>> getMutators() {
        val manipulators = new LinkedList<FuzzingMutator<Quantity>>();
        manipulators.add(this::codeFuzzer);
        manipulators.add(this::unitFuzzer);
        manipulators.add(this::systemFuzzer);
        manipulators.add(this::valueFuzzer);
        return manipulators;
    }

    private void valueFuzzer(Quantity q) {
        val org = q.hasValue() ? q.getValue() : null;
        fuzzerContext.getIntFuzz().fuzz(q::hasValue, () -> q.getValue().toBigInteger().intValue(), q::setValue);
        fuzzerContext.addLog(new FuzzOperationResult<>("set Value in SimpleQuantity", org, q.hasValue() ? q.getValue() : null));

    }

    private void unitFuzzer(Quantity q) {
        val org = q.hasUnit() ? q.getUnit() : null;
        fuzzerContext.getStringFuzz().fuzz(q::hasUnit, q::getUnit, q::setUnit);
        fuzzerContext.addLog(new FuzzOperationResult<>("set Unit in SimpleQuantity", org, q.hasUnit() ? q.getUnit() : null));
    }

    private void systemFuzzer(Quantity q) {
        val org = q.hasSystem() ? q.getSystem() : null;
        fuzzerContext.getStringFuzz().fuzz(q::hasSystem, q::getSystem, q::setSystem);
        fuzzerContext.addLog(new FuzzOperationResult<>("set System in SimpleQuantity", org, q.hasSystem() ? q.getSystem() : null));
    }

    private void codeFuzzer(Quantity q) {
        val org = q.hasCode() ? q.getCode() : null;
        fuzzerContext.getStringFuzz().fuzz(q::hasCode, q::getCode, q::setCode);
        fuzzerContext.addLog(new FuzzOperationResult<>("set Code in SimpleQuantity", org, q.hasCode() ? q.getCode() : null));
    }

    @Override
    public Quantity generateRandom() {
        return new Quantity()
                .setValue(fuzzerContext.getRandom().nextLong())
                .setUnit(fuzzerContext.getStringFuzz().generateRandom(100))
                .setSystem(fuzzerContext.getStringFuzz().generateRandom(100))
                .setCode(fuzzerContext.getStringFuzz().generateRandom(100));
    }

    @Override
    public FuzzerContext getContext() {
        return fuzzerContext;
    }
}
