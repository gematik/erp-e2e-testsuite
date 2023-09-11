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
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Timing;

import java.util.LinkedList;
import java.util.List;

public class TimingFuzzImpl implements FhirTypeFuzz<Timing> {

    private final FuzzerContext fuzzerContext;

    public TimingFuzzImpl(FuzzerContext fuzzerContext) {
        this.fuzzerContext = fuzzerContext;
    }


    @Override
    public Timing fuzz(Timing timing) {
        val m = fuzzerContext.getRandomPart(getMutators());
        for (val f : m) {
            f.accept(timing);
        }
        return timing;
    }

    @Override
    public Timing generateRandom() {
        Timing t = new Timing();
        t.setEvent(List.of(DateTimeType.now()));
        fuzzerContext.getTypeFuzzerFor(CodeableConcept.class).ifPresent(tf -> t.setCode(tf.generateRandom()));
        fuzzerContext.getBaseFuzzerFor(Timing.TimingRepeatComponent.class).ifPresent(tf -> t.setRepeat(tf.generateRandom()));
        return t;
    }



    @Override
    public FuzzerContext getContext() {
        return fuzzerContext;
    }

    private List<FuzzingMutator<Timing>> getMutators() {
        val manipulators = new LinkedList<FuzzingMutator<Timing>>();
        manipulators.add(this::eventFuzz);
        manipulators.add(this::repeatFuzz);
        manipulators.add(this::codeFuzz);
        return manipulators;
    }

    private void codeFuzz(Timing t) {
        val org = t.hasCode() ? t.getCode() : null;
        if (org == null) {
            fuzzerContext.getTypeFuzzerFor(CodeableConcept.class).ifPresent(tf -> t.setCode(tf.generateRandom()));
            fuzzerContext.addLog(new FuzzOperationResult<>("set Code in Timing", null, t.hasCode() ? t.getCode() : null));
        } else {
            fuzzerContext.getTypeFuzzerFor(CodeableConcept.class).ifPresent(tf -> tf.fuzz(t::hasCode, t::getCode, t::setCode));
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Code in Timing", org, t.hasCode() ? t.getCode() : null));
        }

    }

    private void repeatFuzz(Timing t) {
        val org = t.hasRepeat() ? t.getRepeat() : null;
        fuzzerContext.getBaseFuzzerFor(Timing.TimingRepeatComponent.class).ifPresent(tf -> tf.fuzz(t::hasRepeat, t::getRepeat, t::setRepeat));
        fuzzerContext.addLog(new FuzzOperationResult<>("set New Repeat in Timing", org, t.hasRepeat() ? t.getRepeat() : null));
    }



    private void eventFuzz(Timing t) {
        val org = t.hasEvent() ? t.getEvent() : null;
        val randomDate = fuzzerContext.getRandomDate();
        if (org == null || org.isEmpty()) {
            t.setEvent(List.of(new DateTimeType(randomDate)));
            fuzzerContext.addLog(new FuzzOperationResult<>("set event in Timing", org, randomDate));
        } else {
            val infoText = "fuzz event in Timing";
            if (Boolean.TRUE.equals(fuzzerContext.shouldFuzz(t.getEvent()))) {
                t.setEvent(List.of(new DateTimeType()));
                fuzzerContext.addLog(new FuzzOperationResult<>(infoText, org, null));
            } else {
                t.setEvent(List.of(new DateTimeType(randomDate)));
                fuzzerContext.addLog(new FuzzOperationResult<>(infoText, org, randomDate));
            }
        }
    }
}
