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

package de.gematik.test.core.expectations.verifier;

import static java.text.MessageFormat.format;

import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.RequirementsSet;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Communication;

@Slf4j
public class CommunicationVerifier {

  private CommunicationVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  public static VerificationStep<ErxCommunication> matchId(String id, RequirementsSet req) {
    Predicate<ErxCommunication> predicate = communication -> communication.getIdPart().equals(id);
    val step =
        new VerificationStep.StepBuilder<ErxCommunication>(
            req.getRequirement(), format("Es ist eine Nachricht mit der Id: {0} enthalten.", id));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxCommunication> doesNotMatchThatId(
      String id, RequirementsSet req) {
    Predicate<ErxCommunication> predicate = communication -> !communication.getIdPart().equals(id);
    val step =
        new VerificationStep.StepBuilder<ErxCommunication>(
            req.getRequirement(), format("Es ist eine Nachricht mit der Id: {0} enthalten.", id));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxCommunication> emptyReceivedElement() {
    Predicate<ErxCommunication> predicate = communication -> !communication.hasReceived();
    val step =
        new VerificationStep.StepBuilder<ErxCommunication>(
            ErpAfos.A_19521.getRequirement(), "Der Wert in communication.received ist leer!");
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxCommunication> presentReceivedElement() {
    Predicate<ErxCommunication> predicate = Communication::hasReceived;
    val step =
        new VerificationStep.StepBuilder<ErxCommunication>(
            ErpAfos.A_19521.getRequirement(), "Der Wert in communication.received ist vorhanden!");
    return step.predicate(predicate).accept();
  }
}
