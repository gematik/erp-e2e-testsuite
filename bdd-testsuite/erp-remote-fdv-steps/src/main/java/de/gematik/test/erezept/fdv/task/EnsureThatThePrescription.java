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

package de.gematik.test.erezept.fdv.task;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.rest.server.exceptions.ResourceGoneException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import de.gematik.erezept.remotefdv.api.model.WorkFlow;
import de.gematik.test.erezept.fdv.exceptions.GatewayTimeOutException;
import de.gematik.test.erezept.fdv.exceptions.UnexpectedErrorException;
import de.gematik.test.erezept.fdv.questions.ReadPrescription;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor
@Slf4j
public class EnsureThatThePrescription implements Task {
  private final DequeStrategy deque;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val dmcAbility = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmc = deque.chooseFrom(dmcAbility.chooseStack(DmcStack.ACTIVE));

    val response = actor.asksFor(ReadPrescription.withTaskId(dmc.getTaskId()));
    val optionalPres = response.getResourceOptional();

    if (optionalPres.isEmpty()) {
      val error = response.getOperationOutcome();
      if (error.getStatusCode().intValue() == 504) {
        throw new GatewayTimeOutException(
            "Did not receive a timely response from RemoteFdV-Server");
      } else if (error.getStatusCode().intValue() == 410) {
        throw new ResourceGoneException("Task has already been deleted");
      } else if (error.getStatusCode().intValue() == 404) {
        throw new ResourceNotFoundException("Task not found");
      } else throw new UnexpectedErrorException(error.getDetails());
    } else {
      val prescription = optionalPres.get();
      assertEquals(dmc.getTaskId().toString(), prescription.getPrescriptionId());
      if (dmc.getTaskId().getFlowType().isDirectAssignment()) {
        if (dmc.getTaskId().getFlowType().isGkvType()) {
          assertEquals(WorkFlow._169, prescription.getWorkFlow());
        } else {
          assertEquals(WorkFlow._209, prescription.getWorkFlow());
        }
      }
    }
  }

  public static Builder fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static Builder fromStack(DequeStrategy deque) {
    return new Builder(deque);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final DequeStrategy deque;

    public EnsureThatThePrescription isShownCorrectly() {
      return Instrumented.instanceOf(EnsureThatThePrescription.class).withProperties(deque);
    }
  }
}
