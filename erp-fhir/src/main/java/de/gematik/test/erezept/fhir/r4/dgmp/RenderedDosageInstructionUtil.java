/*
 * Copyright 2025 gematik GmbH
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

package de.gematik.test.erezept.fhir.r4.dgmp;

import static java.text.MessageFormat.format;

import com.google.common.base.Strings;
import de.gematik.test.erezept.fhir.profiles.definitions.DgMPStructDef;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.*;

/**
 * <a
 * href="https://github.com/hl7germany/dgMP-DosageTextgenerierung-Skript/blob/main/medication-dosage-to-text.py">medication-dosage-to-text.py</a>
 */
@Slf4j
@UtilityClass
@SuppressWarnings("java:S1192")
public class RenderedDosageInstructionUtil {

  private static final String ERROR_TEXT =
      "Fehler: menschen-lesbare Dosierangabe konnte nicht generiert werden";

  public static Extension createGeneratorExtension() {
    val ex = DgMPStructDef.GENERATE_DOSAGE_INSTRUCTION_META.asExtension();
    ex.addExtension().setUrl("algorithmVersion").setValue(new StringType("1.0.0"));
    ex.addExtension().setUrl("language").setValue(new StringType("de-DE"));
    return ex;
  }

  /**
   * Generate human-readable german dosage text from DosageDgMP object
   *
   * @param dosages to extract the information from
   * @return human-readable germen dosage text
   */
  public static String render(List<DosageDgMP> dosages) {
    if (dosages.isEmpty()) return "";

    val type = determineDosageSchema(dosages);
    log.info(
        "determined dosage schema type: {} from {} dosage instruction(s)", type, dosages.size());
    return switch (type) {
      case SCHEMA_4 -> renderSchema4(dosages);
      case DAY_TIME_COMBI -> renderDayOfWeekAndTimeSchemaText(dosages);
      case TIME_OF_DAY -> renderTimeOfDayText(dosages);
      case DAY_OF_WEEK -> renderDayOfWeekText(dosages);
      case INTERVAL_TIME_COMBI -> renderIntervalAndTimeSchemaText(dosages);
      case INTERVAL -> renderIntervalText(dosages);
      case FREE_TEXT -> renderFreeSchemaText(dosages);
      case UNKNOWN -> {
        log.warn("unsupported dosage schema type {}", type);
        yield ERROR_TEXT;
      }
    };
  }

  /**
   * Determine the dosage schema type based on TimingOnlyOneType constraint logic.
   *
   * <p>This method implements the priority order defined in the constraint: 1. FreeText: Has text
   * but no timing structure 2. 4-Schema: Daily frequency with 'when' codes only 3. DayOfWeek: Has
   * dayOfWeek with daily period, no timing details 4. DayOfWeek + Time/4-Schema: DayOfWeek plus
   * timeOfDay OR when 5. TimeOfDay: Daily period with specific times only 6. Interval +
   * Time/4-Schema: Non-daily period with timeOfDay OR when 7. Interval: Pure interval pattern
   * without timing details
   *
   * @param dosages List of dosage instruction objects
   * @return Schema type
   */
  private static DosageInstructionSchema determineDosageSchema(List<DosageDgMP> dosages) {
    // Schema 1: FreeText - has text but no structured timing
    // Analyze the first dosage instruction (constraint ensures consistency)
    val firstDosage = dosages.get(0);
    if (firstDosage.hasText() && !firstDosage.hasTiming()) return DosageInstructionSchema.FREE_TEXT;

    // extract timing information for further analysis
    val timing = firstDosage.getTiming();
    val repeat = timing.getRepeat();

    val hasPeriod = repeat.hasPeriod();
    val hasPeriodUnit = repeat.hasPeriodUnit();
    val hasPeriodWithUnit = hasPeriod & hasPeriodUnit;
    val hasFrequency = repeat.hasFrequency();
    val hasWhenCodes = repeat.hasWhen();
    val hasTimeOfDay = repeat.hasTimeOfDay();
    val hasDayOfWeek = repeat.hasDayOfWeek();
    val hasTimeOfDayOrWhen = hasTimeOfDay || hasWhenCodes;

    // Helper: Check if this is a daily pattern (period=1, periodUnit='d')
    val periodValue = Optional.ofNullable(repeat.getPeriod()).map(BigDecimal::intValue).orElse(0);
    val periodUnitValue =
        Optional.ofNullable(repeat.getPeriodUnit())
            .map(Timing.UnitsOfTime::toCode)
            .orElse(Timing.UnitsOfTime.NULL.toCode());
    val isDailyPattern = periodValue == 1 && periodUnitValue.equals("d");

    // Schema 2: 4-Schema - daily frequency with 'when' codes only
    if (hasFrequency && isDailyPattern && hasWhenCodes && !hasTimeOfDay && !hasDayOfWeek)
      return DosageInstructionSchema.SCHEMA_4;

    // Schema 3: DayOfWeek - specific weekdays, daily period, no timing details
    if (hasDayOfWeek && hasFrequency && hasPeriodWithUnit && !hasWhenCodes && !hasTimeOfDay)
      return DosageInstructionSchema.DAY_OF_WEEK;

    // Schema 4: DayOfWeek + Time/4-Schema - weekdays plus timing
    if (hasDayOfWeek && hasFrequency && hasPeriodWithUnit && hasTimeOfDayOrWhen)
      return DosageInstructionSchema.DAY_TIME_COMBI;

    // Schema 5: TimeOfDay - daily period with specific times only
    if (hasFrequency && isDailyPattern && !hasDayOfWeek && hasTimeOfDay && !hasWhenCodes)
      return DosageInstructionSchema.TIME_OF_DAY;

    // Schema 6: Interval + Time/4-Schema - non-daily period with timing
    if (hasFrequency && hasPeriodWithUnit && !hasDayOfWeek && hasTimeOfDayOrWhen && !isDailyPattern)
      return DosageInstructionSchema.INTERVAL_TIME_COMBI;

    // Schema 7: Interval - pure interval without timing details
    if (hasFrequency & hasPeriodWithUnit && !hasWhenCodes && !hasTimeOfDay && !hasDayOfWeek)
      return DosageInstructionSchema.INTERVAL;

    return DosageInstructionSchema.UNKNOWN;
  }

  /**
   * Generate text for Interval schema: regular time intervals.
   *
   * <p>Creates text showing regular dosing intervals like "every 8 hours". For interval schema,
   * there should only be one dosage instruction.
   *
   * @param dosages List containing single interval dosage
   * @return Formatted text like "alle 8 Stunden: je 1 Stück" or "wöchentlich: je 2 mg"
   */
  private static String renderIntervalText(List<DosageDgMP> dosages) {
    // For interval schema, use the first (and typically only) dosage
    val dosage = dosages.get(0);

    // Combine parts: [bounds] frequency: dose
    val textParts = new ArrayList<String>();

    // Extract bounds if present
    extractBoundsText(dosage).ifPresent(textParts::add);
    // Generate frequency description (e.g., "täglich", "alle 8 Stunden")
    generateFrequencyDescription(dosage).ifPresent(textParts::add);

    // Extract dose information
    val doseText = extractDoseTextWithPrefix(dosage);
    val leftSide = String.join(" ", textParts);

    val hasLeftSide = !Strings.isNullOrEmpty(leftSide);
    val hasDoseText = !Strings.isNullOrEmpty(doseText);
    if (hasLeftSide && hasDoseText) return format("{0}: {1}", leftSide, doseText);
    else if (hasLeftSide) return leftSide;
    else if (hasDoseText) return doseText;
    else return "";
  }

  /**
   * Generate text for DayOfWeek schema: specific weekdays with doses.
   *
   * <p>Creates text showing which days of the week to take medication, with doses specified for
   * each day.
   *
   * @param dosages List with dayOfWeek specifications
   * @return Formatted text like "montags — je 1 Stück, mittwochs — je 2 Stück"
   */
  private static String renderDayOfWeekText(List<DosageDgMP> dosages) {
    // Group dosages by day and collect dose information
    val dayToDose = new EnumMap<DayOfWeekDE, BigDecimal>(DayOfWeekDE.class);
    Optional<String> boundsText = Optional.empty();
    String unitText = null;

    for (val d : dosages) {
      val timing = d.getTiming();
      val repeat = timing.getRepeat();
      val dayCodes =
          repeat.getDayOfWeek().stream().map(it -> DayOfWeekDE.fromCode(it.getCode())).toList();

      // Extract duration bounds (should be consistent across dosages)
      boundsText = boundsText.stream().findFirst().or(() -> extractBoundsText(d));

      // Extract dose information
      val doseInfo = extractDoseQuantity(d);
      if (doseInfo.isPresent()) {
        val di = doseInfo.get();
        unitText = Optional.ofNullable(unitText).orElseGet(di::unit);

        // Associate this dose with each specified day
        dayCodes.forEach(dc -> dayToDose.put(dc, di.dose()));
      }
    }

    if (dayToDose.isEmpty()) return "";

    // Sort days by weekday order and format each day
    val finalTmpUnitText = unitText;
    val dayTextParts =
        dayToDose.entrySet().stream()
            .sorted(Comparator.comparingInt(e -> e.getKey().ordinal()))
            .map(
                entry -> {
                  // Get German day name
                  val dayName = entry.getKey().getTranslation();

                  // Format dose value and create day entry
                  val formattedDose = formatDecimalValue(entry.getValue());
                  var doseText = format("je {0}", formattedDose);
                  if (!Strings.isNullOrEmpty(finalTmpUnitText)) doseText += " " + finalTmpUnitText;

                  return format("{0} — {1}", dayName, doseText);
                })
            .toList();

    // Combine all days with semicolons (each day is a complete dosage instruction with unit)
    val combinedDays = String.join("; ", dayTextParts);

    return boundsText.map(s -> format("{0}: {1}", s, combinedDays)).orElse(combinedDays);
  }

  /**
   * Generate text for FreeText schema: return user-provided text directly.
   *
   * <p>For free text dosages, we simply extract and concatenate the text fields from all dosage
   * instructions, preserving the original human-readable content.
   *
   * @param dosages List of dosage instructions with text fields
   * @return Concatenated text from all dosage instructions
   */
  private static String renderFreeSchemaText(List<DosageDgMP> dosages) {
    return dosages.stream().map(it -> it.getText().trim()).collect(Collectors.joining(" "));
  }

  /**
   * Generate text for 4-Schema: morning-noon-evening-night pattern.
   *
   * <p>The 4-Schema represents doses at four daily time points using a compact notation:
   * "morning-noon-evening-night" (e.g., "1-0-2-0 Stück").
   *
   * @param dosages List containing dosage instructions with 'when' codes
   * @return Formatted text like "1-0-2-0 Stück" or "für 7 Tage: 2-1-2-1 mg"
   */
  private static String renderSchema4(List<DosageDgMP> dosages) {
    // Initialize dose amounts for each time period (default to 0)
    val doseAmounts =
        Stream.of(WhenCodeDE.values()).collect(Collectors.toMap(key -> key, e -> BigDecimal.ZERO));

    Optional<String> unitText = Optional.empty();
    Optional<String> boundsText = Optional.empty();

    // Process each dosage instruction to extract dose amounts
    for (val d : dosages) {
      val timing = d.getTiming();
      val repeatElement = timing.getRepeat();
      val whenCodes =
          repeatElement.getWhen().stream()
              .flatMap(wc -> WhenCodeDE.fromCode(wc.getCode()).stream())
              .toList();

      // Extract duration bounds (should be consistent across all dosages)
      boundsText = boundsText.stream().findFirst().or(() -> extractBoundsText(d));

      // Extract dose quantity information
      val doseInfo = extractDoseQuantity(d);
      if (doseInfo.isPresent()) {
        val di = doseInfo.get();
        unitText = unitText.stream().findFirst().or(() -> Optional.ofNullable(di.unit()));

        // update dose amounts for each WhenCode
        whenCodes.forEach(it -> doseAmounts.computeIfPresent(it, (wc, v) -> v.add(di.dose())));
      }
    }

    // Format as "morning-noon-evening-night" pattern
    val dosePattern =
        Stream.of(WhenCodeDE.values())
            .map(
                wc -> {
                  val doseValue = doseAmounts.get(wc);
                  return formatDecimalValue(doseValue);
                })
            .collect(Collectors.joining("-"));

    // Add unit if available
    val finalDosePattern =
        unitText.map(it -> format("{0} {1}", dosePattern, it)).orElse(dosePattern);

    // Add bounds if present (e.g., "für 7 Tage: 1-0-2-0 Stück")
    return boundsText.map(it -> format("{0}: {1}", it, finalDosePattern)).orElse(finalDosePattern);
  }

  /**
   * Generate text for TimeOfDay schema: specific times with doses.
   *
   * <p>Creates text showing specific clock times with corresponding doses, formatted as German time
   * expressions with "Uhr".
   *
   * @param dosages List with timeOfDay specifications
   * @return Formatted text like "täglich: 08:00 Uhr — je 1 Stück; 20:00 Uhr — je 2 Stück"
   */
  private static String renderTimeOfDayText(List<DosageDgMP> dosages) {
    val timeDoseParts = new LinkedList<String>();
    Optional<String> boundsText = Optional.empty();

    // Process each dosage instruction
    for (val d : dosages) {
      val timing = d.getTiming();
      val repeat = timing.getRepeat();
      val timeOfDayList = repeat.getTimeOfDay();

      // Extract duration bounds (should be consistent across all dosages)
      boundsText = boundsText.stream().findFirst().or(() -> extractBoundsText(d));

      if (timeOfDayList.isEmpty()) continue;

      // Format times as German time expressions (sort chronologically)
      val formattedTimes =
          timeOfDayList.stream()
              .sorted(Comparator.comparing(PrimitiveType::getValue))
              .map(it -> formatGermanTime(it.getValue()))
              .toList();

      // Extract dose information
      val doseText = extractDoseTextWithPrefix(d);

      // Combine times and dose for this instruction
      if (!formattedTimes.isEmpty() && !Strings.isNullOrEmpty(doseText)) {
        val timesCombined = String.join(", ", formattedTimes);
        timeDoseParts.add(format("{0} — {1}", timesCombined, doseText));
      }
    }

    if (timeDoseParts.isEmpty()) return "";

    // Combine multiple time-dose pairs with semicolons
    val combinedInstructions = String.join("; ", timeDoseParts);

    return boundsText
        .map(s -> format("{0} täglich: {1}", s, combinedInstructions))
        .orElseGet(() -> format("täglich: {0}", combinedInstructions));
  }

  /**
   * Generate text for DayOfWeek + Time/4-Schema combination.
   *
   * <p>This combines specific weekdays with either timeOfDay or when codes. The method determines
   * which sub-type applies and delegates to the appropriate specialized generator.
   *
   * <p>Sub-types:
   * <li>DayOfWeek + TimeOfDay: "montags 08:00 Uhr — je 1 Stück, mittwochs 20:00 Uhr — je 2 Stück"
   * <li>DayOfWeek + When: "montags 1-0-1-0, mittwochs 2-1-2-0 Stück"
   *
   * @param dosages List with both dayOfWeek and timing info
   * @return Formatted combination text
   */
  private static String renderDayOfWeekAndTimeSchemaText(List<DosageDgMP> dosages) {
    // Check whether this uses timeOfDay or when codes
    val firstDosage = dosages.get(0);
    val timing = firstDosage.getTiming();
    val repeat = timing.getRepeat();

    val hasTimeOfDay = repeat.hasTimeOfDay();
    val hasWhenCodes = repeat.hasWhen();

    // Delegate to appropriate sub-type generator
    if (hasTimeOfDay && !hasWhenCodes) {
      return generateDayOfWeekTimeOfDayCombination(dosages);
    } else if (hasWhenCodes) {
      // Handle when codes (with or without timeOfDay)
      return generateDayOfWeekWhenCombination(dosages);
    } else {
      // Fallback to when-based logic if neither present
      return generateDayOfWeekWhenCombination(dosages);
    }
  }

  /**
   * Generate text for Interval + Time/4-Schema combination. This combines regular intervals
   * (non-daily) with either timeOfDay or when codes.
   *
   * @param dosages List with interval and timing information
   * @return Formatted text like "alle 2 Tage: 08:00 Uhr — je 1 Stück; 18:00 Uhr — je 2 Stück"
   */
  private static String renderIntervalAndTimeSchemaText(List<DosageDgMP> dosages) {
    // Extract interval information from first dosage
    val firstDosage = dosages.get(0);
    val timing = firstDosage.getTiming();
    val repeat = timing.getRepeat();

    // Extract bounds if present
    val boundsText = extractBoundsText(firstDosage);
    val intervalText = extractIntervalTextFrom(repeat);

    // Group dosages by time or when code
    val timeToDosages = new HashMap<String, List<DosageDgMP>>();
    for (val d : dosages) {
      val timing2 = d.getTiming();
      val repeat2 = timing2.getRepeat();
      val hasTimeOfDay = repeat2.hasTimeOfDay();
      val hasWhen = repeat2.hasWhen();

      // Process timeOfDay entries
      if (hasTimeOfDay) {
        repeat2
            .getTimeOfDay()
            .forEach(
                tt -> {
                  val list =
                      timeToDosages.computeIfAbsent(tt.getValue(), type -> new LinkedList<>());
                  list.add(d);
                });
      } else if (hasWhen) {
        repeat2
            .getWhen()
            .forEach(
                et -> {
                  val list =
                      timeToDosages.computeIfAbsent(et.getCode(), type -> new LinkedList<>());
                  list.add(d);
                });
      }
    }

    // Generate time-dose text parts
    val timeToDoseParts = new ArrayList<String>();
    val sorted =
        timeToDosages.entrySet().stream()
            .sorted(
                (left, right) -> {
                  val leftCode = WhenCodeDE.fromCode(left.getKey()).map(Enum::ordinal).orElse(100);
                  val rightCode =
                      WhenCodeDE.fromCode(right.getKey()).map(Enum::ordinal).orElse(100);
                  return leftCode.compareTo(rightCode);
                })
            .toList();

    for (val timeKeyEntry : sorted) {
      val dosagesAtTime = timeKeyEntry.getValue();
      val timeKey = timeKeyEntry.getKey();

      // Format time display
      val timeDisplay =
          WhenCodeDE.fromCode(timeKey)
              .map(WhenCodeDE::getTranslation)
              .orElseGet(() -> formatGermanTime(timeKey));

      // Calculate total dose at this time
      var totalDoseValue = BigDecimal.ZERO;
      Optional<String> unitText = Optional.empty();

      for (val d : dosagesAtTime) {
        val doseInfo = extractDoseQuantity(d);
        if (doseInfo.isPresent()) {
          val di = doseInfo.get();
          totalDoseValue = totalDoseValue.add(di.dose());
          unitText = Optional.ofNullable(di.unit());
        }
      }

      // Format dose text
      val formattedDose = formatDecimalValue(totalDoseValue);
      val doseText =
          unitText
              .map(ut -> format("je {0} {1}", formattedDose, ut))
              .orElseGet(() -> format("je {0}", formattedDose));

      timeToDoseParts.add(format("{0} — {1}", timeDisplay, doseText));
    }

    // Build final text with bounds and interval
    val combinedTimes = String.join("; ", timeToDoseParts);
    return boundsText
        .map(bt -> format("{0} {1}: {2}", bt, intervalText, combinedTimes))
        .orElseGet(() -> format("{0}: {1}", intervalText, combinedTimes));
  }

  /**
   * Generate text for DayOfWeek + TimeOfDay combination.
   *
   * @param dosages List with interval and timing information
   * @return Formatted text like "montags 08:00 Uhr — je 1 Stück, mittwochs 20:00 Uhr — je 2 Stück"
   */
  private static String generateDayOfWeekTimeOfDayCombination(List<DosageDgMP> dosages) {
    // Group dosages by day of week
    val dayToDosages = new EnumMap<DayOfWeekDE, List<DosageDgMP>>(DayOfWeekDE.class);
    Optional<String> boundsText = Optional.empty();

    for (val d : dosages) {
      val timing = d.getTiming();
      val repeat = timing.getRepeat();
      val dayCodes =
          repeat.getDayOfWeek().stream().map(it -> DayOfWeekDE.fromCode(it.getCode())).toList();

      // Extract bounds (should be consistent across dosages)
      boundsText = boundsText.stream().findFirst().or(() -> extractBoundsText(d));

      // Group dosages by day
      for (val dayCode : dayCodes) {
        val list = dayToDosages.computeIfAbsent(dayCode, type -> new LinkedList<>());
        list.add(d);
      }
    }

    // Format each day with its time-dose combinations
    val sortedDays =
        dayToDosages.entrySet().stream()
            .sorted(Comparator.comparingInt(e -> e.getKey().ordinal()))
            .toList();

    val dayTextParts = new ArrayList<String>();
    for (val dayCode : sortedDays) {
      val dayDosages = dayToDosages.get(dayCode.getKey());
      val dayName = dayCode.getKey().getTranslation();

      // Generate time-dose combinations for this day
      val timeDoseParts = new ArrayList<String>();
      for (val dd : dayDosages) {
        val dayTiming = dd.getTiming();
        val dayRepeat = dayTiming.getRepeat();

        // Format times (sort chronologically)
        val formattedTimes =
            dayRepeat.getTimeOfDay().stream()
                .sorted(Comparator.comparing(PrimitiveType::getValue))
                .map(it -> formatGermanTime(it.getValue()))
                .toList();

        if (formattedTimes.isEmpty()) continue;

        // Extract dose information
        val doseText = extractDoseTextWithPrefix(dd);

        // Combine times and dose for this dosage
        val timesCombined = String.join(", ", formattedTimes);
        timeDoseParts.add(format("{0} — {1}", timesCombined, doseText));
      }

      // Combine all time-dose parts for this day
      val combinedTimes = String.join("; ", timeDoseParts);
      dayTextParts.add(format("{0} {1}", dayName, combinedTimes));
    }
    // Combine all days with semicolons (each day is a complete dosage instruction with unit)
    val combinedDays = String.join("; ", dayTextParts);

    return boundsText.map(it -> format("{0}: {1}", it, combinedDays)).orElse(combinedDays);
  }

  /**
   * Generate text for DayOfWeek + When combination (4-Schema pattern per day).
   *
   * @param dosages List with interval and timing information
   * @return Formatted text like "montags 1-0-1-0, mittwochs 2-1-2-0 Stück"
   */
  private static String generateDayOfWeekWhenCombination(List<DosageDgMP> dosages) {
    // Group dosages by day and build 4-schema pattern for each day
    val dayToPatterns =
        new EnumMap<DayOfWeekDE, HashMap<WhenCodeDE, BigDecimal>>(DayOfWeekDE.class);
    Optional<String> boundsText = Optional.empty();
    Optional<String> unitText = Optional.empty();

    for (val d : dosages) {
      val timing = d.getTiming();
      val repeat = timing.getRepeat();
      val dayCodes =
          repeat.getDayOfWeek().stream().map(it -> DayOfWeekDE.fromCode(it.getCode())).toList();
      val whenCodes =
          repeat.getWhen().stream().map(it -> WhenCodeDE.valueOf(it.getCode())).toList();

      // Extract bounds (should be consistent across dosages)
      boundsText = boundsText.stream().findFirst().or(() -> extractBoundsText(d));

      // Extract dose quantity and unit
      val doseInfo = extractDoseQuantity(d);
      if (doseInfo.isPresent()) {
        val di = doseInfo.get();
        unitText = unitText.stream().findFirst().or(() -> Optional.ofNullable(di.unit()));

        // For each day and each when code, set the dose
        for (val dayCode : dayCodes) {
          val dayCodedPatterns =
              dayToPatterns.computeIfAbsent(
                  dayCode,
                  type -> {
                    val m = new HashMap<WhenCodeDE, BigDecimal>();
                    Stream.of(WhenCodeDE.values()).forEach(wc -> m.put(wc, BigDecimal.ZERO));
                    return m;
                  });

          for (val whenCode : whenCodes) {
            dayCodedPatterns.computeIfPresent(whenCode, (wc, v) -> v.add(di.dose()));
          }
        }
      }
    }

    if (dayToPatterns.isEmpty()) return "";

    // Format each day with its 4-schema pattern
    val sortedDays =
        dayToPatterns.entrySet().stream()
            .sorted(Comparator.comparingInt(e -> e.getKey().ordinal()))
            .toList();

    val dayTextParts = new ArrayList<String>();
    for (val dayCode : sortedDays) {
      val dosePatterns = dayToPatterns.get(dayCode.getKey());
      val dayName = dayCode.getKey().getTranslation();

      // Format doses as "1-2-1-0" pattern
      val dosePatternText =
          Stream.of(WhenCodeDE.values())
              .map(it -> formatDecimalValue(dosePatterns.get(it)))
              .collect(Collectors.joining("-"));

      // Add unit to each day if available
      val dayPart =
          unitText
              .map(ut -> format("{0} {1} {2}", dayName, dosePatternText, ut))
              .orElseGet(() -> format("{0} {1}", dayName, dosePatternText));
      dayTextParts.add(dayPart);
    }

    // Combine all days with semicolons (each day is a complete dosage instruction with unit)
    val combinedDays = String.join("; ", dayTextParts);
    return boundsText.map(s -> format("{0}: {1}", s, combinedDays)).orElse(combinedDays);
  }

  /**
   * Generate German frequency description from dosage timing.
   *
   * <p>Converts FHIR frequency/period/periodUnit into German text like:
   *
   * <ul>
   *   <li>"täglich" (daily)
   *   <li>"3 x täglich" (3 times daily)
   *   <li>"alle 8 Stunden" (every 8 hours)
   *   <li>"wöchentlich" (weekly)
   * </ul>
   *
   * @param dosage Single dosage instruction with timing
   * @return German frequency description
   */
  private static Optional<String> generateFrequencyDescription(Dosage dosage) {
    val timing = dosage.getTiming();
    val repeat = timing.getRepeat();

    // Handle missing timing information
    val hasFrequency = repeat.hasFrequency();
    val hasPeriod = repeat.hasPeriod();
    val hasPeriodUnit = repeat.hasPeriodUnit();
    if (!hasFrequency && !hasPeriod && !hasPeriodUnit) return Optional.empty();

    val frequency = repeat.getFrequency();
    val period = repeat.getPeriod();
    val periodUnit = repeat.getPeriodUnit();

    // Daily patterns (periodUnit='d', period=1)
    if ("d".equals(periodUnit.toCode()) && BigDecimal.ONE.equals(period)) {
      if (frequency == 1) return Optional.of("täglich");
      else return Optional.of(format("{0} x täglich", frequency));
    }

    // Weekly patterns (periodUnit='wk', period=1)
    if ("wk".equals(periodUnit.toCode()) && BigDecimal.ONE.equals(period)) {
      if (frequency == 1) return Optional.of("wöchentlich");
      else return Optional.of(format("{0} x wöchentlich", frequency));
    }

    // Interval patterns (frequency=1 with various periods)
    if (frequency == 1) {
      val periodDescription = formatPeriodDescription(period, periodUnit);
      return Optional.of(format("alle {0}", periodDescription));
    }

    // Complex patterns (frequency > 1 with intervals)
    val frequencyText = format("{0} x", frequency);
    val periodDescription = formatPeriodDescription(period, periodUnit);
    return Optional.of(format("{0} alle {1}", frequencyText, periodDescription));
  }

  /**
   * Format time string to German format with 'Uhr'.
   *
   * @param timeKey Time in format "HH:MM" or "HH:MM:SS"
   * @return German time format like "08:00 Uhr"
   */
  private static String formatGermanTime(String timeKey) {
    // Extract hour and minute from time string
    val timeParts = timeKey.split(":");
    if (timeParts.length < 2) return timeKey; // Invalid format, return as is

    val hour = timeParts[0];
    val minute = timeParts[1].length() > 1 ? timeParts[1] : "0" + timeParts[1];
    return format("{0}:{1} Uhr", hour, minute);
  }

  /**
   * Format a numeric value with German decimal separator (comma). Removes unnecessary decimal
   * places for whole numbers.
   *
   * @param value Numeric value
   * @return Formatted value (e.g., "1" instead of "1,0", "1,5" kept as is)
   */
  private static String formatDecimalValue(BigDecimal value) {
    return String.valueOf(value);
  }

  /**
   * Format a period with unit into German description.
   *
   * @param period Numeric period value
   * @param periodUnit FHIR period unit code
   * @return German period description like "3 Tage" or "2 Wochen"
   */
  private static String formatPeriodDescription(BigDecimal period, Timing.UnitsOfTime periodUnit) {
    val formattedPeriod = formatDecimalValue(period);
    val unitName = formatTimeUnitGerman(period, periodUnit.toCode());
    return format("{0} {1}", formattedPeriod, unitName);
  }

  /**
   * Extract dose as German text with 'je' prefix.
   *
   * @param dosage Single dosage instruction
   * @return Formatted dose like "je 1 Stück" or "" if no dose
   */
  private static String extractDoseTextWithPrefix(Dosage dosage) {
    return extractDoseQuantity(dosage)
        .map(
            it -> {
              val formattedDose = formatDecimalValue(it.dose());
              if (Strings.isNullOrEmpty(it.unit())) return format("je {0}", formattedDose);
              else return format("je {0} {1}", formattedDose, it.unit());
            })
        .stream()
        .findFirst()
        .orElse("");
  }

  /**
   * Format time unit with proper German singular/plural form.
   *
   * @param value Numeric value
   * @param unit FHIR time unit code (s, min, h, d, wk, mo, a)
   * @return German unit name (e.g., "Tag" vs "Tage")
   */
  private static String formatTimeUnitGerman(BigDecimal value, String unit) {
    // Choose singular or plural based on value
    val timeUnit = UnitsOfTimeDE.fromCode(unit);
    return (value.intValue() == 1) ? timeUnit.getSingular() : timeUnit.getPlural();
  }

  /**
   * Extract duration bounds as German text.
   *
   * @param dosage Single dosage instruction
   * @return Formatted bounds like "für 7 Tage" or "" if no bounds
   */
  private static Optional<String> extractBoundsText(Dosage dosage) {
    val timing = dosage.getTiming();
    val repeatElement = timing.getRepeat();

    if (!repeatElement.hasBoundsDuration()) return Optional.empty();

    val boundsDuration = repeatElement.getBoundsDuration();
    val value = boundsDuration.getValue();
    val unit = boundsDuration.getCode();

    if (unit != null) {
      val formattedValue = formatDecimalValue(value);
      val formattedUnit = formatTimeUnitGerman(value, unit);
      return Optional.of(format("für {0} {1}", formattedValue, formattedUnit));
    }

    return Optional.empty();
  }

  /**
   * Extract dose quantity and unit from a dosage instruction.
   *
   * @param dosage Single dosage instruction
   * @return
   */
  private static Optional<DoseWithUnit> extractDoseQuantity(Dosage dosage) {
    val doseAndRate = dosage.getDoseAndRate();
    if (doseAndRate.isEmpty()) return Optional.empty();

    val firstDose = doseAndRate.get(0);

    if (!firstDose.hasDoseQuantity()) return Optional.empty();

    val doseQuantity = firstDose.getDoseQuantity();
    val doseValue = doseQuantity.getValue();
    val doseUnit = doseQuantity.getUnit();
    return Optional.of(new DoseWithUnit(doseValue, doseUnit));
  }

  private static String extractIntervalTextFrom(Timing.TimingRepeatComponent repeat) {
    // Generate interval text using original logic (ignores frequency!)
    val period = repeat.hasPeriod() ? repeat.getPeriod() : BigDecimal.ONE;
    val periodUnit = repeat.hasPeriodUnit() ? repeat.getPeriodUnit().toCode() : "d";

    // Original hardcoded interval text generation (replicates original behavior exactly)
    var formattedPeriod = "";
    var intervalText = "";
    if ("d".equals(periodUnit)) {
      if (period.equals(BigDecimal.ONE)) {
        intervalText = "täglich";
      } else {
        formattedPeriod = formatDecimalValue(period);
        intervalText = format("alle {0} Tage", formattedPeriod);
      }
    } else if ("wk".equals(periodUnit)) {
      if (period.equals(BigDecimal.ONE)) {
        intervalText = "wöchentlich";
      } else {
        formattedPeriod = formatDecimalValue(period);
        intervalText = format("alle {0} Wochen", formattedPeriod);
      }
    } else {
      formattedPeriod = formatDecimalValue(period);
      intervalText = format("alle {0} {1}", formattedPeriod, periodUnit);
    }

    return intervalText;
  }
}
