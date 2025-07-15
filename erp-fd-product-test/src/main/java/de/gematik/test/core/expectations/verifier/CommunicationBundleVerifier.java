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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.RequirementsSet;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunicationBundle;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class CommunicationBundleVerifier {

  private CommunicationBundleVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  public static VerificationStep<ErxCommunicationBundle> containsCommunicationWithId(
      String id, RequirementsSet req) {
    Predicate<ErxCommunicationBundle> predicate =
        bundle -> bundle.getCommunications().stream().anyMatch(com -> com.getIdPart().equals(id));
    val step =
        new VerificationStep.StepBuilder<ErxCommunicationBundle>(
            req.getRequirement(),
            format("Es ist eine Communication mit der Id: {0} enthalten.", id));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxCommunicationBundle> containsNoCommunicationWithId(
      String id, RequirementsSet req) {
    Predicate<ErxCommunicationBundle> predicate =
        bundle ->
            bundle.getCommunications().stream()
                .filter(com -> com.getIdPart().equals(id))
                .findAny()
                .isEmpty();
    val step =
        new VerificationStep.StepBuilder<ErxCommunicationBundle>(
            req.getRequirement(),
            format("Es ist keine Communication mit der Id: {0} enthalten.", id));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxCommunicationBundle> containsCountOfCommunication(
      int count, RequirementsSet req) {
    Predicate<ErxCommunicationBundle> predicate =
        bundle -> bundle.getCommunications().size() == count;
    val step =
        new VerificationStep.StepBuilder<ErxCommunicationBundle>(
            req.getRequirement(),
            "Die Anzahl der Communication im Bundle ist wie erwartet: " + count);
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxCommunicationBundle> containsOnlyRecipientWith(
      String id, RequirementsSet req) {
    Predicate<ErxCommunicationBundle> predicate =
        bundle ->
            bundle.getCommunications().stream().allMatch(com -> com.getRecipientId().equals(id));
    val step =
        new VerificationStep.StepBuilder<ErxCommunicationBundle>(
            req.getRequirement(), "Alle Communications im Bundle haben als Recipient: " + id);
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxCommunicationBundle> containsOnlyIdentifierWith(String id) {
    Predicate<ErxCommunicationBundle> predicate =
        bundle ->
            bundle.getCommunications().stream()
                .anyMatch(
                    com ->
                        Optional.ofNullable(com.getIdentifierFirstRep())
                            .map(identifier -> identifier.getValue().equals(id))
                            .orElse(false));
    val step =
        new VerificationStep.StepBuilder<ErxCommunicationBundle>(
            ErpAfos.A_24436.getRequirement(),
            "Alle Communications im Bundle haben als Recipient: " + id);
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxCommunicationBundle> onlySenderWith(KVNR kvnr) {
    return onlySenderWith(kvnr.getValue(), ErpAfos.A_19522);
  }

  public static VerificationStep<ErxCommunicationBundle> onlySenderWith(TelematikID id) {
    return onlySenderWith(id.getValue(), ErpAfos.A_19522);
  }

  public static VerificationStep<ErxCommunicationBundle> onlySenderWith(
      String id, RequirementsSet req) {
    Predicate<ErxCommunicationBundle> predicate =
        bundle -> bundle.getCommunications().stream().anyMatch(com -> com.getSenderId().equals(id));
    val step =
        new VerificationStep.StepBuilder<ErxCommunicationBundle>(
            req.getRequirement(), "Alle Communications im Bundle haben als Sender: " + id);
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxCommunicationBundle> sentDateIsEqual(LocalDate date) {
    return verifySentDateWithPredicate(
        ld -> ld.isEqual(date),
        "Die enthaltenen Tasks müssen das AuthoredOn Datum " + date.toString() + " enthalten");
  }

  /**
   * @param localDatePredicate // example: ld -> ld.isBefore(LocalDate.now())
   * @param description // the description of expected behavior as String
   * @return VerificationStep
   */
  public static VerificationStep<ErxCommunicationBundle> verifySentDateWithPredicate(
      Predicate<LocalDate> localDatePredicate, String description) {
    Predicate<ErxCommunicationBundle> predicate =
        bundle ->
            bundle.getCommunications().stream()
                .map(com -> DateConverter.getInstance().dateToLocalDate(com.getSent()))
                .allMatch(localDatePredicate);
    return new VerificationStep.StepBuilder<ErxCommunicationBundle>(ErpAfos.A_25515, description)
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<ErxCommunicationBundle> verifySentDateIsAfter(LocalDate date) {
    return verifySentDateWithPredicate(
        ld -> ld.isAfter(date), format("das enthaltene send datum muss nach {0} liegen ", date));
  }

  public static VerificationStep<ErxCommunicationBundle> verifySentDateIsBefore(LocalDate date) {
    return verifySentDateWithPredicate(
        ld -> ld.isBefore(date), format("das enthaltene send datum muss vor {0} liegen ", date));
  }

  public static VerificationStep<ErxCommunicationBundle> receivedDateIsEqualTo(LocalDate date) {
    return verifyReceivedDateWithPredicate(
        ld -> ld.isEqual(date),
        "Die enthaltenen Communications müssen das Communication.received Datum "
            + date.toString()
            + " enthalten");
  }

  /**
   * @param localDatePredicate // example: ld -> ld.isBefore(LocalDate.now())
   * @param description // the description of expected behavior as String
   * @return VerificationStep
   */
  public static VerificationStep<ErxCommunicationBundle> verifyReceivedDateWithPredicate(
      Predicate<LocalDate> localDatePredicate, String description) {
    Predicate<ErxCommunicationBundle> predicate =
        bundle ->
            bundle.getCommunications().stream()
                .map(com -> DateConverter.getInstance().dateToLocalDate(com.getReceived()))
                .allMatch(localDatePredicate);
    return new VerificationStep.StepBuilder<ErxCommunicationBundle>(ErpAfos.A_25515, description)
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<ErxCommunicationBundle> verifySentDateIsSortedAscend() {
    Predicate<ErxCommunicationBundle> predicate =
        bundle -> {
          val handedOverTimes =
              bundle.getCommunications().stream()
                  .map(medDisp -> DateConverter.getInstance().dateToLocalDate(medDisp.getSent()))
                  .toList();
          return isSortedAscend(handedOverTimes);
        };
    return new VerificationStep.StepBuilder<ErxCommunicationBundle>(
            ErpAfos.A_24438,
            "Die Default Sortierrichtung muss aufsteigend auf dem Wert sent erfolgen ")
        .predicate(predicate)
        .accept();
  }

  private static boolean isSortedAscend(List<LocalDate> sentDates) {
    for (int i = 0; i <= sentDates.size() - 2; i++) {
      if (sentDates.get(i).isAfter(sentDates.get(i + 1))) return false;
    }
    return true;
  }
}
