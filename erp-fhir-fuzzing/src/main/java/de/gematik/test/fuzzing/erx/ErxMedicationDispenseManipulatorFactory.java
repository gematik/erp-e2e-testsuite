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

package de.gematik.test.fuzzing.erx;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.profiles.systems.CommonCodeSystem;
import de.gematik.test.erezept.fhir.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.values.BSNR;
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

  private static final String TELAMATIKID_SYSTEM = "https://gematik.de/fhir/sid/telematik-id";

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
    val manipulators = getAllDateTimeMedicationDispenseManipulators();
    manipulators.addAll(getSystemManipulator());

    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<ErxMedicationDispense>>>
      getAllDateTimeMedicationDispenseManipulators() {
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

  public static List<NamedEnvelope<FuzzingMutator<ErxMedicationDispense>>> getSystemManipulator() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<ErxMedicationDispense>>>();

    manipulators.add(
        NamedEnvelope.of(
            "shorten PrescriptionId-System to trigger Slice-Validation @ ErpFD",
            dispense ->
                dispense.getIdentifier().stream()
                    .filter(ErpWorkflowNamingSystem.PRESCRIPTION_ID::matches)
                    .forEach(
                        prId -> {
                          val system =
                              prId.getSystem()
                                  .substring(0, prId.getSystem().length() - GemFaker.fakerAmount());
                          prId.setSystem(system);
                        })));

    manipulators.add(
        NamedEnvelope.of(
            "shorten KVID-System to trigger Slice-Validation @ ErpFD",
            dispense -> {
              val kvidSystem = dispense.getSubject().getIdentifier().getSystem();
              dispense
                  .getSubject()
                  .getIdentifier()
                  .setSystem(kvidSystem.substring(0, kvidSystem.length() - GemFaker.fakerAmount()));
            }));
    manipulators.add(
        NamedEnvelope.of(
            "shorten actors Telematik-Id-System to trigger Slice-Validation @ ErpFD",
            dispense ->
                dispense.getPerformer().stream()
                    .filter(
                        perf ->
                            perf.getActor().getIdentifier().getSystem().matches(TELAMATIKID_SYSTEM))
                    .forEach(
                        mDCP -> {
                          val manipulatedSystem = mDCP.getActor().getIdentifier().getSystem();
                          mDCP.getActor()
                              .getIdentifier()
                              .setSystem(
                                  manipulatedSystem.substring(
                                      0, manipulatedSystem.length() - GemFaker.fakerAmount()));
                        })));

    manipulators.add(
        NamedEnvelope.of(
            "set TelematikID instead of PrescriptionId-System to trigger Slice-Validation @ ErpFD",
            dispense ->
                dispense.getIdentifier().stream()
                    .filter(ErpWorkflowNamingSystem.PRESCRIPTION_ID::matches)
                    .forEach(prId -> prId.setSystem(TelematikID.random().getSystemUrl()))));

    manipulators.add(
        NamedEnvelope.of(
            "set Accident-System instead of KVID-System to trigger Slice-Validation @ ErpFD",
            dispense ->
                dispense
                    .getSubject()
                    .getIdentifier()
                    .setSystem(KbvItaErpStructDef.ACCIDENT.getCanonicalUrl())));
    manipulators.add(
        NamedEnvelope.of(
            "set SNOMED_SCT instead of Actors Telematik-Id-System to trigger Slice-Validation @"
                + " ErpFD",
            dispense ->
                dispense.getPerformer().stream()
                    .filter(
                        perf ->
                            perf.getActor().getIdentifier().getSystem().matches(TELAMATIKID_SYSTEM))
                    .forEach(
                        mDCP ->
                            mDCP.getActor()
                                .getIdentifier()
                                .setSystem(CommonCodeSystem.SNOMED_SCT.getCanonicalUrl()))));
    manipulators.add(
        NamedEnvelope.of(
            "set BSNR instead of Actors Telematik-Id-System to trigger Slice-Validation @ ErpFD",
            dispense ->
                dispense.getPerformer().stream()
                    .filter(
                        perf ->
                            perf.getActor().getIdentifier().getSystem().matches(TELAMATIKID_SYSTEM))
                    .forEach(
                        mDCP ->
                            mDCP.getActor()
                                .getIdentifier()
                                .setSystem(BSNR.getCodeSystem().getCanonicalUrl()))));

    manipulators.add(
        NamedEnvelope.of(
            "switch PrescriptionId-System to trigger Slice-Validation @ ErpFD",
            dispense ->
                dispense.getIdentifier().stream()
                    .filter(ErpWorkflowNamingSystem.PRESCRIPTION_ID::matches)
                    .forEach(
                        prId ->
                            prId.setSystem(
                                dispense.getSubject().getIdentifier().getSystem())) // kvid System
            ));

    manipulators.add(
        NamedEnvelope.of(
            "switch KVID-System to trigger Slice-Validation @ ErpFD",
            dispense ->
                dispense
                    .getSubject()
                    .getIdentifier()
                    .setSystem((ErpWorkflowNamingSystem.PRESCRIPTION_ID.getCanonicalUrl()))));
    manipulators.add(
        NamedEnvelope.of(
            "switch Actors Telematik-Id-System to trigger Slice-Validation @ ErpFD",
            dispense ->
                dispense.getPerformer().stream()
                    .filter(
                        perf ->
                            perf.getActor().getIdentifier().getSystem().matches(TELAMATIKID_SYSTEM))
                    .forEach(
                        mDCP ->
                            mDCP.getActor()
                                .getIdentifier()
                                .setSystem(
                                    ErpWorkflowNamingSystem.PRESCRIPTION_ID.getCanonicalUrl()))));

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
