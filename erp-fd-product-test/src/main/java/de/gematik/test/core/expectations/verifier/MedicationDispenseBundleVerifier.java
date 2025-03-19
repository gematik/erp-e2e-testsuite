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

package de.gematik.test.core.expectations.verifier;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.coding.SemanticValue;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBase;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.values.TelematikID;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.val;

public class MedicationDispenseBundleVerifier {

  private MedicationDispenseBundleVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  /**
   * @param localDatePredicate // example: ld -> ld.isBefore(LocalDate.now())
   * @param description // the description of expected behavior as String
   * @return VerificationStep
   */
  public static VerificationStep<ErxMedicationDispenseBundle> verifyWhenHandedOverWithPredicate(
      Predicate<LocalDate> localDatePredicate, String description) {
    Predicate<ErxMedicationDispenseBundle> predicate =
        bundle ->
            bundle.getMedicationDispenses().stream()
                .map(
                    medDisp ->
                        DateConverter.getInstance().dateToLocalDate(medDisp.getWhenHandedOver()))
                .allMatch(localDatePredicate);
    return new VerificationStep.StepBuilder<ErxMedicationDispenseBundle>(
            ErpAfos.A_25515, description)
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<ErxMedicationDispenseBundle> verifyCountOfContainedMedication(
      int count) {
    Predicate<ErxMedicationDispenseBundle> predicate =
        bundle -> bundle.getMedicationDispenses().size() == count;
    return new VerificationStep.StepBuilder<ErxMedicationDispenseBundle>(
            ErpAfos.A_19248,
            format("Die Anzahl der enthaltenen MedicationDispense muss {0} sein", count))
        .predicate(predicate)
        .accept();
  }

  private static VerificationStep<ErxMedicationDispenseBundle> containsAllPZNs(
      List<String> expectedPZNs, Function<ErxMedicationDispenseBundle, List<String>> pznProvider) {

    Predicate<ErxMedicationDispenseBundle> predicate =
        bundle -> {
          val givenPZNs = pznProvider.apply(bundle);
          return new HashSet<>(givenPZNs).containsAll(expectedPZNs)
              && new HashSet<>(expectedPZNs).containsAll(givenPZNs);
        };

    return new VerificationStep.StepBuilder<ErxMedicationDispenseBundle>(
            ErpAfos.A_19248,
            "Die enthaltenen MedicationDispense müssen die gleichen sein wie die übergebenen")
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<ErxMedicationDispenseBundle> containsAllPZNsForOldProfiles(
      List<ErxMedicationDispense> medicationDispenses) {

    val expectedPzn =
        medicationDispenses.stream()
            .flatMap(mD -> mD.getContainedKbvMedicationFirstRep().getPzn().stream())
            .toList();

    Function<ErxMedicationDispenseBundle, List<String>> pznProvider =
        bundle ->
            bundle.getMedicationDispenses().stream()
                .flatMap(mD -> mD.getContainedKbvMedicationFirstRep().getPzn().stream())
                .toList();

    return containsAllPZNs(expectedPzn, pznProvider);
  }

  public static VerificationStep<ErxMedicationDispenseBundle> containsAllPZNsForNewProfiles(
      List<GemErpMedication> medications) {

    val expectedPzn =
        medications.stream()
            .flatMap(m -> m.getPzn().stream().map(SemanticValue::getValue))
            .toList();

    Function<ErxMedicationDispenseBundle, List<String>> pznProvider =
        bundle ->
            bundle.getMedications().stream()
                .flatMap(m -> m.getPzn().stream().map(SemanticValue::getValue))
                .toList();

    return containsAllPZNs(expectedPzn, pznProvider);
  }

  public static VerificationStep<ErxMedicationDispenseBundle> verifyWhenHandedOverIsBefore(
      LocalDate givenLd) {
    return verifyWhenHandedOverWithPredicate(
        ld -> ld.isBefore(givenLd),
        format("Der Wert im Feld MedicationDispense.whenHandedOver muss vor {0} sein ", givenLd));
  }

  public static VerificationStep<ErxMedicationDispenseBundle> verifyWhenHandedOverIsEqual(
      LocalDate givenLd) {
    return verifyWhenHandedOverWithPredicate(
        ld -> ld.isEqual(givenLd),
        format("Der Wert im Feld MedicationDispense.whenHandedOver muss vor {0} sein ", givenLd));
  }

  public static VerificationStep<ErxMedicationDispenseBundle> verifyWhenHandedOverIsAfter(
      LocalDate givenLd) {
    return verifyWhenHandedOverWithPredicate(
        ld -> ld.isAfter(givenLd),
        format("Der Wert im Feld MedicationDispense.whenHandedOver muss nach {0} sein ", givenLd));
  }

  public static VerificationStep<ErxMedicationDispenseBundle>
      verifyWhenHandedOverIsSortedSerialAscend() {
    Predicate<ErxMedicationDispenseBundle> predicate =
        bundle -> {
          val handedOverTimes =
              bundle.getMedicationDispenses().stream()
                  .map(
                      medDisp ->
                          DateConverter.getInstance().dateToLocalDate(medDisp.getWhenHandedOver()))
                  .toList();
          return isSortedAscend(handedOverTimes);
        };
    return new VerificationStep.StepBuilder<ErxMedicationDispenseBundle>(
            ErpAfos.A_24438,
            "Der Wert im Feld MedicationDispense.whenHandedOver muss aufsteigend sortiert sein, da"
                + " es der Defaultwert ist")
        .predicate(predicate)
        .accept();
  }

  private static boolean isSortedAscend(List<LocalDate> handedOverTimes) {
    for (int i = 0; i <= handedOverTimes.size() - 2; i++) {
      if (handedOverTimes.get(i).isAfter(handedOverTimes.get(i + 1))) return false;
    }
    return true;
  }

  public static VerificationStep<ErxMedicationDispenseBundle> verifyWhenPreparedIsBefore(
      LocalDate givenDate) {
    return verifyWhenPreparedWithPredicate(
        ld -> ld.isBefore(givenDate),
        format("Der Wert im Feld MedicationDispense.whenPrepared muss vor {0} sein ", givenDate));
  }

  /**
   * @param localDatePredicate // example: ld -> ld.isBefore(LocalDate.now())
   * @param description // the description of expected behavior as String
   * @return VerificationStep
   */
  public static VerificationStep<ErxMedicationDispenseBundle> verifyWhenPreparedWithPredicate(
      Predicate<LocalDate> localDatePredicate, String description) {
    Predicate<ErxMedicationDispenseBundle> predicate =
        bundle ->
            bundle.getMedicationDispenses().stream()
                .map(
                    medDisp ->
                        DateConverter.getInstance().dateToLocalDate(medDisp.getWhenPrepared()))
                .allMatch(localDatePredicate);
    return new VerificationStep.StepBuilder<ErxMedicationDispenseBundle>(
            ErpAfos.A_25515, description)
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<ErxMedicationDispenseBundle> verifyAllPerformerIdsAre(
      TelematikID telematikID) {
    Predicate<ErxMedicationDispenseBundle> predicate =
        bundle ->
            bundle.getMedicationDispenses().stream()
                .map(ErxMedicationDispenseBase::getPerformerIdFirstRep)
                .allMatch(pId -> pId.equals(telematikID.getValue()));
    return new VerificationStep.StepBuilder<ErxMedicationDispenseBundle>(
            ErpAfos.A_25515,
            format(
                "Der Wert im Feld MedicationDispense.performer muss {0} sein ",
                telematikID.getValue()))
        .predicate(predicate)
        .accept();
  }
}
