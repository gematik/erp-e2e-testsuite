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

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.ChargeItemGetByIdCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItem;
import de.gematik.test.erezept.lei.exceptions.InvalidActorRoleException;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.ActorRole;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseOfGetChargeItem extends FhirResponseQuestion<ErxChargeItem> {

  private final DequeStrategy deque;
  private final ActorRole role;

  @Override
  public Class<ErxChargeItem> expectedResponseBody() {
    return ErxChargeItem.class;
  }

  @Override
  public String getOperationName() {
    return "ChargeItem";
  }

  @Override
  public ErpResponse answeredBy(Actor actor) {
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
    val dispensed = deque.chooseFrom(pharmacyStack.getDispensedPrescriptions());
    return new ChargeItemGetByIdCommand(dispensed.getPrescriptionId());
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

    public ResponseOfGetChargeItem asPatient() {
      return withRole(ActorRole.PATIENT);
    }

    public ResponseOfGetChargeItem asPharmacy() {
      return withRole(ActorRole.PHARMACY);
    }

    public ResponseOfGetChargeItem withRole(ActorRole role) {
      return new ResponseOfGetChargeItem(deque, role);
    }
  }
}
