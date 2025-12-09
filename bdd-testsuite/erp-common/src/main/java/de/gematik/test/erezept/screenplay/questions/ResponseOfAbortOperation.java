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

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.exceptions.FeatureNotImplementedException;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.ManageDoctorsPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.ActorRole;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@Slf4j
@Getter
public class ResponseOfAbortOperation extends FhirResponseQuestion<EmptyResource> {

  private final ActorRole role;
  private final DequeStrategy deque;
  private final Map<String, String> replacementMap;

  private ResponseOfAbortOperation(
      ActorRole role, DequeStrategy deque, Map<String, String> replacementMap) {
    this.role = role;
    this.deque = deque;
    this.replacementMap = replacementMap;
  }

  @Override
  public ErpResponse<EmptyResource> answeredBy(Actor actor) {
    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);

    val cmd =
        switch (role) {
          case DOCTOR -> abortAsDoctor(actor);
          case PATIENT -> abortAsPatient(actor);
          case PHARMACY -> abortAsPharmacy(actor);
          case REPRESENTATIVE -> abortAsRepresentative(actor);
          case KTR -> abortAsKtr(actor);
          default -> throw new FeatureNotImplementedException(
              format("You cannot abort a prescription as {0}", role));
        };
    return erpClientAbility.request(cmd);
  }

  private <T extends Actor> TaskAbortCommand abortAsDoctor(T actor) {
    val issuedPrescriptions =
        SafeAbility.getAbility(actor, ManageDoctorsPrescriptions.class).getPrescriptions();
    val toDelete = deque.chooseFrom(issuedPrescriptions);
    val taskId = toDelete.getTaskId();
    val accessCode = toDelete.getAccessCode();
    val cmd = new TaskAbortCommand(taskId, accessCode);
    log.info(
        "Doctor {} is asking for the response of {} with AccessCode {}",
        actor.getName(),
        cmd.getRequestLocator(),
        accessCode);
    return cmd;
  }

  private <T extends Actor> TaskAbortCommand abortAsPharmacy(T actor) {
    val accepted =
        SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class).getAcceptedPrescriptions();
    val toDelete = deque.chooseFrom(accepted);
    val taskId = toDelete.getTaskId();
    val accessCode = toDelete.getTask().getAccessCode();
    val secret = this.getSecret(toDelete);
    val cmd = new TaskAbortCommand(taskId, accessCode, secret);
    log.info(
        "Pharmacy {} is asking for the response of {} with AccessCode {} and Secret {}",
        actor.getName(),
        cmd.getRequestLocator(),
        accessCode,
        secret);
    return cmd;
  }

  private <T extends Actor> TaskAbortCommand abortAsKtr(T actor) {
    val accepted =
        SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class).getAcceptedPrescriptions();
    val toDelete = deque.chooseFrom(accepted);
    val taskId = toDelete.getTaskId();
    val accessCode = toDelete.getTask().getAccessCode();
    val secret = this.getSecret(toDelete);
    val cmd = new TaskAbortCommand(taskId, accessCode, secret);
    log.info(
        "KTR {} is asking for the response of {} with AccessCode {} and Secret {}",
        actor.getName(),
        cmd.getRequestLocator(),
        accessCode,
        secret);
    return cmd;
  }

  private <T extends Actor> TaskAbortCommand abortAsPatient(T actor) {
    val ability = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmcs = ability.getDmcs();
    val toDelete = deque.chooseFrom(dmcs);
    val taskId = toDelete.getTaskId();
    val cmd = new TaskAbortCommand(taskId);
    log.info(
        "Patient {} is asking for the response of {}", actor.getName(), cmd.getRequestLocator());
    return cmd;
  }

  private <T extends Actor> TaskAbortCommand abortAsRepresentative(T actor) {
    val ability = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmcs = ability.getDmcs();
    val toDelete = deque.chooseFrom(dmcs);
    val taskId = toDelete.getTaskId();
    val accessCode = toDelete.getAccessCode();
    val cmd = new TaskAbortCommand(taskId, accessCode);
    log.info(
        "Representative {} is asking for the response of {}",
        actor.getName(),
        cmd.getRequestLocator());
    return cmd;
  }

  private Secret getSecret(ErxAcceptBundle acceptBundle) {
    var secret = acceptBundle.getSecret();
    val replacementSecret = this.replacementMap.get("secret");
    if (replacementSecret != null) {
      log.info("Found a replacement secret for $abort: {}", replacementSecret);
      secret = Secret.from(replacementSecret);
    }
    return secret;
  }

  public static Builder asDoctor() {
    return as(ActorRole.DOCTOR);
  }

  public static Builder asKtr() {
    return as(ActorRole.KTR);
  }

  public static Builder asPharmacy() {
    return as(ActorRole.PHARMACY);
  }

  public static Builder asPatient() {
    return as(ActorRole.PATIENT);
  }

  public static Builder asRepresentative() {
    return as(ActorRole.REPRESENTATIVE);
  }

  public static Builder as(ActorRole role) {
    return new Builder(role);
  }

  public static class Builder {
    private final ActorRole role;
    private final Map<String, String> replacementMap;

    private Builder(ActorRole role) {
      this.role = role;
      this.replacementMap = new HashMap<>();
    }

    public Builder withInvalidSecret() {
      return withInvalidSecret(GemFaker.fakerSecret());
    }

    public Builder withInvalidSecret(String secret) {
      this.replacementMap.put("secret", secret);
      return this;
    }

    public ResponseOfAbortOperation fromStack(String order) {
      return fromStack(DequeStrategy.fromString(order));
    }

    public ResponseOfAbortOperation fromStack(DequeStrategy deque) {
      return new ResponseOfAbortOperation(role, deque, replacementMap);
    }
  }
}
