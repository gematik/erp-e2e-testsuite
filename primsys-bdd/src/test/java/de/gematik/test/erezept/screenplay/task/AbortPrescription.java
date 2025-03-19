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

package de.gematik.test.erezept.screenplay.task;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.exceptions.FeatureNotImplementedException;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.ManageDoctorsPrescriptions;
import de.gematik.test.erezept.screenplay.questions.ResponseOfAbortOperation;
import de.gematik.test.erezept.screenplay.strategy.ActorRole;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.ensure.Ensure;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AbortPrescription implements Task {

  private final ResponseOfAbortOperation question;

  @Override
  public <T extends Actor> void performAs(T actor) {
    switch (question.getRole()) {
      case DOCTOR -> abortAsDoctor(actor);
      case PATIENT -> abortAsPatient(actor);
      case PHARMACY -> abortAsPharmacy(actor);
      default -> throw new FeatureNotImplementedException(
          format("You cannot abort a prescription as {0}", question.getRole()));
    }
  }

  private <T extends Actor> void abortAsDoctor(T actor) {
    val issuedPrescriptions =
        SafeAbility.getAbility(actor, ManageDoctorsPrescriptions.class).getPrescriptions();
    val response = actor.asksFor(question);
    checkReturnCode(response);
    question.getDeque().removeFrom(issuedPrescriptions);
  }

  private <T extends Actor> void abortAsPharmacy(T actor) {
    val response = actor.asksFor(question);
    checkReturnCode(response);
  }

  private <T extends Actor> void abortAsPatient(T actor) {
    val ability = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmcs = ability.getDmcs();
    val deletedDmcs = ability.getDeletedDmcs();

    val response = actor.asksFor(question);
    checkReturnCode(response);
    val deleted = question.getDeque().chooseFrom(ability.getDmcs());
    ability.moveToDeleted(deleted);
  }

  private void checkReturnCode(ErpResponse<Resource> response) {
    // see A_19514-03
    then(Ensure.that(response.getStatusCode()).isEqualTo(204));
  }

  public static Builder asDoctor() {
    return as(ActorRole.DOCTOR);
  }

  public static Builder asPharmacy() {
    return as(ActorRole.PHARMACY);
  }

  public static Builder asPatient() {
    return as(ActorRole.PATIENT);
  }

  public static Builder as(ActorRole role) {
    return new Builder(role);
  }

  public static class Builder {
    private final ResponseOfAbortOperation.Builder wrapee;

    private Builder(ActorRole role) {
      this.wrapee = ResponseOfAbortOperation.as(role);
    }

    public AbortPrescription fromStack(String order) {
      return fromStack(DequeStrategy.fromString(order));
    }

    public AbortPrescription fromStack(DequeStrategy deque) {
      return new AbortPrescription(wrapee.fromStack(deque));
    }
  }
}
