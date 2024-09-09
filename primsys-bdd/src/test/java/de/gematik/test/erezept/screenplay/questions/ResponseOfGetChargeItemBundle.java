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

package de.gematik.test.erezept.screenplay.questions;

import static java.text.MessageFormat.*;

import de.gematik.test.erezept.client.rest.*;
import de.gematik.test.erezept.client.usecases.*;
import de.gematik.test.erezept.exceptions.InvalidActorRoleException;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.strategy.*;
import de.gematik.test.erezept.screenplay.util.*;
import java.util.*;
import javax.annotation.*;
import lombok.*;
import lombok.extern.slf4j.*;
import net.serenitybdd.screenplay.*;

@Slf4j
public class ResponseOfGetChargeItemBundle extends FhirResponseQuestion<ErxChargeItemBundle> {

  private final DequeStrategy deque;
  private final ActorRole role;
  @Nullable private final AccessCode accessCode;

  private ResponseOfGetChargeItemBundle(
      DequeStrategy deque, ActorRole role, @Nullable AccessCode accessCode) {
    super("GET /ChargeItem");
    this.deque = deque;
    this.role = role;
    this.accessCode = accessCode;
  }

  @Override
  public ErpResponse<ErxChargeItemBundle> answeredBy(Actor actor) {
    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);

    ChargeItemGetByIdCommand cmd;
    if (role == ActorRole.PHARMACY) {
      cmd = getPharmacyCommand(actor);
    } else if (role == ActorRole.PATIENT) {
      cmd = getPatientCommand(actor);
    } else {
      throw new InvalidActorRoleException(format("Get ChargeItem as {0}", role));
    }

    log.info(
        format(
            "Actor {0} is asking for the response of {1}",
            actor.getName(), cmd.getRequestLocator()));
    return erpClientAbility.request(cmd);
  }

  private ChargeItemGetByIdCommand getPatientCommand(Actor actor) {
    val receivedDrugs = SafeAbility.getAbility(actor, ReceiveDispensedDrugs.class);
    val dispensed = deque.chooseFrom(receivedDrugs.getDispensedDrugsList());
    return new ChargeItemGetByIdCommand(dispensed);
  }

  private ChargeItemGetByIdCommand getPharmacyCommand(Actor actor) {
    val pharmacyStack = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    val chargeItemChangeAuthorization =
        deque.chooseFrom(pharmacyStack.getChargeItemChangeAuthorizations());
    return new ChargeItemGetByIdCommand(
        chargeItemChangeAuthorization.getPrescriptionId(),
        Optional.ofNullable(accessCode).orElse(chargeItemChangeAuthorization.getAccessCode()));
  }

  public static Builder forPrescription(String order) {
    return forPrescription(DequeStrategy.fromString(order));
  }

  public static Builder forPrescription(DequeStrategy deque) {
    return new Builder(deque);
  }

  public static Builder forLastPrescription() {
    return forPrescription(DequeStrategy.LIFO);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final DequeStrategy deque;
    private AccessCode accessCode;

    public Builder withRandomAccessCode() {
      return withAccessCode(AccessCode.random());
    }

    public Builder withAccessCode(String accessCode) {
      return withAccessCode(AccessCode.fromString(accessCode));
    }

    public Builder withAccessCode(AccessCode accessCode) {
      this.accessCode = accessCode;
      return this;
    }

    public ResponseOfGetChargeItemBundle asPatient() {
      return withRole(ActorRole.PATIENT);
    }

    public ResponseOfGetChargeItemBundle asPharmacy() {
      return withRole(ActorRole.PHARMACY);
    }

    public ResponseOfGetChargeItemBundle withRole(ActorRole role) {
      return new ResponseOfGetChargeItemBundle(deque, role, accessCode);
    }
  }
}
