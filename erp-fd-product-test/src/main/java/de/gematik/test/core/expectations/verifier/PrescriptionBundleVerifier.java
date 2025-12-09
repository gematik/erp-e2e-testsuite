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

package de.gematik.test.core.expectations.verifier;

import static de.gematik.test.erezept.fhir.profiles.definitions.GemErpEuStructDef.EXT_REDEEMABLE_BY_PROPERTIES;
import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.KbvProfileRules;
import de.gematik.test.core.expectations.requirements.Requirement;
import de.gematik.test.core.expectations.requirements.RequirementsSet;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.function.Predicate;
import lombok.val;
import org.hl7.fhir.r4.model.Task;

public class PrescriptionBundleVerifier {

  private PrescriptionBundleVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  private static final String EXTENSION_MESSAGE_PATTERN =
      "Task.extension.{0} muss auf {1} gesetzt sein";

  public static VerificationStep<ErxPrescriptionBundle> bundleHasValidAccessCode() {
    Predicate<ErxPrescriptionBundle> predicate =
        bundle -> bundle.getTask().hasAccessCode() && bundle.getTask().getAccessCode().isValid();
    val step =
        new VerificationStep.StepBuilder<ErxPrescriptionBundle>(
            ErpAfos.A_19021.getRequirement(),
            format(
                "Der AccessCode muss eine 256 Bit Zufallszahl mit einer Mindestentropie von 120 Bit"
                    + " sein"));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxPrescriptionBundle> bundleContainsAccident(
      AccidentExtension accident) {
    Predicate<ErxPrescriptionBundle> predicate =
        bundle ->
            bundle
                .getKbvBundle()
                .orElseThrow(
                    () -> new MissingFieldException(bundle.getClass(), KbvItaErpStructDef.BUNDLE))
                .getMedicationRequest()
                .getAccident()
                .map(accidentExtension -> accidentExtension.equals(accident))
                .isPresent();

    val step =
        new VerificationStep.StepBuilder<ErxPrescriptionBundle>(
            KbvProfileRules.ACCIDENT_EXTENSION,
            format(
                "Das E-Rezept muss ein Unfallkennzeichen mit {0} enthalten",
                accident.accidentCauseType().getDisplay()));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxPrescriptionBundle> bundleContainsPzn(
      PZN expected, RequirementsSet requirementsSet) {

    Predicate<ErxPrescriptionBundle> predicate =
        bundle ->
            bundle
                .getKbvBundle()
                .flatMap(kbvErpBundle -> kbvErpBundle.getMedication().getPznOptional())
                .map(pzn -> pzn.equals(expected))
                .orElse(false);

    val step =
        new VerificationStep.StepBuilder<ErxPrescriptionBundle>(
            requirementsSet,
            format("Das E-Rezept muss die Übergebene PZN {0} enthalten", expected.getValue()));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxPrescriptionBundle> bundleContainsNameInMedicationCompound(
      String expected, RequirementsSet requirementsSet) {
    Predicate<ErxPrescriptionBundle> predicate =
        bundle ->
            bundle
                .getKbvBundle()
                .map(
                    kbvErpBundle ->
                        kbvErpBundle
                            .getMedication()
                            .getIngredientFirstRep()
                            .getItemCodeableConcept()
                            .getText())
                .map(expected::equals)
                .orElse(false);

    val step =
        new VerificationStep.StepBuilder<ErxPrescriptionBundle>(
            requirementsSet,
            format("Das E-Rezept muss die Übergebene Medikamentenbezeichnung {0} haben", expected));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxPrescriptionBundle> bundleHasLastMedicationDispenseDate() {
    return bundleHasLastMedicationDispenseDate(ErpAfos.A_24285_01);
  }

  public static VerificationStep<ErxPrescriptionBundle>
      bundleHasLastMedicationDispenseDateAfterClose() {
    return bundleHasLastMedicationDispenseDate(ErpAfos.A_26337);
  }

  private static VerificationStep<ErxPrescriptionBundle> bundleHasLastMedicationDispenseDate(
      ErpAfos afo) {
    Predicate<ErxPrescriptionBundle> predicate =
        bundle ->
            bundle
                .getTask()
                .getLastMedicationDispenseDateElement()
                .map(ip -> ip.getPrecision().equals(TemporalPrecisionEnum.SECOND))
                .orElse(false);

    val step =
        new VerificationStep.StepBuilder<ErxPrescriptionBundle>(
            afo.getRequirement(),
            "Das 'lastMedicationDispense' muss im korrekten Format 'YYYY-MM-DDThh:mm:ss+zz:zz'"
                + " vorliegen");
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxPrescriptionBundle> prescriptionInStatus(
      Task.TaskStatus status) {
    Predicate<ErxPrescriptionBundle> predicate =
        bundle -> status.equals(bundle.getTask().getStatus());

    val step =
        new VerificationStep.StepBuilder<ErxPrescriptionBundle>(
            ErpAfos.A_19114.getRequirement(), "Task im Status Draft nach Erstellung");
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxPrescriptionBundle> prescriptionHasStatus(
      Task.TaskStatus taskStatus, RequirementsSet requirementsSet) {
    Predicate<ErxPrescriptionBundle> predicate =
        bundle -> bundle.getTask().getStatus().equals(taskStatus);

    val step =
        new VerificationStep.StepBuilder<ErxPrescriptionBundle>(
            requirementsSet,
            format("Das E-Rezept muss im Status {0} sein", taskStatus.getDisplay()));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxPrescriptionBundle>
      hasRedeemableByPropertiesForBundlePrescription(boolean expected) {

    Predicate<ErxPrescriptionBundle> predicate =
        bundle -> ErxTask.fromTask(bundle.getTask()).isRedeemableByProperties(expected);

    return new VerificationStep.StepBuilder<ErxPrescriptionBundle>(
            Requirement.custom("A_27063"),
            format(EXTENSION_MESSAGE_PATTERN, EXT_REDEEMABLE_BY_PROPERTIES, expected))
        .predicate(predicate)
        .accept();
  }

  /** Prüft, ob der Consent einen gültigen UTC-Zeitstempel hat (±1 Minute Differenz) */
  public static VerificationStep<ErxPrescriptionBundle> hasLastMedDspTimestampEq(
      Instant closOpTimeStamp, ErpAfos afo) {
    Predicate<ErxPrescriptionBundle> predicate =
        erxPrescBundle ->
            erxPrescBundle
                .getTask()
                .getLastMedicationDispenseDate()
                .map(
                    it -> {
                      val diffMillis = Math.abs(Duration.between(it, closOpTimeStamp).toMillis());
                      return diffMillis < Duration.ofMinutes(1).toMillis();
                    })
                .orElse(false);

    return new VerificationStep.StepBuilder<ErxPrescriptionBundle>(
            afo.getRequirement(),
            format(
                "TimeStamp des $eu-close ''{0}'' entspricht dem Timestamp der"
                    + " LastMedicationDispense (UTC)",
                closOpTimeStamp))
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<ErxPrescriptionBundle> lastModifiedIsAfter(
      Date previousLastModified) {
    Predicate<ErxPrescriptionBundle> predicate =
        prescription -> prescription.getTask().getLastModified().after(previousLastModified);

    return new VerificationStep.StepBuilder<ErxPrescriptionBundle>(
            // TODO: there is no specific AFO for this, but App relies on A_24436-01 to update
            // prescriptions
            Requirement.custom("A_24436-01"),
            format(
                "Das aktuelle lastModified Datum muss nach dem Datum ''{0}'' liegen",
                previousLastModified))
        .predicate(predicate)
        .accept();
  }
}
