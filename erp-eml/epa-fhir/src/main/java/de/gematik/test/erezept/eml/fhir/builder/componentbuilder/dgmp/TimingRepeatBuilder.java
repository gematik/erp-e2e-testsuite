/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp;

import static de.gematik.test.erezept.eml.fhir.profile.UseFulCodeSystems.UCUM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.*;

public class TimingRepeatBuilder extends TimingBuilder<TimingRepeatBuilder> {

  private Integer frequency;
  private Integer period;
  private Timing.UnitsOfTime periodUnit;
  private List<TimeType> timeOfDay;
  private List<Enumeration<Timing.DayOfWeek>> dayOfWeek;
  private List<Enumeration<Timing.EventTiming>> when;
  private Quantity boundsDuration;

  /**
   * Sets the frequency (number of times per period) for the dosage.
   *
   * @param frequency the frequency value (e.g., 1 for once per period)
   * @return this builder
   */
  public TimingRepeatBuilder frequency(int frequency) {
    this.frequency = frequency;
    return this;
  }

  /**
   * Sets the period (the duration of one cycle) for the event.
   *
   * @param period the period value (e.g., 1 for every periodUnit)
   * @return this builder
   */
  public TimingRepeatBuilder period(int period) {
    this.period = period;
    return this;
  }

  /**
   * Sets the period unit using a string code (e.g., "d" for days).
   *
   * @param periodUnit the period unit as string
   * @return this builder
   */
  public TimingRepeatBuilder periodUnit(String periodUnit) {
    return periodUnit(Timing.UnitsOfTime.fromCode(periodUnit));
  }

  /**
   * Sets the period unit using the FHIR UnitsOfTime enum.
   *
   * @param periodUnit the period unit as enum Timing.UnitsOfTime (e.g., Timing.UnitsOfTime.D for
   *     days)
   * @return this builder
   */
  public TimingRepeatBuilder periodUnit(Timing.UnitsOfTime periodUnit) {
    this.periodUnit = periodUnit;
    return this;
  }

  /**
   * Sets the list of times of day when the event should occur. If {@code timeOfDays} is null, the
   * builder ignores the input and does not set the field.
   *
   * @param timeOfDays list of times in format "HH:mm:ss" (e.g., "08:00:00")
   * @return this builder
   */
  public TimingRepeatBuilder timeOfDay(List<String> timeOfDays) {
    if (timeOfDays == null) {
      return this;
    }
    timeOfDays.forEach(this::timeOfDay);
    return this;
  }

  /**
   * Adds a single time of day when the event should occur. If {@code timeOfDay} is null, the
   * builder ignores the input and does not add anything.
   *
   * @param timeOfDay time in format "HH:mm:ss" (e.g., "08:00:00")
   * @return this builder
   */
  public TimingRepeatBuilder timeOfDay(String timeOfDay) {
    if (containsNothing(timeOfDay)) {
      return this;
    }
    if (this.timeOfDay == null) {
      this.timeOfDay = new ArrayList<>();
    }
    if (timeOfDay.split(" ").length > 1) {
      this.timeOfDay.addAll(
          Arrays.stream(timeOfDay.split(" ")).map(org.hl7.fhir.r4.model.TimeType::new).toList());
    } else {
      this.timeOfDay.add(new org.hl7.fhir.r4.model.TimeType(timeOfDay));
    }
    return this;
  }

  private boolean containsNothing(String value) {
    return value == null || value.isBlank();
  }

  /**
   * Sets the day(s) of the week when the event should occur using a space-separated string of day
   * codes (e.g., "mon tue"). If {@code dayOfWeek} is null, the builder ignores the input and does
   * not set the field. If multiple codes are provided (separated by whitespace), all are added as
   * days of the week.
   *
   * @param dayOfWeek one or more FHIR DayOfWeek codes as a single string (e.g., "mon tue")
   * @return this builder
   */
  public TimingRepeatBuilder dayOfWeek(String dayOfWeek) {
    if (containsNothing(dayOfWeek)) {
      return this;
    }
    if (dayOfWeek.split(" ").length > 1) {
      return dayOfWeek(
          Arrays.stream(dayOfWeek.trim().split("\\s+")).map(Timing.DayOfWeek::fromCode).toList());
    } else {
      return dayOfWeek(Timing.DayOfWeek.fromCode(dayOfWeek));
    }
  }

  /**
   * Sets the list of days of the week when the event should occur. If {@code listDaysOfWeek} is
   * null, the builder ignores the input and does not set the field.
   *
   * @param listDaysOfWeek list of FHIR DayOfWeek enums
   * @return this builder
   */
  public TimingRepeatBuilder dayOfWeek(List<Timing.DayOfWeek> listDaysOfWeek) {
    if (listDaysOfWeek == null) {
      return this;
    }
    listDaysOfWeek.forEach(this::dayOfWeek);
    return this;
  }

  /**
   * Adds a single day of the week when the event should occur. If {@code this.dayOfWeek} is null,
   * the builder instantiate an new ArrqaList().
   *
   * @param dayOfWeek FHIR DayOfWeek enum
   * @return this builder
   */
  public TimingRepeatBuilder dayOfWeek(Timing.DayOfWeek dayOfWeek) {
    if (dayOfWeek == null) return this;
    if (this.dayOfWeek == null) {
      this.dayOfWeek = new ArrayList<>();
    }
    this.dayOfWeek.add(new Enumeration<>(new Timing.DayOfWeekEnumFactory(), dayOfWeek));
    return this;
  }

  /**
   * Sets the event timing(s) using a space-separated string of timing codes (e.g., "MORN NOON"). If
   * {@code when} is null, the builder ignores the input and does not set the field. If multiple
   * codes are provided (separated by whitespace), all are added as event timings.
   *
   * @param when one or more FHIR EventTiming codes as a single string (e.g., "MORN NOON")
   * @return this builder
   */
  public TimingRepeatBuilder when(String when) {
    if (containsNothing(when)) {
      return this;
    }
    if (when.split(" ").length > 1) {
      return when(
          Arrays.stream(when.trim().split("\\s+")).map(Timing.EventTiming::fromCode).toList());
    } else {
      return when(List.of(Timing.EventTiming.fromCode(when)));
    }
  }

  /**
   * Sets the list of event timings (e.g., morning, evening) when the event should occur. If {@code
   * whenList} is null, the builder ignores the input and does not set the field.
   *
   * @param whenList list of FHIR EventTiming enums
   * @return this builder
   */
  public TimingRepeatBuilder when(List<Timing.EventTiming> whenList) {
    if (whenList == null) {
      return this;
    }
    whenList.forEach(this::when);
    return this;
  }

  /**
   * Adds a single event timing (e.g., morning, evening) when the event should occur. If {@code
   * when} is null, the builder ignores the input and does not add anything.
   *
   * @param when FHIR EventTiming enum
   * @return this builder
   */
  public TimingRepeatBuilder when(Timing.EventTiming when) {
    if (when == null) {
      return this;
    }
    if (this.when == null) {
      this.when = new ArrayList<>();
    }
    this.when.add(
        new org.hl7.fhir.r4.model.Enumeration<>(
            new org.hl7.fhir.r4.model.Timing.EventTimingEnumFactory(), when));
    return this;
  }

  /**
   * Sets the duration (bounds) for the event. If {@code boundsDuration} is null, the builder
   * ignores the input and does not set the field.
   *
   * @param boundsDuration FHIR Quantity representing the duration
   * @return this builder
   */
  public TimingRepeatBuilder boundsDuration(Quantity boundsDuration) {
    if (boundsDuration == null) {
      return this;
    }
    this.boundsDuration = boundsDuration.setSystem(UCUM.getCanonicalUrl());
    return this;
  }

  public TimingRepeatBuilder boundsDuration(long boundsDuration, UnitsOfTimeDE unitsOfTime) {
    val bd =
        new Duration()
            .setValue(boundsDuration)
            .setUnit(unitsOfTime.getSingular())
            .setValue(boundsDuration)
            .setCode(unitsOfTime.getCode());
    this.boundsDuration = bd.setSystem(UCUM.getCanonicalUrl());
    return this;
  }

  public TimingRepeatBuilder boundsDuration(long boundsDuration, String unit, String code) {
    val bd = new Duration().setValue(boundsDuration).setUnit(unit).setCode(code);
    this.boundsDuration = bd.setSystem(UCUM.getCanonicalUrl());
    return this;
  }

  /**
   * Builds the FHIR Timing object with the configured repeat component. All fields that were not
   * set (or were set with null) will be omitted from the result.
   *
   * @return a FHIR Timing instance
   */
  @Override
  public Timing build() {
    val repeatComponent = new Timing.TimingRepeatComponent();

    Optional.ofNullable(frequency).ifPresent(repeatComponent::setFrequency);
    Optional.ofNullable(period).ifPresent(repeatComponent::setPeriod);
    Optional.ofNullable(periodUnit).ifPresent(repeatComponent::setPeriodUnit);
    Optional.ofNullable(timeOfDay).ifPresent(repeatComponent::setTimeOfDay);
    Optional.ofNullable(dayOfWeek).ifPresent(repeatComponent::setDayOfWeek);
    Optional.ofNullable(when).ifPresent(repeatComponent::setWhen);
    Optional.ofNullable(boundsDuration).ifPresent(repeatComponent::setBounds);

    return new Timing().setRepeat(repeatComponent);
  }
}
