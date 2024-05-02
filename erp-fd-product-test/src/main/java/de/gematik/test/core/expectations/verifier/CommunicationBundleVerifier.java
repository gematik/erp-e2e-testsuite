/*
 * Copyright 2023 gematik GmbH
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

import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.RequirementsSet;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunicationBundle;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.TelematikID;
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
            bundle.getCommunications().stream().anyMatch(com -> com.getRecipientId().equals(id));
    val step =
        new VerificationStep.StepBuilder<ErxCommunicationBundle>(
            req.getRequirement(), "Alle Communications im Bundle haben als Recipient: " + id);
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxCommunicationBundle> onlySenderWith(KVNR kvnr) {
    return onlySenderWith(kvnr.getValue(), ErpAfos.A_19522_01);
  }

  public static VerificationStep<ErxCommunicationBundle> onlySenderWith(TelematikID id) {
    return onlySenderWith(id.getValue(), ErpAfos.A_19522_01);
  }

  public static VerificationStep<ErxCommunicationBundle> onlySenderWith(
      String id, RequirementsSet req) {
    Predicate<ErxCommunicationBundle> predicate =
        bundle ->
            bundle.getCommunications().stream()
                .anyMatch(com -> com.getSender().getIdentifier().getValue().equals(id));
    val step =
        new VerificationStep.StepBuilder<ErxCommunicationBundle>(
            req.getRequirement(), "Alle Communications im Bundle haben als Sender: " + id);
    return step.predicate(predicate).accept();
  }
}
