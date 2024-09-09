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
import de.gematik.test.fuzzing.fhirfuzz.BaseFuzzer;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzOperationResult;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.TimeType;
import org.hl7.fhir.r4.model.Timing;

public class RepeatFuzzImpl implements BaseFuzzer<Timing.TimingRepeatComponent> {

  private final FuzzerContext fuzzerContext;

  public RepeatFuzzImpl(FuzzerContext fuzzerContext) {
    this.fuzzerContext = fuzzerContext;
  }

  @Override
  public Timing.TimingRepeatComponent fuzz(Timing.TimingRepeatComponent repeatComponent) {
    val m = fuzzerContext.getRandomPart(getMutators());
    for (val f : m) {
      f.accept(repeatComponent);
    }
    return repeatComponent;
  }

  private List<FuzzingMutator<Timing.TimingRepeatComponent>> getMutators() {
    val manipulators = new LinkedList<FuzzingMutator<Timing.TimingRepeatComponent>>();
    manipulators.add(this::fuzzCount);
    manipulators.add(this::fuzzCountMax);
    manipulators.add(this::fuzzDuration);
    manipulators.add(this::fuzzDurationMax);
    manipulators.add(this::fuzzFrequency);
    manipulators.add(this::fuzzFrequencyMax);
    manipulators.add(this::fuzzPeriod);
    manipulators.add(this::fuzzPeriodMax);
    manipulators.add(this::fuzzPeriodUnit);
    manipulators.add(this::fuzzTimeOfDay);
    manipulators.add(this::fuzzId);

    return manipulators;
  }

  private void fuzzId(Timing.TimingRepeatComponent t) {
    if (!t.hasId()) {
      val id = fuzzerContext.getIdFuzzer().generateRandom();
      t.setId(id);
      fuzzerContext.addLog(new FuzzOperationResult<>("generate Repeat.id", null, id));
    } else {
      val org = t.getId();
      fuzzerContext.getIdFuzzer().fuzz(t::getId, t::setId);
      fuzzerContext.addLog(
          new FuzzOperationResult<>("fuzzRepeat.id:", org, t.hasId() ? t.getId() : null));
    }
  }

  private void fuzzCount(Timing.TimingRepeatComponent t) {
    if (!t.hasCount()) {
      val newCount = fuzzerContext.getRandom().nextInt();
      t.setCount(newCount);
      fuzzerContext.addLog(new FuzzOperationResult<>("generate Repeat.Count", null, newCount));
    } else {
      if (fuzzerContext.shouldFuzz(t.getCount())) {
        val org = t.getCount();
        t.setCount(0);
        fuzzerContext.addLog(new FuzzOperationResult<>("set Repeat.Count", org, 0));
      } else {
        val org = t.getCount();
        val newCount = fuzzerContext.getAnother(t.getCount());
        t.setCount(newCount);
        fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Repeat.Count ", org, newCount));
      }
    }
  }

  private void fuzzCountMax(Timing.TimingRepeatComponent t) {
    if (!t.hasCountMax()) {
      val newCount = fuzzerContext.getRandom().nextInt();
      t.setCountMax(newCount);
      fuzzerContext.addLog(new FuzzOperationResult<>("generate Repeat.CountMax ", null, newCount));
    } else {
      if (fuzzerContext.shouldFuzz(t.getCountMax())) {
        val org = t.getCountMax();
        t.setCountMax(0);
        fuzzerContext.addLog(new FuzzOperationResult<>("set Repeat.CountMax ", org, 0));
      } else {
        val org = t.getCountMax();
        val newCount = fuzzerContext.getAnother(t.getCountMax());
        t.setCountMax(newCount);
        fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Repeat.CountMax", org, newCount));
      }
    }
  }

  private void fuzzDuration(Timing.TimingRepeatComponent t) {
    if (!t.hasDuration()) {
      val duration = BigDecimal.valueOf(fuzzerContext.getRandom().nextLong());
      t.setDuration(duration);
      fuzzerContext.addLog(new FuzzOperationResult<>("generate Repeat.Duration ", null, duration));
    } else {
      if (fuzzerContext.shouldFuzz(t.getDuration())) {
        val org = t.getDuration();
        t.setDuration(null);
        fuzzerContext.addLog(new FuzzOperationResult<>("set Repeat.Duration ", org, null));
      } else {
        val org = t.getDuration();
        val duration = fuzzerContext.getAnother(t.getDuration());
        t.setDuration(duration);
        fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Repeat.Duration ", org, duration));
      }
    }
  }

  private void fuzzDurationMax(Timing.TimingRepeatComponent t) {
    if (!t.hasDurationMax()) {
      val duration = BigDecimal.valueOf(fuzzerContext.getRandom().nextLong());
      t.setDurationMax(duration);
      fuzzerContext.addLog(
          new FuzzOperationResult<>("generate Repeat.DurationMax ", null, duration));
    } else {
      if (fuzzerContext.shouldFuzz(t.getDurationMax())) {
        val org = t.getDurationMax();
        t.setDurationMax(null);
        fuzzerContext.addLog(new FuzzOperationResult<>("set Repeat.DurationMax ", org, null));
      } else {
        val org = t.getDurationMax();
        val duration = fuzzerContext.getAnother(t.getDurationMax());
        t.setDurationMax(duration);
        fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Repeat.DurationMax", org, duration));
      }
    }
  }

  private void fuzzFrequency(Timing.TimingRepeatComponent t) {
    if (!t.hasFrequency()) {
      val frequency = fuzzerContext.getRandom().nextInt();
      t.setFrequency(frequency);
      fuzzerContext.addLog(
          new FuzzOperationResult<>("generate Repeat.Frequency ", null, frequency));
    } else {
      if (fuzzerContext.shouldFuzz(t.getFrequency())) {
        val org = t.getFrequency();
        t.setCount(0);
        fuzzerContext.addLog(new FuzzOperationResult<>("set Repeat.Frequency ", org, 0));
      } else {
        val org = t.getCount();
        val newFreq = fuzzerContext.getAnother(t.getCount());
        t.setFrequency(newFreq);
        fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Repeat.Frequency ", org, newFreq));
      }
    }
  }

  private void fuzzFrequencyMax(Timing.TimingRepeatComponent t) {
    if (!t.hasFrequencyMax()) {
      val frequency = fuzzerContext.getRandom().nextInt();
      t.setFrequencyMax(frequency);
      fuzzerContext.addLog(
          new FuzzOperationResult<>("generate Repeat.FrequencyMax ", null, frequency));
    } else {
      if (fuzzerContext.shouldFuzz(t.getFrequencyMax())) {
        val org = t.getFrequencyMax();
        t.setCount(0);
        fuzzerContext.addLog(new FuzzOperationResult<>("set Repeat.FrequencyMax ", org, 0));
      } else {
        val org = t.getCount();
        val frequency = fuzzerContext.getAnother(t.getFrequencyMax());
        t.setFrequencyMax(frequency);
        fuzzerContext.addLog(
            new FuzzOperationResult<>("fuzz Repeat.FrequencyMax ", org, frequency));
      }
    }
  }

  private void fuzzPeriod(Timing.TimingRepeatComponent t) {
    if (!t.hasPeriod()) {
      val period = BigDecimal.valueOf(fuzzerContext.getRandom().nextInt());
      t.setPeriod(period);
      fuzzerContext.addLog(new FuzzOperationResult<>("generate Repeat.Period ", null, period));
    } else {
      if (fuzzerContext.shouldFuzz(t.getPeriod())) {
        val org = t.getPeriod();
        t.setPeriod(null);
        fuzzerContext.addLog(new FuzzOperationResult<>("set Repeat.Period", org, null));
      } else {
        val org = t.getCount();
        val period = fuzzerContext.getAnother(t.getPeriod());
        t.setPeriod(period);
        fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Repeat.Period ", org, period));
      }
    }
  }

  private void fuzzPeriodMax(Timing.TimingRepeatComponent t) {
    if (!t.hasPeriodMax()) {
      val period = BigDecimal.valueOf(fuzzerContext.getRandom().nextInt());
      t.setPeriodMax(period);
      fuzzerContext.addLog(new FuzzOperationResult<>("generate Repeat.PeriodMax ", null, period));
    } else {
      if (fuzzerContext.shouldFuzz(t.getPeriodMax())) {
        val org = t.getPeriodMax();
        t.setPeriodMax(null);
        fuzzerContext.addLog(new FuzzOperationResult<>("set Repeat.Period", org, null));
      } else {
        val org = t.getPeriodMax();
        val period = fuzzerContext.getAnother(t.getPeriodMax());
        t.setPeriodMax(period);
        fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Repeat.Period ", org, period));
      }
    }
  }

  private void fuzzPeriodUnit(Timing.TimingRepeatComponent t) {
    if (!t.hasPeriodUnit()) {
      val period =
          fuzzerContext.getRandomOneOfClass(Timing.UnitsOfTime.class, Timing.UnitsOfTime.NULL);
      t.setPeriodUnit(period);
      fuzzerContext.addLog(new FuzzOperationResult<>("generate Repeat.PeriodUnit ", null, period));
    } else {
      if (fuzzerContext.shouldFuzz(t.getPeriodUnit())) {
        val org = t.getPeriodUnit();
        t.setPeriodUnit(null);
        fuzzerContext.addLog(new FuzzOperationResult<>("set Repeat.PeriodUnit", org, null));
      } else {
        val org = t.getPeriodUnit();
        val period =
            fuzzerContext.getRandomOneOfClass(
                Timing.UnitsOfTime.class, List.of(Timing.UnitsOfTime.NULL, t.getPeriodUnit()));
        t.setPeriodUnit(period);
        fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Repeat.PeriodUnit ", org, period));
      }
    }
  }

  private void fuzzTimeOfDay(Timing.TimingRepeatComponent t) {
    if (!t.hasTimeOfDay()) {
      val time = List.of(new TimeType(fuzzerContext.getStringFuzz().generateRandom(5)));
      t.setTimeOfDay(time);
      fuzzerContext.addLog(
          new FuzzOperationResult<>("generate Repeat.TimeOfDay ", null, time.get(0)));
    } else {
      if (fuzzerContext.shouldFuzz(t.getPeriodUnit())) {
        val org = t.getTimeOfDay();
        t.setPeriodUnit(null);
        fuzzerContext.addLog(new FuzzOperationResult<>("set Repeat.TimeOfDay", org, null));
      } else {
        val org = t.getTimeOfDay();
        org.forEach(
            timeType -> timeType.setValue(fuzzerContext.getStringFuzz().fuzz(timeType.getValue())));
        val time =
            List.of(
                new TimeType(fuzzerContext.getStringFuzz().generateRandom(5)),
                new TimeType(fuzzerContext.getStringFuzz().generateRandom(5)),
                new TimeType(fuzzerContext.getStringFuzz().generateRandom(5)));
        t.setTimeOfDay(time);
        fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Repeat.TimeOfDay ", org, time));
      }
    }
  }

  @Override
  public Timing.TimingRepeatComponent generateRandom() {
    val repeat = new Timing.TimingRepeatComponent();
    repeat
        .setCount(fuzzerContext.getRandom().nextInt())
        .setCountMax(fuzzerContext.getRandom().nextInt())
        .setDuration(new BigDecimal(fuzzerContext.getRandom().nextInt()))
        .setDurationMax(fuzzerContext.getRandom().nextLong())
        .setDurationUnit(
            fuzzerContext.getRandomOneOfClass(Timing.UnitsOfTime.class, Timing.UnitsOfTime.NULL))
        .setFrequency(fuzzerContext.getRandom().nextInt())
        .setFrequencyMax(fuzzerContext.getRandom().nextInt())
        .setPeriod(fuzzerContext.getRandom().nextLong())
        .setPeriodMax(fuzzerContext.getRandom().nextLong())
        .setPeriodUnit(
            fuzzerContext.getRandomOneOfClass(Timing.UnitsOfTime.class, Timing.UnitsOfTime.NULL))
        .setTimeOfDay(List.of(new TimeType()));
    return repeat;
  }

  @Override
  public FuzzerContext getContext() {
    return fuzzerContext;
  }
}
