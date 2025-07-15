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

package de.gematik.test.erezept.screenplay.questions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.r4.erp.CommunicationType;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseOfAcceptDispenseRequestOperation
    extends FhirResponseQuestion<ErxAcceptBundle> {

  private final DequeStrategy order;
  private final Actor sender;

  public static Builder forThePrescription(String order) {
    return new Builder(DequeStrategy.fromString(order));
  }

  @Override
  public ErpResponse<ErxAcceptBundle> answeredBy(Actor actor) {
    val receivedDispenseRequest =
        actor
            .asksFor(GetReceivedCommunication.dispenseRequest().of(order).from(sender))
            .orElseThrow(
                () ->
                    new MissingPreconditionError(
                        format(
                            "No {0} received from {1}",
                            CommunicationType.DISP_REQ, sender.getName())));

    val responseOfAcceptOperation =
        ResponseOfAcceptOperation.forDispenseRequest(receivedDispenseRequest);
    return actor.asksFor(responseOfAcceptOperation);
  }

  public static class Builder {
    private final DequeStrategy order;

    private Builder(DequeStrategy order) {
      this.order = order;
    }

    public ResponseOfAcceptDispenseRequestOperation fromPatient(Actor patient) {
      return new ResponseOfAcceptDispenseRequestOperation(order, patient);
    }
  }
}
