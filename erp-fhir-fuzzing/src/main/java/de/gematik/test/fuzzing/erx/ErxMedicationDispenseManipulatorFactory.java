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
 */

package de.gematik.test.fuzzing.erx;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.DateTimeType;

public class ErxMedicationDispenseManipulatorFactory {

  ErxMedicationDispenseManipulatorFactory() {
    throw new AssertionError("Do not instantiate");
  }

  /**
   * Provides date manipulators for MedicationDispense resources.
   *
   * @return list of NamedEnvelopes containing manipulators
   */
  public static List<NamedEnvelope<FuzzingMutator<ErxMedicationDispense>>>
      getAllMedicationDispenseManipulators() {
    val manipulators = new LinkedList<>(getDateTimeManipulatorsWhenHandedOver());
    manipulators.addAll(getDateTimeManipulatorsWhenPrepared());

    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<ErxMedicationDispense>>>
      getDateTimeManipulatorsWhenPrepared() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<ErxMedicationDispense>>>();

    manipulators.add(
        NamedEnvelope.of(
            "Set WhenPrepared DateTime after WhenHandedOver",
            dispense -> {
              // Get the current WhenHandedOver date
              val whenHandedOver =
                  Optional.ofNullable(dispense.getWhenHandedOver()).orElse(getCurrentDateTime());

              // Set WhenPrepared to a date after WhenHandedOver
              val whenPrepared =
                  Date.from(whenHandedOver.toInstant().plusSeconds(60 * 60 * 24)); // Add 1 day
              dispense.setWhenPreparedElement(new DateTimeType(whenPrepared));
            }));

    return manipulators;
  }

  /** Manipulators for date and time fields in MedicationDispense resources. */
  public static List<NamedEnvelope<FuzzingMutator<ErxMedicationDispense>>>
      getDateTimeManipulatorsWhenHandedOver() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<ErxMedicationDispense>>>();

    manipulators.add(
        NamedEnvelope.of(
            "Set DateTime with unsupported TemopralPrecision",
            dispense ->
                dispense.setWhenHandedOverElement(
                    new DateTimeType(getCurrentDateTime(), TemporalPrecisionEnum.MILLI))));

    manipulators.add(
        NamedEnvelope.of(
            "Set DateTime with second precision",
            dispense ->
                dispense.setWhenHandedOverElement(
                    new DateTimeType(getCurrentDateTime(), TemporalPrecisionEnum.SECOND))));

    manipulators.add(
        NamedEnvelope.of(
            "Set DateTime to future (1 year later)",
            dispense -> dispense.setWhenHandedOverElement(new DateTimeType(getFutureDateTime(1)))));

    return manipulators;
  }

  private static Date getCurrentDateTime() {
    return Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
  }

  private static Date getFutureDateTime(int yearsFromNow) {
    return Date.from(
        LocalDateTime.now().plusYears(yearsFromNow).atZone(ZoneId.systemDefault()).toInstant());
  }
}
