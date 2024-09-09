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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.exceptions.InvalidActorRoleException;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItemBundle;
import de.gematik.test.erezept.screenplay.abilities.ManageChargeItems;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.strategy.ActorRole;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class HasChargeItemBundle implements Question<Boolean> {

  private final DequeStrategy deque;
  private final ActorRole role;

  @Override
  public Boolean answeredBy(final Actor actor) {
    val response =
        actor.asksFor(ResponseOfGetChargeItemBundle.forPrescription(deque).withRole(role));
    if (response.getResourceOptional().isEmpty()) {
      return false; // unexpected response
    }

    if (role == ActorRole.PATIENT) {
      return checkAsPatient(actor, response);
    } else if (role == ActorRole.PHARMACY) {
      return checkAsPharmacy(actor, response);
    } else {
      throw new InvalidActorRoleException(format("Cannot check ChargeItem as {0}", role));
    }
  }

  private Boolean checkAsPatient(Actor actor, ErpResponse<ErxChargeItemBundle> response) {
    val chargeItemBundle = response.getExpectedResource();
    val chargeItem = chargeItemBundle.getChargeItem();

    val egk = SafeAbility.getAbility(actor, ProvideEGK.class);
    // store the fetched charge item on local stack
    val chargeItemAbility = SafeAbility.getAbility(actor, ManageChargeItems.class);
    chargeItemAbility.update(chargeItem);

    if (chargeItem.getReceiptReference().isEmpty() || chargeItemBundle.getReceipt().isEmpty()) {
      return false;
    }

    return chargeItem.getSubjectKvnr().equals(egk.getKvnr());
  }

  private Boolean checkAsPharmacy(Actor actor, ErpResponse<ErxChargeItemBundle> response) {
    val chargeItemBundle = response.getExpectedResource();
    val chargeItem = chargeItemBundle.getChargeItem();

    val pharmacyStack = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    val smcb = SafeAbility.getAbility(actor, UseSMCB.class);
    val dispensed = deque.chooseFrom(pharmacyStack.getClosedPrescriptions());

    if (!chargeItem.getPrescriptionId().equals(dispensed.getPrescriptionId())) {
      log.warn(
          format(
              "Prescription Id {0} of charge item do not match to "
                  + "prescription Id {1} of the task.",
              chargeItem.getPrescriptionId(), dispensed.getPrescriptionId()));
      return false;
    }

    if (chargeItemBundle.getReceipt().isPresent()) {
      log.warn("Charge item bundle contains a receipt resource.");
      return false;
    }

    if (chargeItem.getAccessCode().isPresent()) {
      log.warn(
          format("Charge Item contains the access code {0}.", chargeItem.getAccessCode().get()));
      return false;
    }

    return chargeItem.getEntererTelematikId().getValue().equals(smcb.getTelematikID());
  }

  public static Builder forLastDispensedDrug() {
    return new Builder(DequeStrategy.LIFO);
  }

  public static Builder forPrescription(String order) {
    return forPrescription(DequeStrategy.fromString(order));
  }

  public static Builder forPrescription(DequeStrategy deque) {
    return new Builder(deque);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final DequeStrategy deque;

    public HasChargeItemBundle asPatient() {
      return withRole(ActorRole.PATIENT);
    }

    public HasChargeItemBundle asPharmacy() {
      return withRole(ActorRole.PHARMACY);
    }

    public HasChargeItemBundle withRole(ActorRole role) {
      return new HasChargeItemBundle(deque, role);
    }
  }
}
