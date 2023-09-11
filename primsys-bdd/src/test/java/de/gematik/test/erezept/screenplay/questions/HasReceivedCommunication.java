/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.screenplay.questions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.resources.erp.ChargeItemCommunicationType;
import de.gematik.test.erezept.fhir.resources.erp.CommunicationType;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.resources.erp.ICommunicationType;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.ensure.Ensure;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HasReceivedCommunication implements Question<Boolean> {

  private final GetReceivedCommunication question;

  @Override
  public Boolean answeredBy(Actor actor) {
    val answer = actor.asksFor(question);

    answer.ifPresent(
        com -> {
          if (com.getType().equals(CommunicationType.REPRESENTATIVE)) {
            storeAssignedRepresentativePrescription(actor, com);
          }
        });

    return answer.isPresent();
  }

  private void storeAssignedRepresentativePrescription(Actor actor, ErxCommunication com) {
    val taskId = com.getBasedOnReferenceId();
    val accessCode =
        com.getBasedOnAccessCode()
            .orElseThrow(
                () ->
                    new AssertionError(
                        format(
                            "Received Message ({0}) by {1} from {2} of type {3} does not contain an AccessCode",
                            com.getId(), actor.getName(), com.getSenderId(), com.getType())));
    Ensure.that(
            "BasedOn-Reference contains a valid BasedOn-Reference",
            CheckCommunicationBasedOnReference.forCommunication(com))
        .isTrue();
    val dmcStack = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    dmcStack.appendDmc(DmcPrescription.representativeDmc(taskId, accessCode));
  }

  public static Builder infoRequest() {
    return new Builder(CommunicationType.INFO_REQ);
  }

  public static Builder dispenseRequest() {
    return new Builder(CommunicationType.DISP_REQ);
  }

  public static Builder representative() {
    return new Builder(CommunicationType.REPRESENTATIVE);
  }

  public static Builder reply() {
    return new Builder(CommunicationType.REPLY);
  }

  public static Builder changeRequest() {
    return new Builder(ChargeItemCommunicationType.CHANGE_REQ);
  }

  public static Builder changeReply() {
    return new Builder(ChargeItemCommunicationType.CHANGE_REPLY);
  }

  public static class Builder {
    private final GetReceivedCommunication.Builder wrapee;

    private Builder(ICommunicationType<?> type) {
      this.wrapee = new GetReceivedCommunication.Builder(type).last();
    }

    public HasReceivedCommunication from(Actor sender) {
      return new HasReceivedCommunication(wrapee.from(sender));
    }
  }

  @RequiredArgsConstructor
  private static class CheckCommunicationBasedOnReference implements Question<Boolean> {
    private final ErxCommunication communication;

    @Override
    public Boolean answeredBy(Actor actor) {
      val prescriptionId = PrescriptionId.from(communication.getBasedOnReferenceId());
      val accessCode = communication.getBasedOnAccessCode();
      return prescriptionId.check() && accessCode.isPresent() && accessCode.orElseThrow().isValid();
    }

    @Override
    public String getSubject() {
      return format(
          "dass, die BasedOn-Referenz {0} valide ist",
          communication.getBasedOnFirstRep().getReference());
    }

    private static CheckCommunicationBasedOnReference forCommunication(ErxCommunication com) {
      return new CheckCommunicationBasedOnReference(com);
    }
  }
}
