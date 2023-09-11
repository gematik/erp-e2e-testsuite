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

package de.gematik.test.fuzzing.fhirfuzz.impl;

import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.fhirfuzz.BaseFuzzer;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ListFuzzerImpl<T> implements BaseFuzzer<List<T>> {

    private final FuzzerContext fuzzerContext;
    private final BaseFuzzer<T> fuzzer;

    public ListFuzzerImpl(FuzzerContext fuzzerContext, BaseFuzzer<T> fuzzer) {
        this.fuzzerContext = fuzzerContext;
        this.fuzzer = fuzzer;
    }

    @Override
    public FuzzerContext getContext() {
        return fuzzerContext;
    }

    /**
     * @param value
     * @return USE it
     */
    @Override
    public List<T> fuzz(List<T> value) {
        val mutableList = new LinkedList<>(value);
        val m = fuzzerContext.getRandomPart(getListMutators());
        for (val f : m) {
            f.accept(mutableList);
        }
        return mutableList;
    }

    @Override
    public void fuzz(Supplier<List<T>> getter, Consumer<List<T>> setter) {
        if (getContext().conditionalChance()) {
            if (getContext().conditionalChance()) {
                setter.accept(null);
            } else {
                setter.accept(List.of());
            }
        } else {
            setter.accept(this.fuzz(getter.get()));
        }
    }

    private List<FuzzingMutator<List<T>>> getListMutators() {
        val manipulators = new LinkedList<FuzzingMutator<List<T>>>();
        manipulators.add(l -> {
            if (l.size() > 0) {
                val bound = fuzzerContext.getRandom().nextInt(l.size());
                val amount = fuzzerContext.getRandom().nextInt(bound > 0 ? bound : 1);
                for (int i = 0; i < amount; i++) {
                    val inxToRemove = fuzzerContext.getRandom().nextInt(l.size());
                    l.remove(inxToRemove);
                }
            }
        });
        manipulators.add(l -> {
            if (l != null) {
                for (val e : l) {
                    fuzzer.fuzz(e);
                }
            }
        });
        manipulators.add(l -> {
            if (l.size() > 0) {
                val idx = fuzzerContext.getRandom().nextInt(l.size());
                val newEntry = l.get(idx);
                val newList = new LinkedList<T>();
                newList.addAll(l);
                newList.add(newEntry);
                l = (newList);
            }
        });
        return manipulators;
    }

    @Override
    public List<T> generateRandom() {
        return new LinkedList<>();
    }

}
