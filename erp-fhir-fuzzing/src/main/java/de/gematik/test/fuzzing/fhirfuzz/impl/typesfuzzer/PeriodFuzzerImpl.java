/*
 * Copyright 2024 gematik GmbH
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
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzOperationResult;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Period;

public class PeriodFuzzerImpl implements FhirTypeFuzz<Period> {
  FuzzerContext fuzzerContext;

  public PeriodFuzzerImpl(FuzzerContext fuzzerContext) {
    this.fuzzerContext = fuzzerContext;
  }

  @Override
  public Period fuzz(Period period) {
    val m = fuzzerContext.getRandomPart(getMutators());
    for (FuzzingMutator<Period> f : m) {
      f.accept(period);
    }
    return period;
  }

  @Override
  public FuzzerContext getContext() {
    return fuzzerContext;
  }

  public Period generateRandom() {
    return new Period()
        .setStart(fuzzerContext.getRandomDate())
        .setEnd(fuzzerContext.getRandomDate());
  }

  private List<FuzzingMutator<Period>> getMutators() {
    val manipulators = new LinkedList<FuzzingMutator<Period>>();
    manipulators.add(this::fuzzStart);
    manipulators.add(this::fuzzEnd);
    manipulators.add(this::fuzzSwitch);
    return manipulators;
  }

  private void fuzzStart(Period p) {
    val info = "set Start in Period:";
    if (!p.hasStart()) {
      val newDate = fuzzerContext.getRandomDate();
      p.setStart(fuzzerContext.getRandomDate());
      fuzzerContext.addLog(new FuzzOperationResult<>(info, null, newDate.getTime()));
    } else if (fuzzerContext.conditionalChance()) {
      val start = p.getStart();
      p.setStart(null);
      fuzzerContext.addLog(new FuzzOperationResult<>(info, start.getTime(), null));
    } else {
      val old = p.getStart();
      val newDate = fuzzerContext.getRandomDate();
      p.setStart(newDate);
      fuzzerContext.addLog(
          new FuzzOperationResult<>("fuzz Start in Period:", old.getTime(), newDate.getTime()));
    }
  }

  private void fuzzEnd(Period p) {
    if (!p.hasEnd()) {
      val newDate = fuzzerContext.getRandomDate();
      p.setEnd(newDate);
      fuzzerContext.addLog(
          new FuzzOperationResult<>("set End in Period:", null, newDate.getTime()));
    } else if (fuzzerContext.conditionalChance()) {
      val old = p.getEnd();
      p.setEnd(null);
      fuzzerContext.addLog(new FuzzOperationResult<>("set Start in Period:", old.getTime(), null));
    } else {
      val old = p.getEnd();
      val newDate = fuzzerContext.getRandomDate();
      p.setEnd(newDate);
      fuzzerContext.addLog(
          new FuzzOperationResult<>("fuzz Start in Period:", old.getTime(), newDate.getTime()));
    }
  }

  private void fuzzSwitch(Period p) {
    val infoStart = "switched Start in Period: newStart";
    val infoEnd = "switched Start in Period: new End";
    if (p.hasEnd() && p.hasStart()) {
      val end = p.getEnd();
      val start = p.getStart();
      p.setStart(end);
      fuzzerContext.addLog(new FuzzOperationResult<>(infoStart, start.getTime(), end.getTime()));
      p.setEnd(start);
      fuzzerContext.addLog(new FuzzOperationResult<>(infoEnd, end.getTime(), start.getTime()));
    } else if (p.hasStart()) {
      val start = p.getStart();
      p.setStart(null);
      fuzzerContext.addLog(new FuzzOperationResult<>(infoStart, start.getTime(), null));
      p.setEnd(start);
      fuzzerContext.addLog(new FuzzOperationResult<>(infoEnd, null, start.getTime()));
    } else if (p.hasEnd()) {
      val end = p.getEnd();
      p.setStart(end);
      fuzzerContext.addLog(new FuzzOperationResult<>(infoStart, null, end.getTime()));
      p.setEnd(null);
      fuzzerContext.addLog(new FuzzOperationResult<>(infoEnd, end.getTime(), null));
    }
  }
}
